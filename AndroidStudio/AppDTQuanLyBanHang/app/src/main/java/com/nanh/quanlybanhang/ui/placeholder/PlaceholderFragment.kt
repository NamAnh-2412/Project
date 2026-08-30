package com.nanh.quanlybanhang.ui.placeholder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.FragmentPlaceholderBinding

class PlaceholderFragment : Fragment() {

    private var _binding: FragmentPlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val title = requireArguments().getString(ARG_TITLE).orEmpty()
        binding.textPlaceholderMessage.text =
            getString(R.string.placeholder_tab, title)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "title"

        fun newInstance(screenTitle: String): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = bundleOf(ARG_TITLE to screenTitle)
            }
        }
    }
}
