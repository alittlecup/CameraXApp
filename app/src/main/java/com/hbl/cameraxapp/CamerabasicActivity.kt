package com.hbl.cameraxapp

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hbl.cameraxapp.utils.FLAGS_FULLSCREEN
import java.io.File

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"
private const val IMMERSIVE_FLAG_TIMEOUT = 500L

class CamerabasicActivity : AppCompatActivity() {
    private lateinit var container: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camerabasic)
        container = findViewById(R.id.fragment_container)
    }

    override fun onResume() {
        super.onResume()
        container.postDelayed(
            { container.systemUiVisibility = FLAGS_FULLSCREEN },
            IMMERSIVE_FLAG_TIMEOUT
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val apply = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA, keyCode) }
                LocalBroadcastManager.getInstance(this).sendBroadcast(apply)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mkdir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mkdir != null && mkdir.exists()) mkdir else appContext.filesDir
        }
    }
}
