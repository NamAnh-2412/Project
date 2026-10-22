<?php

namespace App\Domain\CustomPrint\Support;

final class PrintCatalog
{
    public const PLACEMENT_FEE = 25000;

    public const MAX_QUANTITY = 30;

    /** @var array<string, array{label: string, base: int}> */
    public const GARMENTS = [
        'ao-dau' => ['label' => 'Áo đấu', 'base' => 180000],
        'ao-tap' => ['label' => 'Áo tập', 'base' => 150000],
        'quan' => ['label' => 'Quần', 'base' => 120000],
    ];

    /** @var array<string, string> */
    public const COLORS = [
        'navy' => 'Xanh navy',
        'white' => 'Trắng',
        'black' => 'Đen',
        'red' => 'Đỏ',
    ];

    /** @var list<string> */
    public const SIZES = ['S', 'M', 'L', 'XL', 'XXL'];

    /** @var array<string, string> */
    public const PLACEMENTS = [
        'front' => 'Ngực trước',
        'back' => 'Lưng',
        'left_sleeve' => 'Tay trái',
        'right_sleeve' => 'Tay phải',
    ];

    /** @param  list<string>  $placements */
    public static function estimate(string $garment, int $quantity, array $placements): int
    {
        $base = self::GARMENTS[$garment]['base'] ?? 0;
        $zones = count(array_unique(array_values($placements)));

        return ($base + ($zones * self::PLACEMENT_FEE)) * max(1, $quantity);
    }

    public static function garmentLabel(string $garment): string
    {
        return self::GARMENTS[$garment]['label'] ?? $garment;
    }

    public static function colorLabel(string $color): string
    {
        return self::COLORS[$color] ?? $color;
    }

    /** @param  list<string>  $placements */
    public static function placementLabels(array $placements): array
    {
        return array_values(array_map(
            fn (string $key) => self::PLACEMENTS[$key] ?? $key,
            $placements,
        ));
    }
}
