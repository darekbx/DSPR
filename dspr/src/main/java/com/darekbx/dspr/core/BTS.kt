package com.darekbx.dspr.core

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
        for (f in 0 until noImages) {
            var frame = Frame().apply {
                no = f
                start = 776L + 1028 * f
            }
            this.frames[this.longAt(frame.start).toInt()] = frame
        }
    }

    fun rgb(id: Int) = this.palette[id]

    fun getPalette() = this.palette

    fun getFrameCount() = this.noImages
}
