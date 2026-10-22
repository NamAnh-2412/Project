<?php

namespace App\Domain\JerseyStudio\Services;

use App\Domain\Chat\Services\ChatService;
use App\Domain\Inventory\Services\InventoryWriter;
use App\Domain\Notification\Enums\NotificationType;
use App\Domain\Notification\Services\NotificationWriter;
use App\Domain\JerseyStudio\Models\JerseyDesign;
use App\Domain\Product\Models\ProductVariant;
use App\Domain\JerseyStudio\Support\StudioCatalog;
use App\Domain\Order\Enums\LineType;
use App\Domain\Payment\Enums\PaymentMethod;
use App\Domain\Payment\Services\PaymentWriter;
use App\Domain\Order\Services\OrderWriter;
use App\Domain\User\Models\User;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Illuminate\Validation\ValidationException;

class JerseyDesignService
{
    public function __construct(
        private ChatService $chat,
        private OrderWriter $orders,
        private InventoryWriter $inventory,
        private NotificationWriter $notifications,
        private PaymentWriter $payments,
    ) {}

    /** @param  array<string, mixed>  $data */
    public function save(User $customer, array $data, ?UploadedFile $logo = null): JerseyDesign
    {
        $payload = $this->normalize($data['design_payload']);
        $existing = null;
        if (! empty($data['design_id'])) {
            $existing = JerseyDesign::query()
                ->where('user_id', $customer->id)
                ->find($data['design_id']);
            if ($existing === null) {
                throw ValidationException::withMessages([
                    'design_id' => ['Không tìm thấy bản phối.'],
                ]);
            }
            if (in_array($existing->status, ['accepted', 'declined'], true)) {
                throw ValidationException::withMessages([
                    'design_id' => ['Bản phối này đã chốt, không sửa được.'],
                ]);
            }
        }

        if ($logo) {
            $path = $logo->store('jersey-logos', 'public');
            $payload = $this->attachLogo($payload, asset('storage/'.$path), (string) ($data['logo_side'] ?? 'front'), $path);
        }

        $logoPath = $payload['logo_path'] ?? $existing?->logo_path;
        unset($payload['logo_path']);
        $design = $existing ?? new JerseyDesign([
            'user_id' => $customer->id,
            'status' => 'draft',
        ]);
        $design->fill([
            'garment_template' => isset($payload['product_id']) ? 'product-'.$payload['product_id'] : 'football-jersey-phase1',
            'design_payload' => $payload,
            'logo_path' => $logoPath,
            'size' => $this->plain($payload['size'] ?? ($data['size'] ?? null), 12),
            'quantity' => max(1, (int) ($data['quantity'] ?? 1)),
            'notes' => $this->plain($data['notes'] ?? null, 1000),
        ]);
        $design->save();

        return $design->refresh();
    }

    /** @param  array<string, mixed>  $data */
    public function submit(User $customer, JerseyDesign $design, array $data): JerseyDesign
    {
        if ((int) $design->user_id !== (int) $customer->id) {
            throw ValidationException::withMessages([
                'design_id' => ['Bản phối không thuộc tài khoản này.'],
            ]);
        }
        if (in_array($design->status, ['accepted', 'declined'], true)) {
            throw ValidationException::withMessages([
                'design_id' => ['Bản phối này đã chốt.'],
            ]);
        }

        $payload = is_array($data['design_payload'] ?? null)
            ? $this->normalize($data['design_payload'])
            : $design->design_payload;
        $size = $this->plain($payload['size'] ?? ($data['size'] ?? null), 12);
        if ($size === null || (int) ($payload['variant_id'] ?? 0) < 1) {
            throw ValidationException::withMessages([
                'size' => ['Chọn size còn hàng trước khi gửi cửa hàng.'],
            ]);
        }

        return DB::transaction(function () use ($customer, $design, $data, $payload, $size) {
            $design->fill([
                'garment_template' => isset($payload['product_id']) ? 'product-'.$payload['product_id'] : $design->garment_template,
                'design_payload' => $payload,
                'size' => $size,
                'quantity' => max(1, (int) ($data['quantity'] ?? 1)),
                'notes' => $this->plain($data['notes'] ?? null, 1000) ?? $design->notes,
                'status' => $design->status === 'quoted' ? 'quoted' : 'submitted',
                'submitted_at' => $design->submitted_at ?? now(),
            ]);
            $conversation = $this->chat->forCustomer($customer);
            $design->conversation_id = $conversation->id;
            $design->save();

            $color = $design->design_payload['color_label'] ?? $design->design_payload['color'] ?? '';
            $sample = $design->design_payload['product_name'] ?? null;
            $this->chat->post(
                $conversation,
                $customer,
                'Mình gửi bản phối áo 3D #'.$design->id.($sample ? ', mẫu '.$sample : '').', màu '.$color.', size '.$design->size.', số lượng '.$design->quantity.'.',
            );

            return $design->refresh();
        });
    }

