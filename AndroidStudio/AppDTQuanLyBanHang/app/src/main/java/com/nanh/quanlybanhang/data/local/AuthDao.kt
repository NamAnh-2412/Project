package com.nanh.quanlybanhang.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthDao {

    @Query("SELECT COUNT(*) FROM auth_users")
    fun observeUserCount(): Flow<Int>

    @Query("SELECT * FROM auth_users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): AuthUserEntity?

    @Query("SELECT COUNT(*) FROM auth_users")
    suspend fun countUsers(): Int

    @Query("SELECT * FROM auth_users WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun getUserByUsername(username: String): AuthUserEntity?

    @Query("SELECT username FROM auth_users WHERE id = :id")
    fun observeUsernameForUser(id: Long): Flow<String?>

    @Insert
    suspend fun insertUser(user: AuthUserEntity): Long
}
