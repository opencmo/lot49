package com.enremmeta.rtb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.enremmeta.rtb.constants.Lot49Constants;

public class LogUtilsTest {

    @Test
    public void getPhaseOfDecisionTest() {

        assertTrue(LogUtils.getPhaseOfDecision(null).isEmpty());
        assertTrue(LogUtils.getPhaseOfDecision("").isEmpty());
        assertTrue(LogUtils.getPhaseOfDecision(" ").isEmpty());
        assertTrue(LogUtils.getPhaseOfDecision("1").isEmpty());

        assertEquals("09", LogUtils.getPhaseOfDecision(Lot49Constants.DECISION_BIDS_MADE));
        assertEquals("02", LogUtils.getPhaseOfDecision(Lot49Constants.DECISION_CATEGORY));
        assertEquals("01", LogUtils.getPhaseOfDecision(Lot49Constants.DECISION_DATE));
    }

    @Test
    public void getStepOfDecisionTest() {

        assertTrue(LogUtils.getStepOfDecision(null).isEmpty());
        assertTrue(LogUtils.getStepOfDecision("").isEmpty());
        assertTrue(LogUtils.getStepOfDecision(" ").isEmpty());
        assertTrue(LogUtils.getStepOfDecision("01.2").isEmpty());

        assertEquals("01", LogUtils.getStepOfDecision(Lot49Constants.DECISION_BIDS_MADE));
        assertEquals("15", LogUtils.getStepOfDecision(Lot49Constants.DECISION_CATEGORY));
        assertEquals("03", LogUtils.getStepOfDecision(Lot49Constants.DECISION_DATE));
    }

}
