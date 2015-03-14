package com.enremmeta.rtb.proto.pubmatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.RtbBean;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;

public class PubmaticWinLossObject implements RtbBean {

    /**
     * 
     */
    private static final long serialVersionUID = -3573417711109226487L;
    private String rId;
    private String bCur;
    private double wBid;
    private int csa;
    private List<PubmaticBidObject> bInfo;

    public PubmaticWinLossObject(Object o) throws Lot49Exception {
        super();
        try {
            Map map = (Map) o;
            this.rId = (String) map.get("rId");
            this.bCur = (String) map.get("bCur");
            this.wBid = (Double) map.get("wBid");
            this.csa = (Integer) map.get("csa");
            List list = (List) map.get("bInfo");
            if (list != null) {
                bInfo = new ArrayList<PubmaticBidObject>();
                for (Object o2 : list) {
                    Map bmap = (Map) o2;
                    this.bInfo.add(new PubmaticBidObject(bmap));
                }
            }
        } catch (ClassCastException cce) {
            throw new Lot49Exception("Error parsing wli " + o, cce);
        }
    }

    @Override
    public String toString() {
        return "{ \"rId\" : \"" + rId + "\", \"bCur\" : \"" + bCur + "\", \"wBid\" : \"" + wBid
                        + "\", \"csa\" : " + csa + ", \"bInfo\" : [" + bInfo + "] }";

    }

    /**
     * Corresponds to {@link OpenRtbRequest#getId() Bid Request ID}
     */
    public String getrId() {
        return rId;
    }

    public void setrId(String rId) {
        this.rId = rId;
    }

    public String getbCur() {
        return bCur;
    }

    public void setbCur(String bCur) {
        this.bCur = bCur;
    }

    public double getwBid() {
        return wBid;
    }

    public void setwBid(double wBid) {
        this.wBid = wBid;
    }

    public int getCsa() {
        return csa;
    }

    public void setCsa(int csa) {
        this.csa = csa;
    }

    public List<PubmaticBidObject> getbInfo() {
        return bInfo;
    }

    public void setbInfo(List<PubmaticBidObject> bInfo) {
        this.bInfo = bInfo;
    }

}
