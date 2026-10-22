<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('model_print_requests', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->foreignId('conversation_id')->nullable()->constrained('conversations')->nullOnDelete();
            $table->string('original_name');
            $table->string('disk_path');
            $table->string('format', 8);
            $table->string('material', 16);
            $table->string('layer_height', 8);
            $table->unsignedTinyInteger('infill');
            $table->unsignedInteger('quantity');
            $table->decimal('volume_cm3', 10, 3);
            $table->decimal('length_mm', 8, 2)->nullable();
            $table->decimal('width_mm', 8, 2)->nullable();
            $table->decimal('height_mm', 8, 2)->nullable();
            $table->text('notes')->nullable();
            $table->decimal('estimate_amount', 12, 2);
            $table->decimal('quote_amount', 12, 2)->nullable();
            $table->string('status', 24)->default('submitted')->index();
            $table->foreignId('order_id')->nullable()->constrained('orders')->nullOnDelete();
            $table->foreignId('quoted_by')->nullable()->constrained('users')->nullOnDelete();
            $table->timestamp('quoted_at')->nullable();
            $table->timestamp('decided_at')->nullable();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('model_print_requests');
    }
};
