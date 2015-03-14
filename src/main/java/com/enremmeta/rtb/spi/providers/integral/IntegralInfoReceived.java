package com.enremmeta.rtb.spi.providers.integral;

import java.util.concurrent.Future;

import org.apache.http.HttpResponse;

import com.enremmeta.rtb.spi.providers.ProviderInfoReceived;
import com.enremmeta.rtb.spi.providers.integral.result.dto.BrandSafetyDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.IntegralAllResponse;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ViewabilityDto;
import com.enremmeta.util.BidderCalendar;

public class IntegralInfoReceived implements ProviderInfoReceived {

    private IntegralAllResponse integralAllResponse;

    private boolean isError;
    private String errorMsg;
    private String responseJson;
    private String targetingStatus;
    private boolean completed = false;
    private Future<HttpResponse> integralRequest;
    private long startRequestTime = BidderCalendar.getInstance().currentTimeMillis();

    public Integer getTraqScore() {
        return integralAllResponse.getTraq();
    }

    public BrandSafetyDto getBrandSafetyDto() {
        return integralAllResponse.getBrandSafetyDto();
    }

    public ViewabilityDto getViewabilityDto() {
        return integralAllResponse == null ? null : integralAllResponse.getUem();
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        setError(true);
        this.errorMsg = errorMsg;
    }

    public IntegralAllResponse getIntegralAllResponse() {
        return integralAllResponse;
    }

    public void setIntegralAllResponse(IntegralAllResponse integralAllResponse) {
        this.integralAllResponse = integralAllResponse;
    }

    public String getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(String responseJson) {
        this.responseJson = responseJson;
    }

    public String getTargetingStatus() {
        return targetingStatus;
    }

    public void setTargetingStatus(String targetingStatus) {
        this.targetingStatus = targetingStatus;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Future<HttpResponse> getIntegralRequest() {
        return integralRequest;
    }

    public void setIntegralRequest(Future<HttpResponse> integralRequest) {
        this.integralRequest = integralRequest;
    }

    public void cancelIntegralRequest() {
        if (integralRequest != null) {
            integralRequest.cancel(false);
        }
    }

    public long getRequestTime() {
        return BidderCalendar.getInstance().currentTimeMillis() - startRequestTime;
    }
}
