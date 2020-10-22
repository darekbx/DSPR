package com.darekbx.test.dspr
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.darekbx.dspr.core.BTS
import com.darekbx.dspr.core.SPR
import kotlinx.android.synthetic.main.activity_asset.*
import java.lang.IllegalStateException

class AssetActivity : AppCompatActivity() {

    private val typesMap = listOf(
        "SPR",
        "BTS"
    )

    private var activeAsset: Any? = null
    private var frameCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset)

        prepareAssetsSpinner()
        button_execute.setOnClickListener { loadFrame() }
    }

    private fun prepareAssetsSpinner() {
        val assets = loadAssets()

        assets_spinner.adapter = ArrayAdapter(this, R.layout.adapter_spinner, assets)
        assets_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                button_execute.isEnabled = true
                frame_picker.setText("")
                image_preview.setImageBitmap(null)
                loadAsset(assets_spinner.selectedItem as String)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadAssets(): List<String> {
        return R.raw::class.java.fields
            .map {
                val id = resources.getIdentifier(it.name, "raw", packageName)
                val typedValue = TypedValue()
                resources.getValue(id, typedValue, true)
                val pathChunks = typedValue.string.split('/')
                pathChunks.last().toUpperCase()
            }
    }

    private fun loadAsset(assetName: String) {
        var rawName = assetName.toLowerCase()
        typesMap.forEach { rawName = rawName.removeSuffix(".${it.toLowerCase()}") }

        val assetFileId = resources.getIdentifier(rawName, "raw", packageName)
        val assetFile = resources.openRawResource(assetFileId)
        val assetType = assetName.split('.').last()
        val assetReadStart = System.currentTimeMillis()

        if (!typesMap.contains(assetType)) {
            throw IllegalStateException("Asset type is not supported!")
        }

        when {
            assetType == "SPR" -> {
                val spr = SPR(assetFile.readBytes())
                frameCount = spr.getFrameCount()
                activeAsset = spr
            }
            assetType == "BTS" -> {
                val bts = BTS(assetFile.readBytes())
                frameCount = bts.getFrameCount()
                activeAsset = bts
            }
        }

        val assetRenderTime = System.currentTimeMillis() - assetReadStart
        label.setText("${assets_spinner.selectedItem}\nread time: ${assetRenderTime}ms")
        frame_picker.hint = "Frame number from 0 to ${frameCount - 1}"
    }

    private fun loadFrame() {
        frame_picker.text.toString()
            .toIntOrNull()
            ?.takeIf { it >= 0 && it < frameCount }
            ?.let { frame ->
                activeAsset?.let { activeAsset ->
                    val frameReadStart = System.currentTimeMillis()

                    val image = when {
                        activeAsset is SPR -> activeAsset.frameAsImage(frame, false, false)
                        activeAsset is BTS -> activeAsset.frameAsImage(frame, false)
                        else -> throw IllegalStateException("Asset type is not supported!")
                    }
                    val frameRenderTime = System.currentTimeMillis() - frameReadStart

                    label.setText("${assets_spinner.selectedItem}\nframe time: ${frameRenderTime}ms")
                    image_preview.setImageBitmap(image)
                }
            } ?: run { frame_picker.setError("Wrong frame number!") }
    }
}
