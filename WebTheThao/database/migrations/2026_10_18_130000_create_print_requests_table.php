<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('print_requests', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->foreignId('conversation_id')->nullable()->constrained('conversations')->nullOnDelete();
            $table->string('garment', 40);
            $table->string('color', 40);
            $table->string('size', 12);
            $table->unsignedInteger('quantity');
            $table->json('placements');
            $table->text('notes')->nullable();
            $table->string('artwork_path');
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
        Schema::dropIfExists('print_requests');
    }
};
