<?php

namespace App\Domain\CustomPrint\Enums;

enum PrintRequestStatus: string
{
    case Submitted = 'submitted';
    case Quoted = 'quoted';
    case Accepted = 'accepted';
    case Declined = 'declined';

    public function label(): string
    {
        return match ($this) {
            self::Submitted => 'Chờ báo giá',
            self::Quoted => 'Đã báo giá',
            self::Accepted => 'Đã chốt',
            self::Declined => 'Không chốt',
        };
    }
}
