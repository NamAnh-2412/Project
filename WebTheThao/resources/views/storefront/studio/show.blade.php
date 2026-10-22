@extends('storefront.layouts.app')

@push('styles')
    <link rel="stylesheet" href="{{ asset('css/storefront/custom-studio.css') }}?v={{ filemtime(public_path('css/storefront/custom-studio.css')) }}">
@endpush

@section('title', 'In 3D')

@section('content')
    @php
        $initialPayload = $design?->design_payload ?? [
            'model' => 'football-jersey-realistic',
            'product_id' => $selectedGarment['id'] ?? null,
            'color' => $selectedGarment['colors'][0]['key'] ?? 'navy',
            'layers' => [],
        ];
        $activeColor = $initialPayload['color'] ?? ($selectedGarment['colors'][0]['key'] ?? '');
        $swatches = $selectedGarment['colors'] ?? [];
    @endphp
    <div
        class="custom-studio"
        data-custom-studio
        data-model-url="{{ $modelUrl }}"
        data-design-id="{{ $design?->id ?? '' }}"
        data-initial-design='@json($initialPayload)'
        data-garments='@json($garments)'
        data-save-url="{{ route('studio.save') }}"
        data-submit-url="{{ route('studio.submit', ['jerseyDesign' => '__ID__']) }}"
        data-decision-url="{{ route('studio.decide', ['jerseyDesign' => '__ID__']) }}"
    >
        <section class="custom-studio__intro" aria-labelledby="studio-title">
            <div>
                <p class="custom-studio__eyebrow">WEBTHETHAO · IN 3D</p>
                <h1 id="studio-title">Thiết kế áo theo dấu ấn của bạn</h1>
                <p class="custom-studio__lead">Tạo bản phối trên mô hình áo 3D, lưu bản nháp và gửi trực tiếp cho cửa hàng báo giá.</p>
            </div>
            <span class="custom-studio__status" data-studio-status role="status" aria-live="polite">Đang chuẩn bị viewer…</span>
        </section>

        <section class="custom-studio__workspace" aria-label="Studio in 3D">
            <div class="studio-viewer-card">
                <div class="studio-viewer-card__topbar">
                    <span data-garment-label>{{ $selectedGarment['name'] ?? 'Chưa có mẫu áo' }}</span>
                    <span class="studio-viewer-card__model">GLB · UV + PRINT SURFACE</span>
                </div>
                <div class="studio-viewer" data-studio-viewer>
                    <canvas data-studio-canvas aria-label="Mô hình áo 3D"></canvas>
                    <div class="studio-viewer__loading" data-studio-loading>
                        <span class="studio-spinner" aria-hidden="true"></span>
                        <span>Đang tải mô hình 3D…</span>
                    </div>
                    <div class="studio-viewer__fallback" data-studio-fallback hidden>Thiết bị hoặc trình duyệt này chưa hỗ trợ WebGL, hoặc mô hình chưa tải được.</div>
                    <div class="studio-viewer__hint">Kéo để xoay · Shift + kéo để di chuyển lớp đang chọn</div>
                </div>
                <div class="studio-viewer__controls" aria-label="Điều khiển góc nhìn">
                    <button type="button" class="studio-control" data-camera="front">Mặt trước</button>
                    <button type="button" class="studio-control" data-camera="back">Mặt sau</button>
                    <button type="button" class="studio-control" data-camera="left_sleeve">Tay trái</button>
                    <button type="button" class="studio-control" data-camera="right_sleeve">Tay phải</button>
                    <button type="button" class="studio-control" data-camera="reset">Đặt lại góc</button>
                    <button type="button" class="studio-control" data-zoom="out" aria-label="Thu nhỏ">−</button>
                    <button type="button" class="studio-control" data-zoom="in" aria-label="Phóng to">+</button>
                </div>
            </div>

            <aside class="studio-toolbar" aria-label="Công cụ thiết kế">
                <div class="studio-toolbar__heading">
                    <div>
                        <p class="custom-studio__eyebrow">DESIGN TOOLBAR</p>
                        <h2>Tạo bản phối</h2>
                    </div>
                    <span class="studio-phase-badge">LIVE</span>
                </div>

                <section class="studio-panel">
                    <div class="studio-panel__title"><span>01</span><h3>Mẫu & màu áo</h3></div>
                    <label class="studio-field">Sản phẩm
                        <select data-garment-select @disabled($garments === [])>
                            @if ($garments === [])
                                <option>Chưa có áo trong cửa hàng</option>
                            @else
                                @foreach (['white' => 'Áo trắng', 'blank' => 'Phôi in', 'catalog' => 'Sản phẩm có sẵn'] as $group => $groupLabel)
                                    @php $items = collect($garments)->where('group', $group); @endphp
                                    @if ($items->isNotEmpty())
                                        <optgroup label="{{ $groupLabel }}">
                                            @foreach ($items as $garment)
                                                <option value="{{ $garment['id'] }}" @selected(($selectedGarment['id'] ?? null) === $garment['id'])>
                                                    {{ $garment['name'] }}{{ $garment['sport'] ? ' · '.$garment['sport'] : '' }}
                                                </option>
                                            @endforeach
                                        </optgroup>
                                    @endif
                                @endforeach
                            @endif
                        </select>
                    </label>
                    <p class="studio-editor-note" data-blank-note @if (($selectedGarment['group'] ?? '') !== 'white') hidden @endif>In trên áo trắng trơn. Tên, số và logo là phần in thêm.</p>
                    <div class="studio-swatches" data-color-swatches role="group" aria-label="Chọn màu áo">
                        @foreach ($swatches as $color)
                            <button type="button" class="studio-swatch {{ $activeColor === $color['key'] ? 'is-active' : '' }}" data-studio-color="{{ $color['key'] }}" aria-label="{{ $color['label'] }}"><span style="background: {{ $color['hex'] }}"></span>{{ $color['label'] }}</button>
                        @endforeach
                    </div>
                </section>

                <section class="studio-panel studio-panel--zones">
                    <div class="studio-panel__title"><span>02</span><h3>Chọn mặt thiết kế</h3></div>
                    <div class="studio-zone-switcher" role="group" aria-label="Chọn vùng in">
                        <button type="button" class="studio-control is-active" data-print-zone="front">Mặt trước</button>
                        <button type="button" class="studio-control" data-print-zone="back">Mặt sau</button>
                        <button type="button" class="studio-control" data-print-zone="left_sleeve">Tay trái</button>
                        <button type="button" class="studio-control" data-print-zone="right_sleeve">Tay phải</button>
                    </div>
                    <p class="studio-editor-note" data-active-zone-label>Đang thiết kế mặt trước.</p>
                </section>

                <section class="studio-panel">
                    <div class="studio-panel__title"><span>03</span><h3>Thêm nội dung</h3></div>
                    <div class="studio-input-row">
                        <label>Tên<input data-design-name type="text" maxlength="16" placeholder="Ví dụ: DANG"></label>
                        <button type="button" class="studio-small-action" data-add-name>Thêm tên</button>
                    </div>
                    <div class="studio-input-row">
                        <label>Số<input data-design-number type="text" inputmode="numeric" maxlength="2" placeholder="10"></label>
                        <button type="button" class="studio-small-action" data-add-number>Thêm số</button>
                    </div>
                    <label class="studio-upload">
                        <input data-logo-input type="file" accept="image/png,image/jpeg,image/webp">
                        <span>Thêm logo PNG/JPG/WebP</span>
                        <small>Tối đa 2MB · lưu cùng bản nháp</small>
                    </label>
                    <div class="studio-logo-zoom" data-logo-zoom>
                        <span data-logo-zoom-label>Chưa có logo</span>
                        <button type="button" class="studio-small-action" data-logo-zoom="out" disabled>Thu nhỏ</button>
                        <button type="button" class="studio-small-action" data-logo-zoom="in" disabled>Phóng to</button>
                    </div>
                </section>

                <section class="studio-panel studio-panel--request">
                    <div class="studio-panel__title"><span>04</span><h3>Thông tin yêu cầu</h3></div>
                    <div class="studio-input-row">
                        <label>Size
                            <select data-design-size @disabled($swatches === [])>
                                @foreach (collect($swatches)->firstWhere('key', $activeColor)['sizes'] ?? [] as $size)
                                    <option value="{{ $size['variant_id'] }}" data-size="{{ $size['size'] }}" @selected((int) ($initialPayload['variant_id'] ?? 0) === (int) $size['variant_id'])>{{ $size['label'] }} · còn {{ $size['available'] }}</option>
                                @endforeach
                            </select>
                        </label>
                        <label>Số lượng<input data-design-quantity type="number" min="1" max="100" value="{{ $design?->quantity ?: 1 }}"></label>
                    </div>
                    <label class="studio-field">Ghi chú
                        <textarea data-design-notes rows="2" maxlength="1000" placeholder="Mục đích, hạn hoặc yêu cầu thêm…">{{ $design?->notes }}</textarea>
                    </label>
                </section>

                <div class="studio-actions">
                    <button type="button" class="studio-action" data-reset-design>Xóa bản thiết kế <span>Đặt lại texture</span></button>
                    <button type="button" class="studio-action studio-action--primary" data-save-design>Lưu bản nháp <span>Đăng nhập để lưu</span></button>
                    <button type="button" class="studio-action" data-submit-design>Gửi thiết kế cho cửa hàng <span>Báo giá trong hội thoại</span></button>
                    <p class="studio-feedback" data-studio-feedback role="status"></p>
                </div>
            </aside>
        </section>

        @auth
            @if (auth()->user()->isCustomer())
                <section class="studio-my-designs">
                    <div class="studio-toolbar__heading">
                        <div>
                            <p class="custom-studio__eyebrow">MY DESIGNS</p>
                            <h2>Thiết kế của tôi</h2>
                        </div>
                        <a href="{{ route('models.create') }}">Bản mới</a>
                    </div>
                    @forelse ($myDesigns as $saved)
                        <a class="studio-design-card" href="{{ route('models.create', ['design' => $saved->id]) }}">
                            <strong>#{{ $saved->id }} · {{ $saved->statusLabel() }}</strong>
                            <span>{{ $saved->size ?: 'Chưa chọn size' }} · {{ $saved->quantity ?: '—' }} áo</span>
                            @if ($saved->quote_amount)
                                <b>{{ number_format($saved->quote_amount, 0, ',', '.') }}đ</b>
                            @endif
                        </a>
                    @empty
                        <p class="studio-empty">Chưa có thiết kế nào được lưu.</p>
                    @endforelse
                </section>
                @if ($design && $design->status === 'quoted')
                    <section class="studio-conversation">
                        <div class="studio-quote">
                            <strong>Báo giá: {{ number_format($design->quote_amount, 0, ',', '.') }}đ</strong>
                            <button type="button" data-decision="accepted">Chấp nhận báo giá</button>
                            <form method="post" action="{{ route('studio.discuss', $design) }}">
                                @csrf
                                <button class="studio-quote__talk" type="submit">Thảo luận thêm</button>
                            </form>
                            <button type="button" data-decision="declined">Từ chối</button>
                        </div>
                    </section>
                @endif
            @endif
        @endauth
    </div>
@endsection

@push('scripts')
    <script type="importmap">@json(['imports' => ['three' => asset('vendor/three/three.module.js'), 'three/addons/' => asset('vendor/three/addons').'/']])</script>
    <script type="module" src="{{ asset('js/storefront/custom-studio.js') }}?v={{ filemtime(public_path('js/storefront/custom-studio.js')) }}"></script>
@endpush
