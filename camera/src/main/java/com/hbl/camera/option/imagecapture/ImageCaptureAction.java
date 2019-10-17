package com.hbl.camera.option.imagecapture;

import com.hbl.camera.option.CameraAction;

import java.io.File;

public class ImageCaptureAction extends CameraAction {

    public ImageCaptureActionConfig getCaptureActionConfig() {
        return captureActionConfig;
    }

    private final ImageCaptureActionConfig captureActionConfig;

    public ImageCaptureAction(ImageCaptureActionConfig image) {
        this.captureActionConfig = image;
    }

    interface onImageSavedListener {
        void onImageSaved(File file);

        void onError(String msg, Throwable cause);
    }
    interface onImageCapturedListener{
//        void onCaptureSuccess()
    }


}
