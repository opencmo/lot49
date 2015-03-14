package com.enremmeta.rtb.dao.impl.aerospike;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.listener.WriteListener;
import com.enremmeta.rtb.LogUtils;

/**
 * Write handler for Aerospike client
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class AerospikeUserAttributesWriteHandler implements WriteListener {

    @Override
    public void onSuccess(Key key) {}

    @Override
    public void onFailure(AerospikeException exception) {
        LogUtils.debug("Error writing Aerospike record. " + exception);
    }

}
