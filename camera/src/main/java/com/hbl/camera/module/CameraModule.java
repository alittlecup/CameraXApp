package com.hbl.camera.module;

import android.content.Context;
import android.util.Rational;

import androidx.camera.core.AspectRatio;
import androidx.camera.view.CameraView;
import androidx.lifecycle.LifecycleOwner;

import com.hbl.camera.option.Size;


public abstract class CameraModule implements CameraDevicesProvider, CameraController {
    protected final CameraModuleConfig cameraModuleConfig;

    protected static final float UNITY_ZOOM_SCALE = 1f;

    protected static final float ZOOM_NOT_SUPPORTED = UNITY_ZOOM_SCALE;




    public CameraModule(Context context, CameraModuleConfig cameraModuleConfig) {
        this.cameraModuleConfig = cameraModuleConfig;
    }

    public Size getTargetResolution() {
        return cameraModuleConfig.getTargetResolution();
    }

    public int getTargetRotation() {
        return cameraModuleConfig.getTargetRotation();
    }

    public AspectRatio getAspectRatio() {
        return cameraModuleConfig.getTargetAspectRatio();
    }


    public abstract void bindToLifecycle(LifecycleOwner lifecycleOwner);

    public enum CaptureMode {

        IMAGE(0),

        VIDEO(1),

        MIXED(2);

        private int mId;

        int getId() {
            return mId;
        }

        CaptureMode(int id) {
            mId = id;
        }

        static CaptureMode fromId(int id) {
            for (CaptureMode f : values()) {
                if (f.mId == id) {
                    return f;
                }
            }
            throw new IllegalArgumentException();
        }
    }
}
