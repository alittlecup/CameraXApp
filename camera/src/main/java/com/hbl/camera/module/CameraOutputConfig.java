package com.hbl.camera.module;

import android.view.Surface;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;

import com.hbl.camera.option.CameraModule;
import com.hbl.camera.option.ImageOutputConfig;
import com.hbl.camera.option.Option;
import com.hbl.camera.option.Size;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface CameraOutputConfig {

    Option<AspectRatio> OPTION_TARGET_ASPECT_RATIO = Option.create("camerax.core.imageOutput.targetAspectRatio", AspectRatio.class);
    Option<Integer> OPTION_TARGET_ROTATION = Option.create("camerax.core.imageOutput.targetRotation", int.class);
    Option<Size> OPTION_TARGET_RESOLUTION = Option.create("camerax.core.imageOutput.targetResolution", Size.class);
    Option<CameraModule.LensFacing> OPTION_LENSFACING = Option.create("camerax.core.camera.lensFacing", CameraModule.LensFacing.class);



    Size getTargetResolution();

    int getTargetRotation();

    AspectRatio getTargetAspectRatio();

    CameraModule.LensFacing getLensFacing();


    interface Builder<B, C extends CameraOutputConfig> {

        B setTargetAspectRatio(@NonNull AspectRatio aspectRatio);

        B setTargetRotation(@RotationValue int rotation);

        B setTargetResolution(@NonNull Size resolution);

        B setLensFacing(CameraModule.LensFacing lensFacing);


        C build();

    }


    @IntDef({Surface.ROTATION_0, Surface.ROTATION_90, Surface.ROTATION_180, Surface.ROTATION_270})
    @Retention(RetentionPolicy.SOURCE)
    @interface RotationValue {
    }
}
