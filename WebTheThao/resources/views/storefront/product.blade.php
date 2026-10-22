@extends('storefront.layouts.app')
@include('partials.css', ['file' => 'css/storefront/product.css'])
@include('partials.js', ['file' => 'js/storefront/product.js'])

@section('title', $product->name)

@php
    $canSale = in_array($product->offer_mode->value, ['sale', 'both'], true);
    $canRent = in_array($product->offer_mode->value, ['rental', 'both'], true);
    $defaultMode = match (true) {
        ($requestedMode ?? null) === 'rental' && $canRent => 'rental',
        ($requestedMode ?? null) === 'sale' && $canSale => 'sale',
        $canSale => 'sale',
        default => 'rental',
    };
    $modeBadge = match ($product->offer_mode->value) {
        'sale' => 'Chỉ bán',
        'rental' => 'Chỉ thuê',
        default => 'Bán & thuê',
    };
    $genderLabel = match ($product->gender) {
        'nam' => 'Nam',
        'nu' => 'Nữ',
        'unisex' => 'Unisex',
        default => null,
    };
    $saleAvailable = function ($variant): int {
        $stock = $variant->stock;
        if ($stock === null) {
            return 0;
        }

        return max(0, (int) $stock->quantity_on_hand - (int) $stock->quantity_reserved);
    };
    $payload = [
        'offer_mode' => $product->offer_mode->value,
        'variants' => $product->variants->map(fn ($v) => [
            'id' => $v->id,
            'sku' => $v->sku,
            'size' => $v->size,
            'color' => $v->color,
            'sale_price' => $v->sale_price,
            'rental_price_per_day' => $v->rental_price_per_day,
            'rental_price_per_week' => $v->rental_price_per_week,
            'deposit_amount' => $v->deposit_amount,
            'sale_available' => $saleAvailable($v),
        ])->values(),
        'color_images' => $product->colorImages->map(fn ($image) => [
            'color_key' => $image->color_key,
            'src' => $image->imageSrc(),
        ])->filter(fn ($image) => filled($image['src']))->values(),
    ];
@endphp

