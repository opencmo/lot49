package com.enremmeta.rtb.proto.rawtest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.enremmeta.rtb.constants.Lot49Constants;

public class RawRequestLogAsyncTool {

    int urlIndex = 2;
    int jsonIndex = 5;
    int countSuccess = 0;
    int countFailed = 0;

    public int reuseRawRequestLog(String rawRequestLogPath, int countThreads)
                    throws InterruptedException {
        long startTime = System.currentTimeMillis();
        int countRequests = 0;
        System.out.println("Start prepare requests");
        if (rawRequestLogPath != null) {

            Map<String, List<String>> requests = new HashMap<String, List<String>>();

            try {

                BufferedReader br = new BufferedReader(new FileReader(rawRequestLogPath));

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("raw")) {

                        String[] array = line.split("\t");

                        String url = array[urlIndex];
                        final String requestType = url.substring(url.lastIndexOf("/") + 1);

                        String json = array[jsonIndex].trim();

                        if (requestType != null && !requestType.isEmpty() && json != null
                                        && !json.isEmpty()) {
                            if (!requests.containsKey(requestType)) {
                                List<String> requestsList = new ArrayList<String>();
                                requests.put(requestType, requestsList);
                            }
                            requests.get(requestType).add(json);
                        }
                    }
                }

                br.close();
            } catch (IOException e) {

            }
            System.out.println("End prepare requests: "
                            + (System.currentTimeMillis() - startTime) / 1000 + " sec");
            System.out.println("Start send requests");
            long currentTime = System.currentTimeMillis();
            ExecutorService pool = Executors.newFixedThreadPool(countThreads);

            for (String requestType : requests.keySet()) {
                List<String> dataList = requests.get(requestType);
                int partSize = dataList.size() / countThreads;
                for (int i = 0; i < dataList.size(); i = i + partSize) {
                    final int fromIndex = i;
                    final int toIndex = fromIndex + partSize >= dataList.size()
                                    ? dataList.size() - 1 : fromIndex + partSize;
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            RequestSender sender = new RequestSender();
                            sender.sendRequest(requestType, dataList.subList(fromIndex, toIndex));
                        }
                    });
                }
            }

            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            System.out.println("End send requests: "
                            + (System.currentTimeMillis() - startTime) / 1000 + " sec");
            long periodSec = (System.currentTimeMillis() - currentTime) / 1000;
            countRequests = countSuccess + countFailed;
            if (countRequests > 0) {
                System.out.println("Time: " + periodSec + " sec");
                System.out.println("CountSuccess: " + countSuccess);
                System.out.println("CountFailed: " + countFailed);
                System.out.println("AllRequests: " + countRequests);
                System.out.println("QPS: " + countRequests / periodSec);
            }
        }
        return countRequests;
    }

    private class RequestSender {

        String baseUrl = "http://localhost:10000" + Lot49Constants.ROOT_PATH_AUCTIONS + "/";

        public void sendRequest(String requestType, List<String> dataList) {
            switch (requestType) {
                case "adx":
                    sendAdxRequest(dataList);
                    break;
                case "pubmatic":
                    sendPabmaticRequest(dataList);
                    break;
                default:
                    // sendOtherRequest(url, json);
            }
        }

        private void sendAdxRequest(List<String> dataList) {

            HttpAsyncClient asyncClient = new HttpAsyncClient(baseUrl);

            CountDownLatch latch = new CountDownLatch(dataList.size());
            Long startTime = System.currentTimeMillis();
            try {
                for (String json : dataList) {

                    json = clearString(json, ", \"54\"");
                    json = clearString(json, ", \"53\"");
                    json = clearString(json, ", \"52\"");
                    json = clearString(json, ", \"52\"");
                    json = clearString(json, ", \"25\"");
                    json = clearString(json, ", \"23\"");

                    if (json.indexOf(",\"deals\":[]") >= 0) {
                        json = json.replace(",\"deals\":[]", "");
                    }

                    // long start = System.currentTimeMillis();

                    asyncClient.sendAdx(json, new ResultListener() {
                        @Override
                        public void success(String url, String content) {
                            countSuccess++;
                            latch.countDown();
                            // System.out.println("Time Adx success: " + (System.currentTimeMillis()
                            // - start) + " ms");
                        }

                        @Override
                        public void failure(String url) {
                            countFailed++;
                            latch.countDown();
                            // System.out.println("Time Adx failure: " + (System.currentTimeMillis()
                            // - start) + " ms");
                        }
                    });

                }

                latch.await(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            asyncClient.shutdown();

            System.out.println("Time Adx: " + (System.currentTimeMillis() - startTime) / 1000
                            + " sec");
        }

        private void sendPabmaticRequest(List<String> dataList) {
            try {

                HttpAsyncClient asyncClient = new HttpAsyncClient(baseUrl);

                CountDownLatch latch = new CountDownLatch(dataList.size());
                long startTime = System.currentTimeMillis();

                for (String json : dataList) {
                    asyncClient.sendJsonByPost(Lot49Constants.EXCHANGE_PUBMATIC, json,
                                    new ResultListener() {
                                        @Override
                                        public void success(String url, String content) {
                                            countSuccess++;
                                            latch.countDown();
                                        }

                                        @Override
                                        public void failure(String url) {
                                            countFailed++;
                                            latch.countDown();
                                        }
                                    });
                }

                latch.await(10, TimeUnit.SECONDS);
                asyncClient.shutdown();

                System.out.println("Time Pabmatic: "
                                + (System.currentTimeMillis() - startTime) / 1000 + " sec");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private String clearString(String str, String removedStr) {
            int pos = str.indexOf(removedStr);
            if (pos >= 0) {
                str = str.replace(str.substring(pos, str.indexOf("]", pos) + 1), "");
            }
            return str;
        }
    }

}
