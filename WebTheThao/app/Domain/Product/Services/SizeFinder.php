<?php

namespace App\Domain\Product\Services;

use App\Domain\Product\Models\Product;

class SizeFinder
{
    /** @return array{status: string, size: ?string, message: string} */
    public function recommend(Product $product, float $heightCm, float $weightKg): array
    {
        $guide = $product->sizeGuide;
        $rows = is_array($guide?->measurements) ? $guide->measurements : [];
        if ($guide === null || ! $guide->is_active || $rows === []) {
            return [
                'status' => 'unavailable',
                'size' => null,
                'message' => 'Sản phẩm này chưa có bảng size.',
            ];
        }

        $matches = [];
        foreach ($rows as $size => $row) {
            if (! is_array($row)) {
                continue;
            }
            $minHeight = $this->number($row['min_height_cm'] ?? null);
            $maxHeight = $this->number($row['max_height_cm'] ?? null);
            $minWeight = $this->number($row['min_weight_kg'] ?? null);
            $maxWeight = $this->number($row['max_weight_kg'] ?? null);
            if ($minHeight === null || $maxHeight === null || $minWeight === null || $maxWeight === null) {
                continue;
            }
            if ($heightCm >= $minHeight && $heightCm <= $maxHeight && $weightKg >= $minWeight && $weightKg <= $maxWeight) {
                $matches[] = (string) $size;
            }
        }

        if (count($matches) !== 1) {
            return [
                'status' => 'unavailable',
                'size' => null,
                'message' => count($matches) > 1
                    ? 'Số đo nằm trong nhiều size. Hãy xem bảng và chọn size.'
                    : 'Chưa có size khớp chiều cao và cân nặng này.',
            ];
        }

        return [
            'status' => 'recommended',
            'size' => $matches[0],
            'message' => 'Gợi ý size '.$matches[0].' theo bảng của cửa hàng.',
        ];
    }

    private function number(mixed $value): ?float
    {
        return is_numeric($value) ? (float) $value : null;
    }
}
