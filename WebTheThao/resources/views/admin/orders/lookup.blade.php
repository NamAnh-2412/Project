@extends('layouts.admin')
@section('title', 'Soát mã')
@section('content')
    <h1 class="page-title">Soát mã</h1>
    <p class="page-subtitle mb-4">Nhập mã đơn online khách đưa. Đơn nhận tại quầy giao một lần. Đơn giao GHN chỉ để soạn hàng.</p>

    <form class="row g-2 align-items-end mb-4" method="get" action="{{ route('admin.orders.lookup') }}" style="max-width:420px">
        <div class="col">
            <label class="form-label" for="order-code">Mã đơn</label>
            <input class="form-control" id="order-code" name="code" value="{{ $code }}" inputmode="numeric" placeholder="Ví dụ 12" autofocus>
        </div>
        <div class="col-auto">
            <button class="btn btn-success">Kiểm tra</button>
        </div>
    </form>

    @if ($missing)
        <div class="alert alert-warning">Không có mã đơn này.</div>
    @endif

    @if ($order)
        @php
            $online = $order->channel === \App\Domain\Order\Enums\OrderChannel::Online;
            $pickup = $order->shipping_method === \App\Domain\Shipping\Enums\ShippingMethod::Pickup;
            $paid = in_array($order->status, [
                \App\Domain\Order\Enums\OrderStatus::Paid,
                \App\Domain\Order\Enums\OrderStatus::Processing,
                \App\Domain\Order\Enums\OrderStatus::Completed,
            ], true);
        @endphp
        <div class="admin-card p-4" style="max-width:640px">
            <h2 class="h4 mb-1">Đơn #{{ $order->id }}</h2>
            <p class="mb-2">{{ $order->shipping_name }} · {{ $order->shipping_phone }}</p>
            <p class="mb-3">
                {{ $order->channel->label() }}
                @if ($order->shipping_method) · {{ $order->shipping_method->label() }} @endif
                · {{ $order->status->label() }}
                @if ($paid) · Đã thanh toán @else · Chưa thanh toán @endif
            </p>

            @foreach ($order->items as $item)
                <div class="mb-2">
                    {{ $item->line_type->label() }}
                    — {{ $item->product_name }} × {{ $item->quantity }}
                    @if ($item->rental_start)
                        ({{ $item->rental_start->format('d/m/Y') }} → {{ $item->rental_end?->format('d/m/Y') }})
                    @endif
                </div>
            @endforeach
            <p class="fw-semibold mt-3">Tổng {{ number_format($order->grand_total, 0, ',', '.') }}đ</p>

            @if (! $online)
                <div class="alert alert-secondary mb-0">Đây là đơn bán tại quầy. Không soát ở trang này.</div>
            @elseif (! $pickup)
                <div class="alert alert-info mb-0">
                    Đơn giao qua GHN, không giao tại quầy.
                    @if ($order->ghn_order_code)
                        Mã GHN: <strong>{{ $order->ghn_order_code }}</strong>
                    @endif
                </div>
            @elseif ($order->handed_over_at)
                <div class="alert alert-success mb-0">Đã giao lúc {{ $order->handed_over_at->timezone(config('app.timezone'))->format('d/m/Y H:i') }}.</div>
            @elseif (! $order->canHandOver())
                <div class="alert alert-warning mb-0">Chưa thanh toán hoặc đơn không còn giao được.</div>
            @else
                <form method="post" action="{{ route('admin.orders.handover', $order) }}">
                    @csrf
                    <button class="btn btn-success">Đã giao</button>
                </form>
            @endif
            <p class="small mt-3 mb-0"><a href="{{ route('admin.orders.show', $order) }}">Mở chi tiết đơn</a></p>
        </div>
    @endif
@endsection
