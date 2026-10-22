<?php

namespace App\Domain\Product\Services;

use App\Domain\Order\Enums\LineType;
use App\Domain\Order\Enums\OrderStatus;
use App\Domain\Order\Models\OrderItem;
use App\Domain\Product\Enums\OfferMode;
use App\Domain\Product\Models\Category;
use App\Domain\Product\Models\Product;
use App\Domain\Product\Models\ProductVariant;
use App\Domain\Product\Models\Sport;
use App\Domain\Product\Support\CatalogCache;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Support\Collection;

class CatalogService
{
    /** @return Collection<int, Sport> */
    public function sports(): Collection
    {
        return CatalogCache::remember('sports', fn () => Sport::query()
            ->where('is_active', true)
            ->orderBy('sort_order')
            ->orderBy('name')
            ->get());
    }

    /** @return Collection<int, string> */
    public function brands(): Collection
    {
        return CatalogCache::remember('catalog-brands', fn () => $this->published(Product::query())
            ->whereNotNull('brand')
            ->where('brand', '!=', '')
            ->distinct()
            ->orderBy('brand')
            ->pluck('brand')
            ->values());
    }

    /** @return Collection<int, string> */
    public function availableSizes(): Collection
    {
        return CatalogCache::remember('catalog-sizes', function () {
            $order = ['XS' => 10, 'S' => 20, 'M' => 30, 'L' => 40, 'XL' => 50, '2XL' => 60, 'XXL' => 60, '3XL' => 70];

            return ProductVariant::query()
                ->where('is_active', true)
                ->whereNotNull('size')
                ->where('size', '!=', '')
                ->whereHas('product', fn (Builder $query) => $this->published($query))
                ->distinct()
                ->pluck('size')
                ->sortBy(function ($size) use ($order) {
                    $label = strtoupper(trim((string) $size));

                    return $order[$label] ?? (is_numeric($label) ? 100 + (int) $label : 999);
                })
                ->values();
        });
    }

    /** @return Collection<int, string> */
    public function availableColors(): Collection
    {
        return CatalogCache::remember('catalog-colors', fn () => ProductVariant::query()
            ->where('is_active', true)
            ->whereNotNull('color')
            ->where('color', '!=', '')
            ->whereHas('product', fn (Builder $query) => $this->published($query))
            ->distinct()
            ->orderBy('color')
            ->pluck('color')
            ->values());
    }

    /** @return Collection<int, Category> */
    public function categories(): Collection
    {
        return CatalogCache::remember('categories', fn () => Category::query()
            ->where('is_active', true)
            ->orderBy('name')
            ->get());
    }

    public function products(array $filters): LengthAwarePaginator
    {
        $page = max(1, (int) ($filters['page'] ?? 1));
        $limit = min(50, max(1, (int) ($filters['limit'] ?? 12)));
        $cacheKey = 'products:'.md5((string) json_encode([$filters, $page, $limit]));

        return CatalogCache::remember($cacheKey, function () use ($filters, $page, $limit) {
            $query = $this->published(Product::query())
                ->with([
                    'category:id,name,slug,parent_id,is_active',
                    'sport:id,name,slug,sort_order,is_active',
                    'variants' => fn ($variants) => $variants->where('is_active', true)->with('stock'),
                ]);

            $this->applyFilters($query, $filters);
            $this->applySort($query, $filters);

            return $query->paginate($limit, ['*'], 'page', $page);
        });
    }

    public function product(int $id): Product
    {
        return CatalogCache::remember('product:'.$id, function () use ($id) {
            return $this->published(Product::query())
                ->with([
                    'category',
                    'sport',
                    'variants' => fn ($variants) => $variants->where('is_active', true),
                ])
                ->findOrFail($id);
        });
    }

    public function productBySlug(string $slug): Product
    {
        return CatalogCache::remember('product-slug:'.$slug, function () use ($slug) {
            return $this->published(Product::query())
                ->where('slug', $slug)
                ->with([
                    'category',
                    'sport',
                    'variants' => fn ($variants) => $variants->where('is_active', true),
                ])
                ->firstOrFail();
        });
    }

    public function variantForCheckout(int $id): ProductVariant
    {
        return ProductVariant::query()
            ->where('is_active', true)
            ->whereHas('product', fn (Builder $q) => $q->where('is_active', true)->where('is_suspended', false))
            ->with('product')
            ->findOrFail($id);
    }

    /** @return Collection<int, Product> */
    public function bestSellers(int $limit = 8): Collection
    {
        $rows = OrderItem::query()
            ->selectRaw('product_id, SUM(quantity) as sold_qty, SUM(line_total) as sold_revenue')
            ->where('line_type', LineType::Sale)
            ->whereHas('order', fn (Builder $query) => $query->whereIn('status', [
                OrderStatus::Paid->value,
                OrderStatus::Processing->value,
                OrderStatus::Completed->value,
            ]))
            ->groupBy('product_id')
            ->orderByDesc('sold_qty')
            ->orderByDesc('sold_revenue')
            ->limit($limit)
            ->get();

        if ($rows->isEmpty()) {
            return collect();
        }

        $products = $this->published(Product::query())
            ->whereIn('id', $rows->pluck('product_id'))
            ->with([
                'category:id,name,slug,parent_id,is_active',
                'sport:id,name,slug,sort_order,is_active',
                'variants' => fn ($variants) => $variants->where('is_active', true)->with('stock'),
            ])
            ->get()
            ->keyBy('id');

        return $rows
            ->map(function (OrderItem $row) use ($products) {
                $product = $products->get($row->product_id);
                if ($product === null) {
                    return null;
                }

                $product->sold_qty = (int) $row->sold_qty;

                return $product;
            })
            ->filter()
            ->values();
    }

