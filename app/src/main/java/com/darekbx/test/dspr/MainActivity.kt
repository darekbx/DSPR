package com.darekbx.test.dspr
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.darekbx.dspr.core.SPR
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var sprite: SPR? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prepareSpriesSpinner()
        button_execute.setOnClickListener {
            sprite?.let { loadFrame(it) }
        }
    }

    private fun prepareSpriesSpinner() {
        val sprites = loadSprites()

        sprite_spinner.adapter = ArrayAdapter(this, R.layout.adapter_spinner, sprites)
        sprite_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                button_execute.isEnabled = true
                frame_picker.setText("")
                image_preview.setImageBitmap(null)
                loadSprite(sprite_spinner.selectedItem as String)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadSprites(): List<String> {
        val sprites = R.raw::class.java.fields.map {
            "${it.name.toUpperCase()}.SPR"
        }
        return sprites
    }

    private fun loadSprite(spriteName: String) {
        val rawName = spriteName.toLowerCase().removeSuffix(".spr")
        val assetFileId = resources.getIdentifier(rawName, "raw", packageName)
        val assetFile = resources.openRawResource(assetFileId)

        val spriteReadStart = System.currentTimeMillis()
        sprite = SPR(assetFile.readBytes())
        val spriteRenderTime = System.currentTimeMillis() - spriteReadStart
        frame_picker.hint = "Frame number from 0 to ${sprite?.getFrameCount() ?: 0 - 1}"
        label.setText("${sprite_spinner.selectedItem}\nread time: ${spriteRenderTime}ms")
    }

    private fun loadFrame(sprite: SPR) {
        frame_picker.text.toString().toIntOrNull()
            ?.takeIf { it >= 0 && it < sprite.getFrameCount() }
            ?.let { frame ->
                val frameReadStart = System.currentTimeMillis()
                val image = sprite.frameAsImage(frame, false, false)
                val frameRenderTime = System.currentTimeMillis() - frameReadStart

                label.setText("${sprite_spinner.selectedItem}\nframe time: ${frameRenderTime}ms")
                image_preview.setImageBitmap(image)
            } ?: run { frame_picker.setError("Wrong frame number!") }
    }
}
