package com.hbl.camera.module;

import android.os.Build;

import androidx.camera.core.CameraX;

public final class CameraModuleFactory {
    public static CameraModule create() {
        if (Build.VERSION.SDK_INT >= 20) {
            return new CameraXModule();
        } else {
            return new Camera1Module();
        }
    }
}
