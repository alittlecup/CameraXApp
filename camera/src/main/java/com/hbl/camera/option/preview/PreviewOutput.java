package com.hbl.camera.option.preview;

import android.graphics.SurfaceTexture;

import com.hbl.camera.option.Size;

public final class PreviewOutput {
    SurfaceTexture surfaceTexture;
    Size size;
    int rotationDegrees;

    public PreviewOutput(SurfaceTexture surfaceTexture, Size textSize, int rotationDegrees) {
        this.size = textSize;
        this.surfaceTexture = surfaceTexture;
        this.rotationDegrees = rotationDegrees;
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public Size getSize() {
        return size;
    }

    public int getRotationDegrees() {
        return rotationDegrees;
    }

    static PreviewOutput create(SurfaceTexture surfaceTexture, Size textSize, int rotationDegrees) {
        return new PreviewOutput(surfaceTexture, textSize, rotationDegrees);
    }
}
