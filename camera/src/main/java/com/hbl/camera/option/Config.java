package com.hbl.camera.option;

import java.util.Set;

public interface Config {
    boolean containsOption(Option<?> id);

    <ValueT> ValueT retrieveOption(Option<ValueT> id);

    Set<Option<?>> listOptions();

}
