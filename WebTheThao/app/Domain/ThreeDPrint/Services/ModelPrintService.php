<?php

namespace App\Domain\ThreeDPrint\Services;

use App\Domain\Chat\Enums\ChatAttachmentType;
use App\Domain\Chat\Services\ChatService;
use App\Domain\Order\Enums\LineType;
use App\Domain\Order\Models\Order;
use App\Domain\Order\Services\OrderWriter;
use App\Domain\ThreeDPrint\Enums\ModelRequestStatus;
use App\Domain\ThreeDPrint\Models\ModelPrintRequest;
use App\Domain\ThreeDPrint\Support\ModelFileInspector;
use App\Domain\ThreeDPrint\Support\PrintEstimator;
use App\Domain\User\Models\User;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use Illuminate\Validation\ValidationException;

class ModelPrintService
{
    public function __construct(
        private ChatService $chat,
        private OrderWriter $orders,
        private ModelFileInspector $files,
    ) {}

    /** @param  array{material: string, layer_height: string, infill: int, quantity: int, length_mm?: float|null, width_mm?: float|null, height_mm?: float|null, notes?: ?string}  $data */
    public function preview(UploadedFile $file, array $data): array
    {
        $inspected = $this->files->inspect($file, $this->box($data));
        $estimate = PrintEstimator::price(
            $data['material'],
            $data['layer_height'],
            (int) $data['infill'],
            (int) $data['quantity'],
            $inspected['volume_cm3'],
        );

        return [
            'volume_cm3' => $inspected['volume_cm3'],
            'estimate' => $estimate,
            'original_name' => $inspected['original_name'],
        ];
    }

    /** @param  array{material: string, layer_height: string, infill: int, quantity: int, length_mm?: float|null, width_mm?: float|null, height_mm?: float|null, notes?: ?string}  $data */
    public function submit(User $customer, array $data, UploadedFile $file): ModelPrintRequest
    {
        $inspected = $this->files->inspect($file, $this->box($data));
        $estimate = PrintEstimator::price(
            $data['material'],
            $data['layer_height'],
            (int) $data['infill'],
            (int) $data['quantity'],
            $inspected['volume_cm3'],
        );
        $path = $file->storeAs(
            'three-d-prints/'.$customer->id,
            Str::uuid()->toString().'.'.$inspected['format'],
            'local',
        );
        $notes = $this->plain($data['notes'] ?? null, 1000);

        return DB::transaction(function () use ($customer, $data, $inspected, $estimate, $path, $notes) {
            $conversation = $this->chat->forCustomer($customer);
            $request = ModelPrintRequest::query()->create([
                'user_id' => $customer->id,
                'conversation_id' => $conversation->id,
                'original_name' => $inspected['original_name'],
                'disk_path' => $path,
                'format' => $inspected['format'],
                'material' => $data['material'],
                'layer_height' => $data['layer_height'],
                'infill' => (int) $data['infill'],
                'quantity' => (int) $data['quantity'],
                'volume_cm3' => $inspected['volume_cm3'],
                'length_mm' => $data['length_mm'] ?? null,
                'width_mm' => $data['width_mm'] ?? null,
                'height_mm' => $data['height_mm'] ?? null,
                'notes' => $notes,
                'estimate_amount' => $estimate,
                'status' => ModelRequestStatus::Submitted,
            ]);

            $body = 'Mình gửi file '.$request->original_name.' để in 3D. '
                .$request->summary().', số lượng '.$request->quantity.'.';
            if ($notes) {
                $body .= ' Ghi chú: '.$notes;
            }

            $this->chat->post($conversation, $customer, $body, ChatAttachmentType::Model->value, $request->id);

            return $request->refresh();
        });
    }

    public function quote(User $staff, ModelPrintRequest $request, float $amount, ?string $note = null): ModelPrintRequest
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
            $locked = ModelPrintRequest::query()->whereKey($request->id)->lockForUpdate()->firstOrFail();
            if (! in_array($locked->status, [ModelRequestStatus::Submitted, ModelRequestStatus::Quoted], true)) {
                throw ValidationException::withMessages([
                    'quote_amount' => ['Yêu cầu này không còn báo giá được.'],
                ]);
            }

