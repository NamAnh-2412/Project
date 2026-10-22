<?php

namespace App\Domain\ThreeDPrint\Support;

use Illuminate\Http\UploadedFile;
use Illuminate\Validation\ValidationException;

final class ModelFileInspector
{
    /** @param  array{length_mm?: float, width_mm?: float, height_mm?: float}|null  $box */
    public function inspect(UploadedFile $file, ?array $box = null): array
    {
        $extension = strtolower($file->getClientOriginalExtension());
        $format = match ($extension) {
            'stl' => 'stl',
            'obj' => 'obj',
            'step', 'stp' => 'step',
            default => null,
        };
        if ($format === null) {
            throw ValidationException::withMessages([
                'model' => ['Chỉ nhận file STL, OBJ hoặc STEP.'],
            ]);
        }

        $contents = (string) file_get_contents($file->getRealPath());
        $this->rejectExecutable($contents, $format);

        $volume = match ($format) {
            'stl' => MeshVolume::stl($contents),
            'obj' => MeshVolume::obj($contents),
            default => $this->stepVolume($box),
        };

        if ($volume < 0.05 || $volume > 4000) {
            throw ValidationException::withMessages([
                'model' => ['Thể tích phải từ 0,05 đến 4.000 cm³. File STEP cần kích thước hộp bao.'],
            ]);
        }

        return [
            'format' => $format,
            'volume_cm3' => $volume,
            'original_name' => $this->originalName($file, $extension),
        ];
    }

    /** @param  array{length_mm?: float, width_mm?: float, height_mm?: float}|null  $box */
    private function stepVolume(?array $box): float
    {
        $length = (float) ($box['length_mm'] ?? 0);
        $width = (float) ($box['width_mm'] ?? 0);
        $height = (float) ($box['height_mm'] ?? 0);
        if ($length < 5 || $width < 5 || $height < 5 || $length > 250 || $width > 250 || $height > 250) {
            throw ValidationException::withMessages([
                'length_mm' => ['File STEP cần dài, rộng, cao từ 5 đến 250 mm.'],
            ]);
        }

        return MeshVolume::box($length, $width, $height);
    }

    private function rejectExecutable(string $contents, string $format): void
    {
        $head = substr($contents, 0, 512);
        if (str_contains($head, '<?php') || str_starts_with(ltrim($head), '<')) {
            throw ValidationException::withMessages([
                'model' => ['File không phải mô hình 3D.'],
            ]);
        }

        if ($format === 'stl' && ! $this->looksLikeStl($contents)) {
            throw ValidationException::withMessages([
                'model' => ['File STL không đúng cấu trúc.'],
            ]);
        }

        if ($format === 'obj' && ! preg_match('/^v\s+/m', $contents)) {
            throw ValidationException::withMessages([
                'model' => ['File OBJ không có đỉnh.'],
            ]);
        }

        if ($format === 'step' && ! str_contains($head, 'ISO-10303-21')) {
            throw ValidationException::withMessages([
                'model' => ['File STEP không đúng cấu trúc.'],
            ]);
        }
    }

    private function looksLikeStl(string $contents): bool
    {
        $trimmed = ltrim($contents);
        if (str_starts_with($trimmed, 'solid') && str_contains($contents, 'facet')) {
            return true;
        }

        if (strlen($contents) < 84) {
            return false;
        }

        $count = unpack('V', substr($contents, 80, 4))[1];

        return $count >= 1 && strlen($contents) >= 84 + ($count * 50);
    }

    private function originalName(UploadedFile $file, string $extension): string
    {
        $base = pathinfo($file->getClientOriginalName(), PATHINFO_FILENAME);
        $base = trim(preg_replace('/[^A-Za-z0-9._-]+/', '-', (string) $base) ?? '', '-');
        if ($base === '') {
            $base = 'mo-hinh';
        }

        return substr($base, 0, 80).'.'.$extension;
    }
}
