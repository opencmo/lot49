package com.enremmeta.rtb.api;

import com.enremmeta.rtb.spi.providers.integral.result.IntegralValidationResult;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ViewabilityDto;

public class TargetingIntegralViewability {

    private Integer viabmin;
    private Integer ivt;
    private Integer ivl;
    private Integer ivp;
    private Integer ivu;
    private Integer niv;
    private Integer top;

    public TargetingIntegralViewability(Integer viabmin, Integer ivt) {
        this.viabmin = viabmin;
        this.ivt = ivt;
    }

    public TargetingIntegralViewability(Integer viabmin, Integer ivl, Integer ivp, Integer ivt,
                    Integer ivu, Integer niv, Integer top) {
        this.viabmin = viabmin;
        this.ivl = ivl;
        this.ivp = ivp;
        this.ivt = ivt;
        this.ivu = ivu;
        this.niv = niv;
        this.top = top;
    }

    public IntegralValidationResult validate(ViewabilityDto viewabilityDto) {
        String msg = "";
        if (viabmin != null) {
            // Predicted probability of an ad being in-view when the user opens the page is greater
            // than
            msg = "Parameter [iviab] is " + viewabilityDto.getIviab() + ", expected " + viabmin;
            if (viewabilityDto.getIviab() == null || viewabilityDto.getIviab() <= viabmin) {
                return new IntegralValidationResult(false, msg);
            }
        }


        if (ivt != null) {
            // The average time an advertisement is in view on a page
            msg = "Parameter [ivt] is " + viewabilityDto.getIvt() + ", expected " + ivt;
            if (viewabilityDto.getIvt() == null || viewabilityDto.getIvt() < ivt) {
                return new IntegralValidationResult(false, msg);
            }
        }

        if (ivl != null) {
            msg = "Parameter [ivl] is " + viewabilityDto.getIvl() + ", expected " + ivl;
            if (viewabilityDto.getIvl() == null || viewabilityDto.getIvl() < ivl) {
                return new IntegralValidationResult(false, msg);
            }
        }

        if (ivu != null) {
            msg = "Parameter [ivu] is " + viewabilityDto.getIvu() + ", expected " + ivu;
            if (viewabilityDto.getIvu() == null || viewabilityDto.getIvu() < ivu) {
                return new IntegralValidationResult(false, msg);
            }
        }

        if (ivp != null) {
            // Predicted probability of an ad being in-view for more than 5 seconds is greater than
            msg = "Parameter [ivp] is " + viewabilityDto.getIvp() + ", expected " + ivp;
            if (viewabilityDto.getIvp() == null || viewabilityDto.getIvp() < ivp) {
                return new IntegralValidationResult(false, msg);
            }
        }

        if (top != null) {
            msg = "Parameter [top] is " + viewabilityDto.getTop() + ", expected " + top;
            if (viewabilityDto.getTop() == null || viewabilityDto.getTop() < top) {
                return new IntegralValidationResult(false, msg);
            }
        }

        if (niv != null) {
            // Predicted probability that an ad will never be in-view on user's browser is lower
            // than
            msg = "Parameter [niv] is " + viewabilityDto.getNiv() + ", expected " + niv;
            if (viewabilityDto.getNiv() == null || viewabilityDto.getNiv() >= niv) {
                return new IntegralValidationResult(false, msg);
            }
        }

        return new IntegralValidationResult(true, msg);
    }
}
