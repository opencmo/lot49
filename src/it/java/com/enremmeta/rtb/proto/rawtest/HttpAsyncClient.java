package com.enremmeta.rtb.proto.rawtest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Future;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import com.amazonaws.util.StringInputStream;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.google.protobuf.Message;
import com.google.protos.adx.NetworkBid;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;

public class HttpAsyncClient {

    private final String host;

    private CloseableHttpAsyncClient httpAsyncClient;

    public HttpAsyncClient(String host) {

        this.host = host;

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000)
                        .setConnectTimeout(3000).build();

        httpAsyncClient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig)
                        .setMaxConnPerRoute(1000).build();

        httpAsyncClient.start();

    }

    public Future<HttpResponse> sendAdx(String json, final ResultListener resultListener) {

        final String path;

        path = host + Lot49Constants.EXCHANGE_ADX;

        try {

            Message.Builder requestBuilder = NetworkBid.BidRequest.newBuilder();
            JsonFormat jf = new JsonFormat();
            jf.merge(new StringInputStream(json), requestBuilder);

            NetworkBid.BidRequest req = (NetworkBid.BidRequest) requestBuilder.build();

            HttpEntity entity = new ByteArrayEntity(req.toByteArray());

            HttpPost request = new HttpPost(path);
            request.setHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
            request.setHeader("Accept", MediaType.APPLICATION_OCTET_STREAM);
            request.setHeader("X-OPENRTB-VERSION", "2.1");
            request.setEntity(entity);

            if (httpAsyncClient.isRunning()) {
                return httpAsyncClient.execute(request, new FutureCallback<HttpResponse>() {

                    @Override
                    public void completed(final HttpResponse response) {
                        if (response.getStatusLine().getStatusCode() == 200) {
                            try {
                                resultListener.success(json,
                                                NetworkBid.BidResponse
                                                                .parseFrom(response.getEntity()
                                                                                .getContent())
                                                                .toString());
                            } catch (IOException e) {
                                resultListener.failure(json);
                            }

                        } else {
                            resultListener.failure(json);
                        }
                    }

                    @Override
                    public void failed(final Exception ex) {
                        // ex.printStackTrace();
                        resultListener.failure(json);
                    }

                    @Override
                    public void cancelled() {
                        resultListener.failure(json);
                    }

                });
            } else {
                resultListener.failure(json);
                httpAsyncClient.start();
                return null;
            }
        } catch (ParseException e) {
            // System.out.println("ParseException: " + e.getMessage() + "\n JSON: " + json);
            // e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // e.printStackTrace();
        } catch (IOException e) {
            // e.printStackTrace();
        }
        return null;
    }

    public Future<HttpResponse> sendJsonByPost(String requestType, String json,
                    final ResultListener resultListener) {

        final String path;

        path = host + requestType;

        HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

        HttpPost request = new HttpPost(path);
        request.setHeader("Content-Type", MediaType.APPLICATION_JSON);
        request.setHeader("Accept", MediaType.APPLICATION_JSON);
        request.setEntity(entity);

        if (httpAsyncClient.isRunning()) {
            return httpAsyncClient.execute(request, new FutureCallback<HttpResponse>() {

                @Override
                public void completed(final HttpResponse response) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        try {
                            resultListener.success(json, IOUtils
                                            .toString(response.getEntity().getContent(), "UTF-8"));
                        } catch (IOException e) {
                            resultListener.failure(json);
                        }

                    } else {
                        resultListener.failure(json);
                    }
                }

                @Override
                public void failed(final Exception ex) {
                    resultListener.failure(json);
                }

                @Override
                public void cancelled() {
                    resultListener.failure(json);
                }

            });
        } else {
            resultListener.failure(json);
            httpAsyncClient.start();
            return null;
        }
    }

    public void shutdown() {
        if (httpAsyncClient != null) {
            try {
                httpAsyncClient.close();
            } catch (IOException e) {
                httpAsyncClient = null;
            }
            httpAsyncClient = null;
        }
    }
}