    public function quote(User $staff, JerseyDesign $design, float $amount, ?string $note = null): JerseyDesign
    {
        if (! $staff->isStoreAccount() || $amount <= 0) {
            throw ValidationException::withMessages([
                'quote_amount' => ['Báo giá phải lớn hơn 0.'],
            ]);
        }

        return DB::transaction(function () use ($staff, $design, $amount, $note) {
            $locked = JerseyDesign::query()->whereKey($design->id)->lockForUpdate()->firstOrFail();
            if (! in_array($locked->status, ['submitted', 'quoted'], true)) {
                throw ValidationException::withMessages([
                    'quote_amount' => ['Bản phối này không còn báo giá được.'],
                ]);
            }
            $locked->update([
                'quote_amount' => round($amount, 2),
                'status' => 'quoted',
                'quoted_by' => $staff->id,
                'quoted_at' => now(),
            ]);
            $body = 'Báo giá '.number_format((float) $locked->quote_amount, 0, ',', '.').'đ cho bản phối áo 3D #'.$locked->id.'.';
            $plain = $this->plain($note, 500);
            if ($plain) {
                $body .= ' '.$plain;
            }
            $customer = $locked->user;
            $this->chat->post($this->chat->forCustomer($customer), $staff, $body);
            $amountLabel = number_format((float) $locked->quote_amount, 0, ',', '.');
            $this->notifications->recordInApp(
                $customer->id,
                (string) $customer->email,
                NotificationType::QuoteUpdated,
                'Báo giá mới cho bản phối áo 3D #'.$locked->id,
                'Cửa hàng báo '.$amountLabel.'đ cho bản phối #'.$locked->id.'. Mở bản phối để chấp nhận, hoặc thảo luận thêm trong tin nhắn.',
                [
                    'dedupe_key' => 'quote_updated:'.$locked->id.':'.(int) round((float) $locked->quote_amount),
                    'design_id' => $locked->id,
                ],
            );

            return $locked->refresh();
        });
    }

    public function discuss(User $customer, JerseyDesign $design): void
    {
        if ((int) $design->user_id !== (int) $customer->id || $design->status !== 'quoted') {
            throw ValidationException::withMessages([
                'design_id' => ['Chỉ thảo luận được bản phối đang có báo giá.'],
            ]);
        }

        $amount = number_format((float) $design->quote_amount, 0, ',', '.');
        $this->chat->post(
            $this->chat->forCustomer($customer),
            $customer,
            'Mình muốn thảo luận thêm báo giá '.$amount.'đ cho bản phối áo 3D #'.$design->id.'.',
        );
    }

