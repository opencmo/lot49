package com.enremmeta.rtb.api.proto.openrtb.lot49;

import com.enremmeta.rtb.RtbBean;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.rtb.api.UserSegments;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.spi.providers.integral.IntegralInfoReceived;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Data that is gotten from an external source (e.g., a user cache), and, therefore, there may not
 * be a guarantee of this being populated always.
 *
 * @see Lot49Ext
 * @see Ad#canBid1(OpenRtbRequest)
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class Lot49ExtRemote implements RtbBean {

    public Lot49ExtRemote() {
        // TODO Auto-generated constructor stub
    }


    private IntegralInfoReceived integralInfoReceived;

    private UserAttributes userAttributes;

    public IntegralInfoReceived getIntegralInfoReceived() {
        return integralInfoReceived;
    }

    public void setIntegralInfoReceived(IntegralInfoReceived integralInfoReceived) {
        this.integralInfoReceived = integralInfoReceived;
    }

    public UserAttributes getUserAttributes() {
        return userAttributes;
    }

    public void setUserAttributes(UserAttributes userAttributes) {
        this.userAttributes = userAttributes;
    }

    public UserSegments getUserSegments() {
        return userSegments;
    }

    public void setUserSegments(UserSegments userSegments) {
        this.userSegments = userSegments;
    }

    @JsonIgnore
    private UserSegments userSegments;
}
