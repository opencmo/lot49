package com.enremmeta.rtb.api;

import com.enremmeta.rtb.spi.providers.integral.result.IntegralValidationResult;
import com.enremmeta.rtb.spi.providers.integral.result.dto.BrandSafetyDto;

public class TargetingIntegralBrandSafety {

    private boolean isExcludeUnratable;

    private TargetingIntegralBsc adt;
    private TargetingIntegralBsc violence;
    private TargetingIntegralBsc alc;
    private TargetingIntegralBsc dim;
    private TargetingIntegralBsc drg;
    private TargetingIntegralBsc hat;
    private TargetingIntegralBsc off;
    private TargetingIntegralBsc sam;

    public TargetingIntegralBrandSafety(boolean isExcludeUnratable, TargetingIntegralBsc adt,
                    TargetingIntegralBsc violence, TargetingIntegralBsc alc,
                    TargetingIntegralBsc dim, TargetingIntegralBsc drg, TargetingIntegralBsc hat,
                    TargetingIntegralBsc off, TargetingIntegralBsc sam) {
        this.isExcludeUnratable = isExcludeUnratable;
        this.adt = adt;
        this.violence = violence;
        this.alc = alc;
        this.dim = dim;
        this.drg = drg;
        this.hat = hat;
        this.off = off;
        this.sam = sam;
    }

    public IntegralValidationResult validate(BrandSafetyDto brandSafety) {

        if (isExcludeUnratable) {
            if (brandSafety == null || brandSafety.getAction().equalsIgnoreCase("Unknown")
                            || brandSafety.getBsc() == null) {
                return new IntegralValidationResult(false, "Brand safety can not be rated");
            }
        }
        String msg = "";
        if (brandSafety != null && brandSafety.getBsc() != null) {
            if (adt != null) {
                boolean result = brandSafety.getBsc().getAdt() != null
                                && adt.validate(brandSafety.getBsc().getAdt());
                msg = "Parameter [adt] is " + brandSafety.getBsc().getAdt() + ", expected "
                                + brandSafety.getBsc().getAdt().toString();
                if (!result) {
                    return new IntegralValidationResult(false, msg);
                }
            }

            if (violence != null) {
                boolean result = brandSafety.getBsc().getVio() != null
                                && violence.validate(brandSafety.getBsc().getVio());
                msg = "Parameter [vio] is " + brandSafety.getBsc().getVio() + ", expected "
                                + brandSafety.getBsc().getVio().toString();
                if (!result) {
                    return new IntegralValidationResult(false, msg);
                }
            }

            if (alc != null) {
                boolean result = brandSafety.getBsc().getAlc() != null
                                && alc.validate(brandSafety.getBsc().getAlc());
                msg = "Parameter [alc] is " + brandSafety.getBsc().getAlc() + ", expected "
                                + brandSafety.getBsc().getAlc().toString();
                if (!result) {
                    return new IntegralValidationResult(false, msg);
                }
            }

            if (dim != null) {
                boolean result = brandSafety.getBsc().getDim() != null
                                && dim.validate(brandSafety.getBsc().getDim());
                msg = "Parameter [dim] is " + brandSafety.getBsc().getDim() + ", expected "
                                + brandSafety.getBsc().getDim().toString();
                if (!result) {
                    return new IntegralValidationResult(false, msg);
                }
            }

            if (drg != null) {
                boolean result = brandSafety.getBsc().getDrg() != null
                                && drg.validate(brandSafety.getBsc().getDrg());
                msg = "Parameter [drg] is " + brandSafety.getBsc().getDrg() + ", expected "
                                + brandSafety.getBsc().getDrg().toString();
                if (!result) {
                    return new IntegralValidationResult(false, msg);
                }
            }

            if (hat != null) {
                boolean result = brandSafety.getBsc().getHat() != null
                                && hat.validate(brandSafety.getBsc().getHat());
                msg = "Parameter [hat] is " + brandSafety.getBsc().getHat() + ", expected "
                                + brandSafety.getBsc().getHat().toString();
                if (!result) {
                    return new IntegralValidationResult(false, msg);
                }
            }

            if (off != null) {
                boolean result = brandSafety.getBsc().getOff() != null
                                && off.validate(brandSafety.getBsc().getOff());
                msg = "Parameter [off] is " + brandSafety.getBsc().getOff() + ", expected "
                                + brandSafety.getBsc().getOff().toString();
                if (!result) {
                    return new IntegralValidationResult(false, msg);
                }
            }

            if (sam != null) {
                boolean result = brandSafety.getBsc().getSam() != null
                                && sam.validate(brandSafety.getBsc().getSam());
                msg = "Parameter [sam] is " + brandSafety.getBsc().getSam() + ", expected "
                                + brandSafety.getBsc().getSam().toString();
                if (!result) {
                    return new IntegralValidationResult(false, msg);
                }
            }

        }

        return new IntegralValidationResult(true, msg);
    }



}
