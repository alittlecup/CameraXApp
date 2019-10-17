package com.hbl.camera.module;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.util.Rational;

import androidx.annotation.Nullable;
import androidx.camera.camera2.impl.Camera2CameraFactory;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;
import androidx.camera.view.CameraView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;

public class CameraXModule extends CameraModule {

    private static final Rational ASPECT_RATIO_16_9 = new Rational(16, 9);
    private static final Rational ASPECT_RATIO_4_3 = new Rational(4, 3);
    private static final Rational ASPECT_RATIO_9_16 = new Rational(9, 16);
    private static final Rational ASPECT_RATIO_3_4 = new Rational(3, 4);

    private static final String TAG = "CameraXModule";
    private final CameraManager mCameraManager;
    private LifecycleOwner mNewLifecycle;

    private final PreviewConfig.Builder mPreviewConfigBuilder;
    private final VideoCaptureConfig.Builder mVideoCaptureConfigBuilder;
    private final ImageCaptureConfig.Builder mImageCaptureConfigBuilder;

    @Nullable
    private ImageCapture mImageCapture;
    @Nullable
    private VideoCapture mVideoCapture;
    @Nullable
    Preview mPreview;

    LifecycleOwner mCurrentLifecycle;
    private final LifecycleObserver mCurrentLifecycleObserver = new LifecycleObserver() {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy(LifecycleOwner owner) {
            if (owner == mCurrentLifecycle) {
                clearCurrentLifecycle();
                mPreview.removePreviewOutputListener();
            }
        }
    };
    private CameraX.LensFacing mCameraLensFacing = CameraX.LensFacing.FRONT;
    private CaptureMode mCaptureMode;

    @SuppressLint("NewApi")
    public CameraXModule(Context context, CameraModuleConfig cameraModuleConfig) {
        super(context, cameraModuleConfig);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        mPreviewConfigBuilder = new PreviewConfig.Builder().setTargetName("Preview");

        mImageCaptureConfigBuilder = new ImageCaptureConfig.Builder().setTargetName("ImageCapture");

        mVideoCaptureConfigBuilder = new VideoCaptureConfig.Builder().setTargetName("VideoCapture");
    }

    @Override
    public void bindLifecycle(LifecycleOwner lifecycleOwner) {
        mNewLifecycle = lifecycleOwner;
    }

    void bindCameraXLifecycle() {
        if (mNewLifecycle == null) {
            return;
        }
        clearCurrentLifecycle();
        mCurrentLifecycle = mNewLifecycle;
        mNewLifecycle = null;
        if (mCurrentLifecycle.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            mCurrentLifecycle = null;
            throw new IllegalArgumentException("Cannot bind to lifecycle in a destroyed state.");
        }
        final int cameraOrientation;
        try {
            Set<CameraX.LensFacing> available = getAvailableCameraLenFacing();
            if (available.isEmpty()) {
                Log.w(TAG, "Unable to bindToLifeCycle since no cameras available");
                mCameraLensFacing = null;
            }
            if (mCameraLensFacing != null && !available.contains(mCameraLensFacing)) {
                Log.w(TAG, "Camera does not exist with direction " + mCameraLensFacing);
                mCameraLensFacing = available.iterator().next();
                Log.w(TAG, "Defaulting to primary camera with direction " + mCameraLensFacing);
            }
            if (mCameraLensFacing == null) {
                return;
            }
            CameraInfo cameraInfo = CameraX.getCameraInfo(getLensFacing());
            cameraOrientation = cameraInfo.getSensorRotationDegrees();
        } catch (CameraInfoUnavailableException e) {
            throw new IllegalStateException("Unable to get Camera Info.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to bind to lifecycle.", e);
        }

        boolean isDisplayPortrait = getDisplayRotationDegrees() == 0
                || getDisplayRotationDegrees() == 180;

        Rational targetAspectRatio;
        if (getCaptureMode() == CaptureMode.IMAGE) {
            mImageCaptureConfigBuilder.setTargetAspectRatio(AspectRatio.RATIO_4_3);
            targetAspectRatio = isDisplayPortrait ? ASPECT_RATIO_3_4 : ASPECT_RATIO_4_3;
        } else {
            mImageCaptureConfigBuilder.setTargetAspectRatio(AspectRatio.RATIO_16_9);
            targetAspectRatio = isDisplayPortrait ? ASPECT_RATIO_9_16 : ASPECT_RATIO_16_9;
        }


        mImageCaptureConfigBuilder.setTargetRotation(getDisplaySurfaceRotation());
        mImageCaptureConfigBuilder.setLensFacing(mCameraLensFacing);
        mImageCapture = new ImageCapture(mImageCaptureConfigBuilder.build());

        mVideoCaptureConfigBuilder.setTargetRotation(getDisplaySurfaceRotation());
        mVideoCaptureConfigBuilder.setLensFacing(mCameraLensFacing);
//        mVideoCapture = new VideoCapture(mVideoCaptureConfigBuilder.build());
        mPreviewConfigBuilder.setLensFacing(mCameraLensFacing);


        if (getCaptureMode() == CaptureMode.IMAGE) {
            CameraX.bindToLifecycle(mCurrentLifecycle, mImageCapture, mPreview);
        } else if (getCaptureMode() == CaptureMode.VIDEO) {
            CameraX.bindToLifecycle(mCurrentLifecycle, mVideoCapture, mPreview);
        } else {
            CameraX.bindToLifecycle(mCurrentLifecycle, mImageCapture, mVideoCapture, mPreview);
        }
        setZoomLevel(mZoomLevel);
        mCurrentLifecycle.getLifecycle().addObserver(mCurrentLifecycleObserver);
        // Enable flash setting in ImageCapture after use cases are created and binded.
        setFlashMode(getFlashMode());
    }



