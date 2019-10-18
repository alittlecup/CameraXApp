package com.hbl.camera.module;

import android.util.Log;
import android.view.Surface;

import androidx.camera.core.ImageOutputConfig;

public final class CameraOrientationUtil {
    private static final String TAG = "CameraOrientationUtil";
    private static final boolean DEBUG = false;
    private CameraOrientationUtil() {
    }
    public static int getRelativeImageRotation(
            int destRotationDegrees, int sourceRotationDegrees, boolean isOppositeFacing) {
        int result;
        if (isOppositeFacing) {
            result = (sourceRotationDegrees - destRotationDegrees + 360) % 360;
        } else {
            result = (sourceRotationDegrees + destRotationDegrees) % 360;
        }
        if (DEBUG) {
            Log.d(
                    TAG,
                    String.format(
                            "getRelativeImageRotation: destRotationDegrees=%s, "
                                    + "sourceRotationDegrees=%s, isOppositeFacing=%s, "
                                    + "result=%s",
                            destRotationDegrees, sourceRotationDegrees, isOppositeFacing, result));
        }
        return result;
    }
    public static int surfaceRotationToDegrees(@ImageOutputConfig.RotationValue int rotationEnum) {
        int rotationDegrees;
        switch (rotationEnum) {
            case Surface.ROTATION_0:
                rotationDegrees = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270;
                break;
            default:
                throw new IllegalArgumentException("Unsupported surface rotation: " + rotationEnum);
        }

        return rotationDegrees;
    }
}
