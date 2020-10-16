package com.darekbx.test.dspr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.darekbx.dspr.core.SPR
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_execute.setOnClickListener {

            val assetFileId = resources.getIdentifier("syth", "raw", packageName)
            val assetFile = resources.openRawResource(assetFileId)

            val image = SPR(assetFile.readBytes()).frameAsImage(10, false, false)

            label.setText("SYTH.SPR")
            image_preview.setImageBitmap(image)
        }
    }
}
