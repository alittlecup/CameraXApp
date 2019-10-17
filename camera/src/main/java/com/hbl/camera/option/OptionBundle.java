package com.hbl.camera.option;

import android.graphics.Path;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;

public class OptionBundle implements Config {

    private static final OptionBundle EMPTY_BUNDLE = new OptionBundle(new TreeMap<>(new Comparator<Option<?>>() {
        @Override
        public int compare(Option<?> o1, Option<?> o2) {
            return o1.getId().compareTo(o2.getId());
        }
    }));
    protected final TreeMap<Option<?>, Object> mOptions;

    OptionBundle(TreeMap<Option<?>, Object> options) {
        this.mOptions = options;
    }

    @Override
    public boolean containsOption(Option<?> id) {
        return mOptions.containsKey(id);
    }

    @Override
    public <ValueT> ValueT retrieveOption(Option<ValueT> id) {
        if (!mOptions.containsKey(id)) {
            throw new IllegalArgumentException("Option does not exist: " + id);
        }

        @SuppressWarnings("unchecked")
        ValueT value = (ValueT) mOptions.get(id);

        return value;
    }

    @Override
    public Set<Option<?>> listOptions() {
        return Collections.unmodifiableSet(mOptions.keySet());
    }
}
