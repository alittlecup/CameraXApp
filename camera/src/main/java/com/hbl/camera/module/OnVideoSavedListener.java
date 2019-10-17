package com.hbl.camera.module;

import java.io.File;

public interface OnVideoSavedListener {
    void onVideoSaved(File file);

    void onError(String msg, Throwable throwable);
}
