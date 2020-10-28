package com.darekbx.dspr.core

import android.graphics.Bitmap
import android.graphics.Color
import com.darekbx.dspr.core.model.Frame

class SPR(fileData: ByteArray) : GameFile(fileData) {

    private var palette = mutableListOf<IntArray>()
    private var fallbackPalette = mutableListOf<IntArray>()

    private var frames = mutableListOf<Frame>()
    private var team = 7

    private var isCompressed: Boolean
    private var noImages: Int
    private var dataStart: Int

    private var lenCumul = 0L

    init {
        isCompressed = this.byteAt(0) == 129
        noImages = this.shortAt(2)
        dataStart = 776 + (this.noImages * 8)

        val x1 = if (isCompressed) longAt(4) + noImages * 4 else longAt(4)
        val x2 = if (isCompressed) fileData.size - dataStart else fileData.size - 776 + noImages * 4
        assert(x1 == x2.toLong(), { "File integrity check failed!" })

        extractColorPalette()
        extractFrames()
    }

    fun getFrameCount() = noImages

    fun getFrameInfo(f: Int) = this.frames[f]

    fun setFrameInfo(f: Int, info: Frame) {
        this.frames[f] = info
    }

    fun setTeam(team: Int) {
        this.team = team
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
        for (f in 0 until noImages) {
                var infoOffset = 776 + (f * 8L)
                var frame = Frame().apply {
                    width = shortAt(infoOffset)
                    height = shortAt(infoOffset + 2)
                    disX = shortAt(infoOffset + 4)
                    disY = shortAt(infoOffset + 6)
                    start = dataStart + lenCumul
                }

            if (isCompressed) {
                frame.len = longAt(frame.start)
                lenCumul += frame.len + 4
            } else {
                frame.len = (frame.width * frame.height).toLong()
                lenCumul += frame.len
            }

            frames.add(frame)
        }
    }

    private fun rgb(id: Int): IntArray {
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
            ) {
                return this.fallbackPalette[_id]
            }
        }
        return this.palette[id]
    }

    fun frameAsImage(f: Int, doFlip: Boolean, doDisplace: Boolean): Bitmap? {
        var frame = this.frames[f]
        val disX = if (doDisplace) frame.disX else 0
        val disY = if (doDisplace) frame.disY else 0
        val pixelCanvas = PixelCanvas(frame.width, frame.height, doFlip, disX, disY)

        when {
            isCompressed -> drawCompressedFrame(frame, pixelCanvas)
            else -> drawFrame(frame, pixelCanvas)
        }

        return pixelCanvas.getImage()
    }

    private fun drawFrame(frame: Frame, pixelCanvas: PixelCanvas) {
        for (cursor in frame.start until (frame.start + frame.len)) {
            var x = this.byteAt(cursor)
            var rgb = this.rgb(x)
            pixelCanvas.addPixel(Color.argb(255, rgb[0], rgb[1], rgb[2]))
        }
    }

    private fun drawCompressedFrame(frame: Frame, pixelCanvas: PixelCanvas) {
        var cursor = (frame.start + 4)
        while (cursor < (frame.start + frame.len)) {
            var x = this.byteAt(cursor)
            if (x < 128) {
                var noNextRawBytes = x + 1
                for (r in (1..noNextRawBytes)) {
                    var x = this.byteAt(cursor + r)
                    var rgb = this.rgb(x)
                    pixelCanvas.addPixel(Color.argb(255, rgb[0], rgb[1], rgb[2]))
                }
                cursor += (noNextRawBytes + 1)
            } else {
                var noBlackPixels = 256 - x
                for (b in 0 until noBlackPixels) {
                    pixelCanvas.addPixel(Color.BLACK)
                }
                cursor++
            }
        }
    }
}
