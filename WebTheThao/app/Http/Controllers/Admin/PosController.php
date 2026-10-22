<?php

namespace App\Http\Controllers\Admin;

use App\Domain\Chat\Services\ChatService;
use App\Domain\Order\Models\Order;
use App\Domain\Payment\Support\BankTransferQr;
use App\Domain\Product\Models\Category;
use App\Domain\Product\Models\Product;
use App\Domain\Product\Models\ProductVariant;
use App\Domain\User\Enums\UserRole;
use App\Domain\User\Models\User;
use App\Gateway\Services\CheckoutOrchestrator;
use App\Gateway\Services\PaymentOrchestrator;
use App\Http\Controllers\Controller;
use App\Storefront\Support\CartPresenter;
use App\Storefront\Support\CartQtyGuard;
use App\Http\Support\PosCartService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Str;
use Illuminate\View\View;
use Illuminate\Validation\ValidationException;

class PosController extends Controller
{
    public function __construct(
        private PosCartService $cart,
        private CheckoutOrchestrator $checkout,
        private PaymentOrchestrator $payments,
        private ChatService $chat,
        private CartPresenter $presenter,
        private CartQtyGuard $qty,
    ) {}

    public function index(Request $request): View
    {
        $q = trim((string) $request->string('q'));
        $phoi = $request->boolean('phoi');
        $categoryId = $phoi ? null : ($request->integer('category_id') ?: null);

        $categories = $this->deskCategories();
        $products = $this->deskProducts($phoi, $categoryId, $q);

        $customers = User::query()
            ->where('role', UserRole::Customer)
            ->where('is_active', true)
            ->orderBy('name')
            ->get(['id', 'name', 'email', 'phone']);

        $ticketCustomer = $this->cart->customer();
        $cart = $this->presenter->viewData();

        return view('admin.pos.index', array_merge($cart, [
            'products' => $products,
            'categories' => $categories,
            'customers' => $customers,
            'q' => $q,
            'phoi' => $phoi,
            'categoryId' => $categoryId,
            'tickets' => $this->cart->tickets(),
            'activeTicketId' => $this->cart->activeId(),
            'ticketCustomer' => $ticketCustomer,
            'bankQr' => BankTransferQr::display((float) $cart['grand_total']),
        ]));
    }

    public function addTicket(): RedirectResponse
    {
        $this->syncCustomerFromRequest(request());
        $this->cart->addTicket();

        return $this->backToDesk()->with('success', 'Đã mở đơn hàng mới.');
    }

    public function switchTicket(string $ticketId): RedirectResponse
    {
        $this->syncCustomerFromRequest(request());
        $this->cart->switchTicket($ticketId);

        return $this->backToDesk();
    }

    public function removeTicket(string $ticketId): RedirectResponse
    {
        $this->syncCustomerFromRequest(request());
        if (! $this->cart->removeTicket($ticketId)) {
            return $this->backToDesk()->with('error', 'Phải giữ ít nhất một đơn hàng trên quầy.');
        }

        return $this->backToDesk()->with('success', 'Đã xóa đơn hàng.');
    }

    public function add(Request $request): RedirectResponse
    {
        $this->syncCustomerFromRequest($request);

        $data = $request->validate(CartQtyGuard::addLineRules());
        $data['await_print'] = $request->boolean('await_print');

        if (! empty($data['await_print']) && $data['line_type'] === 'sale' && ! $this->qty->saleFits($this->cart->lines(), $data)) {
            return $this->backToDesk()
                ->withInput()
                ->withErrors(['quantity' => 'Không đủ tồn bán của phôi này.']);
        }

        if ($data['line_type'] === 'rental' && ! $this->qty->rentalFits($this->cart->lines(), $data)) {
            return $this->backToDesk()
                ->withInput()
                ->withErrors(['quantity' => 'Không còn đủ món thuê trống cho số lượng này trong khoảng ngày đã chọn.']);
        }

        $this->cart->add($data);

        return $this->backToDesk()->with('success', 'Đã thêm vào giỏ quầy.');
    }

