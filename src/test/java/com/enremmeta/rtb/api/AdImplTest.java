package com.enremmeta.rtb.api;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.proto.adaptv.AdaptvAdapter;
import com.enremmeta.rtb.test.cases.Lot49Test;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdImplTest extends Lot49Test {

    @Mock
    private AdaptvAdapter adaptvAdapter;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString(), any());
    }

    @Test
    public void testSegments() {
        MockitoAnnotations.initMocks(this);
        when(adaptvAdapter.getName()).thenReturn("adaptv");

        try {
            AdImpl ad = new Ad_1111_fake(true);

            OpenRtbRequest req = new OpenRtbRequest();
            req.getLot49Ext().setAdapter(adaptvAdapter);
            List<Impression> imps = new ArrayList<Impression>();
            imps.add(new Impression());
            req.setImp(imps);

            UserSegments userSegments = null;
            assertTrue(ad.checkSegments(req, userSegments));

            userSegments = new UserSegments();
            HashMap<String, String> score1 = new HashMap<String, String>();
            score1.put("score", "0.5");
            userSegments.getUserSegmentsMap().put("Seg1", score1);
            assertTrue(ad.checkSegments(req, userSegments));

            userSegments = null;
            ad.setTargetingSegments("\"Seg2\"");
            assertFalse(ad.checkSegments(req, userSegments));

            ad.setTargetingSegments("NOT(\"Seg2\")");
            assertTrue(ad.checkSegments(req, userSegments));

            userSegments = new UserSegments();
            ad.setTargetingSegments("NOT(\"Seg2\")");
            assertTrue(ad.checkSegments(req, userSegments));

            userSegments = null;
            ad.setTargetingSegments("NOT(AND(\"Seg1\",\"Seg2\"))");
            assertTrue(ad.checkSegments(req, userSegments));

            userSegments = new UserSegments();
            assertTrue(ad.checkSegments(req, userSegments));

            userSegments = null;
            ad.setTargetingSegments("NOT(OR(\"Seg1\",\"Seg2\"))");
            assertTrue(ad.checkSegments(req, userSegments));

            userSegments = new UserSegments();
            assertTrue(ad.checkSegments(req, userSegments));

            userSegments = null;
            ad.setTargetingSegments("OR(\"Seg1\",NOT(\"Seg2\"))");
            assertTrue(ad.checkSegments(req, userSegments));

            userSegments = new UserSegments();
            assertTrue(ad.checkSegments(req, userSegments));

            userSegments = null;
            ad.setTargetingSegments("AND(\"Seg1\",NOT(\"Seg2\"))");
            assertFalse(ad.checkSegments(req, userSegments));

            userSegments = new UserSegments();
            assertFalse(ad.checkSegments(req, userSegments));

            userSegments = null;
            ad.setTargetingSegments("NOT(AND(\"Seg1\",NOT(\"Seg2\")))");
            assertTrue(ad.checkSegments(req, userSegments));

            userSegments = new UserSegments();
            assertTrue(ad.checkSegments(req, userSegments));

            // userSegments = null;
            // ad.setTargetingSegments("NOT(NOT(\"Seg2\"))");
            // assertFalse(ad.checkSegments(req, userSegments));
            //
            // userSegments = new UserSegments();
            // assertFalse(ad.checkSegments(req, userSegments));

            userSegments = new UserSegments();
            HashMap<String, String> score2 = new HashMap<String, String>();
            score1.put("score", "5");
            userSegments.getUserSegmentsMap().put("Seg3", score2);
            ad.setTargetingSegments("\"Seg2\"");
            assertFalse(ad.checkSegments(req, userSegments));

            HashMap<String, String> score3 = new HashMap<String, String>();
            score1.put("score", "0");
            userSegments.getUserSegmentsMap().put("Seg2", score3);
            assertTrue(ad.checkSegments(req, userSegments));

            ad.setTargetingSegments("AND(\"Seg2\",\"Seg4\")");
            assertFalse(ad.checkSegments(req, userSegments));

            HashMap<String, String> score4 = new HashMap<String, String>();
            score1.put("score", "0.1");
            userSegments.getUserSegmentsMap().put("Seg4", score4);
            assertTrue(ad.checkSegments(req, userSegments));

            HashMap<String, String> score5 = new HashMap<String, String>();
            score1.put("score", "0.1");
            userSegments.getUserSegmentsMap().put("Seg5", score5);
            assertTrue(ad.checkSegments(req, userSegments));

        } catch (Lot49Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testFrequencyCapacity() throws InterruptedException {
        MockitoAnnotations.initMocks(this);
        when(adaptvAdapter.getName()).thenReturn("adaptv");
        try {
            AdImpl ad = new Ad_1111_fake(true);
            ad.setWinRate(0.3);
            OpenRtbRequest req = new OpenRtbRequest();
            req.getLot49Ext().setAdapter(adaptvAdapter);
            List<Impression> imps = new ArrayList<Impression>();
            imps.add(new Impression());
            req.setImp(imps);

            FrequencyCap frequencyCapTS = new FrequencyCap(1, 1);
            ad.setFrequencyCap(frequencyCapTS);

            Map<String, Set<String>> bidHistory = new HashMap<String, Set<String>>();
            Map<String, Set<String>> impressionHistory = new HashMap<String, Set<String>>();
            UserFrequencyCapAttributes userFrequencyCap =
                            new UserFrequencyCapAttributes(bidHistory, impressionHistory);

            userFrequencyCap.updateBidsHistoryForTargetingStrategy(ad);
            TimeUnit.MILLISECONDS.sleep(10);
            userFrequencyCap.updateBidsHistoryForTargetingStrategy(ad);
            TimeUnit.MILLISECONDS.sleep(10);
            userFrequencyCap.updateBidsHistoryForTargetingStrategy(ad);

            boolean result = ad.checkFrequencyCap(req, userFrequencyCap);

            assertTrue(result);

            TimeUnit.MILLISECONDS.sleep(10);
            userFrequencyCap.updateBidsHistoryForTargetingStrategy(ad);
            result = ad.checkFrequencyCap(req, userFrequencyCap);

            assertFalse(result);

            TimeUnit.MILLISECONDS.sleep(10);
            userFrequencyCap.updateImpressionsHistoryForTargetingStrategy(ad);
            result = ad.checkFrequencyCap(req, userFrequencyCap);

            assertFalse(result);

            FrequencyCap frequencyCapCamp = new FrequencyCap(1, 1);
            ad.setFrequencyCapCampaign(frequencyCapCamp);

            TimeUnit.MILLISECONDS.sleep(10);
            userFrequencyCap.updateBidsHistoryForCampaign(ad);
            TimeUnit.MILLISECONDS.sleep(10);
            userFrequencyCap.updateBidsHistoryForCampaign(ad);
            TimeUnit.MILLISECONDS.sleep(10);
            userFrequencyCap.updateBidsHistoryForCampaign(ad);

            result = ad.checkFrequencyCap(req, userFrequencyCap);

            assertTrue(result);

            TimeUnit.MILLISECONDS.sleep(10);
            userFrequencyCap.updateImpressionsHistoryForCampaign(ad);
            result = ad.checkFrequencyCap(req, userFrequencyCap);

            assertFalse(result);
        } catch (Lot49Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testMeasurementSegments() throws InterruptedException {
        MockitoAnnotations.initMocks(this);
        when(adaptvAdapter.getName()).thenReturn("adaptv");
        try {
            AdImpl ad = new Ad_1111_fake(true);
            OpenRtbRequest req = new OpenRtbRequest();
            req.getLot49Ext().setAdapter(adaptvAdapter);

            UserSegments userSegments = new UserSegments();
            ad.measurementSegments = new HashSet<String>();

            assertTrue(ad.checkMeasurementSegments(userSegments));

            HashMap<String, String> score1 = new HashMap<String, String>();
            score1.put("score", "0.5");
            userSegments.getUserSegmentsMap().put("Seg1", score1);
            assertTrue(ad.checkMeasurementSegments(userSegments));

            ad.measurementSegments.add("Seg2");
            assertTrue(ad.checkMeasurementSegments(userSegments));

            ad.measurementSegments.add("Seg3");
            assertTrue(ad.checkMeasurementSegments(userSegments));

            HashMap<String, String> score2 = new HashMap<String, String>();
            score2.put("score", "0.7");
            userSegments.getUserSegmentsMap().put("Seg3", score2);
            assertFalse(ad.checkMeasurementSegments(userSegments));

        } catch (Lot49Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    class Ad_1111_fake extends AdImpl {

        public Ad_1111_fake(boolean test) throws Lot49Exception {
            super(test);

        }

    }
}
