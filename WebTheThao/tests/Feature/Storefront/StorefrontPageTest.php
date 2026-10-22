<?php

namespace Tests\Feature\Storefront;

use App\Domain\Inventory\Models\InventoryStock;
use App\Domain\Product\Enums\OfferMode;
use App\Domain\Product\Models\Category;
use App\Domain\Product\Models\Product;
use App\Domain\Product\Models\ProductVariant;
use App\Domain\Product\Models\Sport;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class StorefrontPageTest extends TestCase
{
    use RefreshDatabase;

    public function test_home_and_policy_pages_render(): void
    {
        $this->get('/')
            ->assertOk()
            ->assertSee('WebTheThao')
            ->assertSee('data-hero-carousel', false)
            ->assertSee('Bứt phá từng nhịp chạy')
            ->assertSee('Slide tiếp theo');
        $this->get('/chinh-sach/thue')->assertOk()->assertSee('Chính sách thuê');
        $this->get('/chinh-sach/doi-tra')->assertOk()->assertSee('Đổi trả');
        $this->get('/chinh-sach/coc')->assertOk()->assertSee('Tiền cọc');
    }

    public function test_catalog_and_product_detail_show_buy_rent_toggle(): void
    {
        $sport = Sport::factory()->create(['name' => 'Tennis', 'slug' => 'tennis-storefront']);
        $category = Category::factory()->create(['name' => 'Dụng cụ', 'slug' => 'dung-cu-storefront']);
        $product = Product::factory()->create([
            'name' => 'Vợt storefront test',
            'slug' => 'vot-storefront-test',
            'sport_id' => $sport->id,
            'category_id' => $category->id,
            'offer_mode' => OfferMode::Both,
        ]);
        ProductVariant::factory()->create([
            'product_id' => $product->id,
            'sku' => 'SF-VOT-1',
            'sale_price' => 1200000,
            'rental_price_per_day' => 90000,
            'deposit_amount' => 400000,
        ]);

        $this->get('/san-pham')
            ->assertOk()
            ->assertSee('Sản phẩm')
            ->assertSee('Vợt storefront test');

        $this->get('/san-pham?offer_mode=rental')
            ->assertOk()
            ->assertSee('Thuê từ');

        $this->get('/san-pham/vot-storefront-test')
            ->assertOk()
            ->assertSee('Vợt storefront test')
            ->assertSee('Bán & thuê')
            ->assertSee('Mua')
            ->assertSee('Thuê')
            ->assertSee('Thêm mua')
            ->assertSee('Số lượng mua')
            ->assertSee('value="sale"', false);

        $this->get('/san-pham/vot-storefront-test?offer_mode=rental')
            ->assertOk()
            ->assertSee('Thêm thuê')
            ->assertSee('Số lượng thuê')
            ->assertSee('value="rental"', false);
    }

    public function test_catalog_filters_gender_brand_size_stock_and_price(): void
    {
        $sport = Sport::factory()->create(['name' => 'Bóng đá lọc']);
        $category = Category::factory()->create(['name' => 'Áo lọc']);
        $nam = Product::factory()->create([
            'name' => 'Áo nam lọc',
            'sport_id' => $sport->id,
            'category_id' => $category->id,
            'gender' => 'nam',
            'brand' => 'VyroTest',
            'offer_mode' => OfferMode::Sale,
        ]);
        $namVariant = ProductVariant::factory()->create([
            'product_id' => $nam->id,
            'size' => 'L',
            'color' => 'Đỏ',
            'sale_price' => 200000,
        ]);
        InventoryStock::factory()->create([
            'product_variant_id' => $namVariant->id,
            'quantity_on_hand' => 4,
            'quantity_reserved' => 0,
        ]);
        $nu = Product::factory()->create([
            'name' => 'Áo nữ lọc',
            'gender' => 'nu',
            'brand' => 'KhacTest',
            'offer_mode' => OfferMode::Sale,
        ]);
        ProductVariant::factory()->create([
            'product_id' => $nu->id,
            'size' => 'S',
            'color' => 'Hồng',
            'sale_price' => 900000,
        ]);
        $uni = Product::factory()->create([
            'name' => 'Áo unisex lọc',
            'gender' => 'unisex',
            'brand' => 'VyroTest',
            'offer_mode' => OfferMode::Sale,
        ]);
        ProductVariant::factory()->create([
            'product_id' => $uni->id,
            'size' => 'M',
            'sale_price' => 100000,
        ]);

        $this->get('/san-pham')
            ->assertOk()
            ->assertSee('Bộ lọc sản phẩm')
            ->assertSee('Tất cả thương hiệu')
            ->assertSee('VyroTest')
            ->assertSee('Chỉ còn hàng')
            ->assertSee('Áo nam lọc');

        $this->get('/san-pham?gender=nam')
            ->assertOk()
            ->assertSee('Áo nam lọc')
            ->assertSee('Áo unisex lọc')
            ->assertDontSee('Áo nữ lọc');

        $this->get('/san-pham?brand=VyroTest&size=L&in_stock=1')
            ->assertOk()
            ->assertSee('Áo nam lọc')
            ->assertDontSee('Áo unisex lọc')
            ->assertDontSee('Áo nữ lọc');

        $this->get('/san-pham?gender=nam&sort=price_asc')
            ->assertOk()
            ->assertSeeInOrder(['Áo unisex lọc', 'Áo nam lọc']);
    }

    public function test_guest_can_add_sale_line_to_cart_and_checkout_requires_login(): void
    {
        $product = Product::factory()->create([
            'name' => 'Giày giỏ test',
            'slug' => 'giay-gio-test',
            'offer_mode' => OfferMode::Sale,
        ]);
        $variant = ProductVariant::factory()->create([
            'product_id' => $product->id,
            'sku' => 'CART-SALE-1',
            'sale_price' => 500000,
            'rental_price_per_day' => null,
        ]);
        InventoryStock::factory()->create([
            'product_variant_id' => $variant->id,
            'quantity_on_hand' => 8,
            'quantity_reserved' => 0,
        ]);

        $this->from(route('catalog.show', $product->slug))
            ->post('/gio-hang', [
                'line_type' => 'sale',
                'product_variant_id' => $variant->id,
                'quantity' => 1,
            ])
            ->assertRedirect(route('catalog.show', $product->slug))
            ->assertSessionHas('status', 'Đã thêm vào giỏ hàng.');

        $this->get('/gio-hang')
            ->assertOk()
            ->assertSee('Giỏ mua')
            ->assertDontSee('Giỏ hỗn hợp')
            ->assertSee('Giày giỏ test');

        $this->get('/thanh-toan')->assertRedirect(route('login'));
        $this->get('/dang-nhap')->assertOk()->assertSee('Đăng nhập');
        $this->get('/dang-ky')->assertOk()->assertSee('Đăng ký tài khoản khách');
    }
}
