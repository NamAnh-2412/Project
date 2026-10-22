<?php

namespace App\Domain\ThreeDPrint\Models;

use App\Domain\Chat\Models\Conversation;
use App\Domain\Order\Models\Order;
use App\Domain\ThreeDPrint\Enums\ModelRequestStatus;
use App\Domain\ThreeDPrint\Support\PrintEstimator;
use App\Domain\User\Models\User;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ModelPrintRequest extends Model
{
    protected $fillable = [
        'user_id',
        'conversation_id',
        'original_name',
        'disk_path',
        'format',
        'material',
        'layer_height',
        'infill',
        'quantity',
        'volume_cm3',
        'length_mm',
        'width_mm',
        'height_mm',
        'notes',
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
            'infill' => 'integer',
            'quantity' => 'integer',
            'volume_cm3' => 'decimal:3',
            'length_mm' => 'decimal:2',
            'width_mm' => 'decimal:2',
            'height_mm' => 'decimal:2',
            'estimate_amount' => 'decimal:2',
            'quote_amount' => 'decimal:2',
            'status' => ModelRequestStatus::class,
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

    public function amount(): float
    {
        return (float) ($this->quote_amount ?? $this->estimate_amount);
    }

    public function summary(): string
    {
        return PrintEstimator::materialLabel($this->material)
            .' · '.PrintEstimator::layerLabel($this->layer_height)
            .' · đặc '.$this->infill.'%'
            .' · '.number_format((float) $this->volume_cm3, 2, ',', '.').' cm³';
    }
}
