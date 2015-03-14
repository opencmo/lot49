package com.amazonaws.services.dynamodbv2.nio.http;

import static com.amazonaws.util.IOUtils.closeQuietly;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.AmazonWebServiceResponse;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Request;
import com.amazonaws.RequestClientOptions;
import com.amazonaws.RequestClientOptions.Marker;
import com.amazonaws.Response;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.SDKGlobalTime;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.Signer;
import com.amazonaws.handlers.CredentialsRequestHandler;
import com.amazonaws.handlers.RequestHandler2;
import com.amazonaws.http.HttpResponseHandler;
import com.amazonaws.http.apache.request.impl.ApacheHttpRequestFactory;
import com.amazonaws.http.apache.utils.ApacheUtils;
import com.amazonaws.http.client.HttpClientFactory;
import com.amazonaws.http.request.HttpRequestFactory;
import com.amazonaws.http.settings.HttpClientSettings;
import com.amazonaws.http.timers.client.ClientExecutionTimeoutException;
import com.amazonaws.http.timers.client.SdkInterruptedException;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.retry.internal.AuthRetryParameters;
import com.amazonaws.services.dynamodbv2.nio.handlers.ResultHandler;
import com.amazonaws.services.dynamodbv2.nio.http.apache.client.impl.ApacheHttpClientFactory;
import com.amazonaws.services.dynamodbv2.nio.http.apache.client.impl.ConnectionManagerAwareHttpClient;
import com.amazonaws.services.dynamodbv2.nio.http.apache.client.impl.SdkHttpClient;
import com.amazonaws.util.AWSRequestMetrics;
import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.ImmutableMapParameter;
import com.amazonaws.util.ResponseMetadataCache;

/**
 * Created by amiroshn
 */
@ThreadSafe
public class AmazonHttpClient {
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_SDK_TRANSACTION_ID = "amz-sdk-invocation-id";

    /**
     * Logger for more detailed debugging information, that might not be as useful for end users
     * (ex: HTTP client configuration, etc).
     */
    static final Log log = LogFactory.getLog(AmazonHttpClient.class);

    /**
     * Logger providing detailed information on requests/responses. Users can enable this logger to
     * get access to AWS request IDs for responses, individual requests and parameters sent to AWS,
     * etc.
     */
    private static final Log requestLog = LogFactory.getLog("com.amazonaws.request");

    private static final HttpClientFactory<SdkHttpClient> httpClientFactory =
                    new ApacheHttpClientFactory();

    static {
        // Customers have reported XML parsing issues with the following
        // JVM versions, which don't occur with more recent versions, so
        // if we detect any of these, give customers a heads up.
        // https://bugs.openjdk.java.net/browse/JDK-8028111
        List<String> problematicJvmVersions =
                        Arrays.asList("1.6.0_06", "1.6.0_13", "1.6.0_17", "1.6.0_65", "1.7.0_45");
        String jvmVersion = System.getProperty("java.version");
        if (problematicJvmVersions.contains(jvmVersion)) {
            log.warn("Detected a possible problem with the current JVM version (" + jvmVersion
                            + ").  If you experience XML parsing problems using the SDK, try upgrading to a more recent JVM update.");
        }
    }

    private final HttpRequestFactory<HttpRequestBase> httpRequestFactory =
                    new ApacheHttpRequestFactory();

    /**
     * Internal client for sending HTTP requests
     */
    private ConnectionManagerAwareHttpClient httpClient;

    /**
     * Client configuration options, such as proxy httpClientSettings, max retries, etc.
     */
    private final ClientConfiguration config;

    /**
     * Client configuration options, such as proxy httpClientSettings, max retries, etc.
     */
    private final HttpClientSettings httpClientSettings;

    /**
     * Cache of metadata for recently executed requests for diagnostic purposes
     */
    private final ResponseMetadataCache responseMetadataCache;

    /**
     * A request metric collector used specifically for this httpClientSettings client; or null if
     * there is none. This collector, if specified, always takes precedence over the one specified
     * at the AWS SDK level.
     *
     * @see com.amazonaws.metrics.AwsSdkMetrics
     */
    private final RequestMetricCollector requestMetricCollector;

    /**
     * The time difference in seconds between this client and AWS.
     */
    private volatile int timeOffset = SDKGlobalTime.getGlobalTimeOffset();

    /**
     * Constructs a new AWS client using the specified client configuration options (ex: max retry
     * attempts, proxy httpClientSettings, etc).
     *
     * @param config
     *            Configuration options specifying how this client will communicate with AWS (ex:
     *            proxy httpClientSettings, retry count, etc.).
     */
    public AmazonHttpClient(ClientConfiguration config) {
        this(config, null);
    }

