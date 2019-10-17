package com.hbl.camera.option;


import android.media.Image;
import android.media.ImageReader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.impl.utils.MainThreadAsyncHandler;

import java.nio.ByteBuffer;

public class CameraModule {
    private CameraModule() {

        ImageCaptureConfig build = new ImageCaptureConfig.Builder().build();
        ImageCapture imageCapture = new ImageCapture(build);
        imageCapture.takePicture(null, new ImageCapture.OnImageCapturedListener() {
            @Override
            public void onCaptureSuccess(ImageProxy image, int rotationDegrees) {
            }

            @Override
            public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) {
                super.onError(imageCaptureError, message, cause);
            }
        });
    }


    public enum LensFacing {
        FRONT,
        BACK
    }

    public enum FlashMode {
        AUTO,
        OFF, ON
    }

    public enum CaptureMode {
        MAX_QUALITY,
        MIN_QUALITY,
    }
}
