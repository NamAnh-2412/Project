package com.nanh.quanlybanhang.data.repo

import android.database.sqlite.SQLiteConstraintException
import com.nanh.quanlybanhang.data.local.AuthDao
import com.nanh.quanlybanhang.data.local.AuthPreferences
import com.nanh.quanlybanhang.data.local.AuthUserEntity
import com.nanh.quanlybanhang.util.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

enum class AuthDestination {
    REGISTER,
    LOGIN,
    MAIN,
}

enum class AuthMessage {
    OK,
    USERNAME_TOO_SHORT,
    PASSWORD_TOO_SHORT,
    PASSWORD_MISMATCH,
    DUPLICATE_USERNAME,
    BAD_CREDENTIALS,
    REGISTER_ALREADY_EXISTS,
}

class AuthRepository(
    private val authDao: AuthDao,
    private val prefs: AuthPreferences,
    appScope: CoroutineScope,
) {

    private val sessionUserId = MutableStateFlow(prefs.getLoggedInUserId())

    val destination: StateFlow<AuthDestination> = combine(
        authDao.observeUserCount(),
        sessionUserId,
    ) { count, sid ->
        when {
            count == 0 -> AuthDestination.REGISTER
            sid == null -> AuthDestination.LOGIN
            else -> AuthDestination.MAIN
        }
    }
        .distinctUntilChanged()
        .stateIn(appScope, SharingStarted.Eagerly, AuthDestination.LOGIN)

    val sessionUsername: Flow<String?> = sessionUserId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else authDao.observeUsernameForUser(id)
    }

    suspend fun validateSession(): AuthMessage {
        val id = sessionUserId.value ?: return AuthMessage.OK
        if (authDao.getUserById(id) == null) {
            logout()
            return AuthMessage.BAD_CREDENTIALS
        }
        return AuthMessage.OK
    }

    fun logout() {
        prefs.clearLoggedInUserId()
        sessionUserId.value = null
    }

    suspend fun login(username: String, password: String): AuthMessage {
        val name = username.trim()
        if (name.length < USERNAME_MIN) return AuthMessage.USERNAME_TOO_SHORT
        if (password.length < PASSWORD_MIN) return AuthMessage.PASSWORD_TOO_SHORT
        val user = authDao.getUserByUsername(name) ?: return AuthMessage.BAD_CREDENTIALS
        val salt = PasswordHasher.fromHex(user.saltHex)
        val hash = PasswordHasher.hashPassword(password, salt)
        val stored = PasswordHasher.fromHex(user.passwordHashHex)
        if (!PasswordHasher.hashesEqual(hash, stored)) return AuthMessage.BAD_CREDENTIALS
        prefs.setLoggedInUserId(user.id)
        sessionUserId.value = user.id
        return AuthMessage.OK
    }

    suspend fun register(username: String, password: String, passwordConfirm: String): AuthMessage {
        if (authDao.countUsers() > 0) return AuthMessage.REGISTER_ALREADY_EXISTS
        val name = username.trim()
        if (name.length < USERNAME_MIN) return AuthMessage.USERNAME_TOO_SHORT
        if (password.length < PASSWORD_MIN) return AuthMessage.PASSWORD_TOO_SHORT
        if (password != passwordConfirm) return AuthMessage.PASSWORD_MISMATCH
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hashPassword(password, salt)
        return try {
            val row = AuthUserEntity(
                username = name,
                passwordHashHex = PasswordHasher.toHex(hash),
                saltHex = PasswordHasher.toHex(salt),
                createdAtEpochMillis = System.currentTimeMillis(),
            )
            val id = authDao.insertUser(row)
            prefs.setLoggedInUserId(id)
            sessionUserId.value = id
            AuthMessage.OK
        } catch (_: SQLiteConstraintException) {
            AuthMessage.DUPLICATE_USERNAME
        }
    }

    companion object {
        const val USERNAME_MIN = 3
        const val PASSWORD_MIN = 6
    }
}
