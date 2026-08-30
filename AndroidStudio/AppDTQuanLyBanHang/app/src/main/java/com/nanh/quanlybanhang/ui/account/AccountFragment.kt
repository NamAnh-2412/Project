package com.nanh.quanlybanhang.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.FragmentAccountBinding
import com.nanh.quanlybanhang.util.ShopAvatarStorage
import com.nanh.quanlybanhang.util.bindShopAvatarBadge
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountViewModel by viewModels {
        AccountViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private var suppressNameRefresh: Boolean = false

    private val pickAvatar = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@registerForActivityResult
        val ctx = requireContext()
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val ext = ShopAvatarStorage.guessExtension(ctx, uri)
                val path = ShopAvatarStorage.copyFromPickerUri(ctx, uri, ext)
                val previous = viewModel.profile.value?.avatarLocalPath
                viewModel.applyAvatarPath(path, previous)
            }.onFailure {
                Snackbar.make(binding.root, R.string.account_avatar_pick_failed, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonPickAvatar.setOnClickListener {
            pickAvatar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.buttonClearAvatar.setOnClickListener {
            viewModel.clearAvatar()
        }
        binding.buttonSaveShopName.setOnClickListener {
            viewModel.saveShopName(binding.editShopName.text?.toString().orEmpty()) { outcome ->
                when (outcome) {
                    SaveShopNameOutcome.Empty ->
                        Snackbar.make(binding.root, R.string.account_shop_name_empty, Snackbar.LENGTH_SHORT).show()
                    SaveShopNameOutcome.Saved ->
                        Snackbar.make(binding.root, R.string.account_shop_name_saved, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonLogout.setOnClickListener {
            viewModel.logout()
        }

        binding.editShopName.doAfterTextChanged {
            if (suppressNameRefresh) return@doAfterTextChanged
            refreshAvatarPreviewFromFields()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sessionUsername.collect { username ->
                    binding.textSessionUsername.text =
                        username?.let { getString(R.string.account_logged_in_as, it) }.orEmpty()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profile.collect { profile ->
                    val name = profile?.name?.takeIf { it.isNotBlank() }
                        ?: getString(R.string.app_name)
                    bindShopAvatarBadge(
                        binding.imageShopAvatar,
                        binding.textShopInitials,
                        profile?.avatarLocalPath,
                        name,
                        maxSidePx = 512,
                    )
                    val field = binding.editShopName
                    if (!field.isFocused) {
                        suppressNameRefresh = true
                        field.setText(profile?.name.orEmpty())
                        field.setSelection(field.text?.length ?: 0)
                        suppressNameRefresh = false
                    }
                    binding.buttonClearAvatar.isEnabled = !profile?.avatarLocalPath.isNullOrBlank()
                }
            }
        }
    }

    private fun refreshAvatarPreviewFromFields() {
        val typed = binding.editShopName.text?.toString().orEmpty()
        val name = typed.trim().ifEmpty { getString(R.string.app_name) }
        bindShopAvatarBadge(
            binding.imageShopAvatar,
            binding.textShopInitials,
            viewModel.profile.value?.avatarLocalPath,
            name,
            maxSidePx = 512,
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): AccountFragment = AccountFragment()
    }
}
