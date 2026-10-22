<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('size_guides', function (Blueprint $table) {
            $table->id();
            $table->string('name');
            $table->string('group', 20);
            $table->string('brand', 100)->nullable();
            $table->json('measurements');
            $table->boolean('is_active')->default(true);
            $table->timestamps();
        });

        Schema::table('products', function (Blueprint $table) {
            $table->foreignId('size_guide_id')->nullable()->after('image_url')->constrained('size_guides')->nullOnDelete();
        });

        Schema::create('product_color_images', function (Blueprint $table) {
            $table->id();
            $table->foreignId('product_id')->constrained('products')->cascadeOnDelete();
            $table->string('color_key', 100);
            $table->string('color_name', 100);
            $table->string('image_path');
            $table->unsignedInteger('sort_order')->default(0);
            $table->timestamps();
            $table->index(['product_id', 'color_key']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('product_color_images');
        Schema::table('products', function (Blueprint $table) {
            $table->dropConstrainedForeignId('size_guide_id');
        });
        Schema::dropIfExists('size_guides');
    }
};
