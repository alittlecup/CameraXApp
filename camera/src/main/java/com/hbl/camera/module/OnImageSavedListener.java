package com.hbl.camera.module;

import java.io.File;

public interface OnImageSavedListener {

    void onImageSaved(File file);

    void onError(String msg, Throwable throwable);

}
