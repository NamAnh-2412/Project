@extends('layouts.admin')
@section('title', 'Yêu cầu thiết kế')
@section('content')
    <div class="mb-4">
        <h1 class="page-title">Yêu cầu thiết kế</h1>
        <p class="page-subtitle mb-0">Bản phối khách đã gửi. Báo giá tại đây, khách chốt trong hội thoại.</p>
    </div>

    @if (session('success'))
        <div class="alert alert-success">{{ session('success') }}</div>
    @endif

    <div class="admin-card">
        <div class="table-responsive">
            <table class="table admin-table align-middle mb-0">
                <thead>
                    <tr>
                        <th>Khách</th>
                        <th>Bản phối</th>
                        <th>Trạng thái</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                @forelse ($designs as $design)
                    @php
                        $payload = $design->design_payload ?? [];
                        $badge = match ($design->status) {
                            'submitted' => 'text-bg-warning',
                            'quoted' => 'text-bg-primary',
                            'accepted' => 'text-bg-success',
                            default => 'text-bg-secondary',
                        };
                    @endphp
                    <tr>
                        <td>
                            <div class="fw-semibold">{{ $design->user->name ?? 'Khách đã xóa' }}</div>
                            <div class="small text-muted">{{ $design->submitted_at?->timezone(config('app.timezone'))->format('d/m/Y H:i') }}</div>
                        </td>
                        <td>
                            <div class="d-flex gap-3">
                                @if ($design->logo_path)
                                    <img src="{{ asset('storage/'.$design->logo_path) }}" alt="" style="width:48px;height:48px;object-fit:contain">
                                @endif
                                <div>
                                    <div class="fw-semibold">{{ $payload['product_name'] ?? 'Áo in 3D' }} #{{ $design->id }}</div>
                                    <div class="small text-muted">
                                        {{ $payload['color_label'] ?? '—' }}
                                        · size {{ $design->size ?: '—' }}
                                        · ×{{ $design->quantity }}
                                    </div>
                                    @if ($design->contentLine() !== '')
                                        <div class="small">In: {{ $design->contentLine() }}</div>
                                    @endif
                                    @if ($design->notes)
                                        <div class="small text-muted">{{ $design->notes }}</div>
                                    @endif
                                </div>
                            </div>
                        </td>
                        <td>
                            <span class="badge {{ $badge }}">{{ $design->statusLabel() }}</span>
                            @if ($design->quote_amount)
                                <div class="small mt-1">{{ number_format($design->quote_amount, 0, ',', '.') }}đ</div>
                            @endif
                        </td>
                        <td class="text-end" style="min-width:16rem">
                            @if ($design->conversation_id)
                                <a class="btn btn-sm btn-outline-secondary mb-2" href="{{ route('admin.messages.show', $design->conversation_id) }}">Hội thoại</a>
                            @endif
                            @if (in_array($design->status, ['submitted', 'quoted'], true) && $design->conversation_id)
                                <form method="post" action="{{ route('admin.studio.quote', [$design->conversation_id, $design]) }}" class="d-flex flex-wrap justify-content-end gap-2">
                                    @csrf
                                    <input class="form-control form-control-sm" style="max-width:9rem" name="quote_amount" type="number" min="1" step="1" value="{{ $design->quote_amount ? (int) $design->quote_amount : '' }}" required>
                                    <input class="form-control form-control-sm" style="max-width:10rem" name="note" type="text" maxlength="500" placeholder="Ghi chú">
                                    <button class="btn btn-sm btn-admin-primary" type="submit">Gửi báo giá</button>
                                </form>
                            @endif
                        </td>
                    </tr>
                @empty
                    <tr>
                        <td colspan="4" class="text-center py-5 text-muted">Chưa có yêu cầu thiết kế nào.</td>
                    </tr>
                @endforelse
                </tbody>
            </table>
        </div>
        @if ($designs->hasPages())
            <div class="p-3">{{ $designs->links() }}</div>
        @endif
    </div>
@endsection
