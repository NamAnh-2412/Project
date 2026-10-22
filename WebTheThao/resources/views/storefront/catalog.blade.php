@extends('storefront.layouts.app')
@include('partials.css', ['file' => 'css/storefront/catalog.css'])

@php
    $heading = match ($filters['offer_mode'] ?? '') {
        'sale' => 'Mua',
        'rental' => 'Thuê',
        default => 'Sản phẩm',
    };
    $subtitle = match ($filters['offer_mode'] ?? '') {
        'sale' => 'Đồ thể thao đang bán. Lọc theo giới tính, size, màu, thương hiệu và giá.',
        'rental' => 'Đồ thuê theo ngày cho buổi tập và trận đấu.',
        default => 'Khám phá đồ thể thao của cửa hàng. Lọc theo môn, size, màu và giá.',
    };
    $activeGender = ($filters['gender'] ?? '') === 'all' ? '' : ($filters['gender'] ?? '');
    $resetQuery = array_filter(['offer_mode' => $filters['offer_mode'] ?? null]);
    $activeFilterTags = [];
    if (! empty($filters['q'])) {
        $activeFilterTags[] = ['label' => 'Từ khóa: '.$filters['q'], 'url' => request()->fullUrlWithQuery(['q' => null, 'page' => null])];
    }
    if ($activeGender !== '') {
        $genderLabel = match ($activeGender) { 'nam' => 'Nam', 'nu' => 'Nữ', 'unisex' => 'Unisex', default => $activeGender };
        $activeFilterTags[] = ['label' => 'Giới tính: '.$genderLabel, 'url' => request()->fullUrlWithQuery(['gender' => null, 'page' => null])];
    }
    if (! empty($filters['sport_id'])) {
        $sport = $sports->firstWhere('id', (int) $filters['sport_id']);
        if ($sport) {
            $activeFilterTags[] = ['label' => 'Môn: '.$sport->name, 'url' => request()->fullUrlWithQuery(['sport_id' => null, 'page' => null])];
        }
    }
    if (! empty($filters['category_id'])) {
        $category = $categories->firstWhere('id', (int) $filters['category_id']);
        if ($category) {
            $activeFilterTags[] = ['label' => 'Danh mục: '.$category->name, 'url' => request()->fullUrlWithQuery(['category_id' => null, 'page' => null])];
        }
    }
    if (! empty($filters['brand'])) {
        $activeFilterTags[] = ['label' => 'Thương hiệu: '.$filters['brand'], 'url' => request()->fullUrlWithQuery(['brand' => null, 'page' => null])];
    }
    if (! empty($filters['size'])) {
        $activeFilterTags[] = ['label' => 'Size: '.$filters['size'], 'url' => request()->fullUrlWithQuery(['size' => null, 'page' => null])];
    }
    if (! empty($filters['color'])) {
        $activeFilterTags[] = ['label' => 'Màu: '.$filters['color'], 'url' => request()->fullUrlWithQuery(['color' => null, 'page' => null])];
    }
    if (! empty($filters['in_stock'])) {
        $activeFilterTags[] = ['label' => 'Chỉ còn hàng', 'url' => request()->fullUrlWithQuery(['in_stock' => null, 'page' => null])];
    }
    if (! empty($filters['min_price']) || ! empty($filters['max_price'])) {
        $minText = isset($filters['min_price']) && is_numeric($filters['min_price']) ? number_format((float) $filters['min_price'], 0, ',', '.').'đ' : '0đ';
        $maxText = isset($filters['max_price']) && is_numeric($filters['max_price']) ? number_format((float) $filters['max_price'], 0, ',', '.').'đ' : 'Tối đa';
        $activeFilterTags[] = ['label' => 'Giá: '.$minText.' – '.$maxText, 'url' => request()->fullUrlWithQuery(['min_price' => null, 'max_price' => null, 'page' => null])];
    }
@endphp

@section('title', $heading)

