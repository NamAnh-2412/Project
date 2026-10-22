<?php

namespace App\Domain\Product\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class SizeGuide extends Model
{
    protected $fillable = ['name', 'group', 'brand', 'measurements', 'is_active'];

    protected function casts(): array
    {
        return [
            'measurements' => 'array',
            'is_active' => 'boolean',
        ];
    }

    public function products(): HasMany
    {
        return $this->hasMany(Product::class);
    }

    public function groupLabel(): string
    {
        return match ($this->group) {
            'ao' => 'Áo',
            'quan' => 'Quần',
            'giay' => 'Giày',
            default => $this->group,
        };
    }
}
