package com.initiativesoft.posprinterlib.printer

import com.initiativesoft.posprinterlib.text.TextAlignment
import com.initiativesoft.posprinterlib.text.TextStyle
import java.io.ByteArrayOutputStream

abstract class TextFormat {
    protected val byteArrayOutputStream = ByteArrayOutputStream()

    fun setTextLanguage(language: Int) {

    }

    open fun reset() {

    }

    /**
     * Builds the outputStream into bytes
     */
    open fun buildBytes(): ByteArray {
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * Sets text alignment specified by [textAlignment]
     */
    abstract fun setAlignment(textAlignment: TextAlignment)

    /**
     * Sets the text size specified by [width] and [height]
     */
    abstract fun setSize(width: Int, height: Int)
    /**
     * Sets the [font] type if available
     */
    abstract fun setFont(font: Int)


    /**
     * Sets the [x] and [y] text positions
     */
    abstract fun setPosition(x: Int, y: Int)

    /**
     * Sets the [style] of printed text if available
     */
    abstract fun setStyle(style: TextStyle, state: Boolean)

}