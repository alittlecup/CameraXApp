package com.hbl.camera.option;

import androidx.annotation.NonNull;

public interface CameraDeviceConfig {
    Option<CameraModule.LensFacing> OPTION_LENSFACING = Option.create("camerax.core.camera.lensFacing", CameraModule.LensFacing.class);

    CameraModule.LensFacing getLensFacing();

    interface Builder<B> {
        B setLensFacing(@NonNull CameraModule.LensFacing lensFacing);

    }
}