    public function changeQty(Request $request, string $lineId): RedirectResponse
    {
        $this->syncCustomerFromRequest($request);
        $data = $request->validate([
            'delta' => ['required', 'integer', 'in:-1,1'],
        ]);

        $line = collect($this->cart->lines())->first(
            fn (array $row) => (string) ($row['id'] ?? '') === $lineId,
        );
        if (
            is_array($line)
            && ! empty($line['await_print'])
            && (int) $data['delta'] === 1
            && ! $this->qty->saleFits($this->cart->lines(), [
                'product_variant_id' => $line['product_variant_id'],
                'quantity' => 1,
            ])
        ) {
            return $this->backToDesk()
                ->withErrors(['quantity' => 'Không đủ tồn bán của phôi này.']);
        }

        if (
            is_array($line)
            && ($line['line_type'] ?? '') === 'rental'
            && (int) $data['delta'] === 1
            && ! $this->qty->rentalFits($this->cart->lines(), [
                'product_variant_id' => $line['product_variant_id'],
                'quantity' => 1,
                'rental_start' => $line['rental_start'] ?? null,
                'rental_end' => $line['rental_end'] ?? null,
            ])
        ) {
            return $this->backToDesk()
                ->withErrors(['quantity' => 'Không còn đủ món thuê trống để tăng số lượng.']);
        }

        $this->cart->changeQty($lineId, (int) $data['delta']);

        return $this->backToDesk();
    }

    public function remove(string $lineId): RedirectResponse
    {
        $this->syncCustomerFromRequest(request());
        $this->cart->remove($lineId);

        return $this->backToDesk()->with('success', 'Đã xóa dòng giỏ quầy.');
    }

    public function store(Request $request): RedirectResponse
    {
        $this->syncCustomerFromRequest($request);

        $data = $request->validate([
            'action' => ['nullable', 'in:save,pay'],
            'customer_mode' => ['nullable', 'in:existing,walkin'],
            'customer_id' => ['nullable', 'integer'],
            'walkin_name' => ['nullable', 'string', 'max:255'],
            'walkin_phone' => ['nullable', 'string', 'max:20'],
            'notes' => ['nullable', 'string', 'max:500'],
            'payment_method' => ['nullable', 'in:cash,bank_transfer'],
        ]);

        $printLines = collect($this->cart->lines())
            ->filter(fn (array $line) => ! empty($line['await_print']))
            ->values();

        if ($this->cart->lines() === []) {
            return back()->with('error', 'Giỏ hàng trống.');
        }

        if ($printLines->isNotEmpty() && ($data['customer_mode'] ?? '') !== 'existing') {
            return back()->with('error', 'In phôi cần chọn tài khoản khách để nhắn nội dung in sau.')->withInput();
        }

        $customer = $this->resolvePosCustomer($data);
        if ($customer === null || ! $customer->isCustomer()) {
            return back()->with('error', 'Khách hàng không hợp lệ.')->withInput();
        }

        $method = $data['payment_method'] ?? 'cash';
        $action = $data['action'] ?? 'pay';

        try {
            $order = $this->chargeDesk($request->user(), $customer, $data['notes'] ?? null, $method, $action !== 'save');
        } catch (ValidationException $e) {
            return back()->withErrors($e->errors())->withInput();
        }

        if ($printLines->isNotEmpty()) {
            $this->notifyPrint($request->user(), $customer, $printLines->all(), $action !== 'save');
        }

        $this->cart->clear();

        if ($action === 'save') {
            return $this->backToDesk()->with('success', 'Đã lưu đơn tại quầy (chưa thu tiền).');
        }

        return redirect()->route('admin.orders.bill', $order);
    }

    private function deskCategories()
    {
        return Category::query()
            ->where('is_active', true)
            ->withCount(['products' => fn ($query) => $query->where('is_active', true)])
            ->orderBy('name')
            ->get();
    }

