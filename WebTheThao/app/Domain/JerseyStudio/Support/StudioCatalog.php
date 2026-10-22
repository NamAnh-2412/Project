<?php

namespace App\Domain\JerseyStudio\Support;

use App\Domain\Product\Enums\OfferMode;
use App\Domain\Product\Models\Product;
use App\Domain\Product\Models\ProductVariant;
use Illuminate\Support\Str;

class StudioCatalog
{
    /** @var array<string, string> */
    private const COLOR_HEX = [
        'xanh dương' => '#1f6fbf',
        'xanh navy' => '#1b4f7d',
        'xanh lá cây' => '#2f8f4e',
        'xanh lá' => '#2f8f4e',
        'xanh rêu' => '#4f7a4a',
        'trắng sữa' => '#f4efe6',
        'đen' => '#1c1c1c',
        'trắng' => '#f7f8fa',
        'đỏ' => '#c7362f',
        'vàng' => '#e2b33c',
        'cam' => '#f26b38',
        'hồng' => '#e58aaa',
        'tím' => '#6b4c9a',
        'nâu' => '#7a4b32',
        'xám' => '#8d939c',
        'be' => '#e6d3b3',
        'kem' => '#f3e6c8',
        'bạc' => '#c5c9d1',
        'xanh' => '#1f6fbf',
        'navy' => '#1b4f7d',
        'orange' => '#f26b38',
        'white' => '#f7f8fa',
    ];

    /** @return list<array{id: int, name: string, sport: ?string, blank: bool, group: string, colors: list<array{key: string, label: string, hex: string, sizes: list<array{variant_id: int, size: ?string, label: string, available: int}>}>}> */
    public static function garments(): array
    {
        $rank = ['white' => 0, 'blank' => 1, 'catalog' => 2];

        return Product::query()
            ->where('is_active', true)
            ->where('is_suspended', false)
            ->whereIn('offer_mode', [OfferMode::Sale, OfferMode::Both])
            ->where(function ($query) {
                $query->where('is_print_blank', true)
                    ->orWhereHas('category', function ($query) {
                        $query->where('is_active', true)->where(function ($query) {
                            $query->where('slug', 'like', '%ao%')
                                ->orWhere('name', 'like', '%áo%');
                        });
                    });
            })
            ->with([
                'sport:id,name',
                'variants' => fn ($query) => $query->where('is_active', true)->whereNotNull('sale_price')->with('stock')->orderBy('id'),
            ])
            ->orderBy('name')
            ->get()
            ->map(function (Product $product) {
                $colors = self::colorsFor($product);
                if ($colors === []) {
                    return null;
                }

                $keys = array_column($colors, 'key');
                $group = ! $product->is_print_blank
                    ? 'catalog'
                    : (count(array_unique($keys)) === 1 && $keys[0] === 'trang' ? 'white' : 'blank');

                return [
                    'id' => $product->id,
                    'name' => $product->name,
                    'sport' => $product->sport?->name,
                    'blank' => (bool) $product->is_print_blank,
                    'group' => $group,
                    'colors' => $colors,
                ];
            })
            ->filter()
            ->sort(fn (array $a, array $b) => [$rank[$a['group']], mb_strtolower($a['name'])] <=> [$rank[$b['group']], mb_strtolower($b['name'])])
            ->values()
            ->all();
    }

    /** @return array{id: int, name: string, sport: ?string, blank: bool, group: string, colors: list<array{key: string, label: string, hex: string, sizes: list<array{variant_id: int, size: ?string, label: string, available: int}>}>}|null */
    public static function find(int $id): ?array
    {
        foreach (self::garments() as $garment) {
            if ($garment['id'] === $id) {
                return $garment;
            }
        }

        return null;
    }

    /** @return list<array{key: string, label: string, hex: string, sizes: list<array{variant_id: int, size: ?string, label: string, available: int}>}> */
    public static function colorsFor(Product $product): array
    {
        $colors = [];
        foreach ($product->variants as $variant) {
            $available = self::available($variant);
            $label = trim((string) $variant->color);
            if ($label === '' && $product->is_print_blank) {
                $label = 'Trắng';
            }
            $key = Str::slug($label);
            if ($label === '' || $key === '' || $available < 1) {
                continue;
            }
            if (! isset($colors[$key])) {
                $colors[$key] = [
                    'key' => $key,
                    'label' => $label,
                    'hex' => self::hexFor($label),
                    'sizes' => [],
                ];
            }
            $size = trim((string) $variant->size);
            if ($size === '' && $product->is_print_blank) {
                $size = 'Freesize';
            }
            $colors[$key]['sizes'][] = [
                'variant_id' => $variant->id,
                'size' => $size !== '' ? $size : null,
                'label' => $size !== '' ? $size : 'Một size',
                'available' => $available,
            ];
        }

        return array_values($colors);
    }

    private static function available(ProductVariant $variant): int
    {
        return (int) ($variant->stock?->availableQuantity() ?? 0);
    }

    public static function hexFor(string $label): string
    {
        $normalized = mb_strtolower(trim(preg_replace('/\s+/u', ' ', $label) ?? $label));
        $names = self::COLOR_HEX;
        uksort($names, fn (string $a, string $b) => mb_strlen($b) <=> mb_strlen($a));
        foreach ($names as $name => $hex) {
            if ($normalized === $name || str_starts_with($normalized, $name)) {
                return $hex;
            }
        }

        $hue = abs(crc32($normalized)) % 360;

        return self::hslToHex($hue, 0.48, 0.42);
    }

    private static function hslToHex(int $hue, float $saturation, float $lightness): string
    {
        $c = (1 - abs(2 * $lightness - 1)) * $saturation;
        $x = $c * (1 - abs(fmod($hue / 60, 2) - 1));
        $m = $lightness - $c / 2;
        [$red, $green, $blue] = match (intdiv($hue, 60)) {
            0 => [$c, $x, 0],
            1 => [$x, $c, 0],
            2 => [0, $c, $x],
            3 => [0, $x, $c],
            4 => [$x, 0, $c],
            default => [$c, 0, $x],
        };

        return sprintf(
            '#%02x%02x%02x',
            (int) round(($red + $m) * 255),
            (int) round(($green + $m) * 255),
            (int) round(($blue + $m) * 255),
        );
    }
}
