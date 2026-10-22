<?php

namespace Tests\Feature\Storefront;

use App\Domain\Order\Enums\LineType;
use App\Domain\Order\Models\Order;
use App\Domain\ThreeDPrint\Models\ModelPrintRequest;
use App\Domain\ThreeDPrint\Support\MeshVolume;
use App\Domain\ThreeDPrint\Support\PrintEstimator;
use App\Domain\User\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use Tests\TestCase;

class ModelPrintTest extends TestCase
{
    use RefreshDatabase;

    public function test_mesh_volume_and_price_follow_material_layer_and_infill(): void
    {
        $this->assertEqualsWithDelta(0.167, MeshVolume::stl($this->tetrahedronStl()), 0.001);

        $price = PrintEstimator::price('pla', '0.20', 30, 2, 125);
        $this->assertSame(129000, $price);
        $this->assertSame(PrintEstimator::MINIMUM, PrintEstimator::price('pla', '0.28', 15, 1, 0.1));
    }

    public function test_customer_submits_an_stl_and_acceptance_places_one_order(): void
    {
        Storage::fake('local');
        $customer = User::factory()->create();
        $staff = User::factory()->staff()->create();

        $this->actingAs($customer)
            ->post(route('models.store'), [
                'model' => UploadedFile::fake()->createWithContent('khung.stl', $this->tetrahedronStl()),
                'material' => 'pla',
                'layer_height' => '0.20',
                'infill' => 30,
                'quantity' => 1,
                'notes' => 'Giữ lỗ bắt vít',
            ])
            ->assertRedirect(route('messages.show'));

        $request = ModelPrintRequest::query()->firstOrFail();
        $this->assertSame('stl', $request->format);
        $this->assertEquals(0.167, (float) $request->volume_cm3);
        $this->assertSame(
            (float) PrintEstimator::price('pla', '0.20', 30, 1, 0.167),
            (float) $request->estimate_amount,
        );
        Storage::disk('local')->assertExists($request->disk_path);

        $this->actingAs($customer)
            ->get(route('models.download', $request))
            ->assertOk();

        $this->actingAs($staff)
            ->post(route('admin.models.quote', [$request->conversation_id, $request]), [
                'quote_amount' => 90000,
            ])
            ->assertRedirect();

        $this->actingAs($customer)
            ->get(route('messages.show'))
            ->assertOk()
            ->assertSee('Chốt và tạo đơn')
            ->assertSee('khung.stl');

        $this->actingAs($customer)
            ->post(route('models.decide', $request), ['decision' => 'accept'])
            ->assertRedirect(route('messages.show'));

        $request->refresh();
        $order = Order::query()->with('items')->findOrFail($request->order_id);
        $this->assertSame('90000.00', $order->grand_total);
        $this->assertSame(LineType::Print, $order->items->first()->line_type);
        $this->assertNull($order->items->first()->product_id);

        $this->actingAs($customer)
            ->post(route('models.decide', $request), ['decision' => 'accept'])
            ->assertSessionHasErrors('decision');
        $this->assertSame(1, Order::query()->count());
    }

    public function test_step_price_uses_the_bounding_box_and_rejects_a_script(): void
    {
        Storage::fake('local');
        $customer = User::factory()->create();
        $stranger = User::factory()->create();

        $this->actingAs($customer)
            ->post(route('models.preview'), [
                'model' => UploadedFile::fake()->createWithContent('hop.step', "ISO-10303-21;\nHEADER;\nENDSEC;\nEND-ISO-10303-21;\n"),
                'material' => 'pla',
                'layer_height' => '0.20',
                'infill' => 30,
                'quantity' => 2,
                'length_mm' => 50,
                'width_mm' => 50,
                'height_mm' => 50,
            ])
            ->assertOk()
            ->assertJsonPath('volume_cm3', 125)
            ->assertJsonPath('estimate', 129000);

        $this->actingAs($customer)
            ->post(route('models.store'), [
                'model' => UploadedFile::fake()->createWithContent('shell.stl', "<?php echo 'no';"),
                'material' => 'pla',
                'layer_height' => '0.20',
                'infill' => 30,
                'quantity' => 1,
            ])
            ->assertSessionHasErrors('model');

        $this->actingAs($customer)->post(route('models.store'), [
            'model' => UploadedFile::fake()->createWithContent('hop.step', "ISO-10303-21;\nHEADER;\nENDSEC;\nEND-ISO-10303-21;\n"),
            'material' => 'petg',
            'layer_height' => '0.12',
            'infill' => 50,
            'quantity' => 1,
            'length_mm' => 40,
            'width_mm' => 20,
            'height_mm' => 10,
        ])->assertRedirect();

        $request = ModelPrintRequest::query()->firstOrFail();
        $this->actingAs($stranger)
            ->get(route('models.download', $request))
            ->assertForbidden();
    }

    private function tetrahedronStl(): string
    {
        return <<<'STL'
solid tetra
facet normal 0 0 0
 outer loop
  vertex 0 0 0
  vertex 0 10 0
  vertex 10 0 0
 endloop
endfacet
facet normal 0 0 0
 outer loop
  vertex 0 0 0
  vertex 10 0 0
  vertex 0 0 10
 endloop
endfacet
facet normal 0 0 0
 outer loop
  vertex 0 0 0
  vertex 0 0 10
  vertex 0 10 0
 endloop
endfacet
facet normal 0 0 0
 outer loop
  vertex 10 0 0
  vertex 0 10 0
  vertex 0 0 10
 endloop
endfacet
endsolid tetra
STL;
    }
}
