package com.hbl.camera.option.preview;


import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;

import com.hbl.camera.option.CameraActionConfig;
import com.hbl.camera.option.CameraDeviceConfig;
import com.hbl.camera.option.CameraModule;
import com.hbl.camera.option.ImageOutputConfig;
import com.hbl.camera.option.MutableOptionBundle;
import com.hbl.camera.option.OptionBundle;
import com.hbl.camera.option.Size;

public class PreviewActionConfig implements CameraActionConfig<PreviewAction>, ImageOutputConfig, CameraDeviceConfig {


    private final OptionBundle mOptions;

    public PreviewActionConfig(OptionBundle config) {
        this.mOptions = config;
    }

    @Override
    public CameraModule.LensFacing getLensFacing() {
        return mOptions.retrieveOption(OPTION_LENSFACING);
    }

    @Override
    public Size getTargetResolution() {
        return mOptions.retrieveOption(OPTION_TARGET_RESOLUTION);
    }

    @Override
    public int getTargetRotation() {
        return mOptions.retrieveOption(OPTION_TARGET_ROTATION);

    }

    @Override
    public AspectRatio getTargetAspectRatio() {
        return mOptions.retrieveOption(OPTION_TARGET_ASPECT_RATIO);

    }


    public static class Builder implements CameraActionConfig.Builder<PreviewAction, PreviewActionConfig, Builder>
            , ImageOutputConfig.Builder<Builder>
            , CameraDeviceConfig.Builder<Builder> {

        private final MutableOptionBundle mutableOptionBundle;

        public Builder() {
            this(MutableOptionBundle.create());
        }

        public Builder(MutableOptionBundle mutableOptionBundle) {
            this.mutableOptionBundle = mutableOptionBundle;
        }

        @Override
        public PreviewActionConfig build() {
            return new PreviewActionConfig(mutableOptionBundle);
        }

        @Override
        public Builder setLensFacing(@NonNull CameraModule.LensFacing lensFacing) {
            mutableOptionBundle.insertOption(OPTION_LENSFACING, lensFacing);
            return this;
        }

        @Override
        public Builder setTargetAspectRatio(@NonNull AspectRatio aspectRatio) {
            mutableOptionBundle.insertOption(OPTION_TARGET_ASPECT_RATIO, aspectRatio);
            return this;
        }

        @Override
        public Builder setTargetRotation(int rotation) {
            mutableOptionBundle.insertOption(OPTION_TARGET_ROTATION, rotation);
            return this;
        }

        @Override
        public Builder setTargetResolution(@NonNull Size resolution) {
            mutableOptionBundle.insertOption(OPTION_TARGET_RESOLUTION, resolution);
            return this;
        }
    }
}
