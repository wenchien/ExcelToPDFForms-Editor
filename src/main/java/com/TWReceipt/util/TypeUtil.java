package com.TWReceipt.util;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class TypeUtil {

    public static final TypeUtil INSTANCE = new TypeUtil();

    private TypeUtil () {

    }

    public static TypeUtil getInstance() {
        return INSTANCE;
    }


    public <T> Type getMapType(T t) {
        return new TypeToken<T>() {}.getType();
    }
}
