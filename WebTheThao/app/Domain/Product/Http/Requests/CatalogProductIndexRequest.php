<?php

namespace App\Domain\Product\Http\Requests;

use App\Domain\Product\Enums\OfferMode;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class CatalogProductIndexRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'q' => ['nullable', 'string', 'max:255'],
            'gender' => ['nullable', 'string', Rule::in(['nam', 'nu', 'unisex', 'all'])],
            'brand' => ['nullable', 'string', 'max:100'],
            'sport_id' => ['nullable', 'integer'],
            'category_id' => ['nullable', 'integer'],
            'offer_mode' => ['nullable', Rule::in(array_column(OfferMode::cases(), 'value'))],
            'size' => ['nullable', 'string', 'max:30'],
            'color' => ['nullable', 'string', 'max:50'],
            'min_price' => ['nullable', 'numeric', 'min:0'],
            'max_price' => ['nullable', 'numeric', 'min:0', 'gte:min_price'],
            'in_stock' => ['nullable', 'boolean'],
            'sort' => ['nullable', Rule::in(['name_asc', 'newest', 'price_asc', 'price_desc'])],
            'page' => ['nullable', 'integer', 'min:1'],
            'limit' => ['nullable', 'integer', 'min:1', 'max:50'],
        ];
    }
}
