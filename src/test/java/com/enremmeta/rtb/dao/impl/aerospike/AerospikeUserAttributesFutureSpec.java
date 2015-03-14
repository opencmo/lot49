package com.enremmeta.rtb.dao.impl.aerospike;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.aerospike.client.Record;

public class AerospikeUserAttributesFutureSpec {

    @Test
    public void testSetFutureResult() {
        AerospikeUserAttributesFuture aesUAFuture = new AerospikeUserAttributesFuture();
        
        Record record = new Record(new HashMap<String, Object>(){{
            put("experiment", new HashMap<String, String>(){{
                put("TEST_KEY", "TEST_VALUE");
            }});
        }}, 0, 0);
        
        assertFalse(aesUAFuture.isDone());
        
        aesUAFuture.setFutureResult("TEST_UID", record);
        
        assertTrue(aesUAFuture.isDone());
    }
    
    @Test
    public void testCancell() {
        AerospikeUserAttributesFuture aesUAFuture = new AerospikeUserAttributesFuture();
                
        assertFalse(aesUAFuture.isCancelled());
        
        aesUAFuture.cancel(true);
        
        assertTrue(aesUAFuture.isCancelled());
    }
    
    @Test
    public void testGet() throws InterruptedException, ExecutionException, TimeoutException {
        AerospikeUserAttributesFuture aesUAFuture = new AerospikeUserAttributesFuture();
                
        Record record = new Record(new HashMap<String, Object>(){{
            put("experiment", new HashMap<String, String>(){{
                put("TEST_KEY", "TEST_VALUE");
            }});
        }}, 0, 0);
        
        aesUAFuture.setFutureResult("TEST_UID", record);
        
        assertEquals("com.enremmeta.rtb.api.UserAttributes", aesUAFuture.get().getClass().getCanonicalName());

        assertEquals("com.enremmeta.rtb.api.UserAttributes", aesUAFuture.get(1000L, TimeUnit.MILLISECONDS).getClass().getCanonicalName());
    }

}
