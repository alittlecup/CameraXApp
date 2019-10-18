package com.hbl.camera.module;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.util.Log;
import android.util.Rational;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.FlashMode;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.hbl.camera.option.preview.PreviewOutput;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressLint("NewApi")
public class CameraXModule extends CameraModule {


    private static final String TAG = "CameraXModule";
    private final CameraManager mCameraManager;

    private float mZoomLevel = UNITY_ZOOM_SCALE;


    private final PreviewConfig.Builder mPreviewConfigBuilder;
    private final VideoCaptureConfig.Builder mVideoCaptureConfigBuilder;
    private final ImageCaptureConfig.Builder mImageCaptureConfigBuilder;

    @Nullable
    private ImageCapture mImageCapture;
    @Nullable
    private VideoCapture mVideoCapture;
    @Nullable
    Preview mPreview;

    OnPreviewOutputListener listener;

    LifecycleOwner mCurrentLifecycle;
    private LifecycleOwner mNewLifecycle;

    private final LifecycleObserver mCurrentLifecycleObserver = new LifecycleObserver() {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy(LifecycleOwner owner) {
            if (owner == mCurrentLifecycle) {
                clearCurrentLifecycle();
                mPreview.removePreviewOutputListener();
            }
        }
    };
    private CameraX.LensFacing mCameraLensFacing;
    private CaptureMode mCaptureMode;
    private Rect mCropRegion;
    private FlashMode mFlash = FlashMode.OFF;
    private AtomicBoolean mVideoIsRecording = new AtomicBoolean(false);

    @SuppressLint("NewApi")
    public CameraXModule(Context context, CameraModuleConfig cameraModuleConfig) {
        super(context, cameraModuleConfig);
        mCameraLensFacing = getCameraXLensFacing(cameraModuleConfig.getLensFacing());

        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        mPreviewConfigBuilder = new PreviewConfig.Builder().setTargetName("Preview");

        mImageCaptureConfigBuilder = new ImageCaptureConfig.Builder().setTargetName("ImageCapture");

        mVideoCaptureConfigBuilder = new VideoCaptureConfig.Builder().setTargetName("VideoCapture");
    }

    private CameraX.LensFacing getCameraXLensFacing(com.hbl.camera.option.CameraModule.LensFacing lensFacing) {
        switch (lensFacing) {
            case FRONT:
                return CameraX.LensFacing.FRONT;
            case BACK:
                return CameraX.LensFacing.BACK;
        }
        return null;

    }