    public function decide(User $customer, JerseyDesign $design, string $decision): JerseyDesign
    {
        if ((int) $design->user_id !== (int) $customer->id || ! in_array($decision, ['accepted', 'declined'], true)) {
            throw ValidationException::withMessages([
                'decision' => ['Không chốt được bản phối này.'],
            ]);
        }

        return DB::transaction(function () use ($customer, $design, $decision) {
            $locked = JerseyDesign::query()->whereKey($design->id)->lockForUpdate()->firstOrFail();
            if ($locked->status !== 'quoted' || $locked->order_id !== null) {
                throw ValidationException::withMessages([
                    'decision' => ['Chỉ chốt được bản phối đang có báo giá.'],
                ]);
            }
            if ($decision === 'declined') {
                $locked->update(['status' => 'declined', 'decided_at' => now()]);
                $this->chat->post($this->chat->forCustomer($customer), $customer, 'Mình chưa chốt bản phối áo 3D #'.$locked->id.'.');

                return $locked->refresh();
            }

            $variant = $this->saleVariant($locked);
            $order = $this->placeOrder($customer, $locked, $variant);
            $this->inventory->commitSale(
                $this->inventory->reserveSale($variant->id, max(1, (int) $locked->quantity), $order->id),
            );
            $locked->update([
                'status' => 'accepted',
                'order_id' => $order->id,
                'decided_at' => now(),
            ]);
            $this->chat->post(
                $this->chat->forCustomer($customer),
                $customer,
                'Mình chốt bản phối áo 3D #'.$locked->id.'. Đơn #'.$order->id.'.',
            );

            return $locked->refresh();
        });
    }

    /** @param  array<string, mixed>  $payload */
    private function normalize(array $payload): array
    {
        $productId = (int) ($payload['product_id'] ?? 0);
        $garment = $productId > 0 ? StudioCatalog::find($productId) : null;
        if ($productId > 0 && $garment === null) {
            throw ValidationException::withMessages([
                'design_payload' => ['Sản phẩm áo không còn trong cửa hàng.'],
            ]);
        }
        $variantId = 0;
        $variantSize = null;
        if ($garment !== null) {
            $chosen = null;
            foreach ($garment['colors'] as $colorOption) {
                foreach ($colorOption['sizes'] as $sizeOption) {
                    if ((int) $sizeOption['variant_id'] === (int) ($payload['variant_id'] ?? 0)) {
                        $chosen = [$colorOption, $sizeOption];
                    }
                }
            }
            if ($chosen === null) {
                throw ValidationException::withMessages([
                    'design_payload' => ['Chọn size còn hàng của sản phẩm này.'],
                ]);
            }
            [$match, $sizeOption] = $chosen;
            $color = $match['key'];
            $colorLabel = $match['label'];
            $colorHex = $match['hex'];
            $productName = $garment['name'];
            $variantId = (int) $sizeOption['variant_id'];
            $variantSize = $sizeOption['size'];
        } else {
            $legacy = ['navy' => ['Navy', '#1b4f7d'], 'orange' => ['Cam', '#f26b38'], 'white' => ['Trắng', '#f7f8fa']];
            $color = array_key_exists($payload['color'] ?? '', $legacy) ? $payload['color'] : 'navy';
            [$colorLabel, $colorHex] = $legacy[$color];
            $productName = null;
            $productId = 0;
        }
        $layers = collect($payload['layers'] ?? [])->take(12)->map(function ($layer) {
            $layer = is_array($layer) ? $layer : [];
            $type = in_array($layer['type'] ?? '', ['text', 'number', 'logo', 'pattern', 'drawing'], true) ? $layer['type'] : 'text';
            $side = in_array($layer['side'] ?? '', ['front', 'back', 'left_sleeve', 'right_sleeve'], true) ? $layer['side'] : 'front';

            return [
                'id' => substr((string) ($layer['id'] ?? Str::lower(Str::random(8))), 0, 40),
                'type' => $type,
                'side' => $side,
                'text' => Str::limit(strip_tags((string) ($layer['text'] ?? '')), 24, ''),
                'name' => Str::limit(strip_tags((string) ($layer['name'] ?? $layer['text'] ?? 'Lớp')), 40, ''),
                'x' => max(0.1, min(0.9, (float) ($layer['x'] ?? 0.5))),
                'y' => max(0.1, min(0.9, (float) ($layer['y'] ?? 0.5))),
                'scale' => max(0.1, min(4, (float) ($layer['scale'] ?? 1))),
                'rotation' => max(-180, min(180, (float) ($layer['rotation'] ?? 0))),
                'opacity' => max(0.1, min(1, (float) ($layer['opacity'] ?? 1))),
                'visible' => ($layer['visible'] ?? true) !== false,
                'locked' => ($layer['locked'] ?? false) === true,
                'pattern' => in_array($layer['pattern'] ?? '', ['stripe', 'diagonal', 'grid'], true) ? $layer['pattern'] : null,
                'asset_url' => is_string($layer['asset_url'] ?? null) && str_starts_with($layer['asset_url'], asset('storage/')) ? $layer['asset_url'] : null,
                'strokes' => $type === 'drawing' ? array_slice(is_array($layer['strokes'] ?? null) ? $layer['strokes'] : [], 0, 24) : [],
            ];
        })->values()->all();

        return [
            'model' => 'football-jersey-realistic',
            'product_id' => $productId > 0 ? $productId : null,
            'product_name' => $productName,
            'variant_id' => $variantId > 0 ? $variantId : null,
            'size' => $variantSize,
            'color' => $color,
            'color_label' => $colorLabel,
            'color_hex' => $colorHex,
            'layers' => $layers,
        ];
    }

