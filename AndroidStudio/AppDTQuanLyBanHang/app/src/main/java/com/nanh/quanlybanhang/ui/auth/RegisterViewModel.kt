package com.nanh.quanlybanhang.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.repo.AuthMessage
import com.nanh.quanlybanhang.data.repo.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    fun register(username: String, password: String, confirm: String, onResult: (AuthMessage) -> Unit) {
        viewModelScope.launch {
            onResult(authRepository.register(username, password, confirm))
        }
    }

    companion object {
        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RegisterViewModel(app.authRepository) as T
                }
            }
    }
}
