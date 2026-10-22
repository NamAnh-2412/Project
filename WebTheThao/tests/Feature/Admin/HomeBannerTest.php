<?php

namespace Tests\Feature\Admin;

use App\Domain\Content\Models\HomeBanner;
use App\Domain\User\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use Tests\TestCase;

class HomeBannerTest extends TestCase
{
    use RefreshDatabase;

    public function test_admin_can_upload_home_banner(): void
    {
        Storage::fake('public');
        $admin = User::factory()->admin()->create();

        $this->actingAs($admin)->put(route('admin.home-banner.update'), [
            'image' => UploadedFile::fake()->createWithContent('banner.jpg', base64_decode(
                '/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA='
            )),
        ])->assertRedirect();

        $banner = HomeBanner::current();
        $this->assertNotNull($banner);
        $this->assertNotSame('', $banner->image_url);
        Storage::disk('public')->assertExists($banner->image_url);

        $this->get(route('home'))
            ->assertOk()
            ->assertSee(asset('storage/'.$banner->image_url), false);
    }

    public function test_admin_can_reset_home_banner_to_default(): void
    {
        Storage::fake('public');
        $admin = User::factory()->admin()->create();
        $path = UploadedFile::fake()->createWithContent('banner.jpg', base64_decode(
            '/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA='
        ))->store('banners', 'public');
        HomeBanner::query()->create(['image_url' => $path]);

        $this->actingAs($admin)->delete(route('admin.home-banner.destroy'))->assertRedirect();

        $this->assertNull(HomeBanner::current());
        $this->get(route('home'))
            ->assertOk()
            ->assertSee(asset('media/images/home-banner.png'), false);
    }

    public function test_admin_can_store_a_separate_image_for_each_slide(): void
    {
        Storage::fake('public');
        $admin = User::factory()->admin()->create();
        $jpeg = base64_decode(
            '/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA='
        );

        $this->actingAs($admin)->put(route('admin.home-banner.update'), [
            'position' => 4,
            'image' => UploadedFile::fake()->createWithContent('racket.jpg', $jpeg),
        ])->assertRedirect();

        $slide = HomeBanner::slot(4);
        $this->assertNotNull($slide);
        $this->assertSame(4, $slide->position);
        $this->get(route('home'))
            ->assertOk()
            ->assertSee(asset('storage/'.$slide->image_url), false);

        $this->actingAs($admin)->get(route('admin.home-banner.edit'))
            ->assertOk()
            ->assertSee('Slide 1 — Chạy bộ')
            ->assertSee('Slide 5 — Sẵn sàng cho buổi tập');

        $this->actingAs($admin)->delete(route('admin.home-banner.destroy'), ['position' => 4])->assertRedirect();
        $this->assertNull(HomeBanner::slot(4));
    }

    public function test_each_slide_can_save_its_own_title_and_description(): void
    {
        $admin = User::factory()->admin()->create();

        $this->actingAs($admin)->put(route('admin.home-banner.update'), [
            'position' => 2,
            'title' => 'Vào sân thật chắc',
            'description' => 'Giày và áo đấu cho trận hôm nay.',
        ])->assertRedirect();

        $this->get(route('home'))
            ->assertOk()
            ->assertSee('Vào sân thật chắc')
            ->assertSee('Giày và áo đấu cho trận hôm nay.')
            ->assertDontSee('Ra sân với bản lĩnh');
    }

    public function test_each_slide_sport_name_is_typed_and_shown_on_the_banner(): void
    {
        $admin = User::factory()->admin()->create();

        $this->actingAs($admin)->put(route('admin.home-banner.update'), [
            'position' => 1,
            'sport' => 'Điền kinh',
            'title' => 'Bứt phá từng nhịp chạy',
            'description' => 'Trang phục và phụ kiện cho những buổi chạy giàu năng lượng.',
        ])->assertRedirect();

        $this->get(route('home'))
            ->assertOk()
            ->assertSee('01 / Điền kinh')
            ->assertDontSee('01 / Chạy bộ');

        $this->actingAs($admin)->get(route('admin.home-banner.edit'))
            ->assertOk()
            ->assertSee('Slide 1 — Điền kinh');
    }
}
