package com.hbl.camera.module;

public interface OnImageCapturedListener {

    void onCaptureSuccess(ImageProxy image, int rotationDegrees);

    void onError(String msg, Throwable cause);

}