@section('content')
<div class="product-page">
    <nav class="small mb-3">
        <a href="{{ route('catalog.index') }}">Sản phẩm</a>
        @if ($product->category)
            / {{ $product->category->name }}
        @endif
    </nav>

    <div class="row g-4 product-page">
        <div class="col-lg-5">
            <div class="card card-product gallery-card">
                <div class="thumb">
                    @if ($product->imageSrc() || $product->colorImages->isNotEmpty())
                        <img data-product-image src="{{ $product->colorImages->first()?->imageSrc() ?: $product->imageSrc() }}" alt="{{ $product->name }}">
                    @endif
                    <span>{{ $product->sport->name ?? 'Thể thao' }}</span>
                </div>
                @if ($product->colorImages->isNotEmpty())
                    <div class="product-thumbs" aria-label="Ảnh theo màu">
                        @foreach ($product->colorImages as $image)
                            @if ($image->imageSrc())
                                <button type="button" class="product-thumb {{ $loop->first ? 'is-active' : '' }}" data-gallery-image="{{ $image->imageSrc() }}" data-gallery-color="{{ $image->color_key }}" aria-label="Ảnh màu {{ $image->color_name }}">
                                    <img src="{{ $image->imageSrc() }}" alt="">
                                </button>
                            @endif
                        @endforeach
                    </div>
                @endif
            </div>
        </div>
        <div class="col-lg-7">
            <h1 class="h3">{{ $product->name }}</h1>
            <p class="mb-2 product-badges">
                <span class="badge-offer">{{ $modeBadge }}</span>
                @if ($product->brand)
                    <span class="badge-offer">{{ $product->brand }}</span>
                @endif
                @if ($genderLabel)
                    <span class="badge-offer">{{ $genderLabel }}</span>
                @endif
            </p>
            @if (($reviews ?? collect())->isNotEmpty())
                <p class="small mb-1">Đánh giá: <strong>{{ $reviewAvg }}</strong>/5 ({{ $reviews->count() }} lượt)</p>
            @endif
            <p class="text-secondary">{{ $product->description }}</p>
            <dl class="product-specs">
                @if ($product->brand)<div><dt>Thương hiệu</dt><dd>{{ $product->brand }}</dd></div>@endif
                @if ($genderLabel)<div><dt>Giới tính</dt><dd>{{ $genderLabel }}</dd></div>@endif
                @if ($product->sport)<div><dt>Môn</dt><dd>{{ $product->sport->name }}</dd></div>@endif
                @if ($product->category)<div><dt>Loại</dt><dd>{{ $product->category->name }}</dd></div>@endif
            </dl>

            <div class="mode-toggle btn-group mb-3" role="group" aria-label="Chọn mua hoặc thuê">
                @if ($canSale)
                    <button type="button" class="btn btn-outline-success {{ $defaultMode === 'sale' ? 'active' : '' }}" data-mode="sale" id="btn-mode-sale">Mua</button>
                @endif
                @if ($canRent)
                    <button type="button" class="btn btn-outline-success {{ $defaultMode === 'rental' ? 'active' : '' }}" data-mode="rental" id="btn-mode-rental">Thuê</button>
                @endif
            </div>

            @php
                $variantColors = $product->variants->pluck('color')->filter(fn ($v) => filled($v))->unique()->values();
                $variantSizes = $product->variants->pluck('size')->filter(fn ($v) => filled($v))->unique()->values();
                $hasColorSize = $variantColors->isNotEmpty() || $variantSizes->isNotEmpty();
            @endphp

            <div class="variant-picker mb-3" id="variant-picker" data-has-attrs="{{ $hasColorSize ? '1' : '0' }}">
                @if ($variantColors->isNotEmpty())
                    <div class="variant-group" data-variant-group="color">
                        <div class="variant-group-label">
                            Màu
                            <span class="variant-group-value" data-variant-label="color">—</span>
                        </div>
                        <div class="variant-chips" role="listbox" aria-label="Chọn màu">
                            @foreach ($variantColors as $color)
                                @php $colorStock = $product->variants->where('color', $color)->sum($saleAvailable); @endphp
                                <button type="button" class="variant-chip" role="option" data-attr="color" data-value="{{ $color }}" data-color-key="{{ \Illuminate\Support\Str::slug($color) }}" aria-pressed="false">
                                    {{ $color }}@if ($canSale)<span class="variant-chip-stock">{{ $colorStock > 0 ? 'còn '.$colorStock : 'hết bán' }}</span>@endif
                                </button>
                            @endforeach
                        </div>
                    </div>
                @endif

                @if ($variantSizes->isNotEmpty())
                    <div class="variant-group" data-variant-group="size">
                        <div class="variant-group-label">
                            Size
                            <span class="variant-group-value" data-variant-label="size">—</span>
                        </div>
                        <div class="variant-chips" role="listbox" aria-label="Chọn size">
                            @foreach ($variantSizes as $size)
                                <button type="button" class="variant-chip" role="option" data-attr="size" data-value="{{ $size }}" aria-pressed="false">
                                    {{ $size }}
                                </button>
                            @endforeach
                        </div>
                    </div>
                @endif

                @unless ($hasColorSize)
                    <div class="variant-group" data-variant-group="sku">
                        <div class="variant-group-label">
                            Phân loại
                            <span class="variant-group-value" data-variant-label="sku">—</span>
                        </div>
                        <div class="variant-chips" role="listbox" aria-label="Chọn phân loại">
                            @foreach ($product->variants as $variant)
                                <button
                                    type="button"
                                    class="variant-chip"
                                    role="option"
                                    data-attr="sku"
                                    data-value="{{ $variant->id }}"
                                    data-variant-id="{{ $variant->id }}"
                                    aria-pressed="false"
                                >
                                    {{ $variant->sku }}
                                </button>
                            @endforeach
                        </div>
                    </div>
                @endunless

                <p class="variant-sku small text-secondary mb-0" id="variant-sku-label"></p>
            </div>

            <div id="panel-sale" class="{{ $defaultMode === 'sale' ? '' : 'd-none' }}">
                <div class="price fs-4 mb-1" id="sale-price">—</div>
                <div class="small mb-3" id="sale-availability">Đang kiểm tra tồn kho bán…</div>
            </div>

            <div id="panel-rental" class="{{ $defaultMode === 'rental' ? '' : 'd-none' }}">
                <div class="price fs-4 mb-1" id="rent-price">—</div>
                <div class="small mb-2" id="rent-deposit"></div>
                <div class="row g-2 mb-2">
                    <div class="col-md-6">
                        <label class="form-label" for="start-date">Ngày nhận</label>
                        <input class="form-control" type="date" id="start-date">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="end-date">Ngày trả</label>
                        <input class="form-control" type="date" id="end-date">
                    </div>
                </div>
                <div class="small mb-3" id="rental-availability">Chọn ngày để xem món thuê còn trống.</div>
                <div class="small fw-semibold mb-3" id="rental-quote"></div>
            </div>

            @if (auth()->user()?->isStoreAccount())
                <div class="alert alert-warning mb-2">Tài khoản cửa hàng không mua trên website. Xử lý đơn ở khu Quản trị.</div>
                <a class="btn btn-success" href="{{ route('admin.dashboard') }}">Mở Quản trị</a>
            @else
                <form method="post" action="{{ route('cart.add') }}" id="add-cart-form">
                    @csrf
                    <input type="hidden" name="line_type" id="cart-line-type" value="{{ $defaultMode }}">
                    <input type="hidden" name="product_variant_id" id="cart-variant-id" value="{{ $product->variants->first()?->id }}">
                    <input type="hidden" name="rental_start" id="cart-start">
                    <input type="hidden" name="rental_end" id="cart-end">
                    <div class="product-actions" id="qty-wrap">
                        <label class="form-label" for="cart-qty" id="cart-qty-label">{{ $defaultMode === 'sale' ? 'Số lượng mua' : 'Số lượng thuê' }}</label>
                        <div class="product-actions-row">
                            <input class="form-control product-qty" type="number" name="quantity" id="cart-qty" value="1" min="1">
                            <button class="btn btn-success" type="submit" id="add-cart-btn">{{ $defaultMode === 'sale' ? 'Thêm mua' : 'Thêm thuê' }}</button>
                        </div>
                        <div class="form-text" id="cart-qty-hint">{{ $defaultMode === 'rental' ? 'Mỗi món là một lịch thuê riêng.' : '' }}</div>
                    </div>
                </form>
                <p class="small text-secondary mt-2 mb-0">Có thể mua và thuê cùng một giỏ. Thuê nhiều món: chọn số lượng — hệ thống gán từng món trống.</p>
            @endif
        </div>
    </div>

    @if ($product->sizeGuide && $product->sizeGuide->is_active && $product->sizeGuide->measurements)
        <div class="policy-card mt-4" id="size-guide">
            <h2 class="h5">Bảng size · {{ $product->sizeGuide->name }}</h2>
            <div class="table-responsive">
                <table class="table">
                    <thead><tr><th>Size</th><th>Chiều cao</th><th>Cân nặng</th></tr></thead>
                    <tbody>
                    @foreach ($product->sizeGuide->measurements as $size => $range)
                        <tr>
                            <td>{{ $size }}</td>
                            <td>{{ $range['min_height_cm'] ?? '—' }}–{{ $range['max_height_cm'] ?? '—' }} cm</td>
                            <td>{{ $range['min_weight_kg'] ?? '—' }}–{{ $range['max_weight_kg'] ?? '—' }} kg</td>
                        </tr>
                    @endforeach
                    </tbody>
                </table>
            </div>
            <form class="row g-2 align-items-end" id="size-finder-form">
                <div class="col-6 col-md-3"><label class="form-label" for="height-cm">Chiều cao (cm)</label><input class="form-control" id="height-cm" name="height_cm" type="number" min="50" max="250" step="0.1" required></div>
                <div class="col-6 col-md-3"><label class="form-label" for="weight-kg">Cân nặng (kg)</label><input class="form-control" id="weight-kg" name="weight_kg" type="number" min="10" max="250" step="0.1" required></div>
                <div class="col-md-3"><button class="btn btn-outline-success" type="submit">Gợi ý size</button></div>
                <p class="col-12 small mb-0" id="size-finder-result" role="status"></p>
            </form>
        </div>
    @endif

    <div class="policy-card mt-4">
        <h2 class="h5">Đánh giá</h2>
        @forelse ($reviews ?? [] as $review)
            <div class="border-bottom py-3">
                <div class="fw-semibold">{{ $review->user->name ?? 'Khách' }} · {{ $review->rating }}/5
                    <span class="badge text-bg-light">{{ $review->kind->value === 'sale' ? 'Mua' : 'Thuê' }}</span>
                </div>
                <p class="mb-0">{{ $review->comment }}</p>
            </div>
        @empty
            <p class="text-secondary mb-0">Chưa có đánh giá. Mua hoặc thuê rồi thanh toán để viết nhận xét.</p>
        @endforelse
    </div>
    <script type="application/json" id="product-page-config">{!! json_encode([
        'apiBase' => url('/'),
        'product' => $payload,
        'canSale' => $canSale,
        'canRent' => $canRent,
        'defaultMode' => $defaultMode,
        'sizeUrl' => route('catalog.size', $product->slug),
    ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_HEX_TAG) !!}</script>
</div>
@endsection
