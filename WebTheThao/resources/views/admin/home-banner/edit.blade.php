@extends('layouts.admin')
@section('title', 'Banner trang chủ')
@section('content')
    <h1 class="page-title">Banner trang chủ</h1>
    <p class="page-subtitle mb-4">Mỗi slide có ảnh bên trái và chữ bên phải. Tiêu đề với mô tả hiện trên banner. Nút Mua sắm ngay và Thuê đồ giữ nguyên.</p>

    <div class="d-flex flex-column gap-4">
        @foreach ($slots as $slot)
            @php
                $position = $slot['position'];
                $copy = \App\Domain\Content\Models\HomeBanner::SLIDE_COPY[$position];
                $sportValue = old('position') == $position ? old('sport') : ($slot['banner']?->sport ?: $copy['sport']);
                $titleValue = old('position') == $position ? old('title') : ($slot['banner']?->title ?: $copy['title']);
                $descriptionValue = old('position') == $position ? old('description') : ($slot['banner']?->description ?: $copy['text']);
                $hasCustomImage = $slot['banner'] && $slot['banner']->image_url !== '';
            @endphp
            <div class="admin-card p-4">
                <h2 class="h5 mb-3">Slide {{ $position }} — {{ $sportValue }}</h2>
                <form method="post" action="{{ route('admin.home-banner.update') }}" enctype="multipart/form-data">
                    @csrf
                    @method('PUT')
                    <input type="hidden" name="position" value="{{ $position }}">
                    <div class="row g-4 align-items-start">
                        <div class="col-lg-6">
                            @if ($slot['previewUrl'])
                                <img src="{{ $slot['previewUrl'] }}" alt="{{ $slot['label'] }}" class="img-fluid border rounded mb-2" style="max-height:220px;width:100%;object-fit:cover">
                                <p class="small text-muted mb-3">
                                    {{ $hasCustomImage ? 'Đang dùng ảnh bạn đã tải lên.' : 'Đang dùng ảnh mặc định.' }}
                                </p>
                            @else
                                <div class="border rounded mb-3 d-flex align-items-center justify-content-center text-muted" style="height:180px;background:#eef3f9">
                                    Chưa có ảnh. Slide này đang dùng nền màu.
                                </div>
                            @endif
                            <label class="form-label" for="image-{{ $position }}">Chọn ảnh</label>
                            <input
                                class="form-control @if ((int) old('position') === $position) @error('image') is-invalid @enderror @endif"
                                id="image-{{ $position }}"
                                name="image"
                                type="file"
                                accept="image/*"
                            >
                            @if ((int) old('position') === $position)
                                @error('image')
                                    <div class="invalid-feedback">{{ $message }}</div>
                                @enderror
                            @endif
                            <div class="form-text">Ảnh ngang, khoảng 1600×550 px, tối đa 5MB. Để trống nếu chỉ sửa chữ.</div>
                        </div>
                        <div class="col-lg-6">
                            <div class="mb-3">
                                <label class="form-label" for="sport-{{ $position }}">Môn thể thao</label>
                                <input
                                    class="form-control @if ((int) old('position') === $position) @error('sport') is-invalid @enderror @endif"
                                    id="sport-{{ $position }}"
                                    name="sport"
                                    value="{{ $sportValue }}"
                                    maxlength="80"
                                >
                                @if ((int) old('position') === $position)
                                    @error('sport')
                                        <div class="invalid-feedback">{{ $message }}</div>
                                    @enderror
                                @endif
                                <div class="form-text">Chữ nhỏ phía trên tiêu đề, ví dụ 01 / {{ $sportValue }}.</div>
                            </div>
                            <div class="mb-3">
                                <label class="form-label" for="title-{{ $position }}">Tiêu đề</label>
                                <input
                                    class="form-control @if ((int) old('position') === $position) @error('title') is-invalid @enderror @endif"
                                    id="title-{{ $position }}"
                                    name="title"
                                    value="{{ $titleValue }}"
                                    maxlength="120"
                                >
                                @if ((int) old('position') === $position)
                                    @error('title')
                                        <div class="invalid-feedback">{{ $message }}</div>
                                    @enderror
                                @endif
                            </div>
                            <div class="mb-3">
                                <label class="form-label" for="description-{{ $position }}">Mô tả</label>
                                <textarea
                                    class="form-control @if ((int) old('position') === $position) @error('description') is-invalid @enderror @endif"
                                    id="description-{{ $position }}"
                                    name="description"
                                    rows="4"
                                    maxlength="500"
                                >{{ $descriptionValue }}</textarea>
                                @if ((int) old('position') === $position)
                                    @error('description')
                                        <div class="invalid-feedback">{{ $message }}</div>
                                    @enderror
                                @endif
                                <div class="form-text">Đoạn chữ nằm dưới tiêu đề trên banner.</div>
                            </div>
                        </div>
                    </div>
                    <div class="d-flex flex-wrap gap-2 mt-3">
                        <button class="btn btn-primary" type="submit">Lưu slide</button>
                        @if ($hasCustomImage)
                            <button
                                class="btn btn-outline-secondary"
                                type="submit"
                                form="home-banner-reset-{{ $position }}"
                                onclick="return confirm('{{ $position === 1 ? 'Trở lại ảnh mặc định cho slide này?' : 'Xóa ảnh slide này?' }}')"
                            >{{ $position === 1 ? 'Dùng ảnh mặc định' : 'Xóa ảnh' }}</button>
                        @endif
                    </div>
                </form>

                @if ($hasCustomImage)
                    <form id="home-banner-reset-{{ $position }}" method="post" action="{{ route('admin.home-banner.destroy') }}" class="d-none">
                        @csrf
                        @method('DELETE')
                        <input type="hidden" name="position" value="{{ $position }}">
                    </form>
                @endif
            </div>
        @endforeach
    </div>
@endsection
