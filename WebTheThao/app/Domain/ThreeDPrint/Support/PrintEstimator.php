<?php

namespace App\Domain\ThreeDPrint\Support;

final class PrintEstimator
{
    public const STARTUP = 20000;

    public const MINIMUM = 35000;

    public const SHELL = 0.35;

    /** @var array<string, array{label: string, rate: int}> */
    public const MATERIALS = [
        'pla' => ['label' => 'PLA', 'rate' => 800],
        'petg' => ['label' => 'PETG', 'rate' => 1100],
        'abs' => ['label' => 'ABS', 'rate' => 1200],
        'tpu' => ['label' => 'TPU', 'rate' => 1600],
    ];

    /** @var array<string, array{label: string, multiplier: float}> */
    public const LAYERS = [
        '0.12' => ['label' => '0,12 mm — mịn', 'multiplier' => 1.45],
        '0.20' => ['label' => '0,20 mm — chuẩn', 'multiplier' => 1.0],
        '0.28' => ['label' => '0,28 mm — nhanh', 'multiplier' => 0.8],
    ];

    /** @var list<int> */
    public const INFILLS = [15, 30, 50, 80, 100];

    public static function price(string $material, string $layer, int $infill, int $quantity, float $volumeCm3): int
    {
        $rate = self::MATERIALS[$material]['rate'];
        $layerFactor = self::LAYERS[$layer]['multiplier'];
        $fill = self::SHELL + (1 - self::SHELL) * ($infill / 100);
        $perPiece = $volumeCm3 * $fill * $rate * $layerFactor;
        $total = self::STARTUP + ($perPiece * max(1, $quantity));

        return (int) max(self::MINIMUM, (int) round($total));
    }

    public static function materialLabel(string $material): string
    {
        return self::MATERIALS[$material]['label'] ?? $material;
    }

    public static function layerLabel(string $layer): string
    {
        return self::LAYERS[$layer]['label'] ?? $layer;
    }
}
