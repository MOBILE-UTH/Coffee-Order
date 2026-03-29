package com.coffee.order.util

fun Long.toDisplayTableNumber(): String {
    return toString().padStart(2, '0')
}