    /** @param  array<string, mixed>  $payload */
    private function attachLogo(array $payload, string $url, string $side, string $path): array
    {
        $side = in_array($side, ['front', 'back', 'left_sleeve', 'right_sleeve'], true) ? $side : 'front';
        $attached = false;
        foreach ($payload['layers'] as &$layer) {
            if ($layer['type'] === 'logo' && $layer['asset_url'] === null) {
                $layer['asset_url'] = $url;
                $layer['side'] = $side;
                $attached = true;
                break;
            }
        }
        unset($layer);
        if (! $attached) {
            $payload['layers'][] = [
                'id' => 'logo-'.Str::lower(Str::random(8)),
                'type' => 'logo',
                'side' => $side,
                'text' => '',
                'name' => 'Logo',
                'x' => 0.5,
                'y' => 0.42,
                'scale' => 0.4,
                'rotation' => 0,
                'opacity' => 1,
                'visible' => true,
                'locked' => false,
                'pattern' => null,
                'asset_url' => $url,
                'strokes' => [],
            ];
        }
        $payload['logo_path'] = $path;

        return $payload;
    }

    private function saleVariant(JerseyDesign $design): ProductVariant
    {
        $variantId = (int) ($design->design_payload['variant_id'] ?? 0);
        $productId = (int) ($design->design_payload['product_id'] ?? 0);
        $variant = ProductVariant::query()->with(['product', 'stock'])->find($variantId);
        $available = (int) ($variant?->stock?->availableQuantity() ?? 0);
        if (
            $variant === null
            || (int) $variant->product_id !== $productId
            || $variant->sale_price === null
            || $available < max(1, (int) $design->quantity)
        ) {
            throw ValidationException::withMessages([
                'quantity' => ['Không đủ tồn bán của size này.'],
            ]);
        }

        return $variant;
    }

    private function placeOrder(User $customer, JerseyDesign $design, ProductVariant $variant): \App\Domain\Order\Models\Order
    {
        $quantity = max(1, (int) $design->quantity);
        $total = round((float) $design->quote_amount, 2);

        $order = $this->orders->createPickupPrint($customer, 'Bản phối áo 3D #'.$design->id, [
            'line_type' => LineType::Print,
            'product_id' => $variant->product_id,
            'product_variant_id' => $variant->id,
            'product_name' => filled($design->design_payload['product_name'] ?? null)
                ? 'Áo in 3D · '.$design->design_payload['product_name']
                : 'Áo in 3D #'.$design->id,
            'sku' => $variant->sku,
            'size' => $variant->size ?: ($design->size ?: null),
            'color' => $variant->color ?: ($design->design_payload['color_label'] ?? null),
            'unit_price' => round($total / $quantity, 2),
            'quantity' => $quantity,
            'line_total' => $total,
        ]);

        $this->payments->createIntents($order->id, $customer->id, $total, 0, PaymentMethod::Cash);

        return $order;
    }

    private function plain(mixed $value, int $limit): ?string
    {
        $text = trim(strip_tags((string) $value));

        return $text === '' ? null : Str::limit($text, $limit, '');
    }
}
