@php
    $payload = is_array($payload ?? null) ? $payload : [];
    $kind = (string) ($payload['kind'] ?? '');
    $kindEnum = \App\Domain\Chat\Enums\ChatAttachmentType::tryFrom($kind);
    $title = (string) ($payload['title'] ?? 'Đính kèm');
    $subtitle = (string) ($payload['subtitle'] ?? '');
    $priceLabel = (string) ($payload['price_label'] ?? '');
    $image = $payload['image'] ?? null;
    $items = is_array($payload['items'] ?? null) ? $payload['items'] : [];
    $url = (string) ($payload['url'] ?? '');
    if ($kindEnum === \App\Domain\Chat\Enums\ChatAttachmentType::Order && request()->routeIs('admin.*') && ! empty($payload['id'])) {
        $url = route('admin.orders.show', $payload['id']);
    }
    if ($kindEnum === \App\Domain\Chat\Enums\ChatAttachmentType::Print && request()->routeIs('admin.*') && ! empty($payload['order_id'])) {
        $url = route('admin.orders.show', $payload['order_id']);
    }
    $tone = $mine ?? false;
    $printRequest = null;
    $showPrintActions = false;
    if ($kindEnum === \App\Domain\Chat\Enums\ChatAttachmentType::Print && ! empty($payload['id'])) {
        $printRequest = ($printRequests ?? collect())->get((int) $payload['id']);
        $latestId = ($latestPrintMessageIds ?? [])[(int) $payload['id']] ?? null;
        $showPrintActions = $printRequest && $latestId && isset($message) && (int) $message->id === (int) $latestId;
        if ($showPrintActions) {
            $subtitle = $printRequest->status->label().' · '.$printRequest->summary();
            $priceLabel = number_format($printRequest->amount(), 0, ',', '.').'đ';
            $items = \App\Domain\CustomPrint\Support\PrintCatalog::placementLabels($printRequest->placements ?? []);
        }
    }
    $modelRequest = null;
    $showModelActions = false;
    if ($kindEnum === \App\Domain\Chat\Enums\ChatAttachmentType::Model && ! empty($payload['id'])) {
        $modelRequest = ($modelRequests ?? collect())->get((int) $payload['id']);
        $latestModelId = ($latestModelMessageIds ?? [])[(int) $payload['id']] ?? null;
        $showModelActions = $modelRequest && $latestModelId && isset($message) && (int) $message->id === (int) $latestModelId;
        if ($showModelActions) {
            $subtitle = $modelRequest->status->label().' · '.$modelRequest->summary();
            $priceLabel = number_format($modelRequest->amount(), 0, ',', '.').'đ';
        }
    }
@endphp
<div class="chat-attach {{ $tone ? 'chat-attach-mine' : 'chat-attach-store' }}">
    @if ($image)
        <div class="chat-attach-media">
            <img src="{{ $image }}" alt="" loading="lazy">
        </div>
    @endif
    <div class="chat-attach-body">
        <div class="chat-attach-kind">{{ $kindEnum?->label() ?? 'Đính kèm' }}</div>
        <div class="chat-attach-title">{{ $title }}</div>
        @if ($subtitle !== '')
            <div class="chat-attach-sub">{{ $subtitle }}</div>
        @endif
        @if ($priceLabel !== '')
            <div class="chat-attach-price">{{ $priceLabel }}</div>
        @endif
        @if ($items !== [])
            <div class="chat-attach-items">{{ implode(' · ', array_slice($items, 0, 3)) }}</div>
        @endif
        @if ($url !== '' && $url !== '#')
            <a class="chat-attach-link" href="{{ $url }}">Xem chi tiết</a>
        @endif
        @if ($showPrintActions)
            @include('partials.chat.print-actions', [
                'printRequest' => $printRequest,
                'conversation' => $conversation ?? null,
            ])
        @endif
        @if ($showModelActions)
            @include('partials.chat.model-actions', [
                'modelRequest' => $modelRequest,
                'conversation' => $conversation ?? null,
            ])
        @endif
    </div>
</div>
