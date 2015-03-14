package com.enremmeta.rtb.api;

import com.enremmeta.rtb.spi.providers.integral.result.IntegralValidationResult;

public class TargetingIntegralTraq {

    private Integer traqScore;

    public TargetingIntegralTraq(Integer traqScore) {
        this.traqScore = traqScore;
    }

    public IntegralValidationResult validate(int score) {
        String msg = "";
        if (traqScore != null) {
            msg = "Parameter [traq] is " + score + ", expected " + traqScore;
            if (score < traqScore) {
                return new IntegralValidationResult(false, msg);
            } ;
        }
        return new IntegralValidationResult(true, msg);
    }
}
