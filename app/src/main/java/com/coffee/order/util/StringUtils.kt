package com.coffee.order.util

import java.text.DecimalFormat

fun Long.toDisplayTableNumber(): String {
    return toString().padStart(2, '0')
}

fun formatPrice(price: Int): String {
    return DecimalFormat("#,###").format(price.toDouble())
}