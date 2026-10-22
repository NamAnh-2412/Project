@extends('storefront.layouts.app')
@include('partials.js', ['file' => 'js/storefront/model-print.js'])

@section('title', 'In 3D')

@section('content')
    <div
        class="policy-card col-lg-8 mx-auto"
        data-model-form
        data-preview-url="{{ route('models.preview') }}"
        data-startup="{{ $startup }}"
        data-minimum="{{ $minimum }}"
        data-shell="{{ $shell }}"
        data-materials='@json(collect($materials)->map(fn ($row) => $row["rate"]))'
        data-layers='@json(collect($layers)->map(fn ($row) => $row["multiplier"]))'
    >
        <h1 class="h4 mb-1">In 3D</h1>
        <p class="text-secondary mb-4">Tải file STL, OBJ hoặc STEP. Giá tạm tính từ thể tích, chất liệu, độ dày lớp và độ đặc. Cửa hàng xem file trong hội thoại rồi chốt báo giá.</p>

        @guest
            <p class="mb-3">Đăng nhập để gửi file in 3D.</p>
            <a class="btn btn-success" href="{{ route('login') }}">Đăng nhập</a>
        @else
            <form method="post" action="{{ route('models.store') }}" enctype="multipart/form-data">
                @csrf
                <div class="row g-3">
                    <div class="col-12">
                        <label class="form-label" for="model">File mô hình</label>
                        <input class="form-control @error('model') is-invalid @enderror" id="model" name="model" type="file" accept=".stl,.obj,.step,.stp" required>
                        <div class="form-text">STL và OBJ được đo thể tích từ lưới. STEP dùng kích thước hộp bao. Tối đa 20MB.</div>
                        @error('model')<div class="invalid-feedback">{{ $message }}</div>@enderror
                    </div>
                    <div class="col-12 d-none" data-step-box>
                        <div class="row g-3">
                            <div class="col-md-4">
                                <label class="form-label" for="length_mm">Dài (mm)</label>
                                <input class="form-control" id="length_mm" name="length_mm" type="number" min="5" max="250" step="0.1" value="{{ old('length_mm') }}">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label" for="width_mm">Rộng (mm)</label>
                                <input class="form-control" id="width_mm" name="width_mm" type="number" min="5" max="250" step="0.1" value="{{ old('width_mm') }}">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label" for="height_mm">Cao (mm)</label>
                                <input class="form-control" id="height_mm" name="height_mm" type="number" min="5" max="250" step="0.1" value="{{ old('height_mm') }}">
                            </div>
                        </div>
                        @error('length_mm')<div class="invalid-feedback d-block">{{ $message }}</div>@enderror
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="material">Chất liệu</label>
                        <select class="form-select" id="material" name="material" required>
                            @foreach ($materials as $key => $row)
                                <option value="{{ $key }}" @selected(old('material', 'pla') === $key)>{{ $row['label'] }} — {{ number_format($row['rate'], 0, ',', '.') }}đ/cm³</option>
                            @endforeach
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="layer_height">Độ dày lớp</label>
                        <select class="form-select" id="layer_height" name="layer_height" required>
                            @foreach ($layers as $key => $row)
                                <option value="{{ $key }}" @selected(old('layer_height', '0.20') === $key)>{{ $row['label'] }}</option>
                            @endforeach
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label" for="infill">Độ đặc</label>
                        <select class="form-select" id="infill" name="infill" required>
                            @foreach ($infills as $infill)
                                <option value="{{ $infill }}" @selected((int) old('infill', 30) === $infill)>{{ $infill }}%</option>
                            @endforeach
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label" for="quantity">Số lượng</label>
                        <input class="form-control" id="quantity" name="quantity" type="number" min="1" max="20" value="{{ old('quantity', 1) }}" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="notes">Ghi chú</label>
                        <textarea class="form-control" id="notes" name="notes" rows="3" maxlength="1000" placeholder="Màu, độ bền, vị trí lắp...">{{ old('notes') }}</textarea>
                    </div>
                </div>
                <p class="mt-3 mb-1">Thể tích: <strong data-model-volume>—</strong></p>
                <p class="mb-3">Tạm tính: <strong data-model-estimate>—</strong></p>
                <p class="small text-secondary" data-model-error></p>
                <button class="btn btn-success" type="submit">Gửi vào hội thoại</button>
            </form>
        @endguest
    </div>
@endsection
