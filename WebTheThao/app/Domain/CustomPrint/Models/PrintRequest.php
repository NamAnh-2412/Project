<?php

namespace App\Domain\CustomPrint\Models;

use App\Domain\Chat\Models\Conversation;
use App\Domain\CustomPrint\Enums\PrintRequestStatus;
use App\Domain\CustomPrint\Support\PrintCatalog;
use App\Domain\Order\Models\Order;
use App\Domain\User\Models\User;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class PrintRequest extends Model
{
    protected $fillable = [
        'user_id',
        'conversation_id',
        'garment',
        'color',
        'size',
        'quantity',
        'placements',
        'notes',
        'artwork_path',
        'estimate_amount',
        'quote_amount',
        'status',
        'order_id',
        'quoted_by',
        'quoted_at',
        'decided_at',
    ];

    protected function casts(): array
    {
        return [
            'user_id' => 'integer',
            'conversation_id' => 'integer',
            'quantity' => 'integer',
            'placements' => 'array',
            'estimate_amount' => 'decimal:2',
            'quote_amount' => 'decimal:2',
            'status' => PrintRequestStatus::class,
            'order_id' => 'integer',
            'quoted_by' => 'integer',
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

    public function quotedBy(): BelongsTo
    {
        return $this->belongsTo(User::class, 'quoted_by');
    }

    public function artworkUrl(): string
    {
        $path = (string) $this->artwork_path;

        if (str_starts_with($path, 'http://') || str_starts_with($path, 'https://')) {
            return $path;
        }

        if (str_starts_with($path, '/')) {
            return asset(ltrim($path, '/'));
        }

        return asset('storage/'.$path);
    }

    public function amount(): float
    {
        return (float) ($this->quote_amount ?? $this->estimate_amount);
    }

    public function summary(): string
    {
        return PrintCatalog::garmentLabel($this->garment)
            .' · '.PrintCatalog::colorLabel($this->color)
            .' · size '.$this->size
            .' · ×'.$this->quantity;
    }
}
