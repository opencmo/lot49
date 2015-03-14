package com.amazonaws.services.dynamodbv2.nio.http.apache.client.impl;

import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.conn.NHttpClientConnectionManager;

/**
 *
 * Created by amiroshn
 *
 * An extension of Apache's HttpClient that expose the connection manager associated with the
 * client.
 */
public interface ConnectionManagerAwareHttpClient extends HttpAsyncClient {

    /**
     * Returns the {@link org.apache.http.nio.conn.NHttpClientConnectionManager associated with the
     * http client.
     */
    NHttpClientConnectionManager getHttpClientConnectionManager();

    void start();

    void close();
}
