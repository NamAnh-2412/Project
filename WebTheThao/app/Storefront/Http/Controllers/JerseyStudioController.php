<?php

namespace App\Storefront\Http\Controllers;

use App\Domain\JerseyStudio\Models\JerseyDesign;
use App\Domain\JerseyStudio\Services\JerseyDesignService;
use App\Domain\JerseyStudio\Support\StudioCatalog;
use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class JerseyStudioController extends Controller
{
    public function __construct(private JerseyDesignService $studio) {}

    public function show(Request $request): View
    {
        $design = null;
        $myDesigns = collect();
        if ($request->user()?->isCustomer()) {
            $myDesigns = JerseyDesign::query()->where('user_id', $request->user()->id)->latest()->limit(8)->get();
            if ($request->filled('design')) {
                $design = JerseyDesign::query()
                    ->where('user_id', $request->user()->id)
                    ->findOrFail((int) $request->integer('design'));
            }
        }

        $garments = StudioCatalog::garments();
        $savedProductId = (int) ($design?->design_payload['product_id'] ?? 0);
        $selectedGarment = collect($garments)->firstWhere('id', $savedProductId) ?? ($garments[0] ?? null);

        return view('storefront.studio.show', [
            'design' => $design,
            'myDesigns' => $myDesigns,
            'garments' => $garments,
            'selectedGarment' => $selectedGarment,
            'modelUrl' => asset('models/custom-studio/football-jersey.glb'),
        ]);
    }

    public function save(Request $request): JsonResponse
    {
        $data = $this->validated($request);
        $design = $this->studio->save($request->user(), $data, $request->file('logo'));

        return response()->json([
            'id' => $design->id,
            'status' => $design->status,
            'message' => 'Đã lưu bản nháp.',
        ], 201);
    }

    public function submit(Request $request, JerseyDesign $jerseyDesign): JsonResponse
    {
        $data = $this->validated($request);
        $design = $this->studio->submit($request->user(), $jerseyDesign, $data);

        return response()->json([
            'id' => $design->id,
            'status' => $design->status,
            'message' => 'Đã gửi bản phối vào hội thoại với cửa hàng.',
        ]);
    }

    public function discuss(Request $request, JerseyDesign $jerseyDesign): RedirectResponse
    {
        $this->studio->discuss($request->user(), $jerseyDesign);

        return redirect()
            ->route('messages.show')
            ->with('success', 'Đã mở hội thoại để thảo luận báo giá. Giá trên bản phối giữ nguyên cho đến khi cửa hàng báo giá mới.');
    }

    public function decide(Request $request, JerseyDesign $jerseyDesign): JsonResponse
    {
        $decision = (string) $request->input('decision');
        $design = $this->studio->decide($request->user(), $jerseyDesign, $decision);

        return response()->json([
            'status' => $design->status,
            'message' => $decision === 'accepted' ? 'Đã chốt báo giá và tạo đơn.' : 'Đã ghi nhận bạn chưa chốt.',
        ]);
    }

    /** @return array<string, mixed> */
    private function validated(Request $request): array
    {
        $data = $request->validate([
            'design_id' => ['nullable', 'integer'],
            'garment_template' => ['nullable', 'string', 'max:80'],
            'design_payload' => ['required', 'string', 'max:200000'],
            'size' => ['nullable', 'string', 'max:12'],
            'quantity' => ['nullable', 'integer', 'min:1', 'max:100'],
            'notes' => ['nullable', 'string', 'max:1000'],
            'logo_side' => ['nullable', 'string', 'max:20'],
            'logo' => ['nullable', 'image', 'mimes:jpg,jpeg,png,webp', 'max:2048'],
        ], [
            'design_payload.required' => 'Chưa có dữ liệu bản phối.',
            'logo.image' => 'Logo phải là ảnh.',
            'logo.max' => 'Logo tối đa 2MB.',
        ]);

        $payload = json_decode($data['design_payload'], true);
        if (! is_array($payload)) {
            throw \Illuminate\Validation\ValidationException::withMessages([
                'design_payload' => ['Bản phối không đọc được.'],
            ]);
        }

        return [
            'design_id' => $data['design_id'] ?? null,
            'design_payload' => $payload,
            'size' => $data['size'] ?? null,
            'quantity' => $data['quantity'] ?? 1,
            'notes' => $data['notes'] ?? null,
            'logo_side' => $data['logo_side'] ?? 'front',
        ];
    }
}
