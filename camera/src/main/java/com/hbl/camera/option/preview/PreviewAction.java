package com.hbl.camera.option.preview;

import androidx.annotation.NonNull;
import androidx.camera.core.Preview;

import com.hbl.camera.option.CameraAction;

public class PreviewAction extends CameraAction {

    public PreviewActionConfig getPreviewActionConfig() {
        return previewActionConfig;
    }

    private final PreviewActionConfig previewActionConfig;

    public PreviewAction(PreviewActionConfig build) {
        this.previewActionConfig = build;
    }



    Preview.OnPreviewOutputUpdateListener previewOutputUpdateListener;

    public void setOnPreviewOutputUpdateListener(Preview.OnPreviewOutputUpdateListener listener) {
        this.previewOutputUpdateListener = listener;
    }

}
