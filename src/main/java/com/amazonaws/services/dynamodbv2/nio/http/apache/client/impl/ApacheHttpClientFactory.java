package com.amazonaws.services.dynamodbv2.nio.http.apache.client.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.conn.NHttpClientConnectionManager;

import com.amazonaws.http.apache.utils.ApacheUtils;
import com.amazonaws.http.client.ConnectionManagerFactory;
import com.amazonaws.http.client.HttpClientFactory;
import com.amazonaws.http.conn.SdkConnectionKeepAliveStrategy;
import com.amazonaws.http.settings.HttpClientSettings;

/**
 *
 * Created by amiroshn
 *
 * Factory class that builds the apache http client from the settings.
 */
public class ApacheHttpClientFactory implements HttpClientFactory<SdkHttpClient> {

    private static final Log LOG = LogFactory.getLog(ApacheHttpClientFactory.class);

    private final ConnectionManagerFactory<NHttpClientConnectionManager> cmFactory =
                    new ApacheConnectionManagerFactory();

    @Override
    public SdkHttpClient create(HttpClientSettings settings) {

        final HttpAsyncClientBuilder clientBuilder = HttpAsyncClientBuilder.create();
        // Note that it is important we register the original connection manager with the
        // IdleConnectionReaper as it's required for the successful deregistration of managers
        // from the reaper. See https://github.com/aws/aws-sdk-java/issues/722.
        final NHttpClientConnectionManager cm = cmFactory.create(settings);

        clientBuilder.setKeepAliveStrategy(buildKeepAliveStrategy(settings))
                        .setConnectionManager(cm);

        addProxyConfig(clientBuilder, settings);

        // By default http client enables Gzip compression. So we disable it
        // here.
        // Apache HTTP client removes Content-Length, Content-Encoding and
        // Content-MD5 headers when Gzip compression is enabled. Currently
        // this doesn't affect S3 or Glacier which exposes these headers.
        //
        // if (!(settings.useGzip())) {
        // clientBuilder.disableContentCompression();
        // }

        final SdkHttpClient httpClient = new SdkHttpClient(clientBuilder.build(), cm);

        // if (settings.useReaper()) {
        // IdleConnectionReaper.registerConnectionManager(cm);
        // }

        return httpClient;
    }

    private void addProxyConfig(HttpAsyncClientBuilder builder, HttpClientSettings settings) {
        if (isProxyEnabled(settings)) {

            LOG.info("Configuring Proxy. Proxy Host: " + settings.getProxyHost() + " "
                            + "Proxy Port: " + settings.getProxyPort());
            builder.setProxy(new HttpHost(settings.getProxyHost(), settings.getProxyPort()));

            if (isAuthenticatedProxy(settings)) {
                builder.setDefaultCredentialsProvider(
                                ApacheUtils.newProxyCredentialsProvider(settings));
            }
        }
    }

    private ConnectionKeepAliveStrategy buildKeepAliveStrategy(HttpClientSettings settings) {
        return settings.getMaxIdleConnectionTime() > 0
                        ? new SdkConnectionKeepAliveStrategy(settings.getMaxIdleConnectionTime())
                        : null;
    }

    private boolean isAuthenticatedProxy(HttpClientSettings settings) {
        return settings.getProxyUsername() != null && settings.getProxyPassword() != null;
    }