    /**
     * Constructs a new AWS client using the specified client configuration options (ex: max retry
     * attempts, proxy httpClientSettings, etc), and request metric collector.
     *
     * @param config
     *            Configuration options specifying how this client will communicate with AWS (ex:
     *            proxy httpClientSettings, retry count, etc.).
     * @param requestMetricCollector
     *            client specific request metric collector, which takes precedence over the one at
     *            the AWS SDK level; or null if there is none.
     */
    public AmazonHttpClient(ClientConfiguration config,
                    RequestMetricCollector requestMetricCollector) {
        this(config, requestMetricCollector, false);
    }

    /**
     * Constructs a new AWS client using the specified client configuration options (ex: max retry
     * attempts, proxy httpClientSettings, etc), and request metric collector.
     *
     * @param config
     *            Configuration options specifying how this client will communicate with AWS (ex:
     *            proxy httpClientSettings, retry count, etc.).
     * @param requestMetricCollector
     *            client specific request metric collector, which takes precedence over the one at
     *            the AWS SDK level; or null if there is none.
     */
    public AmazonHttpClient(ClientConfiguration config,
                    RequestMetricCollector requestMetricCollector,
                    boolean useBrowserCompatibleHostNameVerifier) {
        this(config, requestMetricCollector,
                        HttpClientSettings.adapt(config, useBrowserCompatibleHostNameVerifier));
        this.httpClient = httpClientFactory.create(this.httpClientSettings);
        this.httpClient.start();
    }

    private AmazonHttpClient(ClientConfiguration clientConfig,
                    RequestMetricCollector requestMetricCollector,
                    HttpClientSettings httpClientSettings) {
        this.config = clientConfig;
        this.httpClientSettings = httpClientSettings;
        this.requestMetricCollector = requestMetricCollector;
        this.responseMetadataCache =
                        new ResponseMetadataCache(clientConfig.getResponseMetadataCacheSize());
    }

    /**
     * Appends the given user-agent string to the existing one and returns it.
     */
    private static String createUserAgentString(String existingUserAgentString, String userAgent) {
        if (existingUserAgentString.contains(userAgent)) {
            return existingUserAgentString;
        } else {
            return existingUserAgentString.trim() + " " + userAgent.trim();
        }
    }

    /**
     * Returns additional response metadata for an executed request. Response metadata isn't
     * considered part of the standard results returned by an operation, so it's accessed instead
     * through this diagnostic interface. Response metadata is typically used for troubleshooting
     * issues with AWS support staff when services aren't acting as expected.
     *
     * @param request
     *            A previously executed AmazonWebServiceRequest object, whose response metadata is
     *            desired.
     * @return The response metadata for the specified request, otherwise null if there is no
     *         response metadata available for the request.
     */
    public ResponseMetadata getResponseMetadataForRequest(AmazonWebServiceRequest request) {
        return responseMetadataCache.get(request);
    }

    /**
     * Executes the request and returns the result.
     *
     * @param request
     *            The AmazonWebServices request to send to the remote server
     * @param responseHandler
     *            A response handler to accept a successful response from the remote server
     * @param executionContext
     *            Additional information about the context of this web service call
     * @param asyncHandler
     *            Handler
     */
    public <T> Future<T> execute(Request<?> request,
                    HttpResponseHandler<AmazonWebServiceResponse<T>> responseHandler,
                    ExecutionContext executionContext, ResultHandler<T> asyncHandler) {
        if (executionContext == null) {
            throw new AmazonClientException(
                            "Internal SDK Error: No execution context parameter specified.");
        }
        try {
            return doExecute(request, getNonNullResponseHandler(responseHandler), executionContext,
                            asyncHandler);
        } catch (InterruptedException ie) {
            throw handleInterruptedException(executionContext, ie);
        }
    }

    /**
     * Ensures the response handler is not null. If it is this method returns a dummy response
     * handler.
     *
     * @param responseHandler
     *            Response handler passed to
     *            {@link #execute(com.amazonaws.Request, com.amazonaws.http.HttpResponseHandler, com.amazonaws.services.dynamodbv2.nio.http.ExecutionContext, com.amazonaws.services.dynamodbv2.nio.handlers.ResultHandler)}
     * @return Either original response handler or dummy response handler.
     */
    private <T> HttpResponseHandler<T> getNonNullResponseHandler(
                    HttpResponseHandler<T> responseHandler) {
        if (responseHandler != null) {
            return responseHandler;
        } else {
            // Return a Dummy, No-Op handler
            return new HttpResponseHandler<T>() {

                @Override
                public T handle(com.amazonaws.http.HttpResponse response) throws Exception {
                    return null;
                }

                @Override
                public boolean needsConnectionLeftOpen() {
                    return false;
                }
            };
        }
    }

