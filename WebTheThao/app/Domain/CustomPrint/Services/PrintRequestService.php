<?php

namespace App\Domain\CustomPrint\Services;

use App\Domain\Chat\Enums\ChatAttachmentType;
use App\Domain\Chat\Services\ChatService;
use App\Domain\CustomPrint\Enums\PrintRequestStatus;
use App\Domain\CustomPrint\Models\PrintRequest;
use App\Domain\CustomPrint\Support\PrintCatalog;
use App\Domain\Order\Enums\LineType;
use App\Domain\Order\Services\OrderWriter;
use App\Domain\User\Models\User;
use App\Gateway\Media\CloudinaryImageStore;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Illuminate\Validation\ValidationException;

class PrintRequestService
{
    public function __construct(
        private ChatService $chat,
        private OrderWriter $orders,
        private CloudinaryImageStore $images,
    ) {}

    /** @param  array{garment: string, color: string, size: string, quantity: int, placements: list<string>, notes?: ?string}  $data */
    public function submit(User $customer, array $data, UploadedFile $artwork): PrintRequest
    {
        $placements = array_values(array_unique($data['placements']));
        $estimate = PrintCatalog::estimate($data['garment'], (int) $data['quantity'], $placements);
        $notes = $this->plain($data['notes'] ?? null, 1000);
        $path = $this->storeArtwork($artwork);

        return DB::transaction(function () use ($customer, $data, $placements, $estimate, $notes, $path) {
            $conversation = $this->chat->forCustomer($customer);
            $request = PrintRequest::query()->create([
                'user_id' => $customer->id,
                'conversation_id' => $conversation->id,
                'garment' => $data['garment'],
                'color' => $data['color'],
                'size' => $data['size'],
                'quantity' => (int) $data['quantity'],
                'placements' => $placements,
                'notes' => $notes,
                'artwork_path' => $path,
                'estimate_amount' => $estimate,
                'status' => PrintRequestStatus::Submitted,
            ]);

            $body = 'Mình gửi yêu cầu in '.PrintCatalog::garmentLabel($request->garment)
                .', size '.$request->size.', số lượng '.$request->quantity.'.';
            if ($notes) {
                $body .= ' Ghi chú: '.$notes;
            }

            $this->chat->post(
                $conversation,
                $customer,
                $body,
                ChatAttachmentType::Print->value,
                $request->id,
            );

            return $request->refresh();
        });
    }

    public function quote(User $staff, PrintRequest $request, float $amount, ?string $note = null): PrintRequest
    {
        if (! $staff->isStoreAccount()) {
            throw ValidationException::withMessages([
                'quote_amount' => ['Chỉ cửa hàng được báo giá.'],
            ]);
        }

        if ($amount <= 0) {
            throw ValidationException::withMessages([
                'quote_amount' => ['Báo giá phải lớn hơn 0.'],
            ]);
        }

        return DB::transaction(function () use ($staff, $request, $amount, $note) {
            $locked = PrintRequest::query()->whereKey($request->id)->lockForUpdate()->firstOrFail();
            if (! in_array($locked->status, [PrintRequestStatus::Submitted, PrintRequestStatus::Quoted], true)) {
                throw ValidationException::withMessages([
                    'quote_amount' => ['Yêu cầu này không còn báo giá được.'],
                ]);
            }

            $locked->update([
                'quote_amount' => round($amount, 2),
                'status' => PrintRequestStatus::Quoted,
                'quoted_by' => $staff->id,
                'quoted_at' => now(),
            ]);

            $conversation = $this->chat->forCustomer($locked->user);
            $body = 'Báo giá '.number_format($locked->amount(), 0, ',', '.').'đ cho yêu cầu in #'.$locked->id.'.';
            $plain = $this->plain($note, 500);
            if ($plain) {
                $body .= ' '.$plain;
            }

            $this->chat->post(
                $conversation,
                $staff,
                $body,
                ChatAttachmentType::Print->value,
                $locked->id,
            );

            return $locked->refresh();
        });
    }

    public function decide(User $customer, PrintRequest $request, string $decision): PrintRequest
    {
        if ((int) $request->user_id !== (int) $customer->id) {
            throw ValidationException::withMessages([
                'decision' => ['Yêu cầu in không thuộc tài khoản này.'],
            ]);
        }

        if (! in_array($decision, ['accept', 'decline'], true)) {
            throw ValidationException::withMessages([
                'decision' => ['Phản hồi không hợp lệ.'],
            ]);
        }

        return DB::transaction(function () use ($customer, $request, $decision) {
            $locked = PrintRequest::query()->whereKey($request->id)->lockForUpdate()->firstOrFail();
            if ($locked->status !== PrintRequestStatus::Quoted || $locked->order_id !== null) {
                throw ValidationException::withMessages([
                    'decision' => ['Chỉ chốt được yêu cầu đang có báo giá và chưa thành đơn.'],
                ]);
            }

            if ($decision === 'decline') {
                $locked->update([
                    'status' => PrintRequestStatus::Declined,
                    'decided_at' => now(),
                ]);
                $this->chat->post(
                    $this->chat->forCustomer($customer),
                    $customer,
                    'Mình chưa chốt yêu cầu in #'.$locked->id.'.',
                    ChatAttachmentType::Print->value,
                    $locked->id,
                );

                return $locked->refresh();
            }

            $order = $this->placeOrder($customer, $locked);
            $locked->update([
                'status' => PrintRequestStatus::Accepted,
                'order_id' => $order->id,
                'decided_at' => now(),
            ]);
            $this->chat->post(
                $this->chat->forCustomer($customer),
                $customer,
                'Mình chốt báo giá yêu cầu in #'.$locked->id.'. Đơn #'.$order->id.'.',
                ChatAttachmentType::Print->value,
                $locked->id,
            );

            return $locked->refresh();
        });
    }

    private function placeOrder(User $customer, PrintRequest $request): \App\Domain\Order\Models\Order
    {
        $quantity = max(1, (int) $request->quantity);
        $total = round($request->amount(), 2);
        $zones = implode(', ', PrintCatalog::placementLabels($request->placements ?? []));

        return $this->orders->createPickupPrint($customer, 'In theo yêu cầu #'.$request->id.($zones !== '' ? ' · '.$zones : ''), [
            'line_type' => LineType::Print,
            'product_id' => null,
            'product_variant_id' => null,
            'product_name' => 'In '.PrintCatalog::garmentLabel($request->garment).' #'.$request->id,
            'sku' => 'IN-'.$request->id,
            'image_url' => $request->artworkUrl(),
            'size' => $request->size,
            'color' => PrintCatalog::colorLabel($request->color),
            'unit_price' => round($total / $quantity, 2),
            'quantity' => $quantity,
            'line_total' => $total,
        ]);
    }

    private function storeArtwork(UploadedFile $artwork): string
    {
        if ($this->images->configured()) {
            return $this->images->upload($artwork, 'webthethao/prints');
        }

        return $artwork->store('prints', 'public');
    }

    private function plain(?string $value, int $limit): ?string
    {
        $text = trim(strip_tags((string) $value));

        return $text === '' ? null : Str::limit($text, $limit, '');
    }
}
