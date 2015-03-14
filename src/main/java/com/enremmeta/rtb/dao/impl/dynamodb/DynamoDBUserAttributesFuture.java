/**
 * 
 */
package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.util.BidderCalendar;

/**
 * Future for {@link UserAttributes} with conversion from DynamoDB types
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 * 
 */

public class DynamoDBUserAttributesFuture implements Future<UserAttributes> {

    private Future<GetItemResult> f;
    private long startTime;
    private boolean isReceived;
    private UserAttributes value;

    public DynamoDBUserAttributesFuture(Future<GetItemResult> f) {
        super();
        this.f = f;
        this.isReceived = false;
        this.startTime = BidderCalendar.getInstance().currentTimeMillis();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
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
    public UserAttributes get() throws InterruptedException, ExecutionException {
        if (!isReceived) {
            value = DynamoDBDaoMapOfUserAttributes.getResultFromResponse(f.get(), startTime);
            isReceived = true;
        }
        return value;
    }

    @Override
    public UserAttributes get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
        if (!isReceived) {
            value = DynamoDBDaoMapOfUserAttributes.getResultFromResponse(f.get(timeout, unit),
                            startTime);
            isReceived = true;
        }
        return value;
    }
}
