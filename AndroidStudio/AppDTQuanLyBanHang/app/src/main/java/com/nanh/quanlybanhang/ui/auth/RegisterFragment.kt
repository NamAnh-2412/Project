package com.nanh.quanlybanhang.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.databinding.FragmentRegisterBinding
import com.nanh.quanlybanhang.data.repo.AuthMessage

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }

        binding.buttonRegister.setOnClickListener {
            val u = binding.editUsername.text?.toString().orEmpty()
            val p = binding.editPassword.text?.toString().orEmpty()
            val c = binding.editPasswordConfirm.text?.toString().orEmpty()
            viewModel.register(u, p, c) { showAuthResult(it) }
        }
    }

    private fun showAuthResult(message: AuthMessage) {
        val res = message.toErrorStringRes()
        if (res != null) {
            Snackbar.make(binding.root, res, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): RegisterFragment = RegisterFragment()
    }
}
