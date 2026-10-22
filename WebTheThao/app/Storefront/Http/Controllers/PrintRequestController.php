<?php

namespace App\Storefront\Http\Controllers;

use App\Domain\CustomPrint\Models\PrintRequest;
use App\Domain\CustomPrint\Services\PrintRequestService;
use App\Domain\CustomPrint\Support\PrintCatalog;
use App\Http\Controllers\Controller;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

class PrintRequestController extends Controller
{
    public function __construct(private PrintRequestService $prints) {}

    public function store(Request $request): RedirectResponse
    {
        $data = $request->validate([
            'garment' => ['required', 'string', Rule::in(array_keys(PrintCatalog::GARMENTS))],
            'color' => ['required', 'string', Rule::in(array_keys(PrintCatalog::COLORS))],
            'size' => ['required', 'string', Rule::in(PrintCatalog::SIZES)],
            'quantity' => ['required', 'integer', 'min:1', 'max:'.PrintCatalog::MAX_QUANTITY],
            'placements' => ['required', 'array', 'min:1'],
            'placements.*' => ['string', Rule::in(array_keys(PrintCatalog::PLACEMENTS))],
            'notes' => ['nullable', 'string', 'max:1000'],
            'artwork' => ['required', 'image', 'mimes:jpg,jpeg,png,webp', 'max:5120'],
        ], [
            'garment.required' => 'Chọn loại sản phẩm in.',
            'color.required' => 'Chọn màu.',
            'size.required' => 'Chọn size.',
            'quantity.required' => 'Nhập số lượng.',
            'placements.required' => 'Chọn ít nhất một vị trí in.',
            'artwork.required' => 'Tải ảnh mẫu hoặc logo cần in.',
            'artwork.image' => 'File mẫu phải là ảnh.',
            'artwork.max' => 'Ảnh mẫu tối đa 5MB.',
        ]);

        $this->prints->submit($request->user(), [
            'garment' => $data['garment'],
            'color' => $data['color'],
            'size' => $data['size'],
            'quantity' => (int) $data['quantity'],
            'placements' => $data['placements'],
            'notes' => $data['notes'] ?? null,
        ], $request->file('artwork'));

        return redirect()
            ->route('messages.show')
            ->with('status', 'Đã gửi yêu cầu in vào hội thoại với cửa hàng.');
    }

    public function decide(Request $request, PrintRequest $printRequest): RedirectResponse
    {
        $data = $request->validate([
            'decision' => ['required', 'string', Rule::in(['accept', 'decline'])],
        ]);

        $this->prints->decide($request->user(), $printRequest, $data['decision']);

        $message = $data['decision'] === 'accept'
            ? 'Đã chốt báo giá và tạo đơn in.'
            : 'Đã ghi nhận bạn chưa chốt yêu cầu in.';

        return redirect()->route('messages.show')->with('status', $message);
    }
}
