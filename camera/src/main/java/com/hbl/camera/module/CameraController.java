package com.hbl.camera.module;

import com.hbl.camera.option.CameraModule;

import java.io.File;
import java.util.concurrent.Executor;

public interface CameraController {

    void setOnPreviewOutputListener(OnPreviewOutputListener listener);

    void takePicture(Executor executor, OnImageCapturedListener listener);

    void takePicture(File saveLocation, Executor executor, OnImageSavedListener listener);

    void startRecording(File file, Executor executor, OnVideoSavedListener listener);

    void stopRecording();

    boolean isRecording();

    void toggleCamera();


}