            $locked->update([
                'quote_amount' => round($amount, 2),
                'status' => ModelRequestStatus::Quoted,
                'quoted_by' => $staff->id,
                'quoted_at' => now(),
            ]);

            $body = 'Báo giá '.number_format($locked->amount(), 0, ',', '.').'đ cho file '.$locked->original_name.'.';
            $plain = $this->plain($note, 500);
            if ($plain) {
                $body .= ' '.$plain;
            }
            $this->chat->post(
                $this->chat->forCustomer($locked->user),
                $staff,
                $body,
                ChatAttachmentType::Model->value,
                $locked->id,
            );

            return $locked->refresh();
        });
    }

    public function decide(User $customer, ModelPrintRequest $request, string $decision): ModelPrintRequest
    {
        if ((int) $request->user_id !== (int) $customer->id || ! in_array($decision, ['accept', 'decline'], true)) {
            throw ValidationException::withMessages([
                'decision' => ['Không chốt được yêu cầu in 3D này.'],
            ]);
        }

        return DB::transaction(function () use ($customer, $request, $decision) {
            $locked = ModelPrintRequest::query()->whereKey($request->id)->lockForUpdate()->firstOrFail();
            if ($locked->status !== ModelRequestStatus::Quoted || $locked->order_id !== null) {
                throw ValidationException::withMessages([
                    'decision' => ['Chỉ chốt được yêu cầu đang có báo giá và chưa thành đơn.'],
                ]);
            }

            if ($decision === 'decline') {
                $locked->update([
                    'status' => ModelRequestStatus::Declined,
                    'decided_at' => now(),
                ]);
                $this->chat->post(
                    $this->chat->forCustomer($customer),
                    $customer,
                    'Mình chưa chốt in 3D file '.$locked->original_name.'.',
                    ChatAttachmentType::Model->value,
                    $locked->id,
                );

                return $locked->refresh();
            }

            $order = $this->placeOrder($customer, $locked);
            $locked->update([
                'status' => ModelRequestStatus::Accepted,
                'order_id' => $order->id,
                'decided_at' => now(),
            ]);
            $this->chat->post(
                $this->chat->forCustomer($customer),
                $customer,
                'Mình chốt in 3D file '.$locked->original_name.'. Đơn #'.$order->id.'.',
                ChatAttachmentType::Model->value,
                $locked->id,
            );

            return $locked->refresh();
        });
    }

    public function download(User $viewer, ModelPrintRequest $request): \Symfony\Component\HttpFoundation\StreamedResponse
    {
        if (! $viewer->isStoreAccount() && (int) $viewer->id !== (int) $request->user_id) {
            abort(403);
        }
        if (! str_starts_with($request->disk_path, 'three-d-prints/') || str_contains($request->disk_path, '..')) {
            abort(404);
        }

        return Storage::disk('local')->download($request->disk_path, $request->original_name);
    }

    /** @param  array<string, mixed>  $data */
    private function box(array $data): ?array
    {
        if (! isset($data['length_mm'], $data['width_mm'], $data['height_mm'])) {
            return null;
        }

        return [
            'length_mm' => (float) $data['length_mm'],
            'width_mm' => (float) $data['width_mm'],
            'height_mm' => (float) $data['height_mm'],
        ];
    }

    private function placeOrder(User $customer, ModelPrintRequest $request): Order
    {
        $quantity = max(1, (int) $request->quantity);
        $total = round($request->amount(), 2);

        return $this->orders->createPickupPrint($customer, 'In 3D '.$request->original_name.' · '.$request->summary(), [
            'line_type' => LineType::Print,
            'product_id' => null,
            'product_variant_id' => null,
            'product_name' => 'In 3D '.PrintEstimator::materialLabel($request->material).' #'.$request->id,
            'sku' => '3D-'.$request->id,
            'size' => $request->layer_height.' mm',
            'color' => 'Đặc '.$request->infill.'%',
            'unit_price' => round($total / $quantity, 2),
            'quantity' => $quantity,
            'line_total' => $total,
        ]);
    }

    private function plain(?string $value, int $limit): ?string
    {
        $text = trim(strip_tags((string) $value));

        return $text === '' ? null : Str::limit($text, $limit, '');
    }
}
