package com.nanh.quanlybanhang.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.repo.AuthMessage
import com.nanh.quanlybanhang.data.repo.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    fun login(username: String, password: String, onResult: (AuthMessage) -> Unit) {
        viewModelScope.launch {
            onResult(authRepository.login(username, password))
        }
    }

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(app.authRepository) as T
                }
            }
    }
}
