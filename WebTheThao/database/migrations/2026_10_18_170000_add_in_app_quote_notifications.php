<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('notifications', function (Blueprint $table) {
            $table->timestamp('read_at')->nullable();
        });

        if (Schema::getConnection()->getDriverName() !== 'mysql') {
            return;
        }

        DB::statement("ALTER TABLE notifications MODIFY COLUMN type ENUM('order_confirmed', 'rental_due_reminder', 'rental_overdue', 'quote_updated') NOT NULL");
        DB::statement("ALTER TABLE notifications MODIFY COLUMN channel ENUM('email', 'in_app') NOT NULL DEFAULT 'email'");
    }

    public function down(): void
    {
        Schema::table('notifications', function (Blueprint $table) {
            $table->dropColumn('read_at');
        });

        if (Schema::getConnection()->getDriverName() !== 'mysql') {
            return;
        }

        DB::statement("ALTER TABLE notifications MODIFY COLUMN type ENUM('order_confirmed', 'rental_due_reminder', 'rental_overdue') NOT NULL");
        DB::statement("ALTER TABLE notifications MODIFY COLUMN channel ENUM('email') NOT NULL DEFAULT 'email'");
    }
};
