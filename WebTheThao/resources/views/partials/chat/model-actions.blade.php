@php
    $modelRequest = $modelRequest ?? null;
    $conversation = $conversation ?? null;
@endphp
@if ($modelRequest)
    <div class="mt-2">
        <a class="chat-attach-link" href="{{ route('models.download', $modelRequest) }}">Tải file</a>
        @if ($modelRequest->status === \App\Domain\ThreeDPrint\Enums\ModelRequestStatus::Accepted && $modelRequest->order_id)
            <div class="small">Đã tạo đơn #{{ $modelRequest->order_id }}.</div>
        @elseif ($modelRequest->status === \App\Domain\ThreeDPrint\Enums\ModelRequestStatus::Declined)
            <div class="small">Khách chưa chốt file này.</div>
        @elseif (request()->routeIs('admin.*') && in_array($modelRequest->status, [\App\Domain\ThreeDPrint\Enums\ModelRequestStatus::Submitted, \App\Domain\ThreeDPrint\Enums\ModelRequestStatus::Quoted], true) && $conversation)
            <form method="post" action="{{ route('admin.models.quote', [$conversation, $modelRequest]) }}" class="mt-2">
                @csrf
                <label class="form-label small mb-1" for="model-quote-{{ $modelRequest->id }}">Báo giá (đ)</label>
                <input class="form-control form-control-sm mb-2" id="model-quote-{{ $modelRequest->id }}" name="quote_amount" type="number" min="1" step="1" value="{{ (int) $modelRequest->amount() }}" required>
                <input class="form-control form-control-sm mb-2" name="note" type="text" maxlength="500" placeholder="Ghi chú cho khách, nếu cần">
                <button class="btn btn-sm btn-admin-primary" type="submit">Gửi báo giá in 3D</button>
            </form>
        @elseif (request()->routeIs('messages.*') && $modelRequest->status === \App\Domain\ThreeDPrint\Enums\ModelRequestStatus::Quoted && auth()->user()?->isCustomer())
            <div class="d-flex flex-wrap gap-2 mt-2">
                <form method="post" action="{{ route('models.decide', $modelRequest) }}">
                    @csrf
                    <input type="hidden" name="decision" value="accept">
                    <button class="btn btn-sm btn-success" type="submit">Chốt và tạo đơn</button>
                </form>
                <form method="post" action="{{ route('models.decide', $modelRequest) }}">
                    @csrf
                    <input type="hidden" name="decision" value="decline">
                    <button class="btn btn-sm btn-outline-secondary" type="submit">Chưa chốt</button>
                </form>
            </div>
        @endif
    </div>
@endif
