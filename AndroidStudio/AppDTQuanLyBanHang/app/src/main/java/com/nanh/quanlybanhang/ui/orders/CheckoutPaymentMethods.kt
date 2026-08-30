package com.nanh.quanlybanhang.ui.orders

import android.content.Context
import android.widget.TextView
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.DialogCheckoutPaymentBinding

/**
 * Nguồn duy nhất cho hình thức thanh toán:
 * thứ tự ô trên bảng tính (dialog) = `selectedMethodIndex` lưu DB = nhãn trong báo cáo chi tiết.
 */
object CheckoutPaymentMethods {

    private val ordering: List<Pair<Int, (DialogCheckoutPaymentBinding) -> TextView>> = listOf(
        R.string.checkout_method_cash to { b -> b.checkoutMethodCash },
        R.string.checkout_method_qr to { b -> b.checkoutMethodQr },
        R.string.checkout_method_grab to { b -> b.checkoutMethodGrab },
        R.string.checkout_method_shopee to { b -> b.checkoutMethodShopee },
        R.string.checkout_method_be to { b -> b.checkoutMethodBe },
        R.string.checkout_method_bank to { b -> b.checkoutMethodBank },
        R.string.checkout_method_pos to { b -> b.checkoutMethodPos },
    )

    /** Chuỗi `R.string.checkout_method_*` theo đúng thứ tự ô trên dialog. */
    val labelResIds: List<Int> get() = ordering.map { it.first }

    fun methodTextViews(binding: DialogCheckoutPaymentBinding): List<TextView> =
        ordering.map { it.second(binding) }

    /** `null` hoặc chỉ số lạ = đơn cũ / không xác định. */
    fun label(context: Context, index: Int?): String {
        if (index == null) {
            return context.getString(R.string.reports_payment_unknown)
        }
        val resId = labelResIds.getOrNull(index) ?: return context.getString(R.string.reports_payment_unknown)
        return context.getString(resId)
    }
}
