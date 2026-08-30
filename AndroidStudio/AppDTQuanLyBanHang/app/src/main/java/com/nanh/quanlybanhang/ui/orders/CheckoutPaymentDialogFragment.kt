package com.nanh.quanlybanhang.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.DialogCheckoutPaymentBinding
import com.nanh.quanlybanhang.util.formatVndGrouped
import kotlin.math.roundToLong

class CheckoutPaymentDialogFragment : DialogFragment() {

    private var _binding: DialogCheckoutPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var methodViews: List<android.widget.TextView>

    private var selectedMethodIndex: Int = 0
    private var totalDong: Long = 0L
    private var customerPay: Long = 0L

    override fun getTheme(): Int = R.style.Theme_QuanLyBanHang_CheckoutDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCheckoutPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        totalDong = requireArguments().getDouble(ARG_TOTAL_MONEY).roundToLong().coerceAtLeast(0)
        customerPay = totalDong

        methodViews = CheckoutPaymentMethods.methodTextViews(binding)

        binding.textTotalMoney.text = formatVndGrouped(totalDong.toDouble())
        refreshMethodStyles()
        refreshAmountTexts()

        methodViews.forEachIndexed { index, v ->
            v.setOnClickListener {
                selectedMethodIndex = index
                refreshMethodStyles()
            }
        }

        binding.buttonHeaderClose.setOnClickListener { dismiss() }
        binding.buttonFooterClose.setOnClickListener { dismiss() }

        binding.buttonFooterConfirm.setOnClickListener {
            if (customerPay < totalDong) {
                Snackbar.make(binding.root, R.string.checkout_pay_insufficient, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(
                    EXTRA_CONFIRMED to true,
                    EXTRA_METHOD_INDEX to selectedMethodIndex,
                    EXTRA_CUSTOMER_PAY to customerPay,
                    EXTRA_TOTAL to totalDong,
                ),
            )
            dismiss()
        }

        listOf(
            binding.keypad1 to 1,
            binding.keypad2 to 2,
            binding.keypad3 to 3,
            binding.keypad4 to 4,
            binding.keypad5 to 5,
            binding.keypad6 to 6,
            binding.keypad7 to 7,
            binding.keypad8 to 8,
            binding.keypad9 to 9,
            binding.keypad0 to 0,
        ).forEach { (v, d) ->
            v.setOnClickListener { appendDigit(d) }
        }

        binding.keypadClear.setOnClickListener {
            customerPay = 0L
            refreshAmountTexts()
        }

        binding.keypadBackspace.setOnClickListener {
            customerPay /= 10
            refreshAmountTexts()
        }

        binding.keypad00.setOnClickListener { multiplyPay(100L) }
        binding.keypad000.setOnClickListener { multiplyPay(1000L) }

        binding.quick10k.setOnClickListener { addQuick(10_000L) }
        binding.quick20k.setOnClickListener { addQuick(20_000L) }
        binding.quick50k.setOnClickListener { addQuick(50_000L) }
        binding.quick100k.setOnClickListener { addQuick(100_000L) }
        binding.quick200k.setOnClickListener { addQuick(200_000L) }
        binding.quick500k.setOnClickListener { addQuick(500_000L) }
    }

    override fun onStart() {
        super.onStart()
        val dm = resources.displayMetrics
        val w = (dm.widthPixels * 0.94f).toInt().coerceAtMost(dm.widthPixels - 24)
        dialog?.window?.setLayout(w, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refreshMethodStyles() {
        val ctx = requireContext()
        val selectedBg = R.drawable.bg_checkout_method_selected
        val idleBg = R.drawable.bg_checkout_method_outline
        val white = ContextCompat.getColor(ctx, R.color.checkout_surface_white)
        val dark = ContextCompat.getColor(ctx, R.color.checkout_text_primary)

        methodViews.forEachIndexed { i, v ->
            val sel = i == selectedMethodIndex
            v.background = ContextCompat.getDrawable(ctx, if (sel) selectedBg else idleBg)
            v.setTextColor(if (sel) white else dark)
        }
    }

    private fun appendDigit(d: Int) {
        val max = MAX_PAY_DONG
        val next = customerPay * 10L + d
        if (next <= max) {
            customerPay = next
            refreshAmountTexts()
        }
    }

    private fun multiplyPay(factor: Long) {
        customerPay = (customerPay * factor).coerceAtMost(MAX_PAY_DONG)
        refreshAmountTexts()
    }

    private fun addQuick(amount: Long) {
        customerPay = (customerPay + amount).coerceAtMost(MAX_PAY_DONG)
        refreshAmountTexts()
    }

    private fun refreshAmountTexts() {
        binding.textCustomerPay.text = formatVndGrouped(customerPay.toDouble())
        val change = customerPay - totalDong
        binding.textChangeMoney.text = formatVndGrouped(change.coerceAtLeast(0).toDouble())
    }

    companion object {
        const val TAG = "CheckoutPaymentDialog"
        const val REQUEST_KEY = "checkout_payment_result"
        const val EXTRA_CONFIRMED = "confirmed"
        const val EXTRA_METHOD_INDEX = "method_index"
        const val EXTRA_CUSTOMER_PAY = "customer_pay"
        const val EXTRA_TOTAL = "total"

        private const val ARG_TOTAL_MONEY = "total_money"

        private const val MAX_PAY_DONG = 999_999_999_999L

        fun newInstance(totalMoney: Double): CheckoutPaymentDialogFragment =
            CheckoutPaymentDialogFragment().apply {
                arguments = bundleOf(ARG_TOTAL_MONEY to totalMoney)
            }
    }
}
