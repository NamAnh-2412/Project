@extends('layouts.admin')
@section('title', 'Thêm bảng size')
@section('content')
    <h1 class="page-title mb-4">Thêm bảng size</h1>
    <form class="admin-card p-3" method="post" action="{{ route('admin.size-guides.store') }}">
        @csrf
        @include('admin.size-guides._form')
        <button class="btn btn-admin-primary mt-3" type="submit">Lưu bảng</button>
    </form>
@endsection
