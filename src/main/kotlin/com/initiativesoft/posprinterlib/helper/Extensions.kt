package com.initiativesoft.posprinterlib.helper

/**
 * An extension function to transform a [Boolean] into an [Int]
 */
fun Boolean.toByte() = if (this) (1).toByte() else 0