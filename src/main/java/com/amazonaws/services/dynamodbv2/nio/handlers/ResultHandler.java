package com.amazonaws.services.dynamodbv2.nio.handlers;

/**
 * Created by amiroshn.
 */
public interface ResultHandler<RESULT> {

    /**
     * Invoked after an asynchronous request
     * 
     * @param exception
     */
    public void onError(Exception exception);

    /**
     * Invoked after an asynchronous request has completed successfully. Callers have access to the
     * returned response object.
     *
     * @param result
     *            The successful result of the executed operation.
     */
    public void onSuccess(RESULT result);

}
