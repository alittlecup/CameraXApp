package com.hbl.cameraxapp
//
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.util.DisplayMetrics
//import android.util.Log
//import android.util.Rational
//import android.util.Size
//import android.view.Surface
//import android.view.ViewGroup
//import androidx.camera.core.AspectRatio
//import androidx.camera.core.CameraX
//import androidx.camera.core.Preview
//import androidx.camera.core.PreviewConfig
//import androidx.databinding.DataBindingUtil
//import com.hbl.cameraxapp.databinding.ActivityCameraBinding
//
//class CameraActivity : AppCompatActivity() {
//
//    lateinit var mBinding: ActivityCameraBinding
//
//    private var targetRotation = Surface.ROTATION_0
//    private var lenFacing = CameraX.LensFacing.BACK
//    private var targetRatio = AspectRatio.RATIO_4_3
//    private var targetResolution = Size(720, 720)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
//        var displayMetrics = DisplayMetrics().also {
//            windowManager.defaultDisplay.getRealMetrics(it)
//        }
//        updateTextureViewSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
//
//        updateTextureCamera()
//
//        mBinding.btnRotation0.setOnClickListener {
//            targetRotation = getSurfaceRotation(0)
//            updateTextureCamera()
//        }
//        mBinding.btnRotation90.setOnClickListener {
//            targetRotation = getSurfaceRotation(90)
//            updateTextureCamera()
//        }
//        mBinding.btnRotation180.setOnClickListener {
//            targetRotation = getSurfaceRotation(180)
//            updateTextureCamera()
//        }
//        mBinding.btnRotation270.setOnClickListener {
//            targetRotation = getSurfaceRotation(270)
//            updateTextureCamera()
//        }
//
//        mBinding.btnCameraLens.setOnClickListener {
//            lenFacing = changeLenFacing(lenFacing)
//            updateTextureCamera()
//        }
//
//        mBinding.btnRatio11.setOnClickListener {
//            targetRatio = getRatio(1, 1)
//            updateTextureCamera()
//        }
//        mBinding.btnRatio34.setOnClickListener {
//            targetRatio = getRatio(3, 4)
//            updateTextureCamera()
//        }
//        mBinding.btnRatio916.setOnClickListener {
//            targetRatio = getRatio(9, 16)
//            updateTextureCamera()
//        }
//        mBinding.btnRatioFull.setOnClickListener {
//            targetRatio = getRatio(1, 1)
//            updateTextureCamera()
//        }
//
//        mBinding.btn720.setOnClickListener {
//            targetResolution = getSize(720, targetRatio)
//            updateTextureCamera()
//        }
//        mBinding.btn1080.setOnClickListener {
//            targetResolution = getSize(1080, targetRatio)
//            updateTextureCamera()
//        }
//        mBinding.btn2160.setOnClickListener {
//            targetResolution = getSize(2160, targetRatio)
//            updateTextureCamera()
//        }
//        mBinding.btn3024.setOnClickListener {
//            targetResolution = getSize(3024, targetRatio)
//            updateTextureCamera()
//        }
//    }
//
//    private fun getSize(width: Int, targetRatio: Rational): Size {
//        return Size(width, width / targetRatio.numerator * targetRatio.denominator)
//    }
//
//    private fun getRatio(i: Int, i1: Int): Rational = Rational(i, i1)
//
//    private fun changeLenFacing(lenFacing: CameraX.LensFacing): CameraX.LensFacing {
//        return when (lenFacing) {
//            CameraX.LensFacing.FRONT -> CameraX.LensFacing.BACK
//            CameraX.LensFacing.BACK -> CameraX.LensFacing.FRONT
//        }
//    }
//
//
//    fun getSurfaceRotation(rotation: Int): Int {
//        return when (rotation) {
//            0 -> Surface.ROTATION_0
//            90 -> Surface.ROTATION_90
//            180 -> Surface.ROTATION_180
//            270 -> Surface.ROTATION_270
//            else -> Surface.ROTATION_0
//        }
//    }
//
//    fun updateTextureViewSize(width: Int, height: Int) {
//        mBinding.textureView.apply {
//            layoutParams.width = width
//            layoutParams.height = height
//        }
//    }
//
//    fun updateTextureCamera() {
//        setTextInfo()
//        var previewConfig = PreviewConfig.Builder()
//            .setLensFacing(lenFacing)
//            .setTargetAspectRatio(targetRatio)
//            .setTargetRotation(targetRotation)
//            .setTargetResolution(targetResolution)
//            .build()
//        val preview = Preview(previewConfig)
//        preview.setOnPreviewOutputUpdateListener {
//            Log.d("TAG", ": $it")
//            var info = """
//                Size: ${it.textureSize.width} / ${it.textureSize.height}
//                Rotation: ${it.rotationDegrees}
//            """.trimIndent()
//            mBinding.textPreviewInfo.text = info
//            mBinding.textPreviewInfo.visibility
//
//            val parent = mBinding.textureView.parent as ViewGroup
//            parent.removeView(mBinding.textureView)
//            parent.addView(mBinding.textureView, 0)
//
//            mBinding.textureView.surfaceTexture = it.surfaceTexture
//        }
//        CameraX.unbindAll()
//        CameraX.bindToLifecycle(this, preview)
//    }
//
//    private fun setTextInfo() {
//        var info = """
//            lenFacing: ${if (lenFacing == CameraX.LensFacing.FRONT) "FRONT" else "BACK"}
//            Ratio: ${targetRatio.numerator} / ${targetRatio.denominator}
//            Rotation: $targetRotation
//            Resolution: ${targetResolution.width} / ${targetResolution.height}
//        """.trimIndent()
//        mBinding.textSetinfo.text = info
//    }
//}
