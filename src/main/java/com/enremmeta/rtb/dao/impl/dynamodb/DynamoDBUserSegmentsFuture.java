package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.enremmeta.rtb.api.UserSegments;
import com.enremmeta.util.BidderCalendar;

/**
 * Future for {@link UserSegments} with conversion from DynamoDB types
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 * 
 */

public class DynamoDBUserSegmentsFuture implements Future<UserSegments> {

    private Future<GetItemResult> f;
    private long startTime;
    private boolean isReceived;
    private UserSegments value;

    public DynamoDBUserSegmentsFuture(Future<GetItemResult> f) {
        super();
        this.f = f;
        this.isReceived = false;
        this.startTime = BidderCalendar.getInstance().currentTimeMillis();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) {
            return false;
        }
        return f.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return f.isCancelled();
    }

    @Override
    public boolean isDone() {
        return f.isDone();
    }

    @Override
    public UserSegments get() throws InterruptedException, ExecutionException {
        if (!isReceived) {
            value = DynamoDBDaoMapOfUserSegments.getResultFromResponse(f.get(), startTime);
            isReceived = true;
        }
        return value;
    }

    @Override
    public UserSegments get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
        if (!isReceived) {
            value = DynamoDBDaoMapOfUserSegments.getResultFromResponse(f.get(timeout, unit),
                            startTime);
            isReceived = true;
        }
        return value;
    }

}
