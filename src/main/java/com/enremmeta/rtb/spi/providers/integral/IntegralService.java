package com.enremmeta.rtb.spi.providers.integral;

import java.util.Date;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.spi.providers.integral.result.DateTypeAdapter;
import com.enremmeta.rtb.spi.providers.integral.result.ResultListener;
import com.enremmeta.rtb.spi.providers.integral.result.dto.IntegralAllResponse;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Created by mgorbal
 */
public class IntegralService {

    public static final String INTEGRAL_PROVIDER_NAME = "integral";

    private IntegralClient2 integralClient;

    private static final GsonBuilder gsonBuilder;

    static {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new DateTypeAdapter());
    }

    public IntegralService(IntegralConfig integralConfig) {
        if (integralConfig.getMaxConnPerRoute() != null
                        && integralConfig.getMaxConnPerRoute() > 0) {
            integralClient = new IntegralClient2(
                            integralConfig.getHost() + ":" + integralConfig.getPort(),
                            integralConfig.getClientId(), integralConfig.getMaxConnPerRoute());
        } else {
            integralClient = new IntegralClient2(
                            integralConfig.getHost() + ":" + integralConfig.getPort(),
                            integralConfig.getClientId());
        }
    }

    public void checkUrl(String url, IntegralInfoReceived integralInfoReceived) {

        integralInfoReceived.setIntegralRequest(
                        integralClient.send(DataMethod.ALL.toString(), url, new ResultListener() {
                            @Override
                            public void success(String url, String content) {
                                try {
                                    IntegralAllResponse integralAllResponse = gsonBuilder.create()
                                                    .fromJson(content, IntegralAllResponse.class);
                                    integralInfoReceived
                                                    .setIntegralAllResponse(integralAllResponse);
                                    integralInfoReceived.setResponseJson(content);
                                    LogUtils.trace("Received Integral response for url: " + url
                                                    + ". Time to get Integral "
                                                    + integralInfoReceived.getRequestTime());
                                    integralInfoReceived.setCompleted(true);
                                } catch (JsonParseException e) {
                                    LogUtils.error(e);
                                    failure(url, e.toString() + "; Content: " + content);
                                }
                            }

                            @Override
                            public void cancellation() {
                                integralInfoReceived.setCompleted(true);
                            }

                            @Override
                            public void failure(String url, String errorMessage) {
                                String errMsg = "Integral: could not get info for url: " + url
                                                + "; ErrorMessage: " + errorMessage;
                                LogUtils.debug(errMsg);
                                integralInfoReceived.setErrorMsg(errMsg);
                                integralInfoReceived.setCompleted(true);
                            }
                        }));
    }

}
