<?php

namespace Tests\Feature\Admin;

use App\Domain\Order\Enums\LineType;
use App\Domain\Order\Enums\OrderChannel;
use App\Domain\Order\Enums\OrderStatus;
use App\Domain\Order\Models\Order;
use App\Domain\Order\Models\OrderItem;
use App\Domain\Shipping\Enums\FulfillmentStatus;
use App\Domain\Shipping\Enums\ShippingMethod;
use App\Domain\User\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class OrderHandoverTest extends TestCase
{
    use RefreshDatabase;

    public function test_pickup_sale_is_handed_over_once_and_completed(): void
    {
        $staff = User::factory()->staff()->create();
        $customer = User::factory()->create(['name' => 'Khach lay hang']);
        $order = $this->order($customer, OrderChannel::Online, ShippingMethod::Pickup, OrderStatus::Paid, LineType::Sale);

        $this->actingAs($customer)
            ->get(route('orders.show', $order))
            ->assertOk()
            ->assertSee('Đưa mã này khi đến lấy.')
            ->assertSee('#'.$order->id);

        $this->actingAs($staff)
            ->get(route('admin.orders.lookup', ['code' => $order->id]))
            ->assertOk()
            ->assertSee('Khach lay hang')
            ->assertSee('Đã giao');

        $this->actingAs($staff)
            ->post(route('admin.orders.handover', $order))
            ->assertRedirect(route('admin.orders.lookup', ['code' => $order->id]));

        $order->refresh();
        $this->assertNotNull($order->handed_over_at);
        $this->assertSame(OrderStatus::Completed, $order->status);

        $this->actingAs($staff)
            ->from(route('admin.orders.lookup', ['code' => $order->id]))
            ->post(route('admin.orders.handover', $order))
            ->assertRedirect(route('admin.orders.lookup', ['code' => $order->id]))
            ->assertSessionHasErrors('code');
    }

    public function test_rental_pickup_records_handover_without_completing_the_order(): void
    {
        $staff = User::factory()->staff()->create();
        $order = $this->order(User::factory()->create(), OrderChannel::Online, ShippingMethod::Pickup, OrderStatus::Paid, LineType::Rental);

        $this->actingAs($staff)
            ->post(route('admin.orders.handover', $order))
            ->assertRedirect(route('admin.orders.lookup', ['code' => $order->id]));

        $order->refresh();
        $this->assertNotNull($order->handed_over_at);
        $this->assertSame(OrderStatus::Paid, $order->status);
    }

    public function test_ghn_and_pos_orders_are_not_handed_over_at_the_counter(): void
    {
        $staff = User::factory()->staff()->create();
        $delivery = $this->order(User::factory()->create(), OrderChannel::Online, ShippingMethod::Delivery, OrderStatus::Paid, LineType::Sale);
        $delivery->update([
            'ghn_order_code' => 'GHN123',
            'fulfillment_status' => FulfillmentStatus::ReadyToPick,
        ]);
        $pos = $this->order(User::factory()->create(), OrderChannel::Pos, ShippingMethod::Pickup, OrderStatus::Paid, LineType::Sale);

        $this->actingAs($staff)
            ->get(route('admin.orders.lookup', ['code' => $delivery->id]))
            ->assertOk()
            ->assertSee('Đơn giao qua GHN, không giao tại quầy.')
            ->assertSee('GHN123')
            ->assertDontSee('>Đã giao<', false);

        $this->actingAs($staff)
            ->from(route('admin.orders.lookup', ['code' => $delivery->id]))
            ->post(route('admin.orders.handover', $delivery))
            ->assertSessionHasErrors('code');

        $this->assertNull($delivery->fresh()->handed_over_at);
        $this->assertSame(FulfillmentStatus::ReadyToPick, $delivery->fresh()->fulfillment_status);

        $this->actingAs($staff)
            ->get(route('admin.orders.lookup', ['code' => $pos->id]))
            ->assertOk()
            ->assertSee('Đây là đơn bán tại quầy. Không soát ở trang này.');

        $this->actingAs($staff)
            ->get(route('admin.orders.bill', $delivery))
            ->assertNotFound();

        $this->actingAs(User::factory()->create())
            ->get(route('admin.orders.lookup'))
            ->assertForbidden();
    }

    public function test_customer_delivery_order_shows_ghn_instruction(): void
    {
        $customer = User::factory()->create();
        $order = $this->order($customer, OrderChannel::Online, ShippingMethod::Delivery, OrderStatus::Paid, LineType::Sale);
        $order->update(['ghn_order_code' => 'GHN999']);

        $this->actingAs($customer)
            ->get(route('orders.show', $order))
            ->assertOk()
            ->assertSee('Đơn giao qua GHN.')
            ->assertSee('GHN999')
            ->assertDontSee('Đưa mã này khi đến lấy.');
    }

    private function order(User $customer, OrderChannel $channel, ShippingMethod $shipping, OrderStatus $status, LineType $line): Order
    {
        $order = Order::factory()->create([
            'user_id' => $customer->id,
            'channel' => $channel,
            'shipping_method' => $shipping,
            'status' => $status,
            'shipping_name' => $customer->name,
            'shipping_phone' => $customer->phone,
            'grand_total' => 100000,
            'merchandise_total' => $line === LineType::Sale ? 100000 : 0,
            'rental_total' => $line === LineType::Rental ? 100000 : 0,
        ]);
        OrderItem::factory()->create([
            'order_id' => $order->id,
            'line_type' => $line,
            'product_name' => $line === LineType::Sale ? 'Ao lay' : 'Vot lay',
            'line_total' => 100000,
            'rental_start' => $line === LineType::Rental ? now()->toDateString() : null,
            'rental_end' => $line === LineType::Rental ? now()->addDay()->toDateString() : null,
        ]);

        return $order;
    }
}
