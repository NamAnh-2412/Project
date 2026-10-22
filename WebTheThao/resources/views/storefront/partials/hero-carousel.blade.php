@php
    $saleUrl = route('catalog.index', ['offer_mode' => 'sale']);
    $rentUrl = route('catalog.index', ['offer_mode' => 'rental']);
    $brand = config('app.name');
    $heroSlides = $heroSlides ?? [];
    $slideCount = count($heroSlides);
@endphp
<section class="hero hero-carousel mb-4" data-hero-carousel data-hero-interval="5000" aria-roledescription="carousel" aria-label="Bộ sưu tập {{ $brand }}">
    <div class="hero-carousel-slides">
        @foreach ($heroSlides as $index => $slide)
            <article
                class="hero-slide @if ($index === 0) is-active @endif"
                data-hero-slide
                data-hero-tone="{{ $slide['tone'] }}"
                role="group"
                aria-roledescription="slide"
                aria-label="{{ $index + 1 }} / {{ $slideCount }}"
                @if ($index !== 0) aria-hidden="true" @endif
            >
                @if ($slide['image'])
                    <img class="hero-banner" src="{{ $slide['image'] }}" alt="{{ $slide['alt'] }}" width="2048" height="683" @if ($index === 0) fetchpriority="high" @else loading="lazy" @endif>
                @else
                    <div class="hero-placeholder" data-hero-image role="img" aria-label="{{ $slide['title'] }}"></div>
                @endif
                <div class="hero-scrim" aria-hidden="true"></div>
                <div class="hero-copy">
                    <span class="eyebrow">{{ $slide['eyebrow'] }} / {{ $brand }}</span>
                    <h1>{{ $slide['title'] }}</h1>
                    <p>{{ $slide['text'] }}</p>
                    <div class="hero-actions">
                        <a class="hero-cta hero-cta--buy" href="{{ $saleUrl }}">Mua sắm ngay <span aria-hidden="true">→</span></a>
                        <a class="hero-cta hero-cta--rent" href="{{ $rentUrl }}">Thuê đồ <span aria-hidden="true">↗</span></a>
                    </div>
                </div>
                <div class="hero-meta"><span>{{ $slide['eyebrow'] }}</span><span class="hero-meta-line" aria-hidden="true"></span><span>{{ $slide['meta'] }}</span></div>
            </article>
        @endforeach
    </div>
    <button type="button" class="hero-carousel-control hero-carousel-control--prev" data-hero-prev aria-label="Slide trước">←</button>
    <button type="button" class="hero-carousel-control hero-carousel-control--next" data-hero-next aria-label="Slide tiếp theo">→</button>
    <div class="hero-carousel-toolbar" aria-label="Điều khiển trình chiếu">
        <span class="hero-carousel-counter" data-hero-counter aria-live="polite">01 / {{ str_pad((string) $slideCount, 2, '0', STR_PAD_LEFT) }}</span>
        <button type="button" class="hero-carousel-toggle" data-hero-toggle aria-pressed="false" aria-label="Tạm dừng trình chiếu">
            <span data-hero-toggle-icon aria-hidden="true">Ⅱ</span>
            <span data-hero-toggle-label>Tạm dừng</span>
        </button>
        <div class="hero-carousel-dots" role="tablist" aria-label="Chọn slide">
            @foreach ($heroSlides as $index => $slide)
                <button type="button" class="hero-carousel-dot @if ($index === 0) is-active @endif" data-hero-dot="{{ $index }}" role="tab" aria-label="Xem slide {{ $index + 1 }}" aria-selected="{{ $index === 0 ? 'true' : 'false' }}"></button>
            @endforeach
        </div>
    </div>
</section>