    private <T> Future<T> doExecute(Request<?> request,
                    HttpResponseHandler<AmazonWebServiceResponse<T>> responseHandler,
                    ExecutionContext executionContext, ResultHandler<T> asyncHandler)
                    throws InterruptedException {

        final List<RequestHandler2> requestHandler2s = requestHandler2s(request, executionContext);

        setSdkTransactionId(request);
        setUserAgent(request);

        final AmazonWebServiceRequest awsreq = request.getOriginalRequest();
        // add custom headers
        final Map<String, String> customHeaders = awsreq.getCustomRequestHeaders();
        if (customHeaders != null) {
            request.getHeaders().putAll(customHeaders);
        }
        // add custom query parameters
        final Map<String, List<String>> customQueryParams = awsreq.getCustomQueryParameters();
        if (customQueryParams != null) {
            mergeQueryParameters(request, customQueryParams);
        }

        return executeHelper(request, responseHandler, executionContext, requestHandler2s,
                        asyncHandler);
    }

    /**
     * Determine if an interrupted exception is caused by the client execution timer interrupting
     * the current thread or some other task interrupting the thread for another purpose.
     *
     * @param executionContext
     * @param e
     * @return {@link com.amazonaws.http.timers.client.ClientExecutionTimeoutException} if the
     *         {@link InterruptedException} was caused by the
     *         {@link com.amazonaws.http.timers.client.ClientExecutionTimer}. Otherwise
     *         re-interrupts the current thread and returns an
     *         {@link com.amazonaws.AmazonClientException} wrapping an {@link InterruptedException}
     */
    private RuntimeException handleInterruptedException(ExecutionContext executionContext,
                    InterruptedException e) {
        if (e instanceof SdkInterruptedException) {
            if (((SdkInterruptedException) e).getResponse() != null) {
                ((SdkInterruptedException) e).getResponse().getHttpResponse().getHttpRequest()
                                .abort();
            }
        }
        if (executionContext.getClientExecutionTrackerTask().hasTimeoutExpired()) {
            // Clear the interrupt status
            Thread.interrupted();
            return new ClientExecutionTimeoutException();
        } else {
            Thread.currentThread().interrupt();
            return new AmazonClientException(e);
        }
    }

    /**
     * Check if the thread has been interrupted. If so throw an {@link InterruptedException}. Long
     * running tasks should be periodically checked if the current thread has been interrupted and
     * handle it appropriately
     *
     * @throws InterruptedException
     *             If thread has been interrupted
     */
    private void checkInterrupted() throws InterruptedException {
        checkInterrupted(null);
    }

