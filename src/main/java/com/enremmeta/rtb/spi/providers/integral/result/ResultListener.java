package com.enremmeta.rtb.spi.providers.integral.result;

public interface ResultListener {

    void success(String url, String content);

    void cancellation();

    void failure(String url, String errorMessage);
}
