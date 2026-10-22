@extends('layouts.admin')
@section('title', 'Bảng size')
@section('content')
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="page-title">Bảng size</h1>
            <p class="page-subtitle mb-0">Khoảng chiều cao và cân nặng để khách được gợi ý size.</p>
        </div>
        <a href="{{ route('admin.size-guides.create') }}" class="btn btn-admin-primary"><i class="fas fa-plus me-1"></i> Thêm bảng</a>
    </div>
    <div class="admin-card">
        <table class="table admin-table align-middle">
            <thead><tr><th>Tên</th><th>Nhóm</th><th>Thương hiệu</th><th>Size</th><th>Sản phẩm</th><th></th></tr></thead>
            <tbody>
            @forelse ($guides as $guide)
                <tr>
                    <td>{{ $guide->name }}{{ $guide->is_active ? '' : ' · Tắt' }}</td>
                    <td>{{ $guide->groupLabel() }}</td>
                    <td>{{ $guide->brand ?: '—' }}</td>
                    <td>{{ implode(', ', array_keys($guide->measurements ?? [])) }}</td>
                    <td>{{ $guide->products_count }}</td>
                    <td class="text-end">
                        <a class="btn btn-sm btn-outline-primary" href="{{ route('admin.size-guides.edit', $guide) }}"><i class="fas fa-pen"></i></a>
                        <form class="d-inline" method="post" action="{{ route('admin.size-guides.destroy', $guide) }}" onsubmit="return confirm('Xóa bảng size này?')">
                            @csrf @method('DELETE')
                            <button class="btn btn-sm btn-outline-danger"><i class="fas fa-trash"></i></button>
                        </form>
                    </td>
                </tr>
            @empty
                <tr><td colspan="6" class="text-muted">Chưa có bảng size.</td></tr>
            @endforelse
            </tbody>
        </table>
    </div>
@endsection
