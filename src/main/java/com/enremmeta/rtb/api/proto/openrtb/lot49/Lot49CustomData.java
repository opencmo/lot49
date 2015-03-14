package com.enremmeta.rtb.api.proto.openrtb.lot49;

import java.util.Map;

import com.enremmeta.rtb.RtbBean;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.jersey.AuctionsSvc;

/**
 * It may be the case is that the {@link User#getCustomdata() custom user data} we sync (out of
 * band) to exchanges is a JSON document represented by {@link Lot49CustomData}. If the exchange
 * does not allow for cookie sync, then we use {@link Lot49CustomData#getUid() its uid field} to
 * populate with our cookie data. However, it is not automatic, and is a responsibility of any
 * particular endpoint to make this translation (e.g.,
 * {@link AuctionsSvc#onOpenX(javax.ws.rs.container.AsyncResponse, javax.ws.rs.core.UriInfo, com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest, String, javax.servlet.http.HttpServletRequest, String, String)}
 * ).
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class Lot49CustomData implements RtbBean {

    public Lot49CustomData() {
        // TODO Auto-generated constructor stub
    }

    private Map udat;

    private String uid;

    /**
     * Custom user data.
     */
    public Map getUdat() {
        return udat;
    }

    public void setUdat(Map udat) {
        this.udat = udat;
    }

    /**
     * Our user ID.
     * 
     * @see Lot49Config#getUserIdCookie()
     */
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