    private boolean isProxyEnabled(HttpClientSettings settings) {
        return settings.getProxyHost() != null && settings.getProxyPort() > 0;
    }
}
/*
 * public class AsyncClientConfiguration {
 * 
 * public final static void main(String[] args) throws Exception {
 * 
 * // Use custom message parser / writer to customize the way HTTP // messages are parsed from and
 * written out to the data stream. NHttpMessageParserFactory<HttpResponse> responseParserFactory =
 * new DefaultHttpResponseParserFactory() {
 * 
 * @Override public NHttpMessageParser<HttpResponse> create( final SessionInputBuffer buffer, final
 * MessageConstraints constraints) { LineParser lineParser = new BasicLineParser() {
 * 
 * @Override public Header parseHeader(final CharArrayBuffer buffer) { try { return
 * super.parseHeader(buffer); } catch (ParseException ex) { return new
 * BasicHeader(buffer.toString(), null); } }
 * 
 * }; return new DefaultHttpResponseParser( buffer, lineParser, DefaultHttpResponseFactory.INSTANCE,
 * constraints); }
 * 
 * }; NHttpMessageWriterFactory<HttpRequest> requestWriterFactory = new
 * DefaultHttpRequestWriterFactory();
 * 
 * // Use a custom connection factory to customize the process of // initialization of outgoing HTTP
 * connections. Beside standard connection // configuration parameters HTTP connection factory can
 * define message // parser / writer routines to be employed by individual connections.
 * NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory = new
 * ManagedNHttpClientConnectionFactory( requestWriterFactory, responseParserFactory,
 * HeapByteBufferAllocator.INSTANCE);
 * 
 * // Client HTTP connection objects when fully initialized can be bound to // an arbitrary network
 * socket. The process of network socket initialization, // its connection to a remote address and
 * binding to a local one is controlled // by a connection socket factory.
 * 
 * // SSL context for secure connections can be created either based on // system or application
 * specific properties. SSLContext sslcontext = SSLContexts.createSystemDefault(); // Use custom
 * hostname verifier to customize SSL hostname verification. HostnameVerifier hostnameVerifier = new
 * DefaultHostnameVerifier();
 * 
 * // Create a registry of custom connection session strategies for supported // protocol schemes.
 * Registry<SchemeIOSessionStrategy> sessionStrategyRegistry =
 * RegistryBuilder.<SchemeIOSessionStrategy>create() .register("http",
 * NoopIOSessionStrategy.INSTANCE) .register("https", new SSLIOSessionStrategy(sslcontext,
 * hostnameVerifier)) .build();
 * 
 * // Use custom DNS resolver to override the system DNS resolution. DnsResolver dnsResolver = new
 * SystemDefaultDnsResolver() {
 * 
 * @Override public InetAddress[] resolve(final String host) throws UnknownHostException { if
 * (host.equalsIgnoreCase("myhost")) { return new InetAddress[] { InetAddress.getByAddress(new
 * byte[] {127, 0, 0, 1}) }; } else { return super.resolve(host); } }
 * 
 * };
 * 
 * // Create I/O reactor configuration IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
 * .setIoThreadCount(Runtime.getRuntime().availableProcessors()) .setConnectTimeout(30000)
 * .setSoTimeout(30000) .build();
 * 
 * // Create a custom I/O reactort ConnectingIOReactor ioReactor = new
 * DefaultConnectingIOReactor(ioReactorConfig);
 * 
 * // Create a connection manager with custom configuration. PoolingNHttpClientConnectionManager
 * connManager = new PoolingNHttpClientConnectionManager( ioReactor, connFactory,
 * sessionStrategyRegistry, dnsResolver);
 * 
 * // Create message constraints MessageConstraints messageConstraints = MessageConstraints.custom()
 * .setMaxHeaderCount(200) .setMaxLineLength(2000) .build(); // Create connection configuration
 * ConnectionConfig connectionConfig = ConnectionConfig.custom()
 * .setMalformedInputAction(CodingErrorAction.IGNORE)
 * .setUnmappableInputAction(CodingErrorAction.IGNORE) .setCharset(Consts.UTF_8)
 * .setMessageConstraints(messageConstraints) .build(); // Configure the connection manager to use
 * connection configuration either // by default or for a specific host.
 * connManager.setDefaultConnectionConfig(connectionConfig); connManager.setConnectionConfig(new
 * HttpHost("somehost", 80), ConnectionConfig.DEFAULT);
 * 
 * // Configure total max or per route limits for persistent connections // that can be kept in the
 * pool or leased by the connection manager. connManager.setMaxTotal(100);
 * connManager.setDefaultMaxPerRoute(10); connManager.setMaxPerRoute(new HttpRoute(new
 * HttpHost("somehost", 80)), 20);
 * 
 * // Use custom cookie store if necessary. CookieStore cookieStore = new BasicCookieStore(); // Use
 * custom credentials provider if necessary. CredentialsProvider credentialsProvider = new
 * BasicCredentialsProvider(); // Create global request configuration RequestConfig
 * defaultRequestConfig = RequestConfig.custom() .setCookieSpec(CookieSpecs.DEFAULT)
 * .setExpectContinueEnabled(true) .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM,
 * AuthSchemes.DIGEST)) .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)) .build();
 * 
 * // Create an HttpClient with the given custom dependencies and configuration.
 * CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
 * .setConnectionManager(connManager) .setDefaultCookieStore(cookieStore)
 * .setDefaultCredentialsProvider(credentialsProvider) .setProxy(new HttpHost("myproxy", 8080))
 * .setDefaultRequestConfig(defaultRequestConfig) .build();
 * 
 * try { HttpGet httpget = new HttpGet("http://localhost/"); // Request configuration can be
 * overridden at the request level. // They will take precedence over the one set at the client
 * level. RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig)
 * .setSocketTimeout(5000) .setConnectTimeout(5000) .setConnectionRequestTimeout(5000) .setProxy(new
 * HttpHost("myotherproxy", 8080)) .build(); httpget.setConfig(requestConfig);
 * 
 * // Execution context can be customized locally. HttpClientContext localContext =
 * HttpClientContext.create(); // Contextual attributes set the local context level will take //
 * precedence over those set at the client level. localContext.setCookieStore(cookieStore);
 * localContext.setCredentialsProvider(credentialsProvider);
 * 
 * System.out.println("Executing request " + httpget.getRequestLine());
 * 
 * httpclient.start();
 * 
 * // Pass local context as a parameter Future<HttpResponse> future = httpclient.execute(httpget,
 * localContext, null);
 * 
 * // Please note that it may be unsafe to access HttpContext instance // while the request is still
 * being executed
 * 
 * HttpResponse response = future.get(); System.out.println("Response: " +
 * response.getStatusLine());
 * 
 * // Once the request has been executed the local context can // be used to examine updated state
 * and various objects affected // by the request execution.
 * 
 * // Last executed request localContext.getRequest(); // Execution route
 * localContext.getHttpRoute(); // Target auth state localContext.getTargetAuthState(); // Proxy
 * auth state localContext.getTargetAuthState(); // Cookie origin localContext.getCookieOrigin(); //
 * Cookie spec used localContext.getCookieSpec(); // User security token
 * localContext.getUserToken(); } finally { httpclient.close(); } } }
 */
