package com.hbl.camera.option.imagecapture;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.FlashMode;
import androidx.camera.core.ImageCapture;
import androidx.camera.view.CameraView;

import com.hbl.camera.option.CameraActionConfig;
import com.hbl.camera.option.CameraDeviceConfig;
import com.hbl.camera.option.CameraModule;
import com.hbl.camera.option.ImageOutputConfig;
import com.hbl.camera.option.MutableOptionBundle;
import com.hbl.camera.option.Option;
import com.hbl.camera.option.OptionBundle;
import com.hbl.camera.option.Size;

public class ImageCaptureActionConfig implements CameraActionConfig<ImageCaptureAction>, ImageOutputConfig, CameraDeviceConfig {


    static final Option<CameraModule.FlashMode> OPTION_FLASH_MODE = Option.create("camerax.core.imageCapture.flashMode", CameraModule.FlashMode.class);
    static final Option<CameraModule.CaptureMode> OPTION_CAPTURE_MODE = Option.create("camerax.core.imageCapture.captureMode", CameraModule.CaptureMode.class);

    private final OptionBundle mOptionBundles;

    private ImageCaptureActionConfig(OptionBundle optionsBundles) {
        this.mOptionBundles = optionsBundles;

    }

    @Override
    public CameraModule.LensFacing getLensFacing() {
        return mOptionBundles.retrieveOption(OPTION_LENSFACING);
    }

    @Override
    public Size getTargetResolution() {
        return mOptionBundles.retrieveOption(OPTION_TARGET_RESOLUTION);
    }

    @Override
    public int getTargetRotation() {
        return mOptionBundles.retrieveOption(OPTION_TARGET_ROTATION);
    }

    @Override
    public AspectRatio getTargetAspectRatio() {
        return mOptionBundles.retrieveOption(OPTION_TARGET_ASPECT_RATIO);
    }

    public static class Builder implements CameraActionConfig.Builder<ImageCaptureAction, ImageCaptureActionConfig, Builder>
            , ImageOutputConfig.Builder<Builder>
            , CameraDeviceConfig.Builder<Builder> {

        private final MutableOptionBundle optionsBundles;

        public Builder() {
            this(MutableOptionBundle.create());
        }

        public Builder setFlashMode(CameraModule.FlashMode flashMode) {
            optionsBundles.insertOption(OPTION_FLASH_MODE, flashMode);
            return this;
        }

        public Builder setCaptureMode(CameraModule.CaptureMode captureModel) {
            optionsBundles.insertOption(OPTION_CAPTURE_MODE, captureModel);
            return this;
        }


        public Builder(MutableOptionBundle mutableOptionBundle) {
            this.optionsBundles = mutableOptionBundle;
        }

        @Override
        public ImageCaptureActionConfig build() {
            return new ImageCaptureActionConfig(optionsBundles);
        }

        @Override
        public Builder setLensFacing(@NonNull CameraModule.LensFacing lensFacing) {
            optionsBundles.insertOption(CameraDeviceConfig.OPTION_LENSFACING, lensFacing);

            return this;
        }

        @Override
        public Builder setTargetAspectRatio(@NonNull AspectRatio aspectRatio) {
            optionsBundles.insertOption(ImageOutputConfig.OPTION_TARGET_ASPECT_RATIO, aspectRatio);

            return this;
        }

        @Override
        public Builder setTargetRotation(int rotation) {
            optionsBundles.insertOption(ImageOutputConfig.OPTION_TARGET_ROTATION, rotation);
            return this;
        }

        @Override
        public Builder setTargetResolution(@NonNull Size resolution) {
            optionsBundles.insertOption(ImageOutputConfig.OPTION_TARGET_RESOLUTION, resolution);

            return this;
        }
    }

}
