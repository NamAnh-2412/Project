@php
    $printRequest = $printRequest ?? null;
    $conversation = $conversation ?? null;
@endphp
@if ($printRequest)
    <div class="mt-2">
        @if ($printRequest->status === \App\Domain\CustomPrint\Enums\PrintRequestStatus::Accepted && $printRequest->order_id)
            <div class="small">Đã tạo đơn #{{ $printRequest->order_id }}.</div>
        @elseif ($printRequest->status === \App\Domain\CustomPrint\Enums\PrintRequestStatus::Declined)
            <div class="small">Khách chưa chốt yêu cầu này.</div>
        @elseif (request()->routeIs('admin.*') && in_array($printRequest->status, [\App\Domain\CustomPrint\Enums\PrintRequestStatus::Submitted, \App\Domain\CustomPrint\Enums\PrintRequestStatus::Quoted], true) && $conversation)
            <form method="post" action="{{ route('admin.prints.quote', [$conversation, $printRequest]) }}" class="mt-2">
                @csrf
                <label class="form-label small mb-1" for="quote-{{ $printRequest->id }}">Báo giá (đ)</label>
                <input class="form-control form-control-sm mb-2" id="quote-{{ $printRequest->id }}" name="quote_amount" type="number" min="1" step="1" value="{{ old('quote_amount', (int) $printRequest->amount()) }}" required>
                <input class="form-control form-control-sm mb-2" name="note" type="text" maxlength="500" placeholder="Ghi chú cho khách, nếu cần" value="{{ old('note') }}">
                <button class="btn btn-sm btn-admin-primary" type="submit">Gửi báo giá</button>
            </form>
        @elseif (request()->routeIs('messages.*') && $printRequest->status === \App\Domain\CustomPrint\Enums\PrintRequestStatus::Quoted && auth()->user()?->isCustomer())
            <div class="d-flex flex-wrap gap-2 mt-2">
                <form method="post" action="{{ route('prints.decide', $printRequest) }}">
                    @csrf
                    <input type="hidden" name="decision" value="accept">
                    <button class="btn btn-sm btn-success" type="submit">Chốt và tạo đơn</button>
                </form>
                <form method="post" action="{{ route('prints.decide', $printRequest) }}">
                    @csrf
                    <input type="hidden" name="decision" value="decline">
                    <button class="btn btn-sm btn-outline-secondary" type="submit">Chưa chốt</button>
                </form>
            </div>
        @endif
    </div>
@endif
