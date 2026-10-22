<?php

namespace App\Domain\Order\Enums;

enum LineType: string
{
    case Sale = 'sale';
    case Rental = 'rental';
    case Print = 'print';

    public function label(): string
    {
        return match ($this) {
            self::Sale => 'Mua',
            self::Rental => 'Thuê',
            self::Print => 'In theo yêu cầu',
        };
    }
}
