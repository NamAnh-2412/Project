<?php

namespace Tests\Feature\Storefront;

use App\Domain\Inventory\Models\InventoryStock;
use App\Domain\Product\Enums\OfferMode;
use App\Domain\Product\Models\Category;
use App\Domain\Product\Models\Product;
use App\Domain\Product\Models\ProductColorImage;
use App\Domain\Product\Models\ProductVariant;
use App\Domain\Product\Models\SizeGuide;
use App\Domain\User\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use Tests\TestCase;

class ProductDetailTest extends TestCase
{
    use RefreshDatabase;

    public function test_product_page_shows_brand_stock_color_photo_and_size_guide(): void
    {
        $category = Category::factory()->create(['name' => 'Áo thi đấu']);
        $guide = SizeGuide::query()->create([
            'name' => 'Áo bóng đá nam',
            'group' => 'ao',
            'brand' => 'VyroTest',
            'is_active' => true,
            'measurements' => [
                'M' => ['min_height_cm' => 165, 'max_height_cm' => 172, 'min_weight_kg' => 58, 'max_weight_kg' => 68],
                'L' => ['min_height_cm' => 173, 'max_height_cm' => 180, 'min_weight_kg' => 69, 'max_weight_kg' => 80],
            ],
        ]);
        $product = Product::factory()->create([
            'name' => 'Áo sân nhà',
            'slug' => 'ao-san-nha',
            'category_id' => $category->id,
            'gender' => 'nam',
            'brand' => 'VyroTest',
            'offer_mode' => OfferMode::Sale,
            'size_guide_id' => $guide->id,
        ]);
        $variant = ProductVariant::factory()->create([
            'product_id' => $product->id,
            'color' => 'Vàng',
            'size' => 'M',
            'sale_price' => 250000,
        ]);
        InventoryStock::factory()->create([
            'product_variant_id' => $variant->id,
            'quantity_on_hand' => 6,
            'quantity_reserved' => 1,
        ]);
        ProductColorImage::query()->create([
            'product_id' => $product->id,
            'color_key' => 'vang',
            'color_name' => 'Vàng',
            'image_path' => 'products/vang-ao.jpg',
            'sort_order' => 1,
        ]);

        $this->get(route('catalog.show', $product->slug))
            ->assertOk()
            ->assertSee('VyroTest')
            ->assertSee('Nam')
            ->assertSee('Áo thi đấu')
            ->assertSee('còn 5')
            ->assertSee('data-color-key="vang"', false)
            ->assertSee('products/vang-ao.jpg', false)
            ->assertSee('Bảng size')
            ->assertSee('165')
            ->assertSee('Gợi ý size');

        $this->getJson(route('catalog.size', ['slug' => $product->slug, 'height_cm' => 168, 'weight_kg' => 62]))
            ->assertOk()
            ->assertJsonPath('size', 'M');

        $this->getJson(route('catalog.size', ['slug' => $product->slug, 'height_cm' => 190, 'weight_kg' => 90]))
            ->assertOk()
            ->assertJsonPath('size', null);
    }

    public function test_owner_can_save_a_size_guide_and_a_color_photo(): void
    {
        Storage::fake('public');
        $admin = User::factory()->admin()->create();
        $category = Category::factory()->create();
        $product = Product::factory()->create([
            'category_id' => $category->id,
            'offer_mode' => OfferMode::Sale,
        ]);
        $variant = ProductVariant::factory()->create([
            'product_id' => $product->id,
            'sku' => 'AO-VANG-M',
            'color' => 'Vàng',
            'size' => 'M',
            'sale_price' => 180000,
        ]);

        $this->actingAs($admin)
            ->get(route('admin.products.edit', $product))
            ->assertOk()
            ->assertSeeInOrder(['2. Phân loại sản phẩm', 'Ảnh phân loại'])
            ->assertDontSee('3. Ảnh theo màu');

        $this->actingAs($admin)->post(route('admin.size-guides.store'), [
            'name' => 'Áo tập',
            'group' => 'ao',
            'brand' => 'VyroTest',
            'is_active' => 1,
            'rows' => [
                ['size' => 'M', 'min_height_cm' => 160, 'max_height_cm' => 170, 'min_weight_kg' => 55, 'max_weight_kg' => 65],
                ['size' => '', 'min_height_cm' => '', 'max_height_cm' => '', 'min_weight_kg' => '', 'max_weight_kg' => ''],
            ],
        ])->assertRedirect(route('admin.size-guides.index'));

        $guide = SizeGuide::query()->firstOrFail();
        $jpeg = base64_decode('/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA=');

        $this->actingAs($admin)->put(route('admin.products.update', $product), [
            'name' => $product->name,
            'category_id' => $category->id,
            'offer_mode' => 'sale',
            'size_guide_id' => $guide->id,
            'variants' => [[
                'id' => $variant->id,
                'sku' => $variant->sku,
                'size' => 'M',
                'color' => 'Vàng',
                'sale_price' => 180000,
                'rental_price_per_day' => '',
                'deposit_amount' => '',
                'image' => UploadedFile::fake()->createWithContent('vang.jpg', $jpeg),
            ]],
        ])->assertRedirect(route('admin.products.edit', $product));

        $product->refresh();
        $this->assertSame($guide->id, $product->size_guide_id);
        $image = $product->colorImages()->first();
        $this->assertNotNull($image);
        $this->assertSame('vang', $image->color_key);
        Storage::disk('public')->assertExists($image->image_path);
    }
}
