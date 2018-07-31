package com.initiativesoft.posprinterlib.vendor.sam4s

import com.initiativesoft.posprinterlib.printer.CommandBlock
import java.io.ByteArrayOutputStream

class CommandBlock: CommandBlock() {
    private val lineFeedBuffer = ByteArray(3)
    private val cutBuffer = ByteArray(3)
    private val feedUnitBuffer = ByteArray(3)
    /**
     * Just enough to cut under the text
     */
    private var defaultFeedBeforeCutAmount = 100

    init {
        reset()
    }

    /**
     * Resets properties to their default values
     */
    override fun reset() {
        lineFeedBuffer[0] = 27
        lineFeedBuffer[1] = 100
        lineFeedBuffer[2] = 1

        cutBuffer[0] = 29
        cutBuffer[1] = 86
        cutBuffer[2] = 49

        feedUnitBuffer[0] = 27
        feedUnitBuffer[1] = 74
        feedUnitBuffer[2] = 0

        stringBuilder.setLength(0)
        byteArrayOutputSystem.reset()
    }


    /**
     * Adds [text] which will be send to the printer
     */
    override fun addText(text: String) {
        byteArrayOutputSystem.write(byteArrayOf(0, *text.toByteArray(), 0))
    }

    /**
     * Adds a cut either on current position, or adds extra feed before cutting
     */
    override fun addCut(feedBeforeCut: Boolean) {
        if(feedBeforeCut) {
            addFeedUnit(defaultFeedBeforeCutAmount)
        }

        byteArrayOutputSystem.write(cutBuffer)
    }

    /**
     * Adds an [amount] of line feeds
     */
    override fun addLineFeed(amount: Int) {
        lineFeedBuffer[2] = amount.toByte()

        byteArrayOutputSystem.write(lineFeedBuffer)
    }

    /**
     * Adds a feed [unit]
     */
    private fun addFeedUnit(unit: Int) {
        feedUnitBuffer[2] = unit.toByte()

        byteArrayOutputSystem.write(feedUnitBuffer)
    }

    /**
     * Builds the byte array, which will be send to the printer
     */
    override fun buildByteArray(): ByteArray {
        val result = if(textFormat != null) {
            val textFormatBytes = textFormat?.buildBytes() ?: byteArrayOf()
            byteArrayOf(*textFormatBytes, *byteArrayOutputSystem.toByteArray())
        } else {
            byteArrayOutputSystem.toByteArray()
        }

        return result
    }
}