    private function applyFilters(Builder $query, array $filters): void
    {
        $query
            ->when($filters['q'] ?? null, function (Builder $q, string $term) {
                $q->where('products.name', 'like', '%'.$term.'%');
            })
            ->when(filled($filters['gender'] ?? null) && ($filters['gender'] ?? null) !== 'all', function (Builder $q) use ($filters) {
                $gender = match ($filters['gender']) {
                    'nam' => 'nam',
                    'nu' => 'nu',
                    'unisex' => 'unisex',
                    default => null,
                };
                if ($gender === 'unisex') {
                    $q->where('products.gender', 'unisex');
                } elseif ($gender !== null) {
                    $q->where(function (Builder $sub) use ($gender) {
                        $sub->where('products.gender', $gender)->orWhere('products.gender', 'unisex');
                    });
                }
            })
            ->when(filled($filters['brand'] ?? null), fn (Builder $q) => $q->where('products.brand', $filters['brand']))
            ->when($filters['sport_id'] ?? null, fn (Builder $q, $id) => $q->where('sport_id', $id))
            ->when($filters['category_id'] ?? null, fn (Builder $q, $id) => $q->where('category_id', $id))
            ->when($filters['offer_mode'] ?? null, function (Builder $q, string $mode) {
                $q->whereIn('offer_mode', OfferMode::matching($mode));
            });

        $hasVariantFilter = ($filters['size'] ?? null)
            || ($filters['color'] ?? null)
            || ($filters['min_price'] ?? null)
            || ($filters['max_price'] ?? null)
            || ! empty($filters['in_stock']);

        if (! $hasVariantFilter) {
            return;
        }

        $query->whereHas('variants', function (Builder $variants) use ($filters) {
            $variants->where('is_active', true)
                ->when($filters['size'] ?? null, fn (Builder $q, $size) => $q->where('size', $size))
                ->when($filters['color'] ?? null, fn (Builder $q, $color) => $q->where('color', $color))
                ->when(! empty($filters['in_stock']), fn (Builder $q) => $this->whereInStock($q, $filters['offer_mode'] ?? null));

            $min = $filters['min_price'] ?? null;
            $max = $filters['max_price'] ?? null;
            $mode = $filters['offer_mode'] ?? null;

            if ($min === null && $max === null) {
                return;
            }

            $variants->where(function (Builder $price) use ($min, $max, $mode) {
                $sale = in_array($mode, [null, '', OfferMode::Sale->value, OfferMode::Both->value], true);
                $rental = in_array($mode, [null, '', OfferMode::Rental->value, OfferMode::Both->value], true);

                if ($sale) {
                    $price->orWhere(function (Builder $q) use ($min, $max) {
                        $q->whereNotNull('sale_price')
                            ->when($min, fn (Builder $inner) => $inner->where('sale_price', '>=', $min))
                            ->when($max, fn (Builder $inner) => $inner->where('sale_price', '<=', $max));
                    });
                }

                if ($rental) {
                    $price->orWhere(function (Builder $q) use ($min, $max) {
                        $q->whereNotNull('rental_price_per_day')
                            ->when($min, fn (Builder $inner) => $inner->where('rental_price_per_day', '>=', $min))
                            ->when($max, fn (Builder $inner) => $inner->where('rental_price_per_day', '<=', $max));
                    });
                }
            });
        });
    }

    private function applySort(Builder $query, array $filters): void
    {
        $sort = $filters['sort'] ?? 'name_asc';
        if (! in_array($sort, ['price_asc', 'price_desc'], true)) {
            $query->orderBy($sort === 'newest' ? 'products.created_at' : 'products.name', $sort === 'newest' ? 'desc' : 'asc');

            return;
        }

        $column = ($filters['offer_mode'] ?? null) === OfferMode::Rental->value
            ? 'rental_price_per_day'
            : 'sale_price';
        $query->withMin(['variants as sort_price' => fn ($variants) => $variants->where('is_active', true)->whereNotNull($column)], $column)
            ->orderByRaw('sort_price IS NULL')
            ->orderBy('sort_price', $sort === 'price_asc' ? 'asc' : 'desc')
            ->orderBy('products.name');
    }

    private function whereInStock(Builder $variants, ?string $mode): void
    {
        $sale = in_array($mode, [null, '', OfferMode::Sale->value, OfferMode::Both->value], true);
        $rental = in_array($mode, [null, '', OfferMode::Rental->value, OfferMode::Both->value], true);
        $variants->where(function (Builder $stock) use ($sale, $rental) {
            if ($sale) {
                $stock->orWhereHas('stock', fn (Builder $row) => $row->whereRaw('(quantity_on_hand - quantity_reserved) > 0'));
            }
            if ($rental) {
                $stock->orWhereHas('items', fn (Builder $items) => $items->where('status', 'available'));
            }
        });
    }

    private function published(Builder $query): Builder
    {
        return $query->where('is_active', true)->where('is_suspended', false)->where('is_print_blank', false);
    }
}
