package com.hbl.camera.module;

import android.content.Context;

import androidx.camera.view.CameraView;
import androidx.lifecycle.LifecycleOwner;

public abstract class CameraModule implements CameraDevicesProvider, CameraController {
    protected final CameraModuleConfig cameraModuleConfig;

    public CameraModule(Context context, CameraModuleConfig cameraModuleConfig) {
        this.cameraModuleConfig = cameraModuleConfig;
    }

    public abstract void bindLifecycle(LifecycleOwner lifecycleOwner);

    public enum CaptureMode {
        /**
         * A mode where image capture is enabled.
         */
        IMAGE(0),
        /**
         * A mode where video capture is enabled.
         */
        VIDEO(1),
        /**
         * A mode where both image capture and video capture are simultaneously enabled. Note that
         * this mode may not be available on every device.
         */
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
