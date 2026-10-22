<?php

namespace App\Storefront\Http\Controllers;

use App\Domain\Content\Models\HomeBanner;
use App\Domain\Product\Http\Requests\CatalogProductIndexRequest;
use App\Domain\Product\Services\CatalogService;
use App\Domain\Product\Services\SizeFinder;
use App\Domain\Review\Services\ReviewWriter;
use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class CatalogPageController extends Controller
{
    public function __construct(
        private CatalogService $catalog,
        private ReviewWriter $reviews,
        private SizeFinder $sizes,
    ) {}

    public function home(): View
    {
        return view('storefront.home', [
            'sports' => $this->catalog->sports(),
            'categories' => $this->catalog->categories(),
            'products' => $this->catalog->products(['limit' => 8]),
            'bestSellers' => $this->catalog->bestSellers(8),
            'heroSlides' => HomeBanner::slides(),
        ]);
    }

    public function index(CatalogProductIndexRequest $request): View
    {
        $filters = $request->validated();

        return view('storefront.catalog', [
            'sports' => $this->catalog->sports(),
            'categories' => $this->catalog->categories(),
            'brands' => $this->catalog->brands(),
            'availableSizes' => $this->catalog->availableSizes(),
            'availableColors' => $this->catalog->availableColors(),
            'products' => $this->catalog->products($filters),
            'filters' => $filters,
        ]);
    }

    public function show(Request $request, string $slug): View
    {
        $product = $this->catalog->productBySlug($slug);
        $product->load(['colorImages', 'sizeGuide', 'variants.stock']);

        $reviews = $this->reviews->listByProduct($product->id);
        $requestedMode = $request->query('offer_mode');
        $requestedMode = in_array($requestedMode, ['sale', 'rental'], true) ? $requestedMode : null;

        return view('storefront.product', [
            'product' => $product,
            'reviews' => $reviews,
            'reviewAvg' => round((float) $reviews->avg('rating'), 1),
            'requestedMode' => $requestedMode,
        ]);
    }

    public function sizeHint(Request $request, string $slug): JsonResponse
    {
        $data = $request->validate([
            'height_cm' => ['required', 'numeric', 'min:50', 'max:250'],
            'weight_kg' => ['required', 'numeric', 'min:10', 'max:250'],
        ]);
        $product = $this->catalog->productBySlug($slug);
        $product->load('sizeGuide');

        return response()->json($this->sizes->recommend($product, (float) $data['height_cm'], (float) $data['weight_kg']));
    }
}
