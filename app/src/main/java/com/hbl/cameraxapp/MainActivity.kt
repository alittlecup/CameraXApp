package com.hbl.cameraxapp

import android.content.pm.PackageManager
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Rational
import android.util.Size
import android.util.TimeUtils
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit


private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewFinder = findViewById(R.id.view_find)
        if (allPermissionGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private lateinit var viewFinder: TextureView

    private fun startCamera() {
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(640, 640))
        }.build()
        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener {
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)
            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }


        val captureConfig = ImageCaptureConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
        }.build()

        val imageCapture = ImageCapture(captureConfig)

        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpeg")
            imageCapture.takePicture(file, object : ImageCapture.OnImageSavedListener {
                override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
                    val msg = "Photo capture failed: $message"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.e("CameraXApp", msg)
                    cause?.printStackTrace()
                }

                override fun onImageSaved(file: File) {
                    val msg = "Photo capture succeeded: ${file.absolutePath}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d("CameraXApp", msg)
                }

            })
        }

        val analysisConfig = ImageAnalysisConfig.Builder().apply {
            val analyzedThread = HandlerThread("LuninosityAnalysis").apply { start() }
            setCallbackHandler(Handler(analyzedThread.looper))
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()
        val imageAnalysis = ImageAnalysis(analysisConfig).apply {
            analyzer = LuminosityAnalyzer()
        }



        CameraX.bindToLifecycle(this, preview, imageCapture,imageAnalysis)
    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = viewFinder.width / 2f
        val centetY = viewFinder.height / 2f

        val rorationDegress = when (viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rorationDegress.toFloat(), centerX, centetY)
        viewFinder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionGranted(): Boolean = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private class LuminosityAnalyzer : ImageAnalysis.Analyzer {
        private var lastAnalyzedTimeStamp = 0L
        override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis - lastAnalyzedTimeStamp > TimeUnit.SECONDS.toMillis(1)) {
                val buffer = image!!.planes[0].buffer
                var data = buffer.toByteArray()
                val pixels = data.map { it.toInt() and 0xFF }
                val luma = pixels.average()
                Log.d("CameraXApp", "Average luminosity: $luma")
                lastAnalyzedTimeStamp = currentTimeMillis
            }
        }

    }
}

private fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val data = ByteArray(remaining())
    get(data)
    return data
}
