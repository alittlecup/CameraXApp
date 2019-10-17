package com.hbl.cameraxapp.camera.camerax

import android.graphics.ImageFormat
import android.hardware.Camera
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.hbl.cameraxapp.R
import com.hbl.cameraxapp.databinding.FragmentBaseCameraBinding
import com.hbl.cameraxapp.utils.log
import com.hbl.cameraxapp.utils.toast
import java.lang.Exception

class BaseCameraFragment : Fragment(), android.hardware.Camera.PreviewCallback {


    lateinit var mBinding: FragmentBaseCameraBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding =
            DataBindingUtil.inflate(inflater,
                R.layout.fragment_base_camera, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.viewFinder.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                releaseCamera()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (mCamera == null) {
                    openCamera()
                }
                startPreview()
            }

        })
    }

    fun releaseCamera() {

    }

    private val lensFacing = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
    private var mCamera: android.hardware.Camera? = null
    fun openCamera(lensfacing: Int = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
        var supporCameraFacing = supporCameraFacing(lensfacing)

        if (supporCameraFacing) {
            try {
                mCamera = android.hardware.Camera.open(lensfacing)
                initParameters(mCamera!!)
                mCamera?.setPreviewCallback(this)
            } catch (e: Exception) {
                toast("初始化相机失败")
            }
        }
    }

    fun startPreview() {
        mCamera?.let {
            it.setPreviewDisplay(mBinding.viewFinder.holder)
            setCameraDisplayOrientation()
            it.startPreview()
        }
    }

    override fun onPreviewFrame(data: ByteArray?, camera: android.hardware.Camera?) {
    }

    private fun initParameters(mCamera: android.hardware.Camera) {
        try {

            var parameters = mCamera.parameters
            parameters.previewFormat = ImageFormat.NV21
            var bestSize = getBestSize(
                mBinding.viewFinder.width,
                mBinding.viewFinder.height,
                parameters.supportedPictureSizes
            )
            bestSize?.let {
                parameters.setPreviewSize(it.width, it.height)
            }
            mCamera.parameters = parameters
        } catch (e: Exception) {
            toast("初始化相机失败")
        }
    }

    private fun getBestSize(
        width: Int,
        height: Int,
        supportedPictureSizes: List<android.hardware.Camera.Size>
    ): android.hardware.Camera.Size? {
        var bestSize: android.hardware.Camera.Size? = null
        var targetRatio = width / height.toDouble()
        var minDiff = targetRatio

        supportedPictureSizes.forEach {
            var supportedRatio = (it.height.toDouble() / it.width)
            log("系统支持的尺寸 : ${it.height} * ${it.width} ,    比例$supportedRatio")
        }
        supportedPictureSizes.forEach { size ->
            if (size.width == height && size.height == width) {
                bestSize = size
                return@forEach
            }
            var supportRatio = size.width / size.height.toDouble()
            if (Math.abs(supportRatio - targetRatio) < minDiff) {
                minDiff = Math.abs(supportRatio - targetRatio)
                bestSize = size
            }
        }
        log("目标尺寸 ：$width * $height ，   比例  $targetRatio")
        log("最优尺寸 ：${bestSize?.height} * ${bestSize?.width}")
        return bestSize

    }

    private fun setCameraDisplayOrientation() {
        var backCameraInfo = android.hardware.Camera.CameraInfo()
        android.hardware.Camera.getCameraInfo(lensFacing, backCameraInfo)
        var forntCameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, forntCameraInfo)
        var rotation = activity?.windowManager?.defaultDisplay?.rotation
        var screenDegree = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        var mDisplayOrientation = 0
        if (backCameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mDisplayOrientation = (backCameraInfo.orientation + screenDegree) % 360
            mDisplayOrientation = (360 - mDisplayOrientation) % 360
        } else {
            mDisplayOrientation = (backCameraInfo.orientation - screenDegree + 360) % 360
        }
        mCamera?.setDisplayOrientation(mDisplayOrientation)
        log("屏幕的旋转角度 : $rotation")
        log("后置相机的旋转角度 : ${backCameraInfo.orientation}")
        log("前置相机的旋转角度 : ${forntCameraInfo.orientation}")
        log("setDisplayOrientation(result) : $mDisplayOrientation")
    }

    private fun supporCameraFacing(lensfacing: Int): Boolean {
        var info = android.hardware.Camera.CameraInfo()
        for (i in 0..android.hardware.Camera.getNumberOfCameras()) {
            android.hardware.Camera.getCameraInfo(i, info)
            if (info.facing == lensFacing) return true
        }
        return false
    }

}