    public CameraX.LensFacing getLensFacing() {
        return mCameraLensFacing;
    }


    private Set<CameraX.LensFacing> getAvailableCameraLenFacing() {
        Set<CameraX.LensFacing> available = new LinkedHashSet<>(Arrays.asList(CameraX.LensFacing.values()));
        if (mCurrentLifecycle != null) {
            if (!hasCameraWithLensFacing(CameraX.LensFacing.BACK)) {
                available.remove(CameraX.LensFacing.BACK);
            }
            if (!hasCameraWithLensFacing(CameraX.LensFacing.FRONT)) {
                available.remove(CameraX.LensFacing.FRONT);
            }
        }
        return available;
    }

    //判断某个方向的摄像头是否存在  getCameraInfo //todo 利用这个函数判断是否存在
    private boolean hasCameraWithLensFacing(CameraX.LensFacing lensFacing) {

        return true;
    }

    void clearCurrentLifecycle() {
        if (mCurrentLifecycle != null) {
            CameraX.unbind(mImageCapture, mVideoCapture, mPreview);
        }
        mCurrentLifecycle = null;
    }

    public CaptureMode getCaptureMode() {
        return mCaptureMode;
    }

    public void setCaptureMode(CaptureMode captureMode) {
        this.mCaptureMode = captureMode;
        rebindToLifecycle();
    }

    private void rebindToLifecycle() {
        if (mCurrentLifecycle != null) {
            bindLifecycle(mCurrentLifecycle);
        }
    }


    @Override
    public void open() {
        throw new UnsupportedOperationException(
                "Explicit open/close of camera not yet supported. Use bindtoLifecycle() instead.");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException(
                "Explicit open/close of camera not yet supported. Use bindtoLifecycle() instead.");
    }

    @Override
    public void takePicture(Executor executor, OnImageCapturedListener listener) {

    }

    @Override
    public void takePicture(File saveLocation, Executor executor, OnImageSavedListener listener) {

    }

    @Override
    public void startRecordind(File file, Executor executor, OnVideoSavedListener listener) {

    }

    @Override
    public void stopRecording() {

    }

    @Override
    public boolean isRecording() {
        return false;
    }

    @Override
    public void toggleCamera() {

    }



    @Override
    public float getMaxZoomLevel() {
        return 0;
    }

    @Override
    public boolean isZoomSupport() {
        return false;
    }
}
