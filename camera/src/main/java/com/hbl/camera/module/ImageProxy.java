package com.hbl.camera.module;

import android.graphics.Rect;
import android.media.Image;

import androidx.annotation.Nullable;
import androidx.camera.core.ImageInfo;

public class ImageProxy implements androidx.camera.core.ImageProxy {
    private final androidx.camera.core.ImageProxy imageProxy;

    public ImageProxy(androidx.camera.core.ImageProxy imageProxy) {
        this.imageProxy = imageProxy;
    }


    @Override
    public void close() {
        imageProxy.close();
    }

    @Override
    public Rect getCropRect() {
        return imageProxy.getCropRect();
    }

    @Override
    public void setCropRect(Rect rect) {
        imageProxy.setCropRect(rect);
    }

    @Override
    public int getFormat() {
        return imageProxy.getFormat();
    }

    @Override
    public int getHeight() {
        return imageProxy.getHeight();
    }

    @Override
    public int getWidth() {
        return imageProxy.getWidth();
    }

    @Override
    public long getTimestamp() {
        return imageProxy.getTimestamp();
    }

    @Override
    public void setTimestamp(long timestamp) {
        imageProxy.setTimestamp(timestamp);

    }

    @Override
    public PlaneProxy[] getPlanes() {
        return imageProxy.getPlanes();
    }

    @Nullable
    @Override
    public ImageInfo getImageInfo() {
        return imageProxy.getImageInfo();
    }

    @Nullable
    @Override
    public Image getImage() {
        return imageProxy.getImage();
    }
}
