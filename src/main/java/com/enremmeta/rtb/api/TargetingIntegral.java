package com.enremmeta.rtb.api;

import com.enremmeta.rtb.spi.providers.integral.result.IntegralValidationResult;
import com.enremmeta.rtb.spi.providers.integral.result.dto.BrandSafetyDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ViewabilityDto;

public class TargetingIntegral {

    private TargetingIntegralTraq traq;
    private TargetingIntegralBrandSafety brandSafety;
    private TargetingIntegralViewability viewability;

    public TargetingIntegral(TargetingIntegralTraq traq, TargetingIntegralBrandSafety brandSafety,
                    TargetingIntegralViewability viewability) {
        this.traq = traq;
        this.brandSafety = brandSafety;
        this.viewability = viewability;
    }

    public boolean isTraq() {
        return traq != null;
    }

    public boolean isViewability() {
        return viewability != null;
    }

    public boolean isBrandSafety() {
        return brandSafety != null;
    }

    IntegralValidationResult validateTraq(int traqValue) {
        if (traq == null) {
            return new IntegralValidationResult(true, "");
        }
        return traq.validate(traqValue);
    }

    IntegralValidationResult validateViewability(ViewabilityDto viewabilityDto) {
        if (viewability == null) {
            return new IntegralValidationResult(true, "");
        }
        return viewability.validate(viewabilityDto);
    }

    IntegralValidationResult validateBrandSafety(BrandSafetyDto brandSafetyDto) {
        if (brandSafety == null) {
            return new IntegralValidationResult(true, "");
        }
        return brandSafety.validate(brandSafetyDto);
    }

}
