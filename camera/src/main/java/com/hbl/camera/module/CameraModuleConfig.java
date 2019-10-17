package com.hbl.camera.module;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;

import com.hbl.camera.option.CameraModule;
import com.hbl.camera.option.MutableOptionBundle;
import com.hbl.camera.option.Option;
import com.hbl.camera.option.OptionBundle;
import com.hbl.camera.option.Size;

public class CameraModuleConfig implements CameraOutputConfig {

    private final OptionBundle mOptions;

    public CameraModuleConfig(OptionBundle optionBundle) {
        this.mOptions = optionBundle;
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

    @Override
    public CameraModule.LensFacing getLensFacing() {
        return mOptions.retrieveOption(OPTION_LENSFACING);

    }

    public static class Builder implements CameraOutputConfig.Builder<Builder, CameraModuleConfig> {

        private final MutableOptionBundle mOptions;

        public Builder() {
            this(MutableOptionBundle.create());
        }

        private Builder(MutableOptionBundle mutableOptionBundle) {
            this.mOptions = mutableOptionBundle;
        }

        @Override
        public Builder setTargetAspectRatio(@NonNull AspectRatio aspectRatio) {
            mOptions.insertOption(OPTION_TARGET_ASPECT_RATIO, aspectRatio);
            return this;
        }

        @Override
        public Builder setTargetRotation(int rotation) {
            mOptions.insertOption(OPTION_TARGET_ROTATION, rotation);
            return this;
        }

        @Override
        public Builder setTargetResolution(@NonNull Size resolution) {
            mOptions.insertOption(OPTION_TARGET_RESOLUTION, resolution);
            return this;
        }

        @Override
        public Builder setLensFacing(CameraModule.LensFacing lensFacing) {
            mOptions.insertOption(OPTION_LENSFACING, lensFacing);
            return this;
        }

        @Override
        public CameraModuleConfig build() {
            return new CameraModuleConfig(mOptions);
        }
    }
}
