package com.nanh.quanlybanhang.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.data.local.ShopProfileEntity
import com.nanh.quanlybanhang.data.repo.AuthRepository
import com.nanh.quanlybanhang.data.repo.ShopProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: ShopProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val profile: StateFlow<ShopProfileEntity?> = repository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sessionUsername: StateFlow<String?> = authRepository.sessionUsername
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun logout() {
        authRepository.logout()
    }

    fun saveShopName(name: String, onOutcome: (SaveShopNameOutcome) -> Unit) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            onOutcome(SaveShopNameOutcome.Empty)
            return
        }
        viewModelScope.launch {
            repository.updateShopName(trimmed)
            onOutcome(SaveShopNameOutcome.Saved)
        }
    }

    fun applyAvatarPath(newPath: String, previousPath: String?) {
        viewModelScope.launch {
            repository.replaceAvatarKeepingOldCleanup(previousPath, newPath)
        }
    }

    fun clearAvatar() {
        viewModelScope.launch {
            repository.clearAvatar()
        }
    }

    companion object {
        fun factory(app: QuanLyBanHangApp) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AccountViewModel(app.shopProfileRepository, app.authRepository) as T
        }
    }
}

enum class SaveShopNameOutcome {
    Empty,
    Saved,
}
