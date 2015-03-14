package com.amazonaws.services.dynamodbv2.nio.webservice;

/**
 * Created by amiroshn
 */

import com.amazonaws.internal.config.HttpClientConfig;
import com.amazonaws.internal.config.InternalConfig;

/**
 * An internal service name factory.
 */
enum ServiceNameFactory {
    ;

    /**
     * Returns the serviceName config for the specified service client, or null if no explicit
     * config is found.
     */
    static String getServiceName(String httpClientName) {
        InternalConfig config = InternalConfig.Factory.getInternalConfig();
        HttpClientConfig clientConfig = config.getHttpClientConfig(httpClientName);
        return clientConfig == null ? null : clientConfig.getServiceName();
    }

    /**
     * Returns the regionMetadataServiceName config for the specified service client, or null if no
     * explicit config is found.
     */
    static String getServiceNameInRegionMetadata(String httpClientName) {
        InternalConfig config = InternalConfig.Factory.getInternalConfig();
        HttpClientConfig clientConfig = config.getHttpClientConfig(httpClientName);
        return clientConfig == null ? null : clientConfig.getRegionMetadataServiceName();
    }

}
