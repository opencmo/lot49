package com.enremmeta.rtb.spi.providers.integral;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.enremmeta.rtb.spi.providers.integral.result.ResultListener;


public class TestIntegralClient {

    int countSuccess = 0;
    int countFailure = 0;
    long maxDuration = 0;
    long minDuration = 0;
    Map<String, Long> requestList = new HashMap<String, Long>();
    Map<Integer, Long> durationStats = new TreeMap<Integer, Long>();

    CountDownLatch latch;
    IntegralClient2 integralClient;

    public int doTest(int thread, int maxAttempts) throws Exception {

        // String[] urls = {"http://www.cnn.com", "http://www.yahoo.com", "http://www.google.com",
        // "http://www.playboy.com", "http://www.esri.com"};
        String[] urls = {"http://www.cnn.com"};

        Map<Integer, int[]> result = new TreeMap<>();

        int maxCountAttempts = maxAttempts / urls.length;
        int countAttempts = 0;
        for (int attempts = 10; attempts <= maxCountAttempts; attempts = attempts
                        + (countAttempts < 100 ? 30 : (countAttempts < 1000 ? 300 : 3000))) {

            countSuccess = 0;
            countFailure = 0;
            requestList = new HashMap<>();
            maxDuration = 0L;
            minDuration = 0L;

            countAttempts = attempts * urls.length;

            // integralClient = new
            // IntegralClient2("http://ec2-52-2-36-93.compute-1.amazonaws.com:8080", "49600", 1000);
            integralClient = new IntegralClient2(
                            "http://ec2-52-200-162-131.compute-1.amazonaws.com:8080", "49600",
                            1000);

            latch = new CountDownLatch(countAttempts);
            Long startTime = System.currentTimeMillis();

            for (int i = 0; i < attempts; i++) {
                for (String url : urls) {
                    String sendUrl = url + "?" + i;
                    requestList.put(sendUrl, System.currentTimeMillis());

                    integralClient.send(DataMethod.ALL.toString(), sendUrl, new ResultListener() {
                        @Override
                        public void success(String url, String content) {
                            countSuccess++;

                            System.out.println("Thread: "
                                            + StringUtils.leftPad(String.valueOf(thread), 2)
                                            + " | URL: " + StringUtils.rightPad(url, 30)
                                            + " | Content: " + content);

                            long start = requestList.get(url);
                            long end = System.currentTimeMillis();
                            long duration = end - start;
                            if (maxDuration == 0 || maxDuration < duration) {
                                maxDuration = duration;
                            }
                            if (minDuration == 0 || minDuration > (duration)) {
                                minDuration = duration;
                            }

                            latch.countDown();
                        }

                        @Override
                        public void cancellation() {}

                        @Override
                        public void failure(String url, String errorMessage) {
                            countFailure++;

                            System.out.println("Thread: "
                                            + StringUtils.leftPad(String.valueOf(thread), 2)
                                            + " | URL: " + StringUtils.rightPad(url, 30)
                                            + " | Error: " + errorMessage);

                            if (requestList != null && url != null
                                            && requestList.get(url) != null) {
                                Long start = requestList.get(url);
                                Long end = System.currentTimeMillis();
                                Long duration = end - start;
                                if (maxDuration == 0 || maxDuration < duration) {
                                    maxDuration = duration;
                                }
                                if (minDuration == 0 || minDuration > duration) {
                                    minDuration = duration;
                                }
                            }

                            latch.countDown();
                        }
                    });
                }
            }


            latch.await(20, TimeUnit.SECONDS);
            integralClient.shutdown();

            Long endTime = System.currentTimeMillis();
            Long period = endTime - startTime;

            result.put(Integer.valueOf(countAttempts),
                            new int[] {countSuccess, countFailure, period.intValue(),
                                            (int) (period / countAttempts), (int) minDuration,
                                            (int) maxDuration});

        }

        String requestsStr = "Requests:";
        String successStr = " Success:";
        String failureStr = " Failure:";
        String lostStr = "    Lost:";
        String timeStr = "    Time:";
        String minTimeStr = "Min time:";
        String maxTimeStr = "Max time:";
        String averageStr = " Average:";
        int totalRequests = 0;
        int totalSuccess = 0;
        for (Integer key : result.keySet()) {
            int[] values = result.get(key);
            requestsStr = requestsStr + StringUtils.leftPad(String.valueOf(key), 8);
            successStr = successStr + StringUtils.leftPad(String.valueOf(values[0]), 8);
            failureStr = failureStr + StringUtils.leftPad(String.valueOf(values[1]), 8);
            lostStr = lostStr + StringUtils.leftPad(String.valueOf(key - values[0] - values[1]), 8);
            timeStr = timeStr + StringUtils.leftPad(String.valueOf(values[2]), 8);
            averageStr = averageStr + StringUtils.leftPad(String.valueOf(values[3]), 8);
            minTimeStr = minTimeStr + StringUtils.leftPad(String.valueOf(values[4]), 8);
            maxTimeStr = maxTimeStr + StringUtils.leftPad(String.valueOf(values[5]), 8);

            totalRequests += key;
            totalSuccess += values[0];
        }

        int percentOfSuccess = 100 * totalSuccess / totalRequests;

        System.out.println("Thread: " + thread + " -------------------------" + "\n" + requestsStr
                        + "\n" + successStr + "\n" + failureStr + "\n" + lostStr + "\n" + timeStr
                        + "\n" + averageStr + "\n" + minTimeStr + "\n" + maxTimeStr + "\n"
                        + "Percent of Success: " + percentOfSuccess + "\n");

        return percentOfSuccess;
    }


