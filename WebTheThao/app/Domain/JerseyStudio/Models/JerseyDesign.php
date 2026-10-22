<?php

namespace App\Domain\JerseyStudio\Models;

use App\Domain\Chat\Models\Conversation;
use App\Domain\Order\Models\Order;
use App\Domain\User\Models\User;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class JerseyDesign extends Model
{
    protected $fillable = [
        'user_id',
        'conversation_id',
        'garment_template',
        'design_payload',
        'logo_path',
        'size',
        'quantity',
        'notes',
        'status',
        'quote_amount',
        'order_id',
        'quoted_by',
        'submitted_at',
        'quoted_at',
        'decided_at',
    ];

    protected function casts(): array
    {
        return [
            'user_id' => 'integer',
            'conversation_id' => 'integer',
            'design_payload' => 'array',
            'quantity' => 'integer',
            'quote_amount' => 'decimal:2',
            'order_id' => 'integer',
            'quoted_by' => 'integer',
            'submitted_at' => 'datetime',
            'quoted_at' => 'datetime',
            'decided_at' => 'datetime',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function conversation(): BelongsTo
    {
        return $this->belongsTo(Conversation::class);
    }

    public function order(): BelongsTo
    {
        return $this->belongsTo(Order::class);
    }

    public function statusLabel(): string
    {
        return match ($this->status) {
            'draft' => 'Bản nháp',
            'submitted' => 'Chờ báo giá',
            'quoted' => 'Đã báo giá',
            'accepted' => 'Đã chốt',
            'declined' => 'Không chốt',
            default => $this->status,
        };
    }

    public function contentLine(): string
    {
        return collect($this->design_payload['layers'] ?? [])
            ->filter(fn ($layer) => is_array($layer) && in_array($layer['type'] ?? '', ['text', 'number'], true))
            ->map(fn ($layer) => trim((string) ($layer['text'] ?? '')))
            ->filter()
            ->implode(' · ');
    }
}
