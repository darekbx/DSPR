package com.darekbx.dspr.core

import android.graphics.Point
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SPRTest {

    @Test
    fun `Load sprite`() {
        javaClass.getResourceAsStream("/CAMM.SPR").use {
            val bytes = it.readBytes()

            val spr = SPR(bytes)
            assertEquals(17, spr.getFrameCount())

            val image = spr.frameAsImage(1, false, false)

            assertEquals(9, image?.width)
            assertEquals(10, image?.height)

            val pixelVerification = mapOf(
                Point(0, 0) to -16777216,
                Point(1, 1) to -15263977,
                Point(2, 2) to -16550025,
                Point(3, 3) to -16550025,
                Point(4, 4) to -16515073,
                Point(5, 5) to -16560305,
                Point(6, 6) to -16570585,
                Point(7, 7) to -16777216,
                Point(8, 8) to -16777216
            )

            pixelVerification.forEach {
                assertEquals(it.value, image?.getPixel(it.key.x, it.key.y))
            }
        }
    }

    @Test
    fun `Load displaced sprite`() {
        javaClass.getResourceAsStream("/CAMM.SPR").use {
            val bytes = it.readBytes()

            val spr = SPR(bytes)
            assertEquals(17, spr.getFrameCount())

            val image = spr.frameAsImage(1, false, true)

            assertEquals(166, image?.width)
            assertEquals(111, image?.height)


        }
    }
}
