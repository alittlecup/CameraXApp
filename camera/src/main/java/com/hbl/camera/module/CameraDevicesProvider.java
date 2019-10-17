package com.hbl.camera.module;

import androidx.camera.core.CameraInfo;

public interface CameraDevicesProvider {

    float getMaxZoomLevel();

    boolean isZoomSupport();

}
