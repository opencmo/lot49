package com.enremmeta.rtb.test.cases;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.proto.bidswitch.BidSwitchAdapter;
import com.enremmeta.rtb.proto.bidswitch.BidSwitchConfig;
import com.enremmeta.util.Utils;

/**
 * Tests Bidswitch specific stuff. Man, this whole JUnit stuff has changed in an annoying way.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 */
public class BidswitchTest extends ExchangeTest {

    @Test
    public void test_BidswitchJsonParsing() {
        try {
            File bidswitchFile = getTestDataFile("bidswitch/display1.json");

            // Just ensure things parse correctly
            // and check some fields which should be filled with data
            OpenRtbRequest req = Utils.MAPPER.readValue(bidswitchFile, OpenRtbRequest.class);
            assertNotNull(req);
            assertEquals(req.getId(), "64916312-96fc-4e20-802a-e6c7f2e3936e");
            assertNotNull(req.getUser());
            assertEquals(req.getUser().getBuyeruid(), "rB4An1ZS8bQe3Xvh/GigAg");
            assertNotNull(req.getExt());
            assertNotNull(req.getDevice());
            assertNotNull(req.getSite());
            assertNotNull(req.getLot49Ext());
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void test_BidswitchAdapterConvertRequest() {
        BidSwitchAdapter adapter = getBidSwitchAdapter();
        File bidswitchFile = getTestDataFile("bidswitch/display1.json");
        try {
            OpenRtbRequest req = Utils.MAPPER.readValue(bidswitchFile, OpenRtbRequest.class);
            assertNotNull(req);
            OpenRtbRequest convertedRequest = adapter.convertRequest(req);
            assertNotNull(convertedRequest);
            assertNotNull(convertedRequest.getLot49Ext().getAdapter());

            assertNotNull(req.getId());
            assertNotNull(convertedRequest.getId());
            assertEquals(req.getId(), convertedRequest.getId());

            assertNotNull(req.getImp());
            assertNotNull(convertedRequest.getImp());
            assertFalse(convertedRequest.getImp().isEmpty());
            assertEquals(req.getImp().size(), convertedRequest.getImp().size());

            assertEquals(req.getImp().get(0).getTagid(),
                            convertedRequest.getImp().get(0).getTagid());
            assertEquals(req.getImp().get(0).getId(), convertedRequest.getImp().get(0).getId());
            assertEquals(req.getImp().get(0).getBidfloor(),
                            convertedRequest.getImp().get(0).getBidfloor());
            assertEquals(req.getImp().get(0).getBidfloorcur(),
                            convertedRequest.getImp().get(0).getBidfloorcur());

            assertNotNull(req.getUser());
            assertNotNull(convertedRequest.getUser());
            assertEquals(req.getUser().getBuyeruid(), convertedRequest.getUser().getBuyeruid());
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private BidSwitchAdapter getBidSwitchAdapter() {
        BidSwitchConfig conf = new BidSwitchConfig();
        conf.setSeatId("TEST");
        return new BidSwitchAdapter(conf);
    }

}
