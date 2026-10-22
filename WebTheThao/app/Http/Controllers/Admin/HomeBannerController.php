<?php

namespace App\Http\Controllers\Admin;

use App\Domain\Content\Models\HomeBanner;
use App\Gateway\Media\CloudinaryImageStore;
use App\Http\Controllers\Controller;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\View\View;

class HomeBannerController extends Controller
{
    public function __construct(private CloudinaryImageStore $images) {}

    public function edit(): View
    {
        $slots = [];
        for ($position = 1; $position <= HomeBanner::SLOT_COUNT; $position++) {
            $row = HomeBanner::slot($position);
            $slots[] = [
                'position' => $position,
                'label' => HomeBanner::SLOT_LABELS[$position],
                'banner' => $row,
                'previewUrl' => $position === 1 ? HomeBanner::displayUrl() : $row?->resolvedUrl(),
            ];
        }

        return view('admin.home-banner.edit', [
            'slots' => $slots,
        ]);
    }

    public function update(Request $request): RedirectResponse
    {
        $data = $request->validate([
            'position' => ['nullable', 'integer', 'between:1,'.HomeBanner::SLOT_COUNT],
            'image' => ['nullable', 'image', 'max:5120'],
            'sport' => ['nullable', 'string', 'max:80'],
            'title' => ['nullable', 'string', 'max:120'],
            'description' => ['nullable', 'string', 'max:500'],
        ], [
            'image.image' => 'File phải là ảnh.',
            'image.max' => 'Ảnh tối đa 5MB.',
            'sport.max' => 'Môn thể thao tối đa 80 ký tự.',
            'title.max' => 'Tiêu đề tối đa 120 ký tự.',
            'description.max' => 'Mô tả tối đa 500 ký tự.',
        ]);

        $position = (int) ($data['position'] ?? 1);
        $row = HomeBanner::slot($position);
        $previous = $row?->image_url;
        $path = $request->hasFile('image') ? $this->storeImage($request) : ($previous ?? '');
        $copy = [
            'sport' => trim((string) ($data['sport'] ?? '')) ?: null,
            'title' => trim((string) ($data['title'] ?? '')) ?: null,
            'description' => trim((string) ($data['description'] ?? '')) ?: null,
        ];

        if ($row) {
            $row->update(['image_url' => $path, ...$copy]);
        } else {
            HomeBanner::query()->create([
                'position' => $position,
                'image_url' => $path,
                ...$copy,
            ]);
        }

        if ($request->hasFile('image') && $previous && $previous !== $path) {
            $this->deleteImage($previous);
        }

        return back()->with('success', 'Đã cập nhật '.HomeBanner::SLOT_LABELS[$position].'.');
    }

    public function destroy(Request $request): RedirectResponse
    {
        $position = (int) $request->input('position', 1);
        if ($position < 1 || $position > HomeBanner::SLOT_COUNT) {
            $position = 1;
        }

        $row = HomeBanner::slot($position);
        if ($row) {
            $this->deleteImage($row->image_url);
            if (! filled($row->sport) && ! filled($row->title) && ! filled($row->description)) {
                $row->delete();
            } else {
                $row->update(['image_url' => '']);
            }
        }

        $message = $position === 1
            ? 'Đã trở lại ảnh mặc định cho slide 1.'
            : 'Đã xóa ảnh '.HomeBanner::SLOT_LABELS[$position].'.';

        return back()->with('success', $message);
    }

    private function storeImage(Request $request): string
    {
        $file = $request->file('image');

        if ($this->images->configured()) {
            return $this->images->upload($file, 'webthethao/banners');
        }

        return $file->store('banners', 'public');
    }

    private function deleteImage(?string $imageUrl): void
    {
        if ($imageUrl === null || $imageUrl === '') {
            return;
        }

        if (str_contains($imageUrl, 'res.cloudinary.com')) {
            $this->images->delete($imageUrl);

            return;
        }

        if (! str_starts_with($imageUrl, 'http') && ! str_starts_with($imageUrl, '/')) {
            Storage::disk('public')->delete($imageUrl);
        }
    }
}
