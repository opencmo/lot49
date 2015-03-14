package com.enremmeta.rtb.api;

import com.enremmeta.rtb.proto.adx.AdXTargeting;
import com.enremmeta.rtb.proto.openx.OpenXTargeting;

public class ExchangeTargeting {

    public ExchangeTargeting() {
        // TODO Auto-generated constructor stub
    }

    private AdXTargeting adxTargeting;

    public AdXTargeting getAdxTargeting() {
        return adxTargeting;
    }

    public void setAdxTargeting(AdXTargeting adxTargeting) {
        this.adxTargeting = adxTargeting;
    }

    private OpenXTargeting openxTargeting;

    public OpenXTargeting getOpenxTargeting() {
        return openxTargeting;
    }

    public void setOpenxTargeting(OpenXTargeting openxTargeting) {
        this.openxTargeting = openxTargeting;
    }

}
