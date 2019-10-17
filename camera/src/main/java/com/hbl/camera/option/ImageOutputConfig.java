package com.hbl.camera.option;

import android.view.Surface;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface ImageOutputConfig {

    Option<AspectRatio> OPTION_TARGET_ASPECT_RATIO = Option.create("camerax.core.imageOutput.targetAspectRatio", AspectRatio.class);
    Option<Integer> OPTION_TARGET_ROTATION = Option.create("camerax.core.imageOutput.targetRotation", int.class);
    Option<Size> OPTION_TARGET_RESOLUTION = Option.create("camerax.core.imageOutput.targetResolution", Size.class);


    Size getTargetResolution();

    int getTargetRotation();

    AspectRatio getTargetAspectRatio();


    interface Builder<B> {

        B setTargetAspectRatio(@NonNull AspectRatio aspectRatio);

        B setTargetRotation(@RotationValue int rotation);

        B setTargetResolution(@NonNull Size resolution);


    }

    @IntDef({Surface.ROTATION_0, Surface.ROTATION_90, Surface.ROTATION_180, Surface.ROTATION_270})
    @Retention(RetentionPolicy.SOURCE)
    @interface RotationValue {
    }


}


