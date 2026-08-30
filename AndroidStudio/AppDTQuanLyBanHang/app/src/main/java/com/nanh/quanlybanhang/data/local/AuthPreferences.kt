package com.nanh.quanlybanhang.data.local

import android.content.Context
import android.content.SharedPreferences

class AuthPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLoggedInUserId(): Long? {
        if (!prefs.contains(KEY_USER_ID)) return null
        return prefs.getLong(KEY_USER_ID, -1L).takeIf { it >= 0L }
    }

    fun setLoggedInUserId(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun clearLoggedInUserId() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    companion object {
        private const val PREFS_NAME = "quanlybanhang_auth"
        private const val KEY_USER_ID = "logged_in_user_id"
    }
}
