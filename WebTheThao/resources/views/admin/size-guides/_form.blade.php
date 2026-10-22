@php
    $editing = isset($sizeGuide);
    $stored = $editing ? ($sizeGuide->measurements ?? []) : [];
    $rows = old('rows');
    if (! is_array($rows)) {
        $rows = [];
        foreach ($stored as $size => $range) {
            $rows[] = [
                'size' => $size,
                'min_height_cm' => $range['min_height_cm'] ?? '',
                'max_height_cm' => $range['max_height_cm'] ?? '',
                'min_weight_kg' => $range['min_weight_kg'] ?? '',
                'max_weight_kg' => $range['max_weight_kg'] ?? '',
            ];
        }
    }
    if ($rows === []) {
        $rows[] = ['size' => '', 'min_height_cm' => '', 'max_height_cm' => '', 'min_weight_kg' => '', 'max_weight_kg' => ''];
    }
@endphp
<div class="row g-3">
    <div class="col-md-4">
        <label class="form-label" for="name">Tên bảng</label>
        <input class="form-control" id="name" name="name" value="{{ old('name', $editing ? $sizeGuide->name : '') }}" required>
    </div>
    <div class="col-md-3">
        <label class="form-label" for="group">Nhóm</label>
        <select class="form-select" id="group" name="group" required>
            @foreach (['ao' => 'Áo', 'quan' => 'Quần', 'giay' => 'Giày'] as $value => $label)
                <option value="{{ $value }}" @selected(old('group', $editing ? $sizeGuide->group : 'ao') === $value)>{{ $label }}</option>
            @endforeach
        </select>
    </div>
    <div class="col-md-3">
        <label class="form-label" for="brand">Thương hiệu</label>
        <input class="form-control" id="brand" name="brand" value="{{ old('brand', $editing ? $sizeGuide->brand : '') }}" maxlength="100">
    </div>
    <div class="col-md-2 d-flex align-items-end">
        <div class="form-check mb-2">
            <input class="form-check-input" type="checkbox" id="is_active" name="is_active" value="1" @checked(old('is_active', $editing ? $sizeGuide->is_active : true))>
            <label class="form-check-label" for="is_active">Đang dùng</label>
        </div>
    </div>
</div>
<div class="table-responsive mt-3">
    <table class="table admin-table align-middle">
        <thead>
            <tr><th>Size</th><th>Cao từ (cm)</th><th>Cao đến (cm)</th><th>Nặng từ (kg)</th><th>Nặng đến (kg)</th></tr>
        </thead>
        <tbody>
            @foreach ($rows as $index => $row)
                <tr>
                    <td><input class="form-control" name="rows[{{ $index }}][size]" value="{{ $row['size'] ?? '' }}"></td>
                    <td><input class="form-control" type="number" step="0.1" name="rows[{{ $index }}][min_height_cm]" value="{{ $row['min_height_cm'] ?? '' }}"></td>
                    <td><input class="form-control" type="number" step="0.1" name="rows[{{ $index }}][max_height_cm]" value="{{ $row['max_height_cm'] ?? '' }}"></td>
                    <td><input class="form-control" type="number" step="0.1" name="rows[{{ $index }}][min_weight_kg]" value="{{ $row['min_weight_kg'] ?? '' }}"></td>
                    <td><input class="form-control" type="number" step="0.1" name="rows[{{ $index }}][max_weight_kg]" value="{{ $row['max_weight_kg'] ?? '' }}"></td>
                </tr>
            @endforeach
            @for ($index = count($rows); $index < count($rows) + 2; $index++)
                <tr>
                    <td><input class="form-control" name="rows[{{ $index }}][size]" placeholder="Thêm size"></td>
                    <td><input class="form-control" type="number" step="0.1" name="rows[{{ $index }}][min_height_cm]"></td>
                    <td><input class="form-control" type="number" step="0.1" name="rows[{{ $index }}][max_height_cm]"></td>
                    <td><input class="form-control" type="number" step="0.1" name="rows[{{ $index }}][min_weight_kg]"></td>
                    <td><input class="form-control" type="number" step="0.1" name="rows[{{ $index }}][max_weight_kg]"></td>
                </tr>
            @endfor
        </tbody>
    </table>
</div>
@if ($errors->any())
    <div class="alert alert-warning">{{ $errors->first() }}</div>
@endif
