package com.hbl.camera.module;

import android.content.Context;
import android.os.Build;

import androidx.camera.core.CameraX;

public final class CameraModuleFactory {
    public static CameraModule create(Context context, CameraModuleConfig cameraModuleConfig) {
//        if (Build.VERSION.SDK_INT >= 20) {
//            return new CameraXModule(context, cameraModuleConfig);
//        } else {
//            return new Camera1Module();
//        }
        return new Camera1Module(context, cameraModuleConfig);
    }
}
