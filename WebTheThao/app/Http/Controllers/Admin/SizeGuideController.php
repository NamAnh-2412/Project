<?php

namespace App\Http\Controllers\Admin;

use App\Domain\Product\Models\SizeGuide;
use App\Http\Controllers\Controller;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\ValidationException;
use Illuminate\View\View;

class SizeGuideController extends Controller
{
    public function index(): View
    {
        $guides = SizeGuide::query()->withCount('products')->orderBy('name')->get();

        return view('admin.size-guides.index', compact('guides'));
    }

    public function create(): View
    {
        return view('admin.size-guides.create');
    }

    public function store(Request $request): RedirectResponse
    {
        SizeGuide::query()->create($this->validated($request));

        return redirect()->route('admin.size-guides.index')->with('success', 'Đã thêm bảng size.');
    }

    public function edit(SizeGuide $sizeGuide): View
    {
        return view('admin.size-guides.edit', compact('sizeGuide'));
    }

    public function update(Request $request, SizeGuide $sizeGuide): RedirectResponse
    {
        $sizeGuide->update($this->validated($request));

        return redirect()->route('admin.size-guides.index')->with('success', 'Đã cập nhật bảng size.');
    }

    public function destroy(SizeGuide $sizeGuide): RedirectResponse
    {
        if ($sizeGuide->products()->exists()) {
            return back()->with('error', 'Bảng size đang gắn với sản phẩm, chưa xóa được.');
        }
        $sizeGuide->delete();

        return redirect()->route('admin.size-guides.index')->with('success', 'Đã xóa bảng size.');
    }

    /** @return array{name: string, group: string, brand: ?string, measurements: array<string, array<string, float>>, is_active: bool} */
    private function validated(Request $request): array
    {
        $data = $request->validate([
            'name' => ['required', 'string', 'max:255'],
            'group' => ['required', 'in:ao,quan,giay'],
            'brand' => ['nullable', 'string', 'max:100'],
            'is_active' => ['nullable', 'boolean'],
            'rows' => ['required', 'array', 'min:1'],
            'rows.*.size' => ['nullable', 'string', 'max:30'],
            'rows.*.min_height_cm' => ['nullable', 'numeric', 'min:50', 'max:250'],
            'rows.*.max_height_cm' => ['nullable', 'numeric', 'min:50', 'max:250'],
            'rows.*.min_weight_kg' => ['nullable', 'numeric', 'min:10', 'max:250'],
            'rows.*.max_weight_kg' => ['nullable', 'numeric', 'min:10', 'max:250'],
        ]);

        $measurements = [];
        foreach ($data['rows'] as $index => $row) {
            $size = trim((string) ($row['size'] ?? ''));
            if ($size === '') {
                continue;
            }
            foreach (['min_height_cm', 'max_height_cm', 'min_weight_kg', 'max_weight_kg'] as $key) {
                if (! is_numeric($row[$key] ?? null)) {
                    throw ValidationException::withMessages([
                        "rows.$index.$key" => 'Mỗi size cần đủ chiều cao và cân nặng nhỏ nhất, lớn nhất.',
                    ]);
                }
            }
            if ((float) $row['max_height_cm'] < (float) $row['min_height_cm'] || (float) $row['max_weight_kg'] < (float) $row['min_weight_kg']) {
                throw ValidationException::withMessages([
                    "rows.$index.size" => 'Mức lớn nhất phải lớn hơn hoặc bằng mức nhỏ nhất.',
                ]);
            }
            $measurements[$size] = [
                'min_height_cm' => (float) $row['min_height_cm'],
                'max_height_cm' => (float) $row['max_height_cm'],
                'min_weight_kg' => (float) $row['min_weight_kg'],
                'max_weight_kg' => (float) $row['max_weight_kg'],
            ];
        }

        if ($measurements === []) {
            throw ValidationException::withMessages(['rows' => 'Thêm ít nhất một size.']);
        }

        return [
            'name' => $data['name'],
            'group' => $data['group'],
            'brand' => filled($data['brand'] ?? null) ? trim($data['brand']) : null,
            'measurements' => $measurements,
            'is_active' => (bool) ($data['is_active'] ?? false),
        ];
    }
}
