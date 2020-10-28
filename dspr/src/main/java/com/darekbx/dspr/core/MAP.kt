package com.darekbx.dspr.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class MAP(fileData: ByteArray, val bts: BTS) : GameFile(fileData) {

    private var tileCache = mutableMapOf<String, Bitmap>()

    var width = 0L
    var height = 0L

    init {
        this.width = this.longAt(0)
        this.height = this.longAt(4)
    }

    fun getCollisionArea(): Bitmap {
        val paint = Paint().apply {
            color = Color.BLACK
            alpha = 100
        }
        val canvasWidth = width.toInt() * 32
        val canvasHeight = height.toInt() * 32
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_4444)
        val oContext = Canvas(bitmap)

        var noTiles = this.width * this.height
        var curX = 0
        var curY = 0

        for (t in 0 until noTiles) {
            var fpos = 8 + noTiles * 4 + t * 2
            var flagByte = this.byteAt(fpos)
            var x1 = (flagByte shr 1) and 1
            var x2 = (flagByte shr 2) and 1
            var blockViewAhead = (flagByte shr 7) and 1
            var x8 = (flagByte shr 8) and 1

            if (x1 == 1 || x2 == 1 || x8 == 1 || blockViewAhead != 1) {
                val x = curX * 32F
                val y = curY * 32F
                oContext.drawRect(x, y, x + 32F, y + 32F, paint)
            }

            curX++
            if (curX >= this.width) {
                curX = 0
                curY++
            }
        }

        return bitmap
    }

    fun asBitmap(): Bitmap {
        val bitmap = createBitmap()
        val oContext = Canvas(bitmap)
        var noTiles = this.width * this.height
        var curX = 0
        var curY = 0

        tileCache.clear()

        for (t in 0 until noTiles) {
            var mpos = 8 + t * 4
            var mainTile = this.shortAt(mpos)
            var topTile = this.shortAt(mpos + 2)
            var fpos = 8 + noTiles * 4 + t * 2
            var flagByte = this.byteAt(fpos)
            var doFlipMain = (flagByte shr 5) and 1
            var doFlipTop = (flagByte shr 6) and 1

            drawImage(mainTile, doFlipMain, oContext, curX, curY)

            if (topTile > 0) {false
                drawImage(topTile, doFlipTop, oContext, curX, curY)
            }

            curX++
            if (curX >= this.width) {
                curX = 0
                curY++
            }
        }

        return bitmap
    }

    private fun drawImage(fid: Int, doFlip: Int, oContext: Canvas, curX: Int, curY: Int) {
        val cacheMainTileKey = "$fid${doFlip == 1}"
        tileCache.get(cacheMainTileKey)?.let {
            oContext.drawBitmap(it, curX * 32F, curY * 32F, null)
        } ?: run {
            val oMainTile = this.bts.frameAsImageAtFID(fid, doFlip == 1)
            if (oMainTile != null) {
                oContext.drawBitmap(oMainTile, curX * 32F, curY * 32F, null)
                tileCache.put(cacheMainTileKey, oMainTile)
            }
        }
    }

    private fun createBitmap(): Bitmap {
        val canvasWidth = width.toInt() * 32
        val canvasHeight = height.toInt() * 32
        return Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_4444)
    }
}
