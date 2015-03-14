package com.amazonaws.services.dynamodbv2.nio.http;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BufferedHttpEntity;

import com.amazonaws.AmazonWebServiceResponse;
import com.amazonaws.Request;
import com.amazonaws.handlers.RequestHandler2;
import com.amazonaws.http.HttpResponseHandler;
import com.amazonaws.services.dynamodbv2.nio.handlers.ResultHandler;


/**
 * Created by amiroshn
 */
public class AmazonFuture<T> implements Future<T> {

    private Future<HttpResponse> innerFuture;

    private List<RequestHandler2> requestHandlers;
    private HttpResponseHandler<AmazonWebServiceResponse<T>> responseHandler;
    private AmazonHttpClient.ExecOneRequestParams execOneParams;

    private Request<?> request;

    private ResultHandler<T> asyncHandler;

    public AmazonFuture(Request<?> request, AmazonHttpClient.ExecOneRequestParams execOneParams,
                    HttpResponseHandler<AmazonWebServiceResponse<T>> responseHandler,
                    List<RequestHandler2> requestHandlers, ResultHandler<T> asyncHandler) {
        super();

        this.request = request;
        this.execOneParams = execOneParams;
        this.responseHandler = responseHandler;
        this.requestHandlers = requestHandlers;
        this.asyncHandler = asyncHandler;
    }

    public void setInnerFuture(Future<HttpResponse> innerFuture) {
        this.innerFuture = innerFuture;
    }

    public boolean isCancelled() {
        return this.innerFuture.isCancelled();
    }

    public boolean isDone() {
        return this.innerFuture.isDone();
    }

    public synchronized T get() throws InterruptedException, ExecutionException {
        HttpResponse response = innerFuture.get();
        return convert(response);
    }

    public synchronized T get(final long timeout, final TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
        HttpResponse response = innerFuture.get(timeout, unit);
        return convert(response);
    }

    public boolean cancel(final boolean mayInterruptIfRunning) {
        return innerFuture.cancel(mayInterruptIfRunning);
    }

    public boolean cancel() {
        return cancel(true);
    }

    public void handleSuccess() {
        T t;
        try {
            t = this.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            t = null;
            if (this.asyncHandler != null)
                this.asyncHandler.onError(e);
        }
        if (t != null && this.asyncHandler != null) {
            this.asyncHandler.onSuccess(t);
        }
    }

    public void handleError(Exception exception) {
        if (this.asyncHandler != null)
            this.asyncHandler.onError(exception);
    }

    private T convert(HttpResponse response) throws ExecutionException, InterruptedException {
        execOneParams.apacheResponse = response;

        try {
            if (shouldBufferHttpEntity(responseHandler.needsConnectionLeftOpen(), execOneParams)) {
                execOneParams.apacheResponse.setEntity(
                                new BufferedHttpEntity(execOneParams.apacheResponse.getEntity()));
            }

            if (isRequestSuccessful(execOneParams.apacheResponse)) {
                execOneParams.leaveHttpConnectionOpen = responseHandler.needsConnectionLeftOpen();

                com.amazonaws.http.HttpResponse httpResponse = createResponse(
                                execOneParams.apacheRequest, request, execOneParams.apacheResponse);

                return handleResponse(request, responseHandler, httpResponse, requestHandlers);
            }

            throw new ExecutionException(new Exception("Request Failed"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new ExecutionException(e);
        }
    }

    /**
     * @return True if the {@link org.apache.http.HttpEntity} should be wrapped in a
     *         {@link org.apache.http.entity.BufferedHttpEntity}
     */
    private boolean shouldBufferHttpEntity(final boolean needsConnectionLeftOpen,
                    AmazonHttpClient.ExecOneRequestParams execParams) {
        return !needsConnectionLeftOpen && execParams.apacheResponse.getEntity() != null;
    }

    private boolean isRequestSuccessful(org.apache.http.HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        return status / 100 == HttpStatus.SC_OK / 100;
    }

    /**
     * Run
     * {@link com.amazonaws.handlers.RequestHandler2#beforeUnmarshalling(com.amazonaws.Request, com.amazonaws.http.HttpResponse)}
     * callback
     *
     * @param requestHandler2s
     *            List of request handlers to invoke
     * @param request
     *            Original request
     * @param origHttpResponse
     *            Original {@link com.amazonaws.http.HttpResponse}
     * @return {@link com.amazonaws.http.HttpResponse} object to pass to unmarshaller. May have been
     *         modified or replaced by the request handlers
     */
    private com.amazonaws.http.HttpResponse beforeUnmarshalling(
                    List<RequestHandler2> requestHandler2s, Request<?> request,
                    com.amazonaws.http.HttpResponse origHttpResponse) {
        com.amazonaws.http.HttpResponse toReturn = origHttpResponse;
        for (RequestHandler2 requestHandler : requestHandler2s) {
            toReturn = requestHandler.beforeUnmarshalling(request, toReturn);
        }
        return toReturn;
    }

    private com.amazonaws.http.HttpResponse createResponse(HttpRequestBase method,
                    Request<?> request, org.apache.http.HttpResponse apacheHttpResponse)
                    throws IOException {
        com.amazonaws.http.HttpResponse httpResponse =
                        new com.amazonaws.http.HttpResponse(request, method);

        if (apacheHttpResponse.getEntity() != null) {
            httpResponse.setContent(apacheHttpResponse.getEntity().getContent());
        }

        httpResponse.setStatusCode(apacheHttpResponse.getStatusLine().getStatusCode());
        httpResponse.setStatusText(apacheHttpResponse.getStatusLine().getReasonPhrase());
        for (Header header : apacheHttpResponse.getAllHeaders()) {
            httpResponse.addHeader(header.getName(), header.getValue());
        }

        return httpResponse;
    }

    @SuppressWarnings("deprecation")
    private T handleResponse(Request<?> request,
                    HttpResponseHandler<AmazonWebServiceResponse<T>> responseHandler,
                    com.amazonaws.http.HttpResponse httpResponse,
                    List<RequestHandler2> requestHandlers) throws ExecutionException {
        AmazonWebServiceResponse<? extends T> awsResponse = null;
        Exception ee = null;

        try {
            awsResponse = responseHandler
                            .handle(beforeUnmarshalling(requestHandlers, request, httpResponse));

        } catch (Exception e) {
            awsResponse = null;
            ee = e;
        }

        if (awsResponse == null) {
            if (ee == null) {
                ee = new Exception(
                                "Response is null. Unable to unmarshall response metadata. Response Code: "
                                                + httpResponse.getStatusCode() + ", Response Text: "
                                                + httpResponse.getStatusText());
            }

            throw new ExecutionException(ee);
        }

        return awsResponse.getResult();
    }
}
