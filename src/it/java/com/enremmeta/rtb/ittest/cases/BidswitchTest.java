package com.enremmeta.rtb.ittest.cases;

import java.io.File;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Assert;
import org.junit.Test;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.test.cases.ExchangeTest;

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
    public void test_BidswitchAdapter() {
        try {
            File bidswitchFile = getTestDataFile("bidswitch/display1.json");
            String req = loadContents(bidswitchFile);

            String url = testUtils.getConfig().getBaseUrl() + Lot49Constants.ROOT_PATH_AUCTIONS
                            + "/" + Lot49Constants.EXCHANGE_BIDSWITCH;

            System.out.println("Sending request to " + url);
            Client client = ClientBuilder.newClient();
            Invocation.Builder builder = client.target(url).request(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON);
            System.out.println("Sending request to " + url);
            try {
                OpenRtbResponse resp = builder.post(Entity.entity(req, MediaType.APPLICATION_JSON),
                                OpenRtbResponse.class);
                assertNull(resp);
            } catch (BadRequestException bre) {
                MultivaluedMap<String, Object> headers = bre.getResponse().getHeaders();
                for (String key : headers.keySet()) {
                    System.out.println(key + ": " + headers.get(key));
                }
                throw bre;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