    @Override
    public void bindToLifecycle(LifecycleOwner lifecycleOwner) {
        mNewLifecycle = lifecycleOwner;
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
        mImageCaptureConfigBuilder.setTargetRotation(getTargetRotation());
        mImageCaptureConfigBuilder.setLensFacing(mCameraLensFacing);
        mImageCapture = new ImageCapture(mImageCaptureConfigBuilder.build());

        mVideoCaptureConfigBuilder.setTargetRotation(getTargetRotation());
        mVideoCaptureConfigBuilder.setLensFacing(mCameraLensFacing);

        mPreviewConfigBuilder.setLensFacing(mCameraLensFacing);

        mPreviewConfigBuilder.setTargetResolution(new Size(getTargetResolution().getWidth(), getTargetResolution().getHeight())).setTargetRotation(getTargetRotation());
        mPreview = new Preview(mPreviewConfigBuilder.build());

        mPreview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(@NonNull Preview.PreviewOutput output) {
                if (listener != null) {
                    Log.d("TAG", "onUpdated: " + output);
                    Log.d("TAG", "onUpdated: " + output.getSurfaceTexture() + "-->" + output.getSurfaceTexture().isReleased());
                    listener.onUpdated(new PreviewOutput(output.getSurfaceTexture(), new com.hbl.camera.option.Size(output.getTextureSize().getWidth(), output.getTextureSize().getHeight()), output.getRotationDegrees()));
                }
            }
        });


        if (getCaptureMode() == CaptureMode.IMAGE) {
            CameraX.bindToLifecycle(mCurrentLifecycle, mImageCapture, mPreview);
        } else if (getCaptureMode() == CaptureMode.VIDEO) {
            CameraX.bindToLifecycle(mCurrentLifecycle, mVideoCapture, mPreview);
        } else {
            CameraX.bindToLifecycle(mCurrentLifecycle, mImageCapture, mPreview);
        }
        setZoomLevel(mZoomLevel);
        mCurrentLifecycle.getLifecycle().addObserver(mCurrentLifecycleObserver);
        setFlash(getFlash());
    }

    public FlashMode getFlash() {
        return mFlash;
    }

    public void setFlash(FlashMode flash) {
        this.mFlash = flash;

        if (mImageCapture == null) {
            return;
        }
        mImageCapture.setFlashMode(flash);
    }


    private void setZoomLevel(float mZoomLevel) {
        this.mZoomLevel = mZoomLevel;
        if (mPreview == null) return;
        Rect sensorSize;

        try {
            sensorSize = getSensorSize(getActiveCamera());
            if (sensorSize == null) {
                Log.e(TAG, "Failed to get the sensor size.");
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }
        float minZoom = getMinZoomLevel();
        float maxZoom = getMaxZoomLevel();
        if (this.mZoomLevel < minZoom) {
            Log.e(TAG, "Requested zoom level is less than minimum zoom level.");
        }
        if (this.mZoomLevel > maxZoom) {
            Log.e(TAG, "Requested zoom level is greater than maximum zoom level.");
        }
        this.mZoomLevel = Math.max(minZoom, Math.min(maxZoom, this.mZoomLevel));
        float zoomSCaleFactor = (minZoom == maxZoom) ? minZoom : (this.mZoomLevel - minZoom) / (maxZoom - minZoom);

        int minWidth = Math.round(sensorSize.width() / maxZoom);
        int minHeight = Math.round(sensorSize.height() / maxZoom);

        int diffWidth = sensorSize.width() - minWidth;
        int diffHeight = sensorSize.height() - minHeight;
        float cropWidth = diffWidth * zoomSCaleFactor;
        float cropHeight = diffHeight * zoomSCaleFactor;

        Rect cropRegion = new Rect(
                (int) Math.ceil(cropWidth / 2 - 0.5f),
                (int) Math.ceil(cropHeight / 2 - 0.5f),
                (int) Math.floor(sensorSize.width() - cropWidth / 2 + 0.5f),
                (int) Math.floor(sensorSize.height() - cropHeight / 2 + 0.5f));

        if (cropRegion.width() < 50 || cropRegion.height() < 50) {
            Log.e(TAG, "Crop region is too small to compute 3A stats, so ignoring further zoom.");
            return;
        }
        this.mCropRegion = cropRegion;

        mPreview.zoom(cropRegion);
    }

    private float getMinZoomLevel() {
        return UNITY_ZOOM_SCALE;
    }

    private Rect getSensorSize(String activeCamera) throws CameraAccessException {
        CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(activeCamera);
        return cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
    }

    private String getActiveCamera() throws CameraAccessException {
        String[] cameraIdList = mCameraManager.getCameraIdList();
        Set<String> filter = filter(new LinkedHashSet<>(Arrays.asList(cameraIdList)));
        return filter.iterator().next();

    }

    public Set<String> filter(@NonNull Set<String> cameraIdSet) {
        Set<String> resultCameraIdSet = new LinkedHashSet<>();

        for (String cameraId : cameraIdSet) {
            Integer lensFacingInteger = null;
            try {
                lensFacingInteger = mCameraManager.getCameraCharacteristics(cameraId).get(
                        CameraCharacteristics.LENS_FACING);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Unable to retrieve info for camera with id " + cameraId + ".", e);
            }
            if (lensFacingInteger == null) {
                continue;
            }
            if (lensFacingInteger.equals(cameraXLensFacingToCamera2LensFacing(mCameraLensFacing))) {
                resultCameraIdSet.add(cameraId);
            }
        }

        return resultCameraIdSet;
    }


    private Integer cameraXLensFacingToCamera2LensFacing(CameraX.LensFacing lensFacing) {
        Integer lensFacingInteger = -1;
        switch (lensFacing) {
            case BACK:
                lensFacingInteger = CameraMetadata.LENS_FACING_BACK;
                break;
            case FRONT:
                lensFacingInteger = CameraMetadata.LENS_FACING_FRONT;
                break;
        }
        return lensFacingInteger;
    }


    public int getDisplayRotationDegrees() {
        return CameraOrientationUtil.surfaceRotationToDegrees(getTargetRotation());
    }


    public CameraX.LensFacing getLensFacing() {
        return mCameraLensFacing;
    }


    private Set<CameraX.LensFacing> getAvailableCameraLensFacing() {
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
        try {
            CameraX.getCameraInfo(lensFacing);
            return true;
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
            return false;
        }
    }

    void clearCurrentLifecycle() {
        if (mCurrentLifecycle != null) {
            CameraX.unbind(mImageCapture, mPreview);
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
            bindToLifecycle(mCurrentLifecycle);
        }
    }


    @Override
    public void setOnPreviewOutputListener(OnPreviewOutputListener listener) {
        this.listener = listener;
    }

    @Override
    public void takePicture(Executor executor, final OnImageCapturedListener listener) {
        if (mImageCapture == null) {
            return;
        }

        if (getCaptureMode() == CaptureMode.VIDEO) {
            throw new IllegalStateException("Can not take picture under VIDEO capture mode.");
        }

        if (listener == null) {
            throw new IllegalArgumentException("OnImageCapturedListener should not be empty");
        }

        mImageCapture.takePicture(executor, new ImageCapture.OnImageCapturedListener() {
            @Override
            public void onCaptureSuccess(ImageProxy image, int rotationDegrees) {
                listener.onCaptureSuccess(new com.hbl.camera.module.ImageProxy(image), rotationDegrees);
            }

            @Override
            public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) {
                listener.onError(message, cause);
            }
        });
    }

    @Override
    public void takePicture(File saveLocation, Executor executor, final OnImageSavedListener listener) {
        if (mImageCapture == null) {
            return;
        }

        if (getCaptureMode() == CaptureMode.VIDEO) {
            throw new IllegalStateException("Can not take picture under VIDEO capture mode.");
        }

        if (listener == null) {
            throw new IllegalArgumentException("OnImageSavedListener should not be empty");
        }

        ImageCapture.Metadata metadata = new ImageCapture.Metadata();
        metadata.isReversedHorizontal = mCameraLensFacing == CameraX.LensFacing.FRONT;
        mImageCapture.takePicture(saveLocation, metadata, executor, new ImageCapture.OnImageSavedListener() {
            @Override
            public void onImageSaved(@NonNull File file) {
                listener.onImageSaved(file);
            }

            @Override
            public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) {
                listener.onError(message, cause);
            }
        });
    }

    @Override
    public void startRecording(File file, Executor executor, OnVideoSavedListener listener) {
        if (mVideoCapture == null) {
            return;
        }

        if (getCaptureMode() == CaptureMode.IMAGE) {
            throw new IllegalStateException("Can not record video under IMAGE capture mode.");
        }

        if (listener == null) {
            throw new IllegalArgumentException("OnVideoSavedListener should not be empty");
        }

//        mVideoIsRecording.set(true);
//        mVideoCapture.startRecording(
//                file,
//                executor,
//                new VideoCapture.OnVideoSavedListener() {
//                    @Override
//                    public void onVideoSaved(@NonNull File savedFile) {
//                        mVideoIsRecording.set(false);
//                        listener.onVideoSaved(savedFile);
//                    }
//
//                    @Override
//                    public void onError(
//                            @NonNull VideoCapture.VideoCaptureError videoCaptureError,
//                            @NonNull String message,
//                            @Nullable Throwable cause) {
//                        mVideoIsRecording.set(false);
//                        Log.e(TAG, message, cause);
//                        listener.onError(videoCaptureError, message, cause);
//                    }
//                });
    }

    @Override
    public void stopRecording() {
        if (mVideoCapture == null) {
            return;
        }

//        mVideoCapture.stopRecording();
    }

    @Override
    public boolean isRecording() {
        return mVideoIsRecording.get();
    }

    @Override
    public void toggleCamera() {
        Set<CameraX.LensFacing> availableCameraLensFacing = getAvailableCameraLensFacing();

        if (availableCameraLensFacing.isEmpty()) {
            return;
        }

        if (mCameraLensFacing == null) {
            setCameraLensFacing(availableCameraLensFacing.iterator().next());
            return;
        }

        if (mCameraLensFacing == CameraX.LensFacing.BACK
                && availableCameraLensFacing.contains(CameraX.LensFacing.FRONT)) {
            setCameraLensFacing(CameraX.LensFacing.FRONT);
            return;
        }

        if (mCameraLensFacing == CameraX.LensFacing.FRONT
                && availableCameraLensFacing.contains(CameraX.LensFacing.BACK)) {
            setCameraLensFacing(CameraX.LensFacing.BACK);
            return;
        }
    }

    private void setCameraLensFacing(CameraX.LensFacing lensFacing) {
        if (mCameraLensFacing != lensFacing) {
            // If we're not bound to a lifecycle, just update the camera that will be opened when we
            // attach to a lifecycle.
            mCameraLensFacing = lensFacing;

            if (mCurrentLifecycle != null) {
                // Re-bind to lifecycle with new camera
                bindToLifecycle(mCurrentLifecycle);
            }
        }
    }


    @Override
    public float getMaxZoomLevel() {
        try {
            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(getActiveCamera());
            Float maxZoom = cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            if (maxZoom == null) {
                return ZOOM_NOT_SUPPORTED;
            }
            if (maxZoom == ZOOM_NOT_SUPPORTED) {
                return ZOOM_NOT_SUPPORTED;
            }
            return maxZoom;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return ZOOM_NOT_SUPPORTED;
    }

    @Override
    public boolean isZoomSupport() {
        return getMaxZoomLevel() != ZOOM_NOT_SUPPORTED;
    }
}
