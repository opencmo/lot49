package com.enremmeta.rtb.spi.providers.integral;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.spi.providers.integral.result.ResultListener;

public class IntegralClient2 {

    private final String clientId;

    private final String host;

    private CloseableHttpAsyncClient httpAsyncClient;

    public IntegralClient2(String host, String clientId) {
        this.host = host;
        this.clientId = clientId;

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3 * 1000)
                        .setConnectTimeout(3 * 1000).build();

        httpAsyncClient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();

        httpAsyncClient.start();
    }

    public IntegralClient2(String host, String clientId, int maxConnPerRoute) {
        this.host = host;
        this.clientId = clientId;

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3 * 1000)
                        .setConnectTimeout(3 * 1000).build();

        httpAsyncClient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig)
                        .setMaxConnPerRoute(maxConnPerRoute).build();

        httpAsyncClient.start();
    }

    public Future<HttpResponse> send(final String dataMethod, final String url,
                    final ResultListener resultListener) {

        final String path;
        try {
            path = host + "/db2/client/" + clientId + "/" + dataMethod + ".json?adsafe_url="
                            + URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            resultListener.failure(url, e.toString());
            return null;
        }

        HttpGet request = new HttpGet(path);
        if (httpAsyncClient.isRunning()) {
            return httpAsyncClient.execute(request, new FutureCallback<HttpResponse>() {

                @Override
                public void completed(final HttpResponse response) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        try {
                            resultListener.success(url, IOUtils
                                            .toString(response.getEntity().getContent(), "UTF-8"));
                        } catch (IOException e) {
                            LogUtils.error(e);
                            resultListener.failure(url, e.toString());
                        }

                    } else {
                        resultListener.failure(url, String.valueOf(statusCode));
                    }
                }

                @Override
                public void failed(final Exception e) {
                    resultListener.failure(url, "Failed - " + e.toString());
                }

                @Override
                public void cancelled() {
                    resultListener.cancellation();
                }

            });
        } else {
            resultListener.failure(url, "HttpAsyncClient is not running.");
            httpAsyncClient.start();
            return null;
        }
    }

    public void shutdown() {
        if (httpAsyncClient != null) {
            try {
                httpAsyncClient.close();
            } catch (IOException e) {
                LogUtils.error(e);
                httpAsyncClient = null;
            }
            httpAsyncClient = null;
        }
    }
}
