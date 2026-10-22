<?php

namespace App\Domain\Content\Models;

use Illuminate\Database\Eloquent\Model;

class HomeBanner extends Model
{
    public const SLOT_COUNT = 5;

    /** @var array<int, string> */
    public const SLOT_LABELS = [
        1 => 'Slide 1 — Chạy bộ',
        2 => 'Slide 2 — Bóng đá',
        3 => 'Slide 3 — Tập gym',
        4 => 'Slide 4 — Cầu lông và tennis',
        5 => 'Slide 5 — Sẵn sàng cho buổi tập',
    ];

    /** @var array<int, array{tone: string, sport: string, title: string, text: string, meta: string, alt: string}> */
    public const SLIDE_COPY = [
        1 => ['tone' => 'run', 'sport' => 'Chạy bộ', 'title' => 'Bứt phá từng nhịp chạy', 'text' => 'Trang phục và phụ kiện cho những buổi chạy giàu năng lượng.', 'meta' => 'RUN WITH PURPOSE', 'alt' => 'Banner trang chủ'],
        2 => ['tone' => 'pitch', 'sport' => 'Bóng đá', 'title' => 'Ra sân với bản lĩnh', 'text' => 'Chuẩn bị cho trận đấu với lựa chọn phù hợp để thi đấu tự tin.', 'meta' => 'OWN THE PITCH', 'alt' => 'Banner bóng đá'],
        3 => ['tone' => 'gym', 'sport' => 'Tập gym', 'title' => 'Mạnh hơn sau mỗi buổi tập', 'text' => 'Đồ tập thoải mái, bền bỉ và sẵn sàng theo nhịp vận động của bạn.', 'meta' => 'BUILD YOUR EDGE', 'alt' => 'Banner tập gym'],
        4 => ['tone' => 'racket', 'sport' => 'Cầu lông và tennis', 'title' => 'Đánh nhịp chính xác', 'text' => 'Khám phá lựa chọn cho cầu lông, tennis và những cuộc chơi tốc độ.', 'meta' => 'PLAY WITH PRECISION', 'alt' => 'Banner cầu lông và tennis'],
        5 => ['tone' => 'studio', 'sport' => 'Sẵn sàng cho buổi tập', 'title' => 'Sẵn sàng cho buổi tập', 'text' => 'Chọn mua đồ mới hoặc thuê món đang có tại cửa hàng.', 'meta' => 'BUY OR RENT', 'alt' => 'Banner sẵn sàng cho buổi tập'],
    ];

    protected $fillable = [
        'position',
        'image_url',
        'sport',
        'title',
        'description',
    ];

    protected function casts(): array
    {
        return [
            'position' => 'integer',
        ];
    }

    public static function current(): ?self
    {
        return static::slot(1);
    }

    public static function slot(int $position): ?self
    {
        return static::query()->where('position', $position)->first();
    }

    public static function displayUrl(): string
    {
        return static::slideUrls()[0];
    }

    /** @return list<string|null> */
    public static function slideUrls(): array
    {
        $rows = static::query()->get()->keyBy('position');
        $urls = [];

        for ($position = 1; $position <= self::SLOT_COUNT; $position++) {
            $row = $rows->get($position);
            if ($row !== null && $row->image_url !== '') {
                $urls[] = $row->resolvedUrl();
                continue;
            }

            $urls[] = $position === 1 ? asset('media/images/home-banner.png') : null;
        }

        return $urls;
    }

    /** @return list<array{tone: string, image: ?string, alt: string, eyebrow: string, sport: string, title: string, text: string, meta: string}> */
    public static function slides(): array
    {
        $rows = static::query()->get()->keyBy('position');
        $urls = static::slideUrls();
        $slides = [];

        foreach (self::SLIDE_COPY as $position => $copy) {
            $row = $rows->get($position);
            $sport = filled($row?->sport) ? $row->sport : $copy['sport'];
            $slides[] = [
                'tone' => $copy['tone'],
                'image' => $urls[$position - 1],
                'alt' => $copy['alt'],
                'sport' => $sport,
                'eyebrow' => str_pad((string) $position, 2, '0', STR_PAD_LEFT).' / '.$sport,
                'title' => filled($row?->title) ? $row->title : $copy['title'],
                'text' => filled($row?->description) ? $row->description : $copy['text'],
                'meta' => $copy['meta'],
            ];
        }

        return $slides;
    }

    public function resolvedUrl(): string
    {
        $url = (string) $this->image_url;

        if (str_starts_with($url, 'http://') || str_starts_with($url, 'https://')) {
            return $url;
        }

        if (str_starts_with($url, '/')) {
            return asset(ltrim($url, '/'));
        }

        return asset('storage/'.$url);
    }
}
