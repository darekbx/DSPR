package com.darekbx.dspr.core

import android.graphics.Bitmap
import android.graphics.Color
import com.darekbx.dspr.core.model.Frame

class BTS(fileData: ByteArray) : GameFile(fileData) {

    private var palette = mutableListOf<IntArray>()
    private var frames = mutableListOf<Frame>()

    private var noImages: Int

    init {
        this.noImages = this.shortAt(4)

        extractColorPalette()
        extractFrames()
    }

    private fun extractColorPalette() {
        for (i in 0 until 256) {
            palette.add(
                intArrayOf(
                    byteAt(8 + i * 3 + 0L) * 4 + 3,
                    byteAt(8 + i * 3 + 1L) * 4 + 3,
                    byteAt(8 + i * 3 + 2L) * 4 + 3
                )
            )
        }
    }

    private fun extractFrames() {
        this.frames = ArrayList()

        for (i in 0..10000) {
            this.frames.add(i, Frame())
        }

        for (f in 0 until noImages) {
            var frame = Frame().apply {
                no = f
                start = 776L + 1028 * f
            }
            this.frames.set(this.longAt(frame.start).toInt(), frame)
        }
    }

    fun rgb(id: Int) = this.palette[id]

    fun getPalette() = this.palette

    fun getFrameCount() = this.noImages

    fun frameAsImage(f: Int, doFlip: Boolean): Bitmap {
        var frameStart = 776 + 1028 * f
        val canvas = PixelCanvas(32, 32, doFlip)

        var c = frameStart + 4L
        while (c < (frameStart + 1028)) {
            var x = this.byteAt(c)
            var rgb = this.rgb(x)
            if (rgb[0] != 255 && rgb[1] != 3 && rgb[2] != 255) {
                canvas.addPixel(Color.argb(255, rgb[0], rgb[1], rgb[2]))
            } else {
                canvas.next()
            }
            c++
        }

        return canvas.getImage()
    }
}
