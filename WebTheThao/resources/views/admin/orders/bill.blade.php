<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bill #{{ $order->id }} — WebTheThao</title>
    <link rel="stylesheet" href="{{ asset('css/admin/bill.css') }}?v={{ filemtime(public_path('css/admin/bill.css')) }}">
</head>
<body>
    <article class="bill">
        <h1>WebTheThao</h1>
        <p class="center muted">Bill tại quầy</p>
        <p class="code">#{{ $order->id }}</p>
        <p class="center muted">{{ $order->created_at?->timezone(config('app.timezone'))->format('d/m/Y H:i') }}</p>
        <p>{{ $order->shipping_name }}@if ($order->shipping_phone) · {{ $order->shipping_phone }}@endif</p>
        <hr>
        @foreach ($order->items as $item)
            <div class="line">
                <div>
                    {{ $item->line_type->label() }} {{ $item->product_name }}
                    @if ($item->sku) <span class="muted">({{ $item->sku }})</span> @endif
                    @if ($item->rental_start)
                        <div class="muted">{{ $item->rental_start->format('d/m/Y') }} → {{ $item->rental_end?->format('d/m/Y') }}</div>
                    @endif
                </div>
                <div>× {{ $item->quantity }}</div>
                <div class="money">{{ number_format($item->line_total, 0, ',', '.') }}đ</div>
            </div>
        @endforeach
        <hr>
        @if ((float) $order->deposit_total > 0)
            <p class="sum"><span>Cọc</span><span>{{ number_format($order->deposit_total, 0, ',', '.') }}đ</span></p>
        @endif
        @if ((float) $order->discount_total > 0)
            <p class="sum"><span>Giảm</span><span>−{{ number_format($order->discount_total, 0, ',', '.') }}đ</span></p>
        @endif
        <p class="sum total"><span>Tổng</span><span>{{ number_format($order->grand_total, 0, ',', '.') }}đ</span></p>
        <p>{{ $payment ? $payment->method->label() : 'Chưa thu' }}</p>
        <p class="paid">{{ $payment ? 'Đã thu tiền' : 'Chưa thu tiền' }}</p>
    </article>
    <p class="actions">
        <button type="button" onclick="window.print()">In bill</button>
        <a href="{{ route('admin.pos.index') }}">Về quầy</a>
        <a href="{{ route('admin.orders.show', $order) }}">Xem đơn</a>
    </p>
</body>
</html>
