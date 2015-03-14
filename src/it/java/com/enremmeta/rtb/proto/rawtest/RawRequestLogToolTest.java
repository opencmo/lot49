package com.enremmeta.rtb.proto.rawtest;


import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;


public class RawRequestLogToolTest extends TestCase {

    @Test
    public void testReuseRawRequestLog() {
        try {
            RawRequestLogAsyncTool tool = new RawRequestLogAsyncTool();
            // rawrequest.log can be found on S3 in the folder
            // //stats.opendsp.com/year=2016/month=03/day=21/hour=10/type=rawrequest
            int countReqests = tool.reuseRawRequestLog("/tmp/rawrequest.log", 20);

            Assert.assertTrue(countReqests > 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
