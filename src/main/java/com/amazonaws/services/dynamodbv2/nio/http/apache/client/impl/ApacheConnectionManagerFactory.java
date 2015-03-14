package com.amazonaws.services.dynamodbv2.nio.http.apache.client.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.nio.conn.ManagedNHttpClientConnectionFactory;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.IOReactorException;

import com.amazonaws.http.DelegatingDnsResolver;
import com.amazonaws.http.client.ConnectionManagerFactory;
import com.amazonaws.http.settings.HttpClientSettings;

/**
 *
 * Created by amiroshn
 *
 * Factory class to create connection manager used by the apache client.
 */
public class ApacheConnectionManagerFactory
                implements ConnectionManagerFactory<NHttpClientConnectionManager> {

    private final Log LOG = LogFactory.getLog(ApacheConnectionManagerFactory.class);

    @Override
    public NHttpClientConnectionManager create(final HttpClientSettings settings) {
        try {
            final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                            .setConnectTimeout(settings.getConnectionTimeout())
                            .setSoTimeout(settings.getSocketTimeout()).build();

            final Registry<SchemeIOSessionStrategy> sessionStrategyRegistry =
                            RegistryBuilder.<SchemeIOSessionStrategy>create()
                                            .register("http", NoopIOSessionStrategy.INSTANCE)
                                            .register("https",
                                                            SSLIOSessionStrategy
                                                                            .getDefaultStrategy())
                                            .build();


            final PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(
                            new DefaultConnectingIOReactor(ioReactorConfig),
                            ManagedNHttpClientConnectionFactory.INSTANCE, sessionStrategyRegistry,
                            DefaultSchemePortResolver.INSTANCE,
                            new DelegatingDnsResolver(settings.getDnsResolver()),
                            settings.getConnectionPoolTTL(), TimeUnit.MILLISECONDS);

            cm.setDefaultMaxPerRoute(settings.getMaxConnections());
            cm.setMaxTotal(settings.getMaxConnections());
            cm.setDefaultConnectionConfig(buildConnectionConfig(settings));

            return cm;
        } catch (IOReactorException e) {
            LOG.error("Reactor creation failed", e);
        }

        return null;
    }

    private ConnectionConfig buildConnectionConfig(HttpClientSettings settings) {

        int socketBufferSize = Math.max(settings.getSocketBufferSize()[0],
                        settings.getSocketBufferSize()[1]);

        return socketBufferSize <= 0 ? null
                        : ConnectionConfig.custom().setBufferSize(socketBufferSize).build();
    }

}
