package com.enremmeta.rtb.proto.adx;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.protos.adx.NetworkBid;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NetworkBid.BidRequest.class})
public class RealtimeBidding_BidRequestSpec {

    @Test
    public void testRealInitConstructor() throws Exception {

        byte[] buf = {};

        NetworkBid.BidRequest brInstance = Whitebox.invokeConstructor(NetworkBid.BidRequest.class,
                        new Class<?>[] {com.google.protobuf.CodedInputStream.class,
                                        com.google.protobuf.ExtensionRegistryLite.class},
                        new Object[] {com.google.protobuf.CodedInputStream.newInstance(buf),
                                        com.google.protobuf.ExtensionRegistryLite
                                                        .getEmptyRegistry()});
        assertNotNull("", brInstance.getId());
        assertNotNull("", brInstance.getIp());
    }

}
