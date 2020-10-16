package com.darekbx.dspr.core

import java.lang.IllegalStateException

open class GameFile constructor(val fileData: ByteArray) {

    fun byte(index: Long): Int {
        if (index >= Int.MAX_VALUE) {
            throw IllegalStateException("Index exceeds maximum Int size!")
        }
        return this.fileData.get(index.toInt()).toUByte().toInt()
    }

    fun shortAt(index: Long): Int {
        return this.byte(index) + this.byte(index + 1) * 256
    }

    fun longAt(index: Long): Long {
        return (this.byte(index)
                + this.byte(index + 1) * 256
                + this.byte(index + 2) * Math.pow(256.0, 2.0)
                + this.byte(index + 3) * Math.pow(256.0, 3.0)).toLong()
    }

    fun stringAt(index: Int, len: Int): String {
        throw NotImplementedError("GameFile.stringAt is not implemented")
        //return this.fileData.substring(index, index + len)
    }
}
