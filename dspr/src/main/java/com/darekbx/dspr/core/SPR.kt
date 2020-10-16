package com.darekbx.dspr.core

import android.graphics.Bitmap
import android.graphics.Color
import com.darekbx.dspr.core.model.Frame

class SPR(fileData: ByteArray) : GameFile(fileData) {

    var palette = mutableListOf<IntArray>()
    var fallbackPalette = mutableListOf<IntArray>()

    var frames = mutableListOf<Frame>()
    var dataset = {}
    val scale = 1
    val team = 7

    var isCompressed: Boolean
    var noImages: Int
    var dataStart: Int

    var lenCumul = 0L

    init {
        isCompressed = this.byte(0) == 129
        noImages = this.shortAt(2)
        dataStart = 776 + (this.noImages * 8)

        val x1 = if (isCompressed) longAt(4) + noImages * 4 else longAt(4)
        val x2 = if (isCompressed) fileData.size - dataStart else fileData.size - 776 + noImages * 4
        assert(x1 == x2.toLong(), { "File integrity check failed!" })

        for (i in 0 until 256) {
            this.palette.add(
                intArrayOf(
                    this.byte(8 + i * 3 + 0L) * 4 + 3,
                    this.byte(8 + i * 3 + 1L) * 4 + 3,
                    this.byte(8 + i * 3 + 2L) * 4 + 3
                )
            )
        }

        for (f in 0 until noImages) {

            var frame = Frame()

            var infoOffset = 776 + (f * 8L)
            frame.width = this.shortAt(infoOffset)
            frame.height = this.shortAt(infoOffset + 2)
            frame.disX = this.shortAt(infoOffset + 4)
            frame.disY = this.shortAt(infoOffset + 6)

            frame.start = this.dataStart + this.lenCumul

            if (this.isCompressed) {
                frame.len = this.longAt(frame.start)
                this.lenCumul += frame.len + 4
            } else {
                frame.len = (frame.width * frame.height).toLong()
                this.lenCumul += frame.len
            }

            this.frames.add(frame)
        }
    }

    fun getFrameCount(): Int {
        return noImages
    }

    fun getFrameInfo(f: Int): Frame {
        return this.frames[f]
    }

   fun setFrameInfo(f: Int, info: Frame) {
        this.frames[f] = info
    }

    fun rgb(id: Int): IntArray {
        // red(0)		-7*6	Pan Luma
        // blue(1)		-6*6	Stratus
        // yellow(2)	-5*6	Taar
        // purple(3)	-4*6	Taar Council
        // green(4)		-3*6	Unknown
        // orange(5)	-2*6	Roswell Taar (Enemy Drones)
        // peach(6)		-1*6	Unknown
        // cyan(7)		-0*6	Aerogen

        if (listOf(138, 139, 140, 141, 142, 143).contains(id)) {
            var _id = id + (this.team - 7) * 6

            if ( // fallback for missing Stratus color in some sprites
                this.team != 7 &&
                this.palette[138][0] == 3 && this.palette[138][1] == 255 && this.palette[138][2] == 255 &&
                this.fallbackPalette.isNotEmpty()
            )
                return this.fallbackPalette[_id]
        }
        return this.palette[id.toInt()]
    }

    fun frameAsImage(f: Int, doFlip: Boolean, doDisplace: Boolean): Bitmap? {

        if (this.frames[f] == null) return null
        var frame = this.frames[f]

        // TODO: doFlip

        var width = frame.width
        var height = frame.height
        var curX = 0
        var curY = 0

        if (doDisplace) {
            curX = frame.disX
            curY = frame.disY
            width = frame.width + frame.disX
            height = frame.height + frame.disY
        }

        val canvas = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        if (isCompressed) {
            var c = (frame.start+4)
            while (c < (frame.start + frame.len)) {
                var x = this.byte(c)
                if (x < 128) {
                    var noNextRawBytes = x + 1

                    for (r in (1..noNextRawBytes)) {
                        var x = this.byte(c+r)
                        var rgb = this.rgb(x)

                        canvas.setPixel(curX, curY, Color.argb(255, rgb[0], rgb[1], rgb[2]))

                        curX++
                        if (curX >= width) {
                            curX = if (doDisplace) frame.disX else 0
                            curY++
                        }
                    }
                    c += (noNextRawBytes+1)
                } else {
                    var noBlackPixels = 256 - x
                    for (b in 0 until noBlackPixels) {
                        canvas.setPixel(curX, curY, Color.BLACK)

                        curX++
                        if (curX >= width) {
                            curX = if (doDisplace) frame.disX else 0
                            curY++
                        }
                    }
                    c++
                }
            }
        }
        else {
            for (c in frame.start until (frame.start + frame.len)) {
                var x = this.byte(c)
                var rgb = this.rgb(x)
                canvas.setPixel(curX, curY, Color.argb(255, rgb[0], rgb[1], rgb[2]))

                curX++
                if (curX >= width) {
                    curX = if (doDisplace) frame.disX else 0
                    curY++
                }
            }
        }

        return canvas
    }
}