@section('content')
    <div class="catalog-heading">
        <div>
            <h1 class="h3 mb-1">{{ $heading }}</h1>
            <p class="catalog-subtitle mb-0">{{ $subtitle }}</p>
        </div>
        <div class="catalog-count">{{ $products->total() }} sản phẩm</div>
    </div>

    <section class="catalog-gender-bar" aria-label="Bộ lọc giới tính">
        <span class="catalog-gender-label">Giới tính</span>
        <div class="catalog-gender-pills" role="radiogroup" aria-label="Chọn giới tính">
            <a href="{{ request()->fullUrlWithQuery(['gender' => null, 'page' => null]) }}" class="gender-pill {{ $activeGender === '' ? 'active' : '' }}" role="radio" aria-checked="{{ $activeGender === '' ? 'true' : 'false' }}">Tất cả</a>
            <a href="{{ request()->fullUrlWithQuery(['gender' => 'nam', 'page' => null]) }}" class="gender-pill {{ $activeGender === 'nam' ? 'active' : '' }}" role="radio" aria-checked="{{ $activeGender === 'nam' ? 'true' : 'false' }}">Nam</a>
            <a href="{{ request()->fullUrlWithQuery(['gender' => 'nu', 'page' => null]) }}" class="gender-pill {{ $activeGender === 'nu' ? 'active' : '' }}" role="radio" aria-checked="{{ $activeGender === 'nu' ? 'true' : 'false' }}">Nữ</a>
            <a href="{{ request()->fullUrlWithQuery(['gender' => 'unisex', 'page' => null]) }}" class="gender-pill {{ $activeGender === 'unisex' ? 'active' : '' }}" role="radio" aria-checked="{{ $activeGender === 'unisex' ? 'true' : 'false' }}">Unisex</a>
        </div>
    </section>

    @if ($activeFilterTags !== [])
        <div class="active-filters-container" aria-label="Bộ lọc đang áp dụng">
            <span class="active-filters-label">Đang lọc</span>
            @foreach ($activeFilterTags as $tag)
                <a href="{{ $tag['url'] }}" class="active-filter-badge">{{ $tag['label'] }} <span aria-hidden="true">×</span></a>
            @endforeach
            <a class="active-filters-reset-btn" href="{{ route('catalog.index', $resetQuery) }}">Xóa tất cả</a>
        </div>
    @endif

    <details class="catalog-filter-drawer" open>
        <summary class="catalog-filter-summary">
            <span><span class="catalog-filter-kicker">FILTERS</span><strong>Bộ lọc sản phẩm</strong></span>
            <span aria-hidden="true">⌄</span>
        </summary>
        <form class="filter-bar row g-3" method="get" action="{{ route('catalog.index') }}" onsubmit="this.querySelectorAll('input, select').forEach((field) => { if (!field.value || (field.name === 'sort' && field.value === 'name_asc')) field.disabled = true })">
            @if (! empty($filters['offer_mode']))
                <input type="hidden" name="offer_mode" value="{{ $filters['offer_mode'] }}">
            @endif
            <div class="col-12 col-md-4 col-lg-3">
                <label class="filter-label" for="catalog-q">Tìm kiếm</label>
                <input id="catalog-q" class="form-control" name="q" value="{{ $filters['q'] ?? '' }}" placeholder="Tên sản phẩm, từ khóa">
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <label class="filter-label" for="catalog-gender">Giới tính</label>
                <select id="catalog-gender" class="form-select" name="gender">
                    <option value="">Tất cả giới tính</option>
                    <option value="nam" @selected($activeGender === 'nam')>Nam</option>
                    <option value="nu" @selected($activeGender === 'nu')>Nữ</option>
                    <option value="unisex" @selected($activeGender === 'unisex')>Unisex</option>
                </select>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <label class="filter-label" for="catalog-sport">Môn thể thao</label>
                <select id="catalog-sport" class="form-select" name="sport_id">
                    <option value="">Tất cả môn</option>
                    @foreach ($sports as $sport)
                        <option value="{{ $sport->id }}" @selected((string) ($filters['sport_id'] ?? '') === (string) $sport->id)>{{ $sport->name }}</option>
                    @endforeach
                </select>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <label class="filter-label" for="catalog-category">Loại sản phẩm</label>
                <select id="catalog-category" class="form-select" name="category_id">
                    <option value="">Tất cả danh mục</option>
                    @foreach ($categories as $category)
                        <option value="{{ $category->id }}" @selected((string) ($filters['category_id'] ?? '') === (string) $category->id)>{{ $category->name }}</option>
                    @endforeach
                </select>
            </div>
            <div class="col-6 col-md-4 col-lg-3">
                <label class="filter-label" for="catalog-brand">Thương hiệu</label>
                <select id="catalog-brand" class="form-select" name="brand">
                    <option value="">Tất cả thương hiệu</option>
                    @foreach ($brands as $brand)
                        <option value="{{ $brand }}" @selected(($filters['brand'] ?? '') === $brand)>{{ $brand }}</option>
                    @endforeach
                </select>
            </div>
            <div class="col-6 col-md-3 col-lg-2">
                <label class="filter-label" for="catalog-size">Kích cỡ</label>
                <select id="catalog-size" class="form-select" name="size">
                    <option value="">Tất cả size</option>
                    @foreach ($availableSizes as $size)
                        <option value="{{ $size }}" @selected(($filters['size'] ?? '') === $size)>{{ $size }}</option>
                    @endforeach
                </select>
            </div>
            <div class="col-6 col-md-3 col-lg-2">
                <label class="filter-label" for="catalog-color">Màu sắc</label>
                <select id="catalog-color" class="form-select" name="color">
                    <option value="">Tất cả màu</option>
                    @foreach ($availableColors as $color)
                        <option value="{{ $color }}" @selected(($filters['color'] ?? '') === $color)>{{ $color }}</option>
                    @endforeach
                </select>
            </div>
            <div class="col-6 col-md-3 col-lg-2">
                <label class="filter-label" for="catalog-min-price">Giá từ</label>
                <input id="catalog-min-price" class="form-control" name="min_price" value="{{ $filters['min_price'] ?? '' }}" inputmode="decimal" placeholder="0 đ">
            </div>
            <div class="col-6 col-md-3 col-lg-2">
                <label class="filter-label" for="catalog-max-price">Giá đến</label>
                <input id="catalog-max-price" class="form-control" name="max_price" value="{{ $filters['max_price'] ?? '' }}" inputmode="decimal" placeholder="Tối đa đ">
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <label class="filter-label" for="catalog-sort">Sắp xếp</label>
                <select id="catalog-sort" class="form-select" name="sort">
                    <option value="name_asc" @selected(($filters['sort'] ?? 'name_asc') === 'name_asc')>Tên A-Z</option>
                    <option value="newest" @selected(($filters['sort'] ?? '') === 'newest')>Mới nhất</option>
                    <option value="price_asc" @selected(($filters['sort'] ?? '') === 'price_asc')>Giá thấp đến cao</option>
                    <option value="price_desc" @selected(($filters['sort'] ?? '') === 'price_desc')>Giá cao đến thấp</option>
                </select>
            </div>
            <div class="filter-actions">
                <div class="form-check form-switch mb-0">
                    <input class="form-check-input" type="checkbox" id="catalog-instock" name="in_stock" value="1" @checked(! empty($filters['in_stock']))>
                    <label class="form-check-label" for="catalog-instock">Chỉ còn hàng</label>
                </div>
                <div class="filter-action-buttons">
                    <a class="btn btn-light-premium" href="{{ route('catalog.index', $resetQuery) }}">Xóa lọc</a>
                    <button class="btn btn-catalog-apply" type="submit">Hiển thị sản phẩm</button>
                </div>
            </div>
        </form>
    </details>

    <div class="vp-grid">
        @forelse ($products as $index => $product)
            <div class="reveal" style="--delay: {{ min($index, 11) * 55 }}ms">
                @include('storefront.partials.product-card', ['offerFocus' => $filters['offer_mode'] ?? null])
            </div>
        @empty
            <div class="catalog-empty">
                <h2 class="h5">Không có sản phẩm khớp bộ lọc</h2>
                <p class="text-secondary">Thử bỏ bớt điều kiện hoặc xem lại toàn bộ sản phẩm.</p>
                <a class="btn btn-catalog-apply" href="{{ route('catalog.index', $resetQuery) }}">Xóa bộ lọc</a>
            </div>
        @endforelse
    </div>

    <div class="mt-4">{{ $products->withQueryString()->links('pagination::bootstrap-5') }}</div>
@endsection
