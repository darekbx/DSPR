package com.darekbx.dspr.core

import android.graphics.Bitmap

class PixelCanvas (
    var width: Int,
    var height: Int,
    val doFlip: Boolean = false,
    var disX: Int = 0,
    var disY: Int = 0) {

    var curX = 0
    var curY = 0

    var canvas: Bitmap

    init {
        if (disX > 0) {
            curX = disX
            width = width + disX
        }
        if (disY > 0) {
            curY = disY
            height = height + disY
        }
        canvas = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    }

    fun addPixel(color: Int) {
        canvas.setPixel(curX, curY, color)
        next()
    }

    fun getImage() = canvas

    fun next() {
        curX++
        if (curX >= width) {
            curX = if (disX > 0) disX else 0
            curY++
        }
    }
}
