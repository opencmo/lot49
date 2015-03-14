package com.enremmeta.rtb.dao.impl.aerospike;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.async.AsyncClient;
import com.enremmeta.rtb.config.AerospikeDBServiceConfig;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AsyncClient.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AerospikeDBServiceSpec {

    @Test
    public void test() throws Exception {
        AerospikeDBService aesDBService = new AerospikeDBService();

        try {
            aesDBService.init(new AerospikeDBServiceConfig() {
                {
                    setHost("TEST_HOST");
                    setPort(3000);
                }
            });
            fail("Should throw Exception!");
        } catch (AerospikeException ex) {
            assertTrue(ex.getMessage().contains(
                            "TEST_HOST:3000 Error Code 11: Invalid host: TEST_HOST:3000"));
        }
    }

}