    /**
     * Check if the thread has been interrupted. If so throw an {@link InterruptedException}. Long
     * running tasks should be periodically checked if the current thread has been interrupted and
     * handle it appropriately
     *
     * @param response
     *            Response to be closed before returning control to the caller to avoid leaking the
     *            connection.
     * @throws InterruptedException
     *             If thread has been interrupted
     */
    private void checkInterrupted(Response<?> response) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new SdkInterruptedException(response);
        }
    }

    /**
     * Merge query parameters into the given request.
     */
    private void mergeQueryParameters(Request<?> request, Map<String, List<String>> params) {
        Map<String, List<String>> existingParams = request.getParameters();
        for (Entry<String, List<String>> param : params.entrySet()) {
            String pName = param.getKey();
            List<String> pValues = param.getValue();
            existingParams.put(pName,
                            CollectionUtils.mergeLists(existingParams.get(pName), pValues));
        }
    }

    private List<RequestHandler2> requestHandler2s(Request<?> request,
                    ExecutionContext executionContext) {
        List<RequestHandler2> requestHandler2s = executionContext.getRequestHandler2s();
        if (requestHandler2s == null) {
            return Collections.emptyList();
        }
        // Apply any additional service specific request handlers that need
        // to be run
        for (RequestHandler2 requestHandler2 : requestHandler2s) {
            // If the request handler is a type of CredentialsRequestHandler,
            // then set the credentials in the request handler.
            if (requestHandler2 instanceof CredentialsRequestHandler)
                ((CredentialsRequestHandler) requestHandler2).setCredentials(
                                executionContext.getCredentialsProvider().getCredentials());
            requestHandler2.beforeRequest(request);
        }
        return requestHandler2s;
    }

    /**
     * Internal method to execute the HTTP method given.
     */
    private <T> Future<T> executeHelper(final Request<?> request,
                    HttpResponseHandler<AmazonWebServiceResponse<T>> responseHandler,
                    final ExecutionContext executionContext, List<RequestHandler2> requestHandlers,
                    ResultHandler<T> asyncHandler) throws InterruptedException {

        checkInterrupted();

        final ExecOneRequestParams execOneParams = new ExecOneRequestParams();

        execOneParams.initPerRetry();

        if (execOneParams.redirectedURI != null) {
            /*
             * [scheme:][//authority][path][?query][#fragment]
             */
            String scheme = execOneParams.redirectedURI.getScheme();
            String beforeAuthority = scheme == null ? "" : scheme + "://";
            String authority = execOneParams.redirectedURI.getAuthority();
            String path = execOneParams.redirectedURI.getPath();

            request.setEndpoint(URI.create(beforeAuthority + authority));
            request.setResourcePath(path);
        }

        if (execOneParams.authRetryParam != null) {
            request.setEndpoint(execOneParams.authRetryParam.getEndpointForRetry());
        }

        try {
            return executeOneRequest(request, responseHandler, executionContext, execOneParams,
                            requestHandlers, asyncHandler);
        } catch (IOException ioe) {
            if (log.isInfoEnabled()) {
                log.info("Unable to execute HTTP request: " + ioe.getMessage(), ioe);
            }

            throw new AmazonClientException("Unable to execute HTTP request: " + ioe.getMessage(),
                            ioe);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Error e) {
            e.printStackTrace();
            throw e;
        } finally {
            /*
             * Some response handlers need to manually manage the HTTP connection and will take care
             * of releasing the connection on their own, but if this response handler doesn't need
             * the connection left open, we go ahead and release the it to free up resources.
             */
            if (!execOneParams.leaveHttpConnectionOpen) {
                if (execOneParams.apacheResponse != null) {
                    HttpEntity entity = execOneParams.apacheResponse.getEntity();
                    if (entity != null) {
                        try {
                            closeQuietly(entity.getContent(), log);
                        } catch (IOException e) {
                            log.warn("Cannot close the response content.", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the credentials from the execution if exists. Else returns null.
     */
    private AWSCredentials getCredentialsFromContext(final ExecutionContext executionContext) {

        final AWSCredentialsProvider credentialsProvider =
                        executionContext.getCredentialsProvider();

        if (credentialsProvider != null) {
            return credentialsProvider.getCredentials();
        }

        return null;
    }

    /**
     * Returns the response from executing one httpClientSettings request; or null for retry.
     */
    private <T> Future<T> executeOneRequest(final Request<?> request,
                    final HttpResponseHandler<AmazonWebServiceResponse<T>> responseHandler,
                    final ExecutionContext execContext, ExecOneRequestParams execOneParams,
                    final List<RequestHandler2> requestHandlers,
                    final ResultHandler<T> asyncHandler) throws IOException, InterruptedException {

        checkInterrupted();

        if (requestLog.isDebugEnabled()) {
            requestLog.debug("Sending Request: " + request);
        }

        final AWSRequestMetrics awsRequestMetrics = execContext.getAwsRequestMetrics();
        final AWSCredentials credentials = getCredentialsFromContext(execContext);

        // Sign the request if a signer was provided
        execOneParams.newSigner(request, execContext);
        if (execOneParams.signer != null && credentials != null) {
            if (timeOffset != 0) {
                // Always use the client level timeOffset if it was
                // non-zero; Otherwise, we respect the timeOffset in the
                // request, which could have been externally configured (at
                // least for the 1st non-retry request).
                //
                // For retry due to clock skew, the timeOffset in the
                // request used for the retry is assumed to have been
                // adjusted when execution reaches here.
                request.setTimeOffset(timeOffset);
            }
            execOneParams.signer.sign(request, credentials);
        }

        checkInterrupted();
        execOneParams.newApacheRequest(httpRequestFactory, request, httpClientSettings);

        final HttpClientContext localRequestContext =
                        ApacheUtils.newClientContext(httpClientSettings,
                                        ImmutableMapParameter.of(
                                                        AWSRequestMetrics.class.getSimpleName(),
                                                        awsRequestMetrics));

        execOneParams.resetBeforeHttpRequest();

        /////////// Send HTTP request ////////////
        final AmazonFuture<T> amazonFuture = new AmazonFuture<T>(request, execOneParams,
                        responseHandler, requestHandlers, asyncHandler);
        Future<HttpResponse> future = httpClient.execute(execOneParams.apacheRequest,
                        localRequestContext, new FutureCallback<HttpResponse>() {

                            @Override
                            public void completed(HttpResponse result) {
                                amazonFuture.handleSuccess();
                            }

                            @Override
                            public void failed(Exception ex) {
                                amazonFuture.handleError(ex);
                            }

                            @Override
                            public void cancelled() {
                                // do nothing
                            }
                        });
        amazonFuture.setInnerFuture(future);
        return amazonFuture;
    }

    /**
     * Create a client side identifier that will be sent with the initial request and each retry.
     */
    private void setSdkTransactionId(Request<?> request) {
        request.addHeader(HEADER_SDK_TRANSACTION_ID, UUID.randomUUID().toString());
    }

    /**
     * Sets a User-Agent for the specified request, taking into account any custom data.
     */
    private void setUserAgent(Request<?> request) {
        String userAgent = config.getUserAgent();
        if (userAgent != null) {
            if (!userAgent.equals(ClientConfiguration.DEFAULT_USER_AGENT)) {
                userAgent += ", " + ClientConfiguration.DEFAULT_USER_AGENT;
            }
            request.addHeader(HEADER_USER_AGENT, userAgent);
        }

        AmazonWebServiceRequest awsreq = request.getOriginalRequest();
        RequestClientOptions opts = awsreq.getRequestClientOptions();
        if (opts != null) {
            String userAgentMarker = opts.getClientMarker(Marker.USER_AGENT);
            if (userAgentMarker != null) {
                request.addHeader(HEADER_USER_AGENT,
                                createUserAgentString(userAgent, userAgentMarker));
            }
        }
    }

    /**
     * Shuts down this HTTP client object, releasing any resources that might be held open. This is
     * an optional method, and callers are not expected to call it, but can if they want to
     * explicitly release any open resources. Once a client has been shutdown, it cannot be used to
     * make more requests.
     */
    public void shutdown() {
        httpClient.close();
        // httpClient.getHttpClientConnectionManager().shutdown();
    }

    @Override
    protected void finalize() throws Throwable {
        this.shutdown();
        super.finalize();
    }

    /**
     * Returns the httpClientSettings client specific request metric collector; or null if there is
     * none.
     */
    public RequestMetricCollector getRequestMetricCollector() {
        return requestMetricCollector;
    }



    /**
     * Stateful parameters that are used for executing a single httpClientSettings request.
     */
    public static class ExecOneRequestParams {
        /**
         * Last delay between retries
         */
        AmazonClientException retriedException; // last retryable exception
        HttpRequestBase apacheRequest;
        org.apache.http.HttpResponse apacheResponse;
        URI redirectedURI;
        AuthRetryParameters authRetryParam;
        /*
         * Depending on which response handler we end up choosing to handle the HTTP response, it
         * might require us to leave the underlying HTTP connection open, depending on whether or
         * not it reads the complete HTTP response stream from the HTTP connection, or if delays
         * reading any of the content until after a response is returned to the caller.
         */
        boolean leaveHttpConnectionOpen;
        private Signer signer; // cached
        private URI signerURI;

        void initPerRetry() {
            apacheRequest = null;
            apacheResponse = null;
            leaveHttpConnectionOpen = false;
        }

        Signer newSigner(final Request<?> request, final ExecutionContext execContext) {
            if (authRetryParam != null) {
                signerURI = authRetryParam.getEndpointForRetry();
                signer = authRetryParam.getSignerForRetry();
                // Push the local signer override back to the execution context
                execContext.setSigner(signer);
            } else if (redirectedURI != null && !redirectedURI.equals(signerURI)) {
                signerURI = redirectedURI;
                signer = execContext.getSignerByURI(signerURI);
            } else if (signer == null) {
                signerURI = request.getEndpoint();
                signer = execContext.getSignerByURI(signerURI);
            }
            return signer;
        }

        /**
         * @throws com.amazonaws.util.FakeIOException
         *             thrown only during test simulation
         */
        HttpRequestBase newApacheRequest(
                        final HttpRequestFactory<HttpRequestBase> httpRequestFactory,
                        final Request<?> request, final HttpClientSettings options)
                        throws IOException {

            apacheRequest = httpRequestFactory.create(request, options);
            if (redirectedURI != null)
                apacheRequest.setURI(redirectedURI);
            return apacheRequest;
        }

        void resetBeforeHttpRequest() {
            retriedException = null;
            authRetryParam = null;
            redirectedURI = null;
        }
    }
}
