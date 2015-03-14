package com.enremmeta.rtb.dao.impl.aerospike;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.aerospike.client.Record;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.util.BidderCalendar;

/**
 * Future for {@link UserAttributes} with conversion from Aerospike types
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class AerospikeUserAttributesFuture implements Future<UserAttributes> {

    private UserAttributes userAttributes;
    private boolean cancelled = false;
    private boolean received = false;
    private CountDownLatch lock = new CountDownLatch(1);
    private long startRequestTime = BidderCalendar.getInstance().currentTimeMillis();

    public void setFutureResult(String uid, Record record) {
        userAttributes = AerospikeDaoMapOfUserAttributes.recordToUserAttributes(record);
        LogUtils.trace("Time to get userAttributes for " + uid + ": "
                        + (BidderCalendar.getInstance().currentTimeMillis() - startRequestTime)
                        + " from Aerospike");
        received = true;
        lock.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (received) {
            return false;
        } else if (!cancelled) {
            cancelled = true;
            lock.countDown();
            return !received;
        } else {
            return false;
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return received;
    }

    @Override
    public UserAttributes get() throws InterruptedException, ExecutionException {
        if (received) {
            return userAttributes;
        } else {
            lock.await();
            if (!cancelled) {
                return userAttributes;
            }
        }
        return null;
    }

    @Override
    public UserAttributes get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
        if (received) {
            return userAttributes;
        } else {
            lock.await(timeout, unit);
            if (!cancelled) {
                return userAttributes;
            }
        }
        return null;
    }

}
