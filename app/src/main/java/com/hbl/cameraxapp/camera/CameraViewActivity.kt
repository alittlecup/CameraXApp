package com.hbl.cameraxapp.camera

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.databinding.DataBindingUtil
import com.hbl.camera.module.Camera1Module
import com.hbl.camera.module.CameraModuleConfig
import com.hbl.camera.module.CameraModuleFactory
import com.hbl.camera.module.CameraXModule
import com.hbl.camera.option.CameraModule
import com.hbl.cameraxapp.R
import com.hbl.cameraxapp.databinding.ActivityCameraViewBinding
import com.hbl.cameraxapp.utils.toast

class CameraViewActivity : AppCompatActivity() {
    val dataBinding by lazy {
        DataBindingUtil.setContentView<ActivityCameraViewBinding>(
            this,
            R.layout.activity_camera_view
        )
    }
    lateinit var cameraModule: com.hbl.camera.module.CameraModule
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding.actiivty = this
        var cameraModuleConfig =
            CameraModuleConfig.Builder().setLensFacing(CameraModule.LensFacing.BACK)
                .setTargetRotation(Surface.ROTATION_90)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetResolution(com.hbl.camera.option.Size(1080, 1920))
                .build()
        cameraModule = CameraModuleFactory.create(this, cameraModuleConfig);
        cameraModule.setOnPreviewOutputListener {
            dataBinding.apply {
                var viewGroup = viewFinder.parent as ViewGroup
                viewGroup.removeView(viewFinder)
                viewGroup.addView(viewFinder, 0)
                viewFinder.surfaceTexture = it.surfaceTexture
            }
        }
        if (cameraModule is Camera1Module) {
            (cameraModule as Camera1Module).setSurfaceTexture(dataBinding.viewFinder.surfaceTexture)
        }
        cameraModule.bindToLifecycle(this)

    }

    @SuppressLint("RestrictedApi")
    fun onCameraClick() {
    }

    var flashMode = false

    fun onFlashClick() {
        flashMode = !flashMode
        dataBinding.imgFlash.setImageResource(if (flashMode) R.drawable.ic_flash_on_black else R.drawable.ic_flash_off_black)
    }

    var ratio: Int = 1
    fun onRatioChange() {
        ratio = when (ratio) {
            1 -> 3
            3 -> 9
            9 -> 16
            else -> 1
        }
        dataBinding.imgRatio.setImageResource(
            when (ratio) {
                1 -> R.drawable.ic_ratio_1_1_black
                3 -> R.drawable.ic_ratio_4_3_black
                9 -> R.drawable.ic_ratio_full_black
                else -> R.drawable.ic_ratio_1_1_black
            }
        )
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        var cameraHeight = getCameraHeight(displayMetrics.widthPixels, ratio)
        if (cameraHeight > displayMetrics.heightPixels) {
            cameraHeight = displayMetrics.heightPixels
        }
        updateCameraViewSize(
            displayMetrics.widthPixels,
            height = cameraHeight
        )

    }

    private fun getCameraHeight(widthPixels: Int, ratio: Int): Int {
        return when (ratio) {
            1 -> widthPixels
            3 -> widthPixels / 3 * 4
            9 -> widthPixels / 9 * 16
            else -> widthPixels
        }
    }

    private fun updateCameraViewSize(width: Int, height: Int) {
        dataBinding.apply {
            var layoutParams = viewFinder.layoutParams
            layoutParams.width = width
            layoutParams.height = height
            viewFinder.layoutParams = layoutParams
        }
    }

    var lensFacing = CameraX.LensFacing.FRONT
    fun switchLenFacing() {
        lensFacing = when (lensFacing) {
            CameraX.LensFacing.FRONT -> CameraX.LensFacing.BACK
            CameraX.LensFacing.BACK -> CameraX.LensFacing.FRONT
        }
        toast(lensFacing.name)
        cameraModule.toggleCamera()
    }
}