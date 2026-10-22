<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('home_banners', function (Blueprint $table) {
            $table->unsignedTinyInteger('position')->default(1)->after('id');
        });

        Schema::table('home_banners', function (Blueprint $table) {
            $table->unique('position');
        });
    }

    public function down(): void
    {
        Schema::table('home_banners', function (Blueprint $table) {
            $table->dropUnique(['position']);
            $table->dropColumn('position');
        });
    }
};
