package com.darekbx.dspr.core

import com.darekbx.dspr.core.model.Animation
import com.darekbx.dspr.core.model.AnimationFrame

class FIN(fileData: ByteArray) : GameFile(fileData) {

    val animations = mutableMapOf<String, Animation>()

    init {

        val noWeirdBlocks = this.shortAt(2)
        val noAnimations = this.shortAt(4)
        val noUsedSPRs = this.shortAt(6)
        var pos = 8L

        // Blocks 1: Names of used graphics (each = 8 bytes long)
        var usedSPRs = mutableListOf<String>()
        for (u  in 0 until noUsedSPRs) {
            usedSPRs.add(this.stringAt(pos, 8))
            pos += 8
        }

        // Blocks 2: Animations (each = 20 bytes long)
        var fpos = pos + noAnimations * 20 + noWeirdBlocks * 164 // frames (block 4) startposition
        for (a in 0 until noAnimations) {
            var name = this.stringAt(pos, 16).replace("[^\\w\\d]".toRegex(), "")
            var start = this.shortAt(pos + 16L)
            var end = this.shortAt(pos + 16 + 2)
            animations[name] = Animation().also {
                it.start = start
                it.end = end
            }
            for (f in start until (end + 1)) {
                var pwpf = fpos + f * 22 // current frame startposition
                var sprName = this.stringAt(pwpf, 8).replace("[^\\w\\d]".toRegex(), "").toUpperCase()
                animations[name]?.frames?.add(
                    AnimationFrame().also {
                        it.sprName = sprName
                        it.sprFrameNo = byteAt(pwpf + 8)
                        it.b9 = byteAt(pwpf + 9)
                        it.b10 = byteAt(pwpf + 10)
                        it.b11 = byteAt(pwpf + 11)
                        it.b12 = byteAt(pwpf + 12)
                        it.b13 = byteAt(pwpf + 13)
                        it.b14 = byteAt(pwpf + 14)
                        it.b15 = byteAt(pwpf + 15)
                        it.b16 = byteAt(pwpf + 16)
                        it.b17 = byteAt(pwpf + 17)
                        it.isMainLayer = this.shortAt(pwpf + 18)
                        it.isFlipped = this.shortAt(pwpf + 20)
                    }
                )
            }
            //this.frameCount = end + 1;
            pos += 20;
        }

        // Blocks 3: Weird Blocks (each = 164 bytes long)
        //pos += this.noWeirdBlocks * 164;

        // Blocks 4: Animation Frames (each = 22 bytes long)
        /*this.frames = [];
        this.sprLoadQueue = [];
        for (var f = 0; f < this.frameCount; f++) {
            var spr = this.stringAt(pos, 8);
            this.frames[f] = {
                spr: spr,
                sprFrameNo: this.byte(pos + 8),
                isMainLayer: this.shortAt(pos + 18),
                isFlipped: this.shortAt(pos + 20)
            };
            if (!this.sprLoadQueue.includes(spr)) this.sprLoadQueue.push(spr);
            pos += 22;
        }
        console.log(this.frames);*/

    }
}
