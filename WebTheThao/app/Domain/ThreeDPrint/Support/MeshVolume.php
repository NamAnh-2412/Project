<?php

namespace App\Domain\ThreeDPrint\Support;

use Illuminate\Validation\ValidationException;

final class MeshVolume
{
    private const MAX_TRIANGLES = 200000;

    /** Thể tích khép kín, đơn vị cm³. */
    public static function stl(string $contents): float
    {
        $trimmed = ltrim($contents);
        if (str_starts_with($trimmed, 'solid') && str_contains($contents, 'facet')) {
            return self::millimetersToCm(self::asciiStl($contents));
        }

        return self::millimetersToCm(self::binaryStl($contents));
    }

    /** Thể tích khép kín, đơn vị cm³. */
    public static function obj(string $contents): float
    {
        $vertices = [];
        $triangles = [];
        foreach (preg_split("/\r\n|\n|\r/", $contents) as $line) {
            $line = trim($line);
            if (str_starts_with($line, 'v ')) {
                $parts = preg_split('/\s+/', $line);
                if (count($parts) < 4) {
                    continue;
                }
                $vertices[] = [(float) $parts[1], (float) $parts[2], (float) $parts[3]];
                if (count($vertices) > self::MAX_TRIANGLES) {
                    self::fail('File OBJ quá lớn để tính thể tích.');
                }
            }
            if (! str_starts_with($line, 'f ') || $vertices === []) {
                continue;
            }
            $indexes = [];
            foreach (array_slice(preg_split('/\s+/', $line), 1) as $token) {
                $index = (int) explode('/', $token)[0];
                if ($index < 0) {
                    $index = count($vertices) + $index + 1;
                }
                if ($index < 1 || $index > count($vertices)) {
                    self::fail('File OBJ có mặt không hợp lệ.');
                }
                $indexes[] = $index - 1;
            }
            if (count($indexes) < 3) {
                continue;
            }
            for ($i = 1; $i < count($indexes) - 1; $i++) {
                $triangles[] = [$vertices[$indexes[0]], $vertices[$indexes[$i]], $vertices[$indexes[$i + 1]]];
                if (count($triangles) > self::MAX_TRIANGLES) {
                    self::fail('File OBJ quá lớn để tính thể tích.');
                }
            }
        }

        if ($triangles === []) {
            self::fail('File OBJ không có mặt để tính thể tích.');
        }

        return self::millimetersToCm(self::signedVolume($triangles));
    }

    public static function box(float $lengthMm, float $widthMm, float $heightMm): float
    {
        return self::millimetersToCm($lengthMm * $widthMm * $heightMm);
    }

    /** @param  list<array{0: array{0: float, 1: float, 2: float}, 1: array{0: float, 1: float, 2: float}, 2: array{0: float, 1: float, 2: float}}>  $triangles */
    private static function signedVolume(array $triangles): float
    {
        $sum = 0.0;
        foreach ($triangles as [$a, $b, $c]) {
            $sum += $a[0] * ($b[1] * $c[2] - $b[2] * $c[1])
                - $a[1] * ($b[0] * $c[2] - $b[2] * $c[0])
                + $a[2] * ($b[0] * $c[1] - $b[1] * $c[0]);
        }

        return abs($sum) / 6.0;
    }

    private static function millimetersToCm(float $mm3): float
    {
        return round($mm3 / 1000, 3);
    }

    private static function binaryStl(string $contents): float
    {
        if (strlen($contents) < 84) {
            self::fail('File STL không đủ dữ liệu.');
        }

        $count = unpack('V', substr($contents, 80, 4))[1];
        if ($count < 1 || $count > self::MAX_TRIANGLES) {
            self::fail('File STL không có khối hợp lệ.');
        }
        if (strlen($contents) < 84 + ($count * 50)) {
            self::fail('File STL bị cắt cụt.');
        }

        $triangles = [];
        $offset = 84;
        for ($i = 0; $i < $count; $i++) {
            $values = unpack('g9', substr($contents, $offset + 12, 36));
            $triangles[] = [
                [$values[1], $values[2], $values[3]],
                [$values[4], $values[5], $values[6]],
                [$values[7], $values[8], $values[9]],
            ];
            $offset += 50;
        }

        return self::signedVolume($triangles);
    }

    private static function asciiStl(string $contents): float
    {
        preg_match_all('/vertex\s+([-+0-9.eE]+)\s+([-+0-9.eE]+)\s+([-+0-9.eE]+)/', $contents, $matches, PREG_SET_ORDER);
        if (count($matches) < 3 || count($matches) % 3 !== 0 || count($matches) / 3 > self::MAX_TRIANGLES) {
            self::fail('File STL chữ không có khối hợp lệ.');
        }

        $triangles = [];
        for ($i = 0; $i < count($matches); $i += 3) {
            $triangles[] = [
                [(float) $matches[$i][1], (float) $matches[$i][2], (float) $matches[$i][3]],
                [(float) $matches[$i + 1][1], (float) $matches[$i + 1][2], (float) $matches[$i + 1][3]],
                [(float) $matches[$i + 2][1], (float) $matches[$i + 2][2], (float) $matches[$i + 2][3]],
            ];
        }

        return self::signedVolume($triangles);
    }

    private static function fail(string $message): never
    {
        throw ValidationException::withMessages([
            'model' => [$message],
        ]);
    }
}
