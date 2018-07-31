package com.initiativesoft.posprinterlib.vendor.sam4s

import com.initiativesoft.posprinterlib.helper.toByte
import com.initiativesoft.posprinterlib.printer.TextFormat
import com.initiativesoft.posprinterlib.text.TextAlignment
import com.initiativesoft.posprinterlib.text.TextStyle


class TextFormat: TextFormat() {

    /**
     * Byte data to control alignment
     */
    private val alignmentBuffer = ByteArray(3)
    /**
     * Byte data to control size
     */
    private val sizeBuffer = ByteArray(3)
    /**
     * Byte data to control font type
     */
    private val fontBuffer = ByteArray(3)
    /**
     * Byte data to control position
     */
    private val positionBuffer = ByteArray(4)
    /**
     * Byte data to control reverse state
     */
    private val reverseBuffer = ByteArray(3)
    /**
     * Byte data to control underline
     */
    private val underlineBuffer = ByteArray(3)
    /**
     * Byte data to control bold state
     */
    private val boldBuffer = ByteArray(3)
    /**
     * Byte data to control color
     */
    private val colorBuffer = ByteArray(7)



    init {
        reset()
    }

    override fun buildBytes(): ByteArray {
        byteArrayOutputStream.write(alignmentBuffer)
        byteArrayOutputStream.write(sizeBuffer)
        byteArrayOutputStream.write(fontBuffer)
        byteArrayOutputStream.write(positionBuffer)
        byteArrayOutputStream.write(reverseBuffer)
        byteArrayOutputStream.write(underlineBuffer)
        byteArrayOutputStream.write(boldBuffer)
        byteArrayOutputStream.write(colorBuffer)

        return byteArrayOutputStream.toByteArray()
    }

    /**
     * Resets properties to their default values
     */
    override fun reset() {
        alignmentBuffer[0] = 27
        alignmentBuffer[1] = 97
        alignmentBuffer[2] = 0

        fontBuffer[0] = 27
        fontBuffer[1] = 77
        fontBuffer[2] = 0

        sizeBuffer[0] = 29
        sizeBuffer[1] = 33
        sizeBuffer[2] = 0

        positionBuffer[0] = 29
        positionBuffer[1] = 33
        positionBuffer[2] = 0
        positionBuffer[3] = 0

        reverseBuffer[0] = 29
        reverseBuffer[1] = 66
        reverseBuffer[2] = 0

        underlineBuffer[0] = 27
        underlineBuffer[1] = 45
        underlineBuffer[2] = 0

        boldBuffer[0] = 27
        boldBuffer[1] = 69
        boldBuffer[2] = 0

        colorBuffer[0] = 29
        colorBuffer[1] = 40
        colorBuffer[2] = 78
        colorBuffer[3] = 2
        colorBuffer[4] = 0
        colorBuffer[5] = 48
        colorBuffer[6] = 49

        byteArrayOutputStream.reset()
    }

    /**
     * Sets text alignment specified by [textAlignment]
     */
    override fun setAlignment(textAlignment: TextAlignment) {
        alignmentBuffer[2] = textAlignment.ordinal.toByte()
    }

    /**
     * Sets the text size specified by [width] and [height]
     */
    override fun setSize(width: Int, height: Int) {
        sizeBuffer[2] = ((width - 1) * 16 + (height - 1)).toByte()
    }

    /**
     * Sets the [font] type if available
     */
    override fun setFont(font: Int) {
        fontBuffer[2] = font.toByte()
    }

    /**
     * Sets the [x] and [y] text positions
     */
    override fun setPosition(x: Int, y: Int) {
        fontBuffer[2] = (x % 256).toByte()
        fontBuffer[3] = (x / 256).toByte()
    }

    /**
     * Sets the [style] of printed text if available
     */
    override fun setStyle(style: TextStyle, state: Boolean) {
        val stateByteValue = state.toByte()
        val bufferToOperateOn = when(style) {
            TextStyle.Reverse -> reverseBuffer
            TextStyle.Underline -> underlineBuffer
            TextStyle.Bold -> boldBuffer
            else -> byteArrayOf(0, 0, 0)
        }

        bufferToOperateOn[2] = stateByteValue
    }


}