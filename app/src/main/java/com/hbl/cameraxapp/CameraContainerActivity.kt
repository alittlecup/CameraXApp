package com.hbl.cameraxapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hbl.cameraxapp.databinding.ActivityCameraContainerBinding

class CameraContainerActivity : AppCompatActivity() {
    lateinit var mBinding: ActivityCameraContainerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera_container)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, BaseCameraFragment()).commit()
    }
}