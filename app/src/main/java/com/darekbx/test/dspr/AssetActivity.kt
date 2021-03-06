package com.darekbx.test.dspr
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.darekbx.dspr.core.BTS
import com.darekbx.dspr.core.FIN
import com.darekbx.dspr.core.MAP
import com.darekbx.dspr.core.SPR
import com.darekbx.dspr.core.model.Animation
import kotlinx.android.synthetic.main.activity_asset.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.timerTask

class AssetActivity : AppCompatActivity() {

    companion object {
        val ANIMATION_TIME = 120L
    }

    private val typesMap = listOf(
        "SPR",
        "BTS",
        "FIN",
        "MAP"
    )

    private var activeAsset: Any? = null
    private var frameCount = 0
    private var animationTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset)

        prepareAssetsSpinner()
        button_execute.setOnClickListener { loadFrame() }
    }

    override fun onDestroy() {
        super.onDestroy()
        animationTimer?.cancel()
        animationTimer = null
    }

    private fun prepareAssetsSpinner() {
        val assets = loadAssets()

        assets_spinner.adapter = ArrayAdapter(this, R.layout.adapter_spinner, assets)
        assets_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
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
        if (assetFileId == 0) {
            Toast.makeText(this, "Asset '$rawName' was not found!", Toast.LENGTH_LONG).show()
            return
        }

        val assetFile = resources.openRawResource(assetFileId)
        val assetType = assetName.split('.').last()
        val assetReadStart = System.currentTimeMillis()

        if (!typesMap.contains(assetType)) {
            throw IllegalStateException("Asset type is not supported!")
        }

        animation_spinner.visibility = View.GONE
        frame_picker.visibility = View.VISIBLE
        button_execute.visibility = View.VISIBLE

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
            assetType == "MAP" -> {
                val assetBtsFileId = resources.getIdentifier("desert", "raw", packageName)
                val assetBtsFile = resources.openRawResource(assetBtsFileId)
                val bts = BTS(assetBtsFile.readBytes())
                val map = MAP(assetFile.readBytes(), bts)
                displayMap(map)
            }
            assetType == "FIN" -> {
                val fin = FIN(assetFile.readBytes())
                animation_spinner.visibility = View.VISIBLE
                frame_picker.visibility = View.GONE
                button_execute.visibility = View.GONE
                loadAnimationSpinner(fin)
            }
        }

        val assetRenderTime = System.currentTimeMillis() - assetReadStart
        label.setText("${assets_spinner.selectedItem}\nread time: ${assetRenderTime}ms")
        frame_picker.hint = "Frame number from 0 to ${frameCount - 1}"
    }

    private fun displayMap(map: MAP) {
        animation_spinner.visibility = View.GONE
        frame_picker.visibility = View.GONE
        button_execute.visibility = View.GONE
        loading_view.visibility = View.VISIBLE

        GlobalScope.launch(Dispatchers.Main) {
            val frameReadStart = System.currentTimeMillis()
            val mapImage = loadMap(map).await()
            val collisionMapImage = loadCollisionMap(map).await()
            val frameRenderTime = System.currentTimeMillis() - frameReadStart
            label.setText("Map rendering time: ${frameRenderTime}ms")

            val combinedBitmap = Bitmap.createBitmap(mapImage.width, mapImage.height, Bitmap.Config.RGB_565)
            val canvas = Canvas(combinedBitmap)
            canvas.drawBitmap(mapImage, Matrix(), null)
            canvas.drawBitmap(collisionMapImage, 0F, 0F, null)

            image_preview.setImageBitmap(combinedBitmap)

            loading_view.visibility = View.GONE
        }
    }

    private fun loadMap(map: MAP): Deferred<Bitmap> {
        return CoroutineScope(Dispatchers.IO).async {
            map.asBitmap()
        }
    }

    private fun loadCollisionMap(map: MAP): Deferred<Bitmap> {
        return CoroutineScope(Dispatchers.IO).async {
            map.getCollisionArea()
        }
    }

    private fun loadFrame() {
        animationTimer?.cancel()

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

    private fun loadAnimationSpinner(fin: FIN) {
        animation_spinner.adapter = ArrayAdapter(
            this,
            R.layout.adapter_spinner,
            fin.animations.keys.toTypedArray()
        )
        animation_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (animation_spinner.selectedItem != null) {
                    val animation = animation_spinner.selectedItem as String
                    fin.animations.get(animation)?.let {
                        loadAnimation(it)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadAnimation(animation: Animation) {
        val rawName = animation.frames.first().sprName.toLowerCase()
        Log.d("DSPR", "Loading asset: $rawName")

        val assetFileId = resources.getIdentifier(rawName, "raw", packageName)
        if (assetFileId == 0) {
            Toast.makeText(this, "Asset '$rawName' was not found!", Toast.LENGTH_LONG).show()
            return
        }

        val assetFile = resources.openRawResource(assetFileId)
        val spr = SPR(assetFile.readBytes())
        var index = 0

        animationTimer?.cancel()
        animationTimer = null

        animationTimer = Timer().apply {
            scheduleAtFixedRate(timerTask {
                val frame = animation.frames.get(index)

                val image = spr.frameAsImage(frame.sprFrameNo, frame.isFlipped == 1, false)
                image_preview.post {
                    image_preview.setImageBitmap(image)
                }

                if (animation.frames.size <= 1) {
                    Log.d("DSPR", "Animation has one frame")
                    animationTimer?.cancel()
                    return@timerTask
                }

                if (++index > animation.frames.size - 1) {
                    index = 0
                }

                Log.d("DSPR", "Animation tick")

            }, 0L, ANIMATION_TIME)
        }
    }
}
