package com.hbl.camera.option;


public interface CameraActionConfig<T extends CameraAction> {



    interface Builder<T extends CameraAction, C extends CameraActionConfig<T>, B> {

        C build();
    }
}
