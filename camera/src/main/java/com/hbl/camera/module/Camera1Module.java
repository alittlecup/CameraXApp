package com.hbl.camera.module;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.hbl.camera.option.preview.PreviewOutput;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.concurrent.Executor;

import javax.crypto.interfaces.PBEKey;

public class Camera1Module extends CameraModule implements Camera.PreviewCallback {

    private LifecycleOwner mNewLifecycleOwner;
    private LifecycleOwner mCurrentLifecycle;
    private SurfaceTexture surfaceTexture;
    private final LifecycleObserver mCurrentLifecucleObserver = new LifecycleObserver() {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        public void onCreate(LifecycleOwner owner) {
            if (owner == mCurrentLifecycle) {
                Log.d("TAG", "onCreate: ");
                if (mCurrentCamera == null) {
                    openCamera();
                }
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        public void onResume(LifecycleOwner owner) {
            if (owner == mCurrentLifecycle) {
                Log.d("TAG", "onResume: ");
                startPreview();
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        public void onPause(LifecycleOwner owner) {
            if (owner == mCurrentLifecycle) {
                Log.d("TAG", "onPause: ");

            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy(LifecycleOwner owner) {
            if (owner == mCurrentLifecycle) {
                Log.d("TAG", "onDestroy: ");

            }
        }
    };
    Surface surface;

    public Camera1Module(Context context, CameraModuleConfig cameraModuleConfig) {
        super(context, cameraModuleConfig);
        surfaceTexture = new SurfaceTexture(0);
        surfaceTexture.setDefaultBufferSize(getTargetResolution().getWidth(), getTargetResolution().getHeight());
        surfaceTexture.detachFromGLContext();
        surface = new Surface(surfaceTexture);
    }

    private void startPreview() {
        try {
            mCurrentCamera.setPreviewTexture(surfaceTexture);
            setCameraDisplayOrientation(mCurrentCamera);
            mCurrentCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
//        this.surfaceTexture = surfaceTexture;
    }

    private void setCameraDisplayOrientation(Camera mCurrentCamera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(getCameraLensFacing(cameraModuleConfig.getLensFacing()), info);
        //rotation是预览Window的旋转方向，对于手机而言，当在清单文件设置Activity的screenOrientation="portait"时，
        //rotation=0，这时候没有旋转，当screenOrientation="landScape"时，rotation=1。
        int rotation = getTargetRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        //计算图像所要旋转的角度
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        //调整预览图像旋转角度
        mCurrentCamera.setDisplayOrientation(result);
    }

    private final String TAG = "Camera1Module";


    private Camera mCurrentCamera;

    private void openCamera() {
        int cameraLensFacing = getCameraLensFacing(cameraModuleConfig.getLensFacing());
        boolean isSupportCamera = isSupport(cameraLensFacing);
        if (isSupportCamera) {
            try {

                mCurrentCamera = Camera.open(cameraLensFacing);
                mCurrentCamera.setPreviewCallback(this);
                initParameters(mCurrentCamera);
                if (mCurrentCamera != null) {
                    mCurrentCamera.setPreviewCallback(this);
                }
            } catch (Exception e) {

            }
        }
    }

    private void initParameters(Camera mCurrentCamera) {
        if (mCurrentCamera == null) return;
        Camera.Parameters parameters = mCurrentCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setExposureCompensation(5);
        setPreviewSize(parameters);
        setPictureSize(parameters);
        if (isSupportFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (isSupportFocus(Camera.Parameters.FOCUS_MODE_AUTO)) {
            //自动对焦(单次)
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        //给相机设置参数
        mCurrentCamera.setParameters(parameters);
    }

    private boolean isSupportFocus(String focusModeContinuousPicture) {
        return false;

    }

    private void setPictureSize(Camera.Parameters parameter) {
        List<Camera.Size> localSizes = parameter.getSupportedPictureSizes();
        Camera.Size biggestSize = null;
        Camera.Size fitSize = null;// 优先选预览界面的尺寸
        Camera.Size previewSize = parameter.getPreviewSize();//获取预览界面尺寸
        float previewSizeScale = 0;
        if (previewSize != null) {
            previewSizeScale = previewSize.width / (float) previewSize.height;
        }

        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();
            for (int n = 0; n < cameraSizeLength; n++) {
                Camera.Size size = localSizes.get(n);
                if (biggestSize == null) {
                    biggestSize = size;
                } else if (size.width >= biggestSize.width && size.height >= biggestSize.height) {
                    biggestSize = size;
                }

                // 选出与预览界面等比的最高分辨率
                if (previewSizeScale > 0
                        && size.width >= previewSize.width && size.height >= previewSize.height) {
                    float sizeScale = size.width / (float) size.height;
                    if (sizeScale == previewSizeScale) {
                        if (fitSize == null) {
                            fitSize = size;
                        } else if (size.width >= fitSize.width && size.height >= fitSize.height) {
                            fitSize = size;
                        }
                    }
                }
            }

            // 如果没有选出fitSize, 那么最大的Size就是FitSize
            if (fitSize == null) {
                fitSize = biggestSize;
            }
            parameter.setPictureSize(fitSize.width, fitSize.height);
        }

    }

    private void setPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> localSizes = parameters.getSupportedPreviewSizes();
        Camera.Size biggestSize = null;//最大分辨率
        Camera.Size fitSize = null;// 优先选屏幕分辨率
        Camera.Size targetSize = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(大)的size
        Camera.Size targetSiz2 = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(小)的size
        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();
            for (int n = 0; n < cameraSizeLength; n++) {
                Camera.Size size = localSizes.get(n);
                Log.d("sssd-系统支持的尺寸:", size.width + "*" + size.height);
                if (biggestSize == null ||
                        (size.width >= biggestSize.width && size.height >= biggestSize.height)) {
                    biggestSize = size;
                }

                //如果支持的比例都等于所获取到的宽高
                if (size.width == getTargetResolution().getHeight()
                        && size.height == getTargetResolution().getWidth()) {
                    fitSize = size;
                    //如果任一宽或者高等于所支持的尺寸
                } else if (size.width == getTargetResolution().getHeight()
                        || size.height == getTargetResolution().getWidth()) {
                    if (targetSize == null) {
                        targetSize = size;
                        //如果上面条件都不成立 如果任一宽高小于所支持的尺寸
                    } else if (size.width < getTargetResolution().getHeight()
                            || size.height < getTargetResolution().getWidth()) {
                        targetSiz2 = size;
                    }
                }
            }

            if (fitSize == null) {
                fitSize = targetSize;
            }

            if (fitSize == null) {
                fitSize = targetSiz2;
            }

            if (fitSize == null) {
                fitSize = biggestSize;
            }
            Log.d("sssd-最佳预览尺寸:", fitSize.width + "*" + fitSize.height);
            parameters.setPreviewSize(fitSize.width, fitSize.height);
        }

    }

    private float mZoomLevel = UNITY_ZOOM_SCALE;

    public void setZoomLevel(float zoomLevel) {
        this.mZoomLevel = zoomLevel;
        if (mCurrentCamera == null) {
            return;

        }
        Camera.Parameters parameters;
        parameters = mCurrentCamera.getParameters();
        if (!isZoomSupport()) {
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
        this.mZoomLevel = Math.max(minZoom, Math.min(maxZoom, mZoomLevel));
        parameters.setZoom((int) mZoomLevel);
        mCurrentCamera.setParameters(parameters);
    }

    private float getMinZoomLevel() {
        return UNITY_ZOOM_SCALE;
    }


    private boolean isSupport(int cameraLensFacing) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == cameraLensFacing) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void bindToLifecycle(LifecycleOwner lifecycleOwner) {
        mCurrentLifecycle = lifecycleOwner;
        mCurrentLifecycle.getLifecycle().addObserver(mCurrentLifecucleObserver);

    }

    private int getCameraLensFacing(com.hbl.camera.option.CameraModule.LensFacing lensFacing) {
        switch (lensFacing) {
            case FRONT:
                return Camera.CameraInfo.CAMERA_FACING_FRONT;
            case BACK:
                return Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        return -1;
    }

    OnPreviewOutputListener listener;

    @Override
    public void setOnPreviewOutputListener(OnPreviewOutputListener listener) {
        this.listener = listener;
    }

    @Override
    public void takePicture(Executor executor, OnImageCapturedListener listener) {

    }

    @Override
    public void takePicture(File saveLocation, Executor executor, OnImageSavedListener listener) {

    }

    @Override
    public void startRecording(File file, Executor executor, OnVideoSavedListener listener) {

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
        if (mCurrentCamera == null) {
            return ZOOM_NOT_SUPPORTED;
        }
        int maxZoom = mCurrentCamera.getParameters().getMaxZoom();
        if (maxZoom <= ZOOM_NOT_SUPPORTED) {
            return ZOOM_NOT_SUPPORTED;
        }
        return maxZoom;
    }

    @Override
    public boolean isZoomSupport() {
        if (mCurrentCamera != null) {
            return mCurrentCamera.getParameters().isZoomSupported();
        }
        return false;
    }

    boolean flag;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (listener != null) {
            if (!flag) {
                listener.onUpdated(new PreviewOutput(surfaceTexture, getTargetResolution(), 0));
                flag = !flag;
            }
        }
    }
}
