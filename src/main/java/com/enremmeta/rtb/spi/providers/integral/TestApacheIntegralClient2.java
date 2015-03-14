package com.enremmeta.rtb.spi.providers.integral;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.enremmeta.rtb.spi.providers.integral.result.ResultListener;
import com.enremmeta.rtb.spi.providers.integral.result.ResultListenerAdapter;
import com.enremmeta.rtb.spi.providers.integral.result.dto.BrandSafetyDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ContextualDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.IntegralAllResponse;
import com.enremmeta.rtb.spi.providers.integral.result.dto.PageClutterDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.PageLanguageDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.TRAQScoreDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ViewabilityDto;


public class TestApacheIntegralClient2 {

    static int countAttempts;
    static int countSuccess;
    static int countFailure;

    public static void main(final String[] args) throws Exception {
        // String[] urls = {"www.yahoo.com", "www.google.com", "www.playboy.com", "www.esri.com"};
        String[] urls = {"www.yahoo.com", "www.google.com", "www.playboy.com"};

        int countRequests = 3000;

        for (int maxConnPerRoute = 100; maxConnPerRoute <= 10000; maxConnPerRoute =
                        maxConnPerRoute + 100) {

            countAttempts = 0;
            countSuccess = 0;
            countFailure = 0;

            final CountDownLatch latch = new CountDownLatch(countRequests * urls.length);

            ResultListener allResponseListener = new ResultListenerAdapter<IntegralAllResponse>(
                            IntegralAllResponse.class) {
                @Override
                public void extSuccess(String url, IntegralAllResponse obj) {
                    countSuccess++;
                    latch.countDown();
                    // System.out.println("IntegralAllResponse check extSuccess: " + url + " - "
                    // + obj.toString());
                }

                @Override
                public void cancellation() {}

                @Override
                public void extFailure(String url, String errorMessage) {
                    countFailure++;
                    latch.countDown();
                    System.out.println("IntegralAllResponse check extFailure: " + url
                                    + "; errorMessage:" + errorMessage);
                }

            };

            ResultListener brandSafetyListener =
                            new ResultListenerAdapter<BrandSafetyDto>(BrandSafetyDto.class) {
                                @Override
                                public void extSuccess(String url, BrandSafetyDto obj) {
                                    latch.countDown();
                                    // System.out.println("BrandSafety check extSuccess: " +
                                    // url);
                                }

                                @Override
                                public void cancellation() {}

                                @Override
                                public void extFailure(String url, String errorMessage) {
                                    latch.countDown();
                                    // System.out.println("BrandSafety check extFailure: " + url
                                    // + "; errorMessage:" + errorMessage);
                                }
                            };

            ResultListener contextualListener =
                            new ResultListenerAdapter<ContextualDto>(ContextualDto.class) {
                                @Override
                                public void extSuccess(String url, ContextualDto obj) {
                                    latch.countDown();
                                    // System.out.println("Contextual check extSuccess: " + url
                                    // + " - " + obj.toString());
                                }

                                @Override
                                public void cancellation() {}

                                @Override
                                public void extFailure(String url, String errorMessage) {
                                    latch.countDown();
                                    // System.out.println("Contextual check extFailure: " + url
                                    // + "; errorMessage:" + errorMessage);
                                }
                            };

            ResultListener pageClutterListener =
                            new ResultListenerAdapter<PageClutterDto>(PageClutterDto.class) {
                                @Override
                                public void extSuccess(String url, PageClutterDto obj) {
                                    latch.countDown();
                                    // System.out.println("PageClutter check extSuccess: " + url
                                    // + " - " + obj.toString());
                                }

                                @Override
                                public void cancellation() {}

                                @Override
                                public void extFailure(String url, String errorMessage) {
                                    latch.countDown();
                                    // System.out.println("PageClutter check extFailure: " + url
                                    // + "; errorMessage:" + errorMessage);
                                }
                            };

            ResultListener pageLanguageListener =
                            new ResultListenerAdapter<PageLanguageDto>(PageLanguageDto.class) {
                                @Override
                                public void extSuccess(String url, PageLanguageDto obj) {
                                    latch.countDown();
                                    // System.out.println("PageLanguage check extSuccess: " +
                                    // url + " - " + obj.toString());
                                }


                                @Override
                                public void cancellation() {}

                                @Override
                                public void extFailure(String url, String errorMessage) {
                                    latch.countDown();
                                    // System.out.println("PageLanguage check extFailure: " +
                                    // url + "; errorMessage:" + errorMessage);
                                }
                            };

            ResultListener traqScoreDtoListener =
                            new ResultListenerAdapter<TRAQScoreDto>(TRAQScoreDto.class) {
                                @Override
                                public void extSuccess(String url, TRAQScoreDto obj) {
                                    latch.countDown();
                                    // System.out.println("TRAQScore check extSuccess: " + url +
                                    // " - " + obj.toString());
                                }

                                @Override
                                public void cancellation() {}

                                @Override
                                public void extFailure(String url, String errorMessage) {
                                    latch.countDown();
                                    // System.out.println("TRAQScore check extFailure: " + url +
                                    // "; errorMessage:" + errorMessage);
                                }
                            };

            ResultListener viewabilityListener =
                            new ResultListenerAdapter<ViewabilityDto>(ViewabilityDto.class) {
                                @Override
                                public void extSuccess(String url, ViewabilityDto obj) {
                                    latch.countDown();
                                    // System.out.println("Viewability check extSuccess: " + url
                                    // + " - " + obj.toString());
                                }

                                @Override
                                public void cancellation() {}

                                @Override
                                public void extFailure(String url, String errorMessage) {
                                    latch.countDown();
                                    // System.out.println("Viewability check extFailure: " + url
                                    // + "; errorMessage:" + errorMessage);
                                }
                            };

            IntegralClient2 integralClient =
                            new IntegralClient2("http://localhost:8080", "10", maxConnPerRoute);
            // IntegralClient2 integralClient = new IntegralClient2("http://localhost:8080", "10");

            Date startTime = new Date();

            // for (DataMethod dataMethod : DataMethod.values()) {
            List<DataMethod> dataMethods = Arrays.asList(DataMethod.ALL);

            for (DataMethod dataMethod : dataMethods) {

                String dataMethodString = dataMethod.toString();

                ResultListener resultHandler = brandSafetyListener;

                if (dataMethod.equals(DataMethod.ALL)) {
                    resultHandler = allResponseListener;
                } else if (dataMethod.equals(DataMethod.BRAND_SAFETY)) {
                    resultHandler = brandSafetyListener;
                } else if (dataMethod.equals(DataMethod.CONTEXTUAL)) {
                    resultHandler = contextualListener;
                } else if (dataMethod.equals(DataMethod.PAGE_CLUTTER)) {
                    resultHandler = pageClutterListener;
                } else if (dataMethod.equals(DataMethod.PAGE_LANGUAGE)) {
                    resultHandler = pageLanguageListener;
                } else if (dataMethod.equals(DataMethod.TRAQ_SCORE)) {
                    resultHandler = traqScoreDtoListener;
                } else if (dataMethod.equals(DataMethod.VIEWABILITY)) {
                    resultHandler = viewabilityListener;
                } else {
                    continue;
                }

                for (int i = 0; i < countRequests; i++) {
                    for (String url : urls) {
                        countAttempts++;
                        integralClient.send(dataMethodString, url, resultHandler);
                    }
                }
            }

            latch.await();
            integralClient.shutdown();

            Date endTime = new Date();
            Long period = endTime.getTime() - startTime.getTime();
            System.out.println("Time:" + period.toString() + "; maxConnPerRoute:" + maxConnPerRoute
                            + "; countAttempts:" + countAttempts + "; Success:" + countSuccess
                            + "; Failure:" + countFailure);
        }
        System.out.println("=====================================================================");
    }


}
