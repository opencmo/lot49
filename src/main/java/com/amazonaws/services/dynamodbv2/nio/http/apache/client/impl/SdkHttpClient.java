package com.amazonaws.services.dynamodbv2.nio.http.apache.client.impl;

import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

/**
 *
 * Created by amiroshn
 *
 * An instance of {@link ConnectionManagerAwareHttpClient} that delegates all the requests to the
 * given http client.
 */
public class SdkHttpClient implements ConnectionManagerAwareHttpClient {

    private final HttpAsyncClient delegate;

    private final NHttpClientConnectionManager cm;

    public SdkHttpClient(final HttpAsyncClient delegate, final NHttpClientConnectionManager cm) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate " + "cannot be null");
        }
        if (cm == null) {
            throw new IllegalArgumentException("connection manager " + "cannot be null");
        }
        this.delegate = delegate;
        this.cm = cm;
    }

    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer,
                    HttpAsyncResponseConsumer<T> responseConsumer, HttpContext context,
                    FutureCallback<T> callback) {
        return delegate.execute(requestProducer, responseConsumer, context, callback);
    }

    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer,
                    HttpAsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback) {
        return delegate.execute(requestProducer, responseConsumer, callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpHost target, HttpRequest request, HttpContext context,
                    FutureCallback<HttpResponse> callback) {
        return delegate.execute(target, request, context, callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpHost target, HttpRequest request,
                    FutureCallback<HttpResponse> callback) {
        return delegate.execute(target, request, callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpUriRequest request, HttpContext context,
                    FutureCallback<HttpResponse> callback) {
        return delegate.execute(request, context, callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpUriRequest request,
                    FutureCallback<HttpResponse> callback) {
        return delegate.execute(request, callback);
    }

    @Override
    public NHttpClientConnectionManager getHttpClientConnectionManager() {
        return cm;
    }

    @Override
    public void start() {
        if (delegate instanceof CloseableHttpAsyncClient) {
            ((CloseableHttpAsyncClient) delegate).start();
        }
    }

    @Override
    public void close() {
        try {
            if (delegate instanceof CloseableHttpAsyncClient) {
                ((CloseableHttpAsyncClient) delegate).close();
            }
            cm.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
