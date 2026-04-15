package com.coffee.order.util

import java.text.DecimalFormat

fun Long.toDisplayTableNumber(): String {
    return toString().padStart(2, '0')
}

/**
 * Định dạng giá tiền một cách linh hoạt, hỗ trợ cả Int, Double, Long và giá trị null.
 */
fun formatPrice(price: Number?): String {
    return DecimalFormat("#,###").format(price?.toDouble() ?: 0.0)
}