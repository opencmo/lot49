package com.enremmeta.rtb.proto.rawtest;

public interface ResultListener {

    void success(String url, String content);

    void failure(String url);
}
