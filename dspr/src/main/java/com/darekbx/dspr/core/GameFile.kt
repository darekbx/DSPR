package com.darekbx.dspr.core

import java.lang.IllegalStateException

open class GameFile constructor(val fileData: ByteArray) {

    fun byteAt(index: Long): Int {
        if (index >= Int.MAX_VALUE) {
            throw IllegalStateException("Index exceeds maximum Int size!")
        }
        return this.fileData.get(index.toInt()).toUByte().toInt()
    }

    fun shortAt(index: Long): Int {
        return this.byteAt(index) + this.byteAt(index + 1) * 256
    }

    fun longAt(index: Long): Long {
        return (this.byteAt(index)
                + this.byteAt(index + 1) * 256
                + this.byteAt(index + 2) * Math.pow(256.0, 2.0)
                + this.byteAt(index + 3) * Math.pow(256.0, 3.0)).toLong()
    }

    fun stringAt(index: Long, len: Int): String {
        val destination = ByteArray(len)
        this.fileData.copyInto(destination, 0, index.toInt(), index.toInt() + len)
        return String(destination)
    }
}
