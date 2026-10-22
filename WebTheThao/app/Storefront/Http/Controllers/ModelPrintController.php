<?php

namespace App\Storefront\Http\Controllers;

use App\Domain\ThreeDPrint\Models\ModelPrintRequest;
use App\Domain\ThreeDPrint\Services\ModelPrintService;
use App\Domain\ThreeDPrint\Support\PrintEstimator;
use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;
use Illuminate\View\View;
use Symfony\Component\HttpFoundation\StreamedResponse;

class ModelPrintController extends Controller
{
    public function __construct(private ModelPrintService $models) {}

    public function create(): View
    {
        return view('storefront.models.create', [
            'materials' => PrintEstimator::MATERIALS,
            'layers' => PrintEstimator::LAYERS,
            'infills' => PrintEstimator::INFILLS,
            'startup' => PrintEstimator::STARTUP,
            'minimum' => PrintEstimator::MINIMUM,
            'shell' => PrintEstimator::SHELL,
        ]);
    }

    public function preview(Request $request): JsonResponse
    {
        $data = $this->validated($request);

        return response()->json($this->models->preview($request->file('model'), $data));
    }

    public function store(Request $request): RedirectResponse
    {
        $data = $this->validated($request);
        $this->models->submit($request->user(), $data, $request->file('model'));

        return redirect()
            ->route('messages.show')
            ->with('status', 'Đã gửi file in 3D vào hội thoại với cửa hàng.');
    }

    public function decide(Request $request, ModelPrintRequest $modelRequest): RedirectResponse
    {
        $data = $request->validate([
            'decision' => ['required', 'string', Rule::in(['accept', 'decline'])],
        ]);
        $this->models->decide($request->user(), $modelRequest, $data['decision']);

        return redirect()
            ->route('messages.show')
            ->with('status', $data['decision'] === 'accept' ? 'Đã chốt in 3D và tạo đơn.' : 'Đã ghi nhận bạn chưa chốt in 3D.');
    }

    public function download(Request $request, ModelPrintRequest $modelRequest): StreamedResponse
    {
        return $this->models->download($request->user(), $modelRequest);
    }

    /** @return array{material: string, layer_height: string, infill: int, quantity: int, length_mm: ?float, width_mm: ?float, height_mm: ?float, notes: ?string} */
    private function validated(Request $request): array
    {
        $data = $request->validate([
            'model' => ['required', 'file', 'max:20480'],
            'material' => ['required', 'string', Rule::in(array_keys(PrintEstimator::MATERIALS))],
            'layer_height' => ['required', 'string', Rule::in(array_keys(PrintEstimator::LAYERS))],
            'infill' => ['required', 'integer', Rule::in(PrintEstimator::INFILLS)],
            'quantity' => ['required', 'integer', 'min:1', 'max:20'],
            'length_mm' => ['nullable', 'numeric', 'min:5', 'max:250'],
            'width_mm' => ['nullable', 'numeric', 'min:5', 'max:250'],
            'height_mm' => ['nullable', 'numeric', 'min:5', 'max:250'],
            'notes' => ['nullable', 'string', 'max:1000'],
        ], [
            'model.required' => 'Chọn file STL, OBJ hoặc STEP.',
            'model.max' => 'File tối đa 20MB.',
            'material.required' => 'Chọn chất liệu.',
            'layer_height.required' => 'Chọn độ dày lớp.',
            'infill.required' => 'Chọn độ đặc.',
        ]);

        return [
            'material' => $data['material'],
            'layer_height' => $data['layer_height'],
            'infill' => (int) $data['infill'],
            'quantity' => (int) $data['quantity'],
            'length_mm' => isset($data['length_mm']) ? (float) $data['length_mm'] : null,
            'width_mm' => isset($data['width_mm']) ? (float) $data['width_mm'] : null,
            'height_mm' => isset($data['height_mm']) ? (float) $data['height_mm'] : null,
            'notes' => $data['notes'] ?? null,
        ];
    }
}
