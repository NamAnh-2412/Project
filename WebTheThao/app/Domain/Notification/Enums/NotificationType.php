<?php

namespace App\Domain\Notification\Enums;

enum NotificationType: string
{
    case OrderConfirmed = 'order_confirmed';
    case RentalDueReminder = 'rental_due_reminder';
    case RentalOverdue = 'rental_overdue';
    case QuoteUpdated = 'quote_updated';

    public function label(): string
    {
        return match ($this) {
            self::OrderConfirmed => 'Đã nhận đơn',
            self::RentalDueReminder => 'Nhắc hạn trả',
            self::RentalOverdue => 'Thuê quá hạn',
            self::QuoteUpdated => 'Báo giá in áo',
        };
    }
}
