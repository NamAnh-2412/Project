@extends('layouts.admin')
@section('title', 'Sửa bảng size')
@section('content')
    <h1 class="page-title mb-4">Sửa bảng size</h1>
    <form class="admin-card p-3" method="post" action="{{ route('admin.size-guides.update', $sizeGuide) }}">
        @csrf
        @method('PUT')
        @include('admin.size-guides._form')
        <button class="btn btn-admin-primary mt-3" type="submit">Cập nhật</button>
    </form>
@endsection