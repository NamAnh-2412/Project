package com.nanh.quanlybanhang.ui.auth

import androidx.annotation.StringRes
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.data.repo.AuthMessage

@StringRes
fun AuthMessage.toErrorStringRes(): Int? = when (this) {
    AuthMessage.OK -> null
    AuthMessage.USERNAME_TOO_SHORT -> R.string.auth_err_username_short
    AuthMessage.PASSWORD_TOO_SHORT -> R.string.auth_err_password_short
    AuthMessage.PASSWORD_MISMATCH -> R.string.auth_err_password_mismatch
    AuthMessage.DUPLICATE_USERNAME -> R.string.auth_err_duplicate_username
    AuthMessage.BAD_CREDENTIALS -> R.string.auth_err_bad_credentials
    AuthMessage.REGISTER_ALREADY_EXISTS -> R.string.auth_err_register_closed
}
