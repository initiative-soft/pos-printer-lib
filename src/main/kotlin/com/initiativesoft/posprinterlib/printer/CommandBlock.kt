package com.initiativesoft.posprinterlib.printer

import java.io.ByteArrayOutputStream


abstract class CommandBlock {
    var textFormat: TextFormat? = null
    protected val stringBuilder = StringBuilder()
    protected val byteArrayOutputSystem = ByteArrayOutputStream()


    /**
     * Resets properties to their default values
     */
    abstract fun reset()


    /**
     * Adds [text] which will be send to the printer
     */
    abstract fun addText(text: String)

    /**
     * Adds a cut either on current position, or adds extra feed before cutting
     */
    abstract fun addCut(feedBeforeCut: Boolean = false)

    /**
     * Adds an [amount] of line feeds
     */
    abstract fun addLineFeed(amount: Int)


    /**
     * Builds the byte array, which will be send to the printer
     */
    abstract fun buildByteArray(): ByteArray
}