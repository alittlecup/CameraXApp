package com.hbl.camera.module;

import androidx.annotation.NonNull;
import androidx.camera.core.Preview;

import com.hbl.camera.option.preview.PreviewOutput;

public interface OnPreviewOutputListener {

    void onUpdated(@NonNull PreviewOutput output);

}
