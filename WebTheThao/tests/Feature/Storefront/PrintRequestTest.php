<?php

namespace Tests\Feature\Storefront;

use App\Domain\Chat\Enums\ChatAttachmentType;
use App\Domain\Chat\Models\Message;
use App\Domain\CustomPrint\Enums\PrintRequestStatus;
use App\Domain\CustomPrint\Models\PrintRequest;
use App\Domain\Order\Enums\LineType;
use App\Domain\Order\Models\Order;
use App\Domain\User\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use Tests\TestCase;

class PrintRequestTest extends TestCase
{
    use RefreshDatabase;

    public function test_customer_submits_a_print_request_into_the_store_chat(): void
    {
        Storage::fake('public');
        $customer = User::factory()->create();

        $this->actingAs($customer)
            ->post(route('prints.store'), $this->payload([
                'quantity' => 2,
                'placements' => ['front', 'back'],
            ]))
            ->assertRedirect(route('messages.show'));

        $request = PrintRequest::query()->firstOrFail();
        $this->assertSame(460000.0, (float) $request->estimate_amount);
        $this->assertSame(PrintRequestStatus::Submitted, $request->status);
        Storage::disk('public')->assertExists($request->artwork_path);

        $message = Message::query()->firstOrFail();
        $this->assertSame(ChatAttachmentType::Print, $message->attachment_type);
        $this->assertSame($request->id, $message->attachment_id);
        $this->assertSame('460.000đ', $message->attachment_payload['price_label'] ?? null);

        $this->actingAs($customer)
            ->get(route('messages.show'))
            ->assertOk()
            ->assertSee('Yêu cầu in')
            ->assertSee('Ngực trước');
    }

    public function test_store_quotes_in_chat_and_customer_acceptance_places_one_order(): void
    {
        Storage::fake('public');
        $customer = User::factory()->create(['name' => 'Khách in']);
        $staff = User::factory()->staff()->create();
        $this->actingAs($customer)->post(route('prints.store'), $this->payload())->assertRedirect();

        $request = PrintRequest::query()->firstOrFail();
        $conversationId = $request->conversation_id;

        $this->actingAs($customer)
            ->post(route('prints.decide', $request), ['decision' => 'accept'])
            ->assertSessionHasErrors('decision');
        $this->assertSame(0, Order::query()->count());

        $this->actingAs($staff)
            ->post(route('admin.prints.quote', [$conversationId, $request]), [
                'quote_amount' => 250000,
                'note' => 'In trong 3 ngày.',
            ])
            ->assertRedirect(route('admin.messages.show', $conversationId));

        $this->actingAs($staff)
            ->get(route('admin.messages.show', $conversationId))
            ->assertOk()
            ->assertSee('Gửi báo giá')
            ->assertSee('In trong 3 ngày.');

        $this->actingAs($customer)
            ->get(route('messages.show'))
            ->assertOk()
            ->assertSee('Chốt và tạo đơn')
            ->assertSee('250.000đ');

        $this->actingAs($customer)
            ->post(route('prints.decide', $request), ['decision' => 'accept'])
            ->assertRedirect(route('messages.show'));

        $request->refresh();
        $this->assertSame(PrintRequestStatus::Accepted, $request->status);
        $this->assertNotNull($request->order_id);
        $order = Order::query()->with('items')->findOrFail($request->order_id);
        $this->assertSame('250000.00', $order->grand_total);
        $this->assertSame(LineType::Print, $order->items->first()->line_type);
        $this->assertNull($order->items->first()->product_id);

        $this->actingAs($customer)
            ->post(route('prints.decide', $request), ['decision' => 'accept'])
            ->assertSessionHasErrors('decision');
        $this->assertSame(1, Order::query()->count());

        $this->actingAs($customer)
            ->get(route('orders.show', $order))
            ->assertOk()
            ->assertSee('In theo yêu cầu')
            ->assertDontSee('kho đã khóa');
    }

    public function test_declined_request_does_not_place_an_order_and_cannot_be_quoted_again(): void
    {
        Storage::fake('public');
        $customer = User::factory()->create();
        $other = User::factory()->create();
        $staff = User::factory()->staff()->create();
        $this->actingAs($customer)->post(route('prints.store'), $this->payload())->assertRedirect();
        $request = PrintRequest::query()->firstOrFail();

        $this->actingAs($staff)->post(route('admin.prints.quote', [$request->conversation_id, $request]), [
            'quote_amount' => 200000,
        ])->assertRedirect();

        $this->actingAs($other)
            ->post(route('prints.decide', $request), ['decision' => 'decline'])
            ->assertSessionHasErrors('decision');

        $this->actingAs($customer)
            ->post(route('prints.decide', $request), ['decision' => 'decline'])
            ->assertRedirect();

        $this->assertSame(PrintRequestStatus::Declined, $request->refresh()->status);
        $this->assertSame(0, Order::query()->count());

        $this->actingAs($staff)
            ->post(route('admin.prints.quote', [$request->conversation_id, $request]), [
                'quote_amount' => 210000,
            ])
            ->assertSessionHasErrors('quote_amount');
    }

    public function test_chat_cannot_attach_another_customers_print_request(): void
    {
        Storage::fake('public');
        $owner = User::factory()->create();
        $stranger = User::factory()->create();
        $this->actingAs($owner)->post(route('prints.store'), $this->payload())->assertRedirect();
        $request = PrintRequest::query()->firstOrFail();

        $this->actingAs($stranger)
            ->from(route('messages.show'))
            ->post(route('messages.store'), [
                'body' => 'Lấy mẫu này',
                'attachment_type' => ChatAttachmentType::Print->value,
                'attachment_id' => $request->id,
            ])
            ->assertSessionHasErrors('attachment_id');
    }

    /** @param  array<string, mixed>  $overrides */
    private function payload(array $overrides = []): array
    {
        return array_merge([
            'garment' => 'ao-dau',
            'color' => 'navy',
            'size' => 'M',
            'quantity' => 1,
            'placements' => ['front'],
            'notes' => 'Tên ANH số 10',
            'artwork' => UploadedFile::fake()->createWithContent('logo.jpg', base64_decode(
                '/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA='
            )),
        ], $overrides);
    }
}
