package com.enremmeta.rtb.proto.adx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AdXConstantsSpec {

    @Test
    public void shouldSetBothIndividualConstantsAndCREATIVE_STATUS_CODE() {
        assertEquals("Creative won the auction", AdXConstants.CREATIVE_STATUS_CODE.get(1));
        assertEquals(8, AdXConstants.EXCLUDED_COOKIETARGETING_ISCOOKIETARGETED);
    }

}
