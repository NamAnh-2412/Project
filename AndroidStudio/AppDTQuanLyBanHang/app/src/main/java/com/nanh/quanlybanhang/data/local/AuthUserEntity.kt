package com.nanh.quanlybanhang.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auth_users",
    indices = [
        Index(value = ["username"], unique = true),
    ],
)
data class AuthUserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHashHex: String,
    val saltHex: String,
    val createdAtEpochMillis: Long,
)