    private function deskProducts(bool $phoi, ?int $categoryId, string $q)
    {
        return Product::query()
            ->where('is_active', true)
            ->where('is_print_blank', $phoi)
            ->with(['variants' => fn ($v) => $v->where('is_active', true)->with('stock')])
            ->when($categoryId, fn ($query) => $query->where('category_id', $categoryId))
            ->when($q !== '', fn ($query) => $query->where('name', 'like', '%'.$q.'%'))
            ->orderBy('name')
            ->limit(60)
            ->get();
    }

    private function chargeDesk(User $staff, User $customer, ?string $notes, string $method, bool $settle): Order
    {
        $order = $this->checkout->checkout($staff, [
            'channel' => 'pos',
            'customer_id' => $customer->id,
            'shipping_name' => $customer->name,
            'shipping_phone' => $customer->phone,
            'shipping_address' => 'Nhận tại quầy',
            'notes' => $notes,
            'payment_method' => $method,
            'lines' => $this->presenter->checkoutLines(),
        ]);

        if ($settle) {
            $this->payments->settleOrder($order);
        }

        return $order;
    }

    /** @param  array<string, mixed>  $data */
    private function resolvePosCustomer(array $data): ?User
    {
        $mode = $data['customer_mode'] ?? null;
        $customerId = (int) ($data['customer_id'] ?? 0);

        if ($mode === 'existing' || ($customerId > 0 && $mode !== 'walkin')) {
            if ($customerId <= 0) {
                return null;
            }

            return User::query()->find($customerId);
        }

        $name = trim((string) ($data['walkin_name'] ?? '')) ?: 'Khách lẻ';
        $phone = trim((string) ($data['walkin_phone'] ?? ''));

        return $this->createWalkIn($name, $phone !== '' ? $phone : null);
    }

    private function createWalkIn(string $name, ?string $phone): User
    {
        return User::query()->create([
            'name' => $name,
            'email' => 'pos.'.Str::lower(Str::random(10)).'@vang.lai.local',
            'phone' => $phone,
            'password' => Str::password(16),
            'role' => UserRole::Customer,
            'is_active' => true,
        ]);
    }

    /** @param  list<array<string, mixed>>  $lines */
    private function notifyPrint(User $staff, User $customer, array $lines, bool $paid): void
    {
        $variants = ProductVariant::query()
            ->with('product')
            ->whereIn('id', collect($lines)->pluck('product_variant_id'))
            ->get()
            ->keyBy('id');
        $parts = [];
        foreach ($lines as $line) {
            $variant = $variants->get((int) $line['product_variant_id']);
            if ($variant === null) {
                continue;
            }
            $parts[] = $variant->product->name
                .' · '.($variant->color ?: 'không màu')
                .' · '.($variant->size ?: 'một size')
                .' × '.(int) $line['quantity'];
        }
        if ($parts === []) {
            return;
        }

        $this->chat->post(
            $this->chat->forCustomer($customer),
            $staff,
            'Quầy đã nhận phôi: '.implode('; ', $parts).'. Nhắn tên, số hoặc logo để quán in. '.($paid ? 'Kho đã trừ theo số lượng này.' : 'Kho trừ khi thu tiền đơn này.'),
        );
    }

    private function syncCustomerFromRequest(Request $request): void
    {
        if (! $request->exists('sync_customer_name') && ! $request->exists('sync_customer_id')) {
            return;
        }

        $id = $request->filled('sync_customer_id') ? $request->integer('sync_customer_id') : null;
        $this->cart->rememberCustomer(
            $id ?: null,
            (string) $request->string('sync_customer_name'),
            (string) $request->string('sync_customer_phone'),
        );
    }

    private function backToDesk(): RedirectResponse
    {
        return redirect()->route('admin.pos.index', array_filter([
            'phoi' => request()->boolean('phoi') ? 1 : null,
            'category_id' => request()->boolean('phoi') ? null : (request()->integer('category_id') ?: null),
            'q' => trim((string) request()->string('q')) ?: null,
        ], fn ($value) => $value !== null && $value !== ''));
    }
}
