package com.enremmeta.rtb.spi.providers.integral.result;

import java.util.Date;

import com.enremmeta.rtb.LogUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public abstract class ResultListenerAdapter<T> implements ResultListener {

    private static final GsonBuilder gsonBuilder;

    static {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new DateTypeAdapter());
    }

    private Class<T> type;

    public ResultListenerAdapter(Class<T> type) {
        this.type = type;
    }

    public void success(String url, String content) {
        try {
            T dto = gsonBuilder.create().fromJson(content, type);

            extSuccess(url, dto);
        } catch (JsonParseException e) {
            LogUtils.error(e);
            extFailure(url, e.getMessage());
        }
    }

    public void failure(String url, String errorMessage) {
        extFailure(url, errorMessage);
    }

    public abstract void extSuccess(String url, T obj);

    public abstract void extFailure(String url, String errorMessage);
}
