<?php

namespace Tests\Feature\Storefront;

use App\Domain\Chat\Models\Message;
use App\Domain\Inventory\Models\InventoryStock;
use App\Domain\Notification\Models\Notification;
use App\Domain\JerseyStudio\Models\JerseyDesign;
use App\Domain\Order\Enums\OrderStatus;
use App\Domain\Order\Models\Order;
use App\Domain\Payment\Enums\PaymentStatus;
use App\Domain\Payment\Models\Payment;
use App\Domain\Product\Models\Category;
use App\Domain\Product\Models\Product;
use App\Domain\Product\Models\ProductVariant;
use App\Domain\User\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class JerseyStudioTest extends TestCase
{
    use RefreshDatabase;

    public function test_studio_page_matches_the_reference_layout(): void
    {
        $this->get(route('models.create'))
            ->assertOk()
            ->assertSee('Thiết kế áo theo dấu ấn của bạn')
            ->assertSee('Tạo bản phối')
            ->assertSee('Mặt trước')
            ->assertSee('Phóng to')
            ->assertSee('Thu nhỏ')
            ->assertDontSee('Biên tập trên vùng in')
            ->assertDontSee('Tự vẽ họa tiết')
            ->assertSee('data-studio-canvas', false)
            ->assertSee('models/custom-studio/football-jersey.glb', false)
            ->assertDontSee('>In áo<', false);
    }

    public function test_studio_lists_apparel_products_and_saves_that_products_color(): void
    {
        $apparel = Category::factory()->create(['name' => 'Áo thi đấu', 'slug' => 'ao-thi-dau']);
        $gear = Category::factory()->create(['name' => 'Dụng cụ', 'slug' => 'dung-cu-test']);
        $shirt = Product::factory()->create([
            'name' => 'Áo bóng rổ',
            'category_id' => $apparel->id,
            'is_active' => true,
            'is_suspended' => false,
        ]);
        $variant = ProductVariant::factory()->create([
            'product_id' => $shirt->id,
            'sku' => 'AO-RO-VANG',
            'color' => 'Vàng',
            'size' => 'L',
        ]);
        InventoryStock::factory()->create([
            'product_variant_id' => $variant->id,
            'quantity_on_hand' => 4,
        ]);
        $blank = Product::factory()->create([
            'name' => 'Quần phôi',
            'category_id' => $gear->id,
            'is_print_blank' => true,
        ]);
        $blankVariant = ProductVariant::factory()->create([
            'product_id' => $blank->id,
            'sku' => 'QUAN-PHOI-DEN',
            'color' => 'Đen',
            'size' => 'M',
        ]);
        InventoryStock::factory()->create([
            'product_variant_id' => $blankVariant->id,
            'quantity_on_hand' => 3,
        ]);
        $white = Product::factory()->create([
            'name' => 'Áo in',
            'category_id' => $gear->id,
            'is_print_blank' => true,
        ]);
        $whiteVariant = ProductVariant::factory()->create([
            'product_id' => $white->id,
            'sku' => 'AO-TRANG',
            'color' => null,
            'size' => null,
        ]);
        InventoryStock::factory()->create([
            'product_variant_id' => $whiteVariant->id,
            'quantity_on_hand' => 8,
        ]);
        $machine = Product::factory()->create([
            'name' => 'Máy chạy bộ',
            'category_id' => $gear->id,
        ]);
        ProductVariant::factory()->create([
            'product_id' => $machine->id,
            'sku' => 'MAY-DEN',
            'color' => 'Đen',
        ]);

        $this->get(route('models.create'))
            ->assertOk()
            ->assertSee('Áo bóng rổ')
            ->assertSee('Phôi in')
            ->assertSee('Quần phôi')
            ->assertSee('Áo trắng')
            ->assertSee('Áo in')
            ->assertSee('Trắng')
            ->assertSee('Freesize')
            ->assertDontSee('Máy chạy bộ');

        $this->get(route('catalog.index'))
            ->assertOk()
            ->assertSee('Áo bóng rổ')
            ->assertDontSee('Quần phôi');

        $customer = User::factory()->create();
        $this->actingAs($customer)
            ->post(route('studio.save'), [
                'design_payload' => json_encode([
                    'product_id' => $shirt->id,
                    'variant_id' => $variant->id,
                    'color' => 'vang',
                    'layers' => [],
                ], JSON_THROW_ON_ERROR),
                'size' => 'L',
                'quantity' => 1,
            ], ['Accept' => 'application/json'])
            ->assertCreated();

        $design = JerseyDesign::query()->firstOrFail();
        $this->assertSame($shirt->id, $design->design_payload['product_id']);
        $this->assertSame('Áo bóng rổ', $design->design_payload['product_name']);
        $this->assertSame('Vàng', $design->design_payload['color_label']);
        $this->assertSame('#e2b33c', $design->design_payload['color_hex']);
        $this->assertSame($variant->id, $design->design_payload['variant_id']);
        $this->assertSame('L', $design->size);

        $this->actingAs($customer)
            ->post(route('studio.save'), [
                'design_payload' => json_encode([
                    'product_id' => $machine->id,
                    'color' => 'den',
                    'layers' => [],
                ], JSON_THROW_ON_ERROR),
            ], ['Accept' => 'application/json'])
            ->assertStatus(422);

        $this->actingAs($customer)
            ->post(route('studio.save'), [
                'design_payload' => json_encode([
                    'product_id' => $white->id,
                    'variant_id' => $whiteVariant->id,
                    'color' => 'trang',
                    'layers' => [],
                ], JSON_THROW_ON_ERROR),
                'quantity' => 1,
            ], ['Accept' => 'application/json'])
            ->assertCreated();
        $whiteDesign = JerseyDesign::query()->latest('id')->firstOrFail();
        $this->assertSame('Trắng', $whiteDesign->design_payload['color_label']);
        $this->assertSame('Freesize', $whiteDesign->design_payload['size']);
    }

    public function test_customer_saves_and_submits_a_jersey_then_acceptance_places_one_order(): void
    {
        $customer = User::factory()->create();
        $staff = User::factory()->staff()->create();
        $blank = Product::factory()->create([
            'name' => 'Áo phôi bóng rổ',
            'is_print_blank' => true,
        ]);
        $variant = ProductVariant::factory()->create([
            'product_id' => $blank->id,
            'sku' => 'PHOI-VANG-L',
            'color' => 'Vàng',
            'size' => 'L',
            'sale_price' => 180000,
        ]);
        InventoryStock::factory()->create([
            'product_variant_id' => $variant->id,
            'quantity_on_hand' => 5,
        ]);
        $payload = json_encode([
            'product_id' => $blank->id,
            'variant_id' => $variant->id,
            'color' => 'vang',
            'layers' => [[
                'id' => 'text-1',
                'type' => 'text',
                'text' => 'ANH',
                'side' => 'back',
            ]],
        ], JSON_THROW_ON_ERROR);

        $this->actingAs($customer)
            ->post(route('studio.save'), [
                'design_payload' => $payload,
                'size' => 'L',
                'quantity' => 2,
            ], ['Accept' => 'application/json'])
            ->assertCreated()
            ->assertJsonPath('message', 'Đã lưu bản nháp.');

        $design = JerseyDesign::query()->firstOrFail();
        $this->assertSame('ANH', $design->design_payload['layers'][0]['text']);

        $this->actingAs($customer)
            ->post(route('studio.submit', $design), [
                'design_payload' => $payload,
                'size' => 'L',
                'quantity' => 2,
            ], ['Accept' => 'application/json'])
            ->assertOk();

        $this->assertSame('submitted', $design->refresh()->status);
        $this->assertTrue(Message::query()->where('body', 'like', '%bản phối áo 3D #'.$design->id.'%')->exists());

        $this->actingAs($staff)
            ->get(route('admin.studio.index'))
            ->assertOk()
            ->assertSee('Yêu cầu thiết kế')
            ->assertSee('Áo phôi bóng rổ')
            ->assertSee('Chờ báo giá')
            ->assertSee('ANH');

        $this->actingAs($staff)
            ->post(route('admin.studio.quote', [$design->conversation_id, $design]), [
                'quote_amount' => 320000,
            ])
            ->assertRedirect();

        $this->actingAs($customer)
            ->postJson(route('studio.decide', $design), ['decision' => 'accepted'])
            ->assertOk();

        $design->refresh();
        $this->assertSame('accepted', $design->status);
        $order = Order::query()->findOrFail($design->order_id);
        $this->assertSame('320000.00', $order->grand_total);
        $this->assertSame($variant->id, $order->items()->first()->product_variant_id);
        $this->assertSame(3, InventoryStock::query()->where('product_variant_id', $variant->id)->value('quantity_on_hand'));
        $this->assertSame(PaymentStatus::Pending, Payment::query()->where('order_id', $order->id)->first()->status);

        $this->actingAs($staff)
            ->post(route('admin.orders.mark-paid', $order))
            ->assertRedirect()
            ->assertSessionHas('success');
        $this->assertSame(OrderStatus::Paid, $order->fresh()->status);
        $paid = Payment::query()->where('order_id', $order->id)->first();
        $this->assertSame(PaymentStatus::Completed, $paid->status);
        $this->assertSame('320000.00', $paid->amount);
        $this->assertSame(3, InventoryStock::query()->where('product_variant_id', $variant->id)->value('quantity_on_hand'));

        $this->actingAs($customer)
            ->postJson(route('studio.decide', $design), ['decision' => 'accepted'])
            ->assertStatus(422);
        $this->assertSame(1, Order::query()->count());
    }

    public function test_customer_can_discuss_a_quote_and_the_new_price_shows_in_notifications(): void
    {
        $customer = User::factory()->create();
        $staff = User::factory()->staff()->create();
        $blank = Product::factory()->create([
            'name' => 'Áo phôi thảo luận',
            'is_print_blank' => true,
        ]);
        $variant = ProductVariant::factory()->create([
            'product_id' => $blank->id,
            'sku' => 'PHOI-THAO-LUAN',
            'color' => 'Vàng',
            'size' => 'L',
            'sale_price' => 180000,
        ]);
        InventoryStock::factory()->create([
            'product_variant_id' => $variant->id,
            'quantity_on_hand' => 4,
        ]);
        $payload = json_encode([
            'product_id' => $blank->id,
            'variant_id' => $variant->id,
            'color' => 'vang',
            'layers' => [],
        ], JSON_THROW_ON_ERROR);

        $this->actingAs($customer)->post(route('studio.save'), [
            'design_payload' => $payload,
            'size' => 'L',
            'quantity' => 1,
        ], ['Accept' => 'application/json'])->assertCreated();

        $design = JerseyDesign::query()->firstOrFail();
        $this->actingAs($customer)->post(route('studio.submit', $design), [
            'design_payload' => $payload,
            'size' => 'L',
            'quantity' => 1,
        ], ['Accept' => 'application/json'])->assertOk();
        $design->refresh();

        $this->actingAs($staff)->post(route('admin.studio.quote', [$design->conversation_id, $design]), [
            'quote_amount' => 270000,
        ])->assertRedirect();

        $this->actingAs($customer)
            ->get(route('models.create', ['design' => $design->id]))
            ->assertOk()
            ->assertSee('Báo giá: 270.000đ')
            ->assertSee('Thảo luận thêm')
            ->assertSee('has-items', false);

        $this->actingAs($customer)
            ->post(route('studio.discuss', $design))
            ->assertRedirect(route('messages.show'));
        $this->assertSame('quoted', $design->refresh()->status);
        $this->assertTrue(Message::query()->where('body', 'like', '%thảo luận thêm báo giá 270.000đ%')->exists());

        $this->actingAs($staff)->post(route('admin.studio.quote', [$design->conversation_id, $design]), [
            'quote_amount' => 300000,
        ])->assertRedirect();

        $this->actingAs($customer)
            ->get(route('models.create', ['design' => $design->id]))
            ->assertOk()
            ->assertSee('Báo giá: 300.000đ');

        $this->actingAs($customer)
            ->get(route('notifications.index'))
            ->assertOk()
            ->assertSee('270.000đ')
            ->assertSee('300.000đ')
            ->assertSee('Mở bản phối');
        $this->assertSame(0, Notification::query()->where('user_id', $customer->id)->whereNull('read_at')->count());
        $this->assertSame(4, InventoryStock::query()->where('product_variant_id', $variant->id)->value('quantity_on_hand'));
    }
}
