package com.enremmeta.rtb.dao.impl.aerospike;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.listener.RecordListener;
import com.enremmeta.rtb.LogUtils;

/**
 * Read handler for Aerospike client
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */

public class AerospikeUserAttributesReadHandler implements RecordListener {

    private AerospikeUserAttributesFuture future;

    public AerospikeUserAttributesReadHandler(AerospikeUserAttributesFuture future) {
        this.future = future;
    }

    @Override
    public void onSuccess(Key key, Record record) {
        future.setFutureResult(key.userKey.toString(), record);
    }

    @Override
    public void onFailure(AerospikeException exception) {
        LogUtils.debug("Error getting Aerospike record. " + exception);
        future.cancel(false);
    }

}