    public Map<Integer, Long> doTimeTest(int thread, int maxAttempts) throws Exception {

        String[] urls = {"http://www.cnn.com"};

        int maxCountAttempts = maxAttempts / urls.length;
        int countAttempts = 0;
        // for (int attempts = 1; attempts <= maxCountAttempts; attempts = attempts + (countAttempts
        // < 100 ? 10 : (countAttempts < 1000 ? 100: 1000))) {
        for (int attempts = maxCountAttempts; attempts <= maxCountAttempts; attempts =
                        attempts + 1) {

            requestList = new HashMap<>();
            durationStats = new HashMap<>();

            countAttempts = attempts * urls.length;

            // integralClient = new
            // IntegralClient2("http://ec2-52-2-36-93.compute-1.amazonaws.com:8080", "49600", 1000);

            integralClient = new IntegralClient2(
                            "http://ec2-52-200-162-131.compute-1.amazonaws.com:8080", "49600",
                            1000);
            // integralClient = new
            // IntegralClient2("http://ec2-52-200-162-131.compute-1.amazonaws.com:8080",
            // "49600");

            // integralClient = new
            // IntegralClient2("http://ec2-52-8-75-31.us-west-1.compute.amazonaws.com:8080",
            // "49600", 1000);
            // integralClient = new
            // IntegralClient2("http://ec2-52-9-163-56.us-west-1.compute.amazonaws.com:8080",
            // "49600", 1000);

            latch = new CountDownLatch(countAttempts);

            for (int i = 0; i < attempts; i++) {
                for (String url : urls) {
                    String sendUrl = url + "?" + i;
                    requestList.put(sendUrl, System.currentTimeMillis());

                    integralClient.send(DataMethod.ALL.toString(), sendUrl, new ResultListener() {
                        @Override
                        public void success(String url, String content) {
                            countSuccess++;

                            putDuration(url, true);
                            // System.out.println(content);
                            latch.countDown();
                        }

                        @Override
                        public void cancellation() {}

                        @Override
                        public void failure(String url, String errorMessage) {
                            countFailure++;

                            System.out.println(errorMessage);

                            putDuration(url, false);

                            latch.countDown();
                        }
                    });
                }
            }


            latch.await(20, TimeUnit.SECONDS);
            integralClient.shutdown();

        }

        return durationStats;
    }

    public Integer getDurationKey(long duration) {
        Integer durationKey = 0;

        if (duration >= 0 && duration <= 5) {
            durationKey = 5;
        } else if (duration > 5 && duration <= 50) {
            durationKey = (int) (duration + (5 - duration % 5));
        } else if (duration > 50 && duration <= 100) {
            durationKey = (int) (duration + (10 - duration % 10));
        } else if (duration > 100 && duration <= 1000) {
            durationKey = (int) (duration + (50 - duration % 50));
        } else {
            durationKey = (int) (duration + (1000 - duration % 1000));
        }

        return durationKey;
    }

    static public String getDurationName(Integer to) {
        String durationName = "none";

        if (to >= 0 && to <= 5) {
            durationName = "<5ms:";
        } else if (to > 5 && to <= 50) {
            Integer from = to - 5 + 1;
            durationName = from + "-" + to + "ms:";
        } else if (to > 50 && to <= 100) {
            long from = to - 10 + 1;
            durationName = from + "-" + to + "ms:";
        } else if (to > 100 && to <= 1000) {
            long from = to - 50 + 1;
            durationName = from + "-" + to + "ms:";
        } else {
            long from = to - 1000 + 1;
            durationName = from + "-" + to + "ms:";
        }

        return StringUtils.leftPad(durationName, 15);
    }

    private void putDuration(String url, Boolean isSuccess) {

        Long start = requestList.get(url);
        if (isSuccess && start != null) {
            Long end = System.currentTimeMillis();
            Long duration = end - start;

            Integer durationKey = getDurationKey(duration);

            Long existsValue = durationStats.get(durationKey);
            if (existsValue == null) {
                existsValue = 1L;
            } else {
                existsValue++;
            }

            durationStats.put(durationKey, existsValue);
        }
    }
}
