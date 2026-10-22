@extends('storefront.layouts.app')

@section('title', 'Thông báo')

@section('content')
    <h1 class="h3 mb-3">Thông báo</h1>

    @forelse ($notifications as $row)
        <div class="policy-card mb-3 {{ $row->read_at === null ? 'border border-warning' : '' }}">
            <div class="d-flex justify-content-between flex-wrap gap-2">
                <strong>{{ $row->subject }}</strong>
                <span class="small text-secondary">{{ $row->type->label() }} · {{ $row->created_at?->timezone(config('app.timezone'))->format('d/m/Y H:i') }}</span>
            </div>
            <p class="small mb-2 mt-2" style="white-space: pre-wrap;">{{ $row->body }}</p>
            @if (! empty($row->payload['design_id']))
                <a href="{{ route('models.create', ['design' => $row->payload['design_id']]) }}">Mở bản phối</a>
            @endif
        </div>
    @empty
        <div class="policy-card">Chưa có thông báo.</div>
    @endforelse
@endsection
