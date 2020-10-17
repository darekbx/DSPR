package com.darekbx.dspr.core

import android.graphics.Bitmap
import com.darekbx.dspr.core.model.Frame

class PixelCanvas (val frame: Frame, val doFlip: Boolean, val doDisplace: Boolean) {

    var width = 0
    var curX = 0
    var curY = 0

    var canvas: Bitmap

    init {

        width = frame.width
        var height = frame.height

        if (doDisplace) {
            curX = frame.disX
            curY = frame.disY
            width = frame.width + frame.disX
            height = frame.height + frame.disY
        }

        canvas = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    }

    fun addPixel(color: Int) {
        canvas.setPixel(curX, curY, color)
        next()
    }

    fun getImage() = canvas

    private fun next() {
        curX++
        if (curX >= width) {
            curX = if (doDisplace) frame.disX else 0
            curY++
        }
    }
}
