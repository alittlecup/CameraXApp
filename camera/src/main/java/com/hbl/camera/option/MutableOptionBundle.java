package com.hbl.camera.option;

import androidx.camera.core.Config;

import java.util.Comparator;
import java.util.TreeMap;

public final class MutableOptionBundle extends OptionBundle {

    private static final Comparator<Option<?>> ID_COMPARAE = new Comparator<Option<?>>() {
        @Override
        public int compare(Option<?> o1, Option<?> o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };

    private MutableOptionBundle(TreeMap<Option<?>, Object> treeMap) {
        super(treeMap);
    }

    public static MutableOptionBundle create() {
        return new MutableOptionBundle(new TreeMap<>(ID_COMPARAE));
    }

    public <ValueT> ValueT removeOption(Option<ValueT> opt) {
        @SuppressWarnings("unchecked")
        ValueT value = (ValueT) mOptions.remove(opt);
        return value;
    }

    public <ValueT> void insertOption(Option<ValueT> opt, ValueT value) {
        mOptions.put(opt, value);
    }
}
