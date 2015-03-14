package com.enremmeta.rtb.proto;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.rawtest.HttpAsyncClient;
import com.enremmeta.rtb.proto.rawtest.ResultListener;
import com.enremmeta.util.Utils;

public class PubmaticTest extends AdaptersBaseTest {

    @Test
    public void testPubmatic() throws Exception {

        try {
            File pubmaticDir = getTestDataFile("pubmatic");
            for (File json : pubmaticDir.listFiles()) {
                File requestJson = getTestDataFile("bidswitch-request.json");

                String req = Utils.readFile(requestJson);

                String url = lot49Config.getBaseUrl() + Lot49Constants.ROOT_PATH_AUCTIONS + "/";

                System.out.println("Sending request to " + url);
                HttpAsyncClient asyncClient = new HttpAsyncClient(url);
                CountDownLatch latch = new CountDownLatch(1);
                long startTime = System.currentTimeMillis();

                asyncClient.sendJsonByPost(Lot49Constants.EXCHANGE_PUBMATIC, req,
                                new ResultListener() {
                                    @Override
                                    public void success(String url, String content) {
                                        System.out.println("Response: " + content);
                                        latch.countDown();
                                    }

                                    @Override
                                    public void failure(String content) {
                                        System.out.println("Failure: " + content);
                                        latch.countDown();
                                    }
                                });

                latch.await(10, TimeUnit.SECONDS);
                asyncClient.shutdown();

                System.out.println("Time Pabmatic: "
                                + (System.currentTimeMillis() - startTime) / 1000 + " sec");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
