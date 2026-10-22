<?php

namespace App\Domain\Chat\Enums;

enum ChatAttachmentType: string
{
    case Product = 'product';
    case Order = 'order';
    case Print = 'print';
    case Model = 'model';

    public function label(): string
    {
        return match ($this) {
            self::Product => 'Sản phẩm',
            self::Order => 'Đơn hàng',
            self::Print => 'Yêu cầu in',
            self::Model => 'In 3D',
        };
    }
}
