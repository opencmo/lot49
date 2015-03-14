package com.enremmeta.rtb.spi.providers.integral;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import com.enremmeta.rtb.spi.providers.integral.result.ResultListener;
import com.enremmeta.rtb.spi.providers.integral.result.ResultListenerAdapter;
import com.enremmeta.rtb.spi.providers.integral.result.dto.BrandSafetyDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ContextualDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.PageClutterDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.PageLanguageDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.TRAQScoreDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ViewabilityDto;
import com.enremmeta.util.BidderCalendar;


public class TestApacheIntegralClient {
    public static void main(final String[] args) throws Exception {
        String[] urls = {"www.yahoo.com", "www.google.com", "www.playboy.com", "www.esri.com"};

        ResultListener brandSafetyListener =
                        new ResultListenerAdapter<BrandSafetyDto>(BrandSafetyDto.class) {
                            @Override
                            public void extSuccess(String url, BrandSafetyDto obj) {
                                // System.out.println("BrandSafety check extSuccess: " + url + " - "
                                // + obj.toString());
                            }

                            @Override
                            public void cancellation() {}

                            @Override
                            public void extFailure(String url, String errorMessage) {
                                // System.out.println("BrandSafety check extFailure: " + url);
                            }
                        };

        ResultListener contextualListener =
                        new ResultListenerAdapter<ContextualDto>(ContextualDto.class) {
                            @Override
                            public void extSuccess(String url, ContextualDto obj) {
                                // System.out.println("Contextual check extSuccess: " + url + " - "
                                // + obj.toString());
                            }


                            @Override
                            public void cancellation() {}

                            @Override
                            public void extFailure(String url, String errorMessage) {
                                // System.out.println("Contextual check extFailure: " + url);
                            }
                        };

        ResultListener pageClutterListener =
                        new ResultListenerAdapter<PageClutterDto>(PageClutterDto.class) {
                            @Override
                            public void extSuccess(String url, PageClutterDto obj) {
                                // System.out.println("PageClutter check extSuccess: " + url + " - "
                                // + obj.toString());
                            }

                            @Override
                            public void cancellation() {}

                            @Override
                            public void extFailure(String url, String errorMessage) {
                                // System.out.println("PageClutter check extFailure: " + url);
                            }
                        };

        ResultListener pageLanguageListener =
                        new ResultListenerAdapter<PageLanguageDto>(PageLanguageDto.class) {
                            @Override
                            public void extSuccess(String url, PageLanguageDto obj) {
                                // System.out.println("PageLanguage check extSuccess: " + url +
                                // " - " + obj.toString());
                            }

                            @Override
                            public void cancellation() {}

                            @Override
                            public void extFailure(String url, String errorMessage) {
                                // System.out.println("PageLanguage check extFailure: " + url);
                            }
                        };

        ResultListener traqScoreDtoListener =
                        new ResultListenerAdapter<TRAQScoreDto>(TRAQScoreDto.class) {
                            @Override
                            public void extSuccess(String url, TRAQScoreDto obj) {
                                // System.out.println("TRAQScore check extSuccess: " + url + " - " +
                                // obj.toString());
                            }

                            @Override
                            public void cancellation() {}

                            @Override
                            public void extFailure(String url, String errorMessage) {
                                // System.out.println("TRAQScore check extFailure: " + url);
                            }
                        };

        ResultListener viewabilityListener =
                        new ResultListenerAdapter<ViewabilityDto>(ViewabilityDto.class) {
                            @Override
                            public void extSuccess(String url, ViewabilityDto obj) {
                                // System.out.println("Viewability check extSuccess: " + url + " - "
                                // + obj.toString());
                            }

                            @Override
                            public void cancellation() {}

                            @Override
                            public void extFailure(String url, String errorMessage) {
                                // System.out.println("Viewability check extFailure: " + url);
                            }
                        };


        IntegralClient integralClient = new IntegralClient("localhost", 8080, "10");

        final CountDownLatch latch = new CountDownLatch(1);

        Date startTime = BidderCalendar.getInstance().currentDate();

        for (DataMethod dataMethod : DataMethod.values()) {

            String dataMethodString = dataMethod.toString();

            ResultListener resultHandler = brandSafetyListener;

            if (dataMethod.equals(DataMethod.BRAND_SAFETY)) {
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

            for (int i = 0; i < 25000; i++) {
                for (String url : urls) {
                    integralClient.send(dataMethodString, url, resultHandler);
                }
            }
        }

        Date endTime = BidderCalendar.getInstance().currentDate();
        Long period = endTime.getTime() - startTime.getTime();
        System.out.println("Time:" + period.toString());

        latch.await();
        integralClient.shutdown();
    }


}
