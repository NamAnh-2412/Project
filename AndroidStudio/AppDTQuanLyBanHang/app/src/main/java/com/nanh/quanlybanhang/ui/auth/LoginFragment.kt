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
import com.nanh.quanlybanhang.data.repo.AuthMessage
import com.nanh.quanlybanhang.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }

        binding.buttonLogin.setOnClickListener {
            val u = binding.editUsername.text?.toString().orEmpty()
            val p = binding.editPassword.text?.toString().orEmpty()
            viewModel.login(u, p) { showAuthResult(it) }
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
        fun newInstance(): LoginFragment = LoginFragment()
    }
}
