package com.enremmeta.rtb.spi.providers.integral;

import java.io.IOException;
import java.nio.CharBuffer;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.spi.providers.integral.result.ResultListener;

public class IntegralClient {

    private final String clientId;

    private final HttpHost httpHost;

    private CloseableHttpAsyncClient httpAsyncClient;

    public IntegralClient(String host, Integer port, String clientId) {

        this.clientId = clientId;

        httpHost = new HttpHost(host, port);

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000)
                        .setConnectTimeout(3000).build();
        httpAsyncClient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();

        httpAsyncClient.start();
    }

    public void send(final String dataMethod, final String url,
                    final ResultListener resultListener) {

        final String path = "/db2/clientId/" + clientId + "/" + dataMethod + "?adsafe_url=" + url;

        HttpGet request = new HttpGet(path);
        httpAsyncClient.execute(new BasicAsyncRequestProducer(httpHost, request),
                        new ResponseConsumer(request, url, resultListener), null);

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

    class ResponseConsumer extends AsyncCharConsumer<Boolean> {

        private final HttpRequest request;

        private final String url;

        private final ResultListener resultListener;

        private Integer statusCode;

        ResponseConsumer(final HttpRequest request, final String url,
                        final ResultListener resultListener) {
            this.request = request;
            this.url = url;
            this.resultListener = resultListener;
        }

        @Override
        protected void onResponseReceived(final HttpResponse response) {
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                resultListener.failure(url, statusCode.toString());
            }
        }

        @Override
        protected void onCharReceived(final CharBuffer buf, final IOControl ioctrl)
                        throws IOException {
            if (statusCode == 200)
                resultListener.success(url, buf.toString());
            else
                resultListener.failure(url, statusCode.toString());
        }

        @Override
        protected Boolean buildResult(final HttpContext context) {
            return Boolean.TRUE;
        }
    }


}
