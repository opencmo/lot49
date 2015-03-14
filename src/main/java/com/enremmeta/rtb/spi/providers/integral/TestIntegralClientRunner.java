package com.enremmeta.rtb.spi.providers.integral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

public class TestIntegralClientRunner {

    static int maxAttempts = 1000;
    static int maxThreads = 10;

    static int totalSuccess = 0;
    static int totalPercentOfSuccess = 0;

    static Map<Integer, Long> durationStats = new TreeMap<Integer, Long>();

    public static void main(final String[] args) throws Exception {

        String typeTest = "t1";

        if (args[0] != null) {
            typeTest = args[0];
        }

        if (args[1] != null) {
            try {
                maxThreads = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (args[2] != null) {
            try {
                maxAttempts = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (typeTest.equals("t1")) {
            doTest();
        } else {
            doTimeTest();
        }

    }

    public static void doTest() throws InterruptedException {

        ExecutorService pool = Executors.newFixedThreadPool(maxThreads);

        for (int i = 1; i <= maxThreads; i++) {
            final int thread = i;
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        TestIntegralClient test = new TestIntegralClient();

                        int percentOfSuccess = test.doTest(thread, maxAttempts);
                        totalPercentOfSuccess += percentOfSuccess;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        System.out.println("Total percent of Success: " + totalPercentOfSuccess / maxThreads);

    }

    public static void doTimeTest() throws InterruptedException {

        ExecutorService pool = Executors.newFixedThreadPool(maxThreads);

        for (int i = 1; i <= maxThreads; i++) {
            final int thread = i;
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        TestIntegralClient test = new TestIntegralClient();

                        final Map<Integer, Long> durationStatsThread =
                                        test.doTimeTest(thread, maxAttempts);

                        for (Integer key : durationStatsThread.keySet()) {

                            Long existsValue = durationStats.get(key);
                            if (existsValue == null) {
                                existsValue = durationStatsThread.get(key);
                            } else {
                                existsValue += durationStatsThread.get(key);
                            }

                            durationStats.put(key, existsValue);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        String resultStr = "By time of response \n";

        List<Integer> keyList = new ArrayList<>(durationStats.keySet());
        Collections.sort(keyList);

        for (Integer key : keyList) {
            resultStr = resultStr + TestIntegralClient.getDurationName(key)
                            + StringUtils.leftPad(durationStats.get(key).toString(), 8) + "\n";

            totalSuccess += durationStats.get(key);
        }

        System.out.println(resultStr + "\n" + "Total of Success: " + totalSuccess
                        + "; Total percent of Success: "
                        + (100 * totalSuccess) / (maxAttempts * maxThreads));

    }

}
