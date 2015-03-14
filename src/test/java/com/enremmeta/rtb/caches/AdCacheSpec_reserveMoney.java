package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.caches.AdCache.BudgetAllocationResult;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.dao.impl.redis.RedisDaoCounters;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ServiceRunner;
import com.lambdaworks.redis.RedisConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, RedisConnection.class, LocalOrchestrator.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_reserveMoney {

    private ServiceRunner serviceRunnerSimpleMock;
    private Lot49Config configMock;
    private AdCacheConfig config;

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        configMock = Mockito.mock(Lot49Config.class);
        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.doReturn(configMock).when(serviceRunnerSimpleMock).getConfig();

        config = new AdCacheConfig();

        RedisServiceConfig redisServiceConfig = new RedisServiceConfig();
        redisServiceConfig.setHost("221.34.157.44");
        redisServiceConfig.setPort(3000);

        config.setPacing(new PacingServiceConfig());
        config.getPacing().setRedis(redisServiceConfig);

        PowerMockito.mockStatic(LogUtils.class);
    }

    @Test
    public void negativeFlow_notABidder() throws Lot49Exception {
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(Mockito.mock(LocalOrchestrator.class));

        AdCache adc = new AdCache(config);

        Ad ad = new SharedSetUp.Ad_1001001_mock();

        BudgetAllocationResult result = null;
        try {
            result = Whitebox.invokeMethod(adc, "reserveMoney", Mockito.mock(RedisConnection.class),
                            ad);
        } catch (Exception e) {
            fail("unexpected exception thrown" + e.getMessage());
        }

        assertTrue(Whitebox.getInternalState(result, "msg").toString()
                        .contains("I am not a bidder, done."));

    }

    @Test
    public void negativeFlow_noMoneyLeft() throws Lot49Exception {
        LocalOrchestrator lo = new LocalOrchestrator(new OrchestratorConfig());
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator()).thenReturn(lo);

        AdCache adc = new AdCache(config);

        Ad ad = new SharedSetUp.Ad_1001001_mock();

        BudgetAllocationResult result = null;
        try {
            result = Whitebox.invokeMethod(adc, "reserveMoney", Mockito.mock(RedisConnection.class),
                            ad);
        } catch (Exception e) {
            fail("unexpected exception thrown" + e.getMessage());
        }

        String msg = Whitebox.getInternalState(result, "msg").toString();
        assertFalse(msg.contains("I am not a bidder, done."));
        assertTrue(msg.contains("No money left: $0.00 (micro$0)"));
    }

    @Test
    public void positiveFlow_prevAdIsNull() throws Exception {
        LocalOrchestrator lo = new LocalOrchestrator(new OrchestratorConfig());
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator()).thenReturn(lo);

        AdCache adc = new AdCache(config);
        Whitebox.setInternalState(adc, "ttlMinutes", 1000);

        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidPrice(1000L);

        RedisDaoCounters rdc = Mockito.mock(RedisDaoCounters.class);
        Mockito.when(rdc.get(KVKeysValues.WIN_COUNT_PREFIX + ad.getId())).thenReturn(1000L);
        Whitebox.setInternalState(adc, "winRateCounters", rdc);

        RedisConnection rCon = Mockito.mock(RedisConnection.class);
        Mockito.when(rCon.get(KVKeysValues.BUDGET_PREFIX + ad.getId())).thenReturn("10000000");


        BudgetAllocationResult result = null;

        result = Whitebox.invokeMethod(adc, "reserveMoney", rCon, ad);


        String msg = Whitebox.getInternalState(result, "msg").toString();
        assertTrue(msg.contains(
                        "Based on projected spend of 10000000, and bid price 1000,  we want to win 10000000/1000=10000 impressions."));
        assertTrue(msg.contains(
                        "For 10000 impressions at 0.1 win rate we need to make 100000 bids."));
    }

    @Test
    public void positiveFlow_prevAdNotNull() throws Exception {
        LocalOrchestrator lo = new LocalOrchestrator(new OrchestratorConfig());
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator()).thenReturn(lo);

        AdCache adc = new AdCache(config);
        Whitebox.setInternalState(adc, "ttlMinutes", 1000);

        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidPrice(1000L);

        RedisDaoCounters rdc = Mockito.mock(RedisDaoCounters.class);
        Mockito.when(rdc.get(KVKeysValues.WIN_COUNT_PREFIX + ad.getId())).thenReturn(1000L);
        Whitebox.setInternalState(adc, "winRateCounters", rdc);

        RedisConnection rCon = Mockito.mock(RedisConnection.class);
        Mockito.when(rCon.get(KVKeysValues.BUDGET_PREFIX + ad.getId())).thenReturn("10000000");

        AdCache adcSpy = Mockito.spy(adc);
        Ad prevAd = Mockito.mock(Ad.class);
        Mockito.when(prevAd.getLoadedOn()).thenReturn(ad.getLoadedOn() - 20000000L);
        Mockito.when(adcSpy.getAd(any())).thenReturn(prevAd);

        BudgetAllocationResult result = null;

        result = Whitebox.invokeMethod(adcSpy, "reserveMoney", rCon, ad);


        String msg = Whitebox.getInternalState(result, "msg").toString();
        assertTrue(msg.contains(
                        "AdCache: Ad 1001001: Based on projected spend of 2500000, and bid price 1000,  we want to win 2500000/1000=2500 impressions."));
        assertTrue(msg.contains(
                        "For 2500 impressions at 0.1 win rate we need to make 25000 bids."));
    }

    // additional tests
    
    private long bidPrice = 100;
    private long wins = 10;
    private long bids = 50;
    private long budget = 3000;
    
    private long spendAmount = 7000;
    private long prevProjectedSpend = 0;
    private long prevSpendAmount = 0;
    
    private int ttlMinutes = 1000;
    private String startsOnStr = null;
    private String endsOnStr = null;
    
    private AdCache createAdCache() throws Lot49Exception {
        LocalOrchestrator lo = new LocalOrchestrator(new OrchestratorConfig());
        Mockito.doReturn(lo).when(serviceRunnerSimpleMock).getOrchestrator();
        
        config.setTtlMinutes(ttlMinutes);
        AdCache adc = new AdCache(config);
        
        return adc;
    }

    private RedisDaoCounters createRedisDaoCountersMock(Ad ad, long wins, long bids, long spendAmount, long bidAmount) throws Lot49Exception {
        String adId = ad.getId();
        RedisDaoCounters rdc = Mockito.mock(RedisDaoCounters.class);
        
        if (wins != 0) { Mockito.when(rdc.get(KVKeysValues.WIN_COUNT_PREFIX + adId)).thenReturn(wins); }
        if (bids != 0) { Mockito.when(rdc.get(KVKeysValues.BID_COUNT_PREFIX + adId)).thenReturn(bids); }
        if (spendAmount != 0) { Mockito.when(rdc.get(KVKeysValues.SPEND_AMOUNT_PREFIX + adId)).thenReturn(spendAmount); }
        if (bidAmount != 0) { Mockito.when(rdc.get(KVKeysValues.BID_AMOUNT_PREFIX + adId)).thenReturn(bidAmount); }
        
        return rdc;
    }

    @SuppressWarnings("unchecked")
    private RedisConnection<String, String> createRedisConnectionMock(Ad ad, long budget, String startsOnStr, String endsOnStr, long bidPrice) {
        String adId = ad.getId();
        RedisConnection<String, String> rCon = Mockito.mock(RedisConnection.class);
        
        if (budget != 0) {
            String budgetStr = budget == Long.MAX_VALUE ? KVKeysValues.BUDGET_UNLIMITED : String.valueOf(budget);
            if (budgetStr != null) { Mockito.when(rCon.get(KVKeysValues.BUDGET_PREFIX + adId)).thenReturn(budgetStr); }
        }
        
        if (startsOnStr != null) { Mockito.when(rCon.get(KVKeysValues.STARTS_ON_PREFIX + adId)).thenReturn(startsOnStr); }
        if (endsOnStr != null) { Mockito.when(rCon.get(KVKeysValues.ENDS_ON_PREFIX + adId)).thenReturn(endsOnStr); }
        
        if (bidPrice > 0) { Mockito.when(rCon.get(KVKeysValues.BID_PRICE_PREFIX + adId)).thenReturn(String.valueOf(bidPrice)); }
        if (bidPrice < 0) { Mockito.when(rCon.get(KVKeysValues.BID_PRICE_PREFIX + adId)).thenReturn("Invalid bid price (" + bidPrice + ")"); }
        
        return rCon;
    }
    
    private class ControlValues {
        long projectedSpend = 0;
        double avgWinPrice = 0;
        double winRate = 0;
        long impsNeeded = 0;
        long bidsNeeded = 0;
        
        public ControlValues() { 
            this(0, 0, 0, 0, 0);
        }
        
        public ControlValues(long projectedSpend, double avgWinPrice, double winRate, long impsNeeded, long bidsNeeded) {
            this.projectedSpend = projectedSpend;
            this.avgWinPrice = avgWinPrice;
            this.winRate = winRate;
            this.impsNeeded = impsNeeded;
            this.bidsNeeded = bidsNeeded;
        }
    }
    
    private class CalcControlValues extends ControlValues {
        public CalcControlValues(String adId) throws Lot49Exception {
            calculate(adId);
        }
        
        public void calculate(String adId) throws Lot49Exception {
            boolean asap = (endsOnStr != null) && endsOnStr.equalsIgnoreCase(KVKeysValues.PACING_ASAP);
            final DateTime now = getBidderCalendar().currentDateTime();
            DateTime endsOn = now.plusDays(1); /// tomorrow

            if (endsOnStr != null && !asap) {
                try {
                    endsOn = getBidderCalendar().toDateTime(endsOnStr);
                } catch (IllegalArgumentException iae) { }
            }
            
            if (endsOn.isBefore(now)) { throw new IllegalArgumentException(String.format("End date (%s) is before now (%s)", endsOn, now)); }
            
            int peerCount = serviceRunnerSimpleMock.getOrchestrator().getNumberOfPeers();
            int minsLeft = Minutes.minutesBetween(now, endsOn).getMinutes();
            long periodLength = ttlMinutes;
            long refreshPeriodsLeft = minsLeft / periodLength;
            
            projectedSpend = budget / peerCount / (asap ? 1 : refreshPeriodsLeft);
            
            long unspentAmountReal = prevProjectedSpend - prevSpendAmount;
            /* !!! calculation algorithm for projectedSpend was changed !!!
            if (unspentAmountReal < 0) {
                if ((projectedSpend + unspentAmountReal) > 0) {
                    projectedSpend += unspentAmountReal;
                } else {
                    projectedSpend = 0;
                }
            } else {
                projectedSpend += unspentAmountReal;
            }
            */
            long unspentAmountAdjusted = unspentAmountReal;
            if (unspentAmountReal < 0) {
                unspentAmountAdjusted = 0;
            }
            projectedSpend += unspentAmountAdjusted;

            if (bids != 0 && wins != 0) {
                avgWinPrice = ((double) spendAmount) / wins;
                winRate = ((double) wins) / bids;
            }
            
            if (winRate < config.getPacing().getWinRateMin())
            {
                winRate = config.getPacing().getEffectiveWinRateIfLessThanMin();
            }
            
            if (avgWinPrice > 0)
            {
                impsNeeded = Math.round(Math.ceil(projectedSpend / avgWinPrice));
            } else {
                impsNeeded = Math.round(Math.ceil(projectedSpend / bidPrice));
            }

            bidsNeeded = Math.round(Math.ceil(impsNeeded / winRate));
        }
    }
    
    @Test
    public void positiveFlow_prevAdIsNullAndNotFirstRun() throws Exception {
        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidPrice(bidPrice);

        RedisDaoCounters rdc = createRedisDaoCountersMock(ad, wins, 0, 0, 0);
        RedisConnection<String, String> rCon = createRedisConnectionMock(ad, budget, null, null, bidPrice);

        AdCache adc = createAdCache();
        Whitebox.setInternalState(adc, "winRateCounters", rdc);
        Whitebox.setInternalState(adc, "first", false);

        BudgetAllocationResult result = Whitebox.invokeMethod(adc, "reserveMoney", rCon, ad);

        String msg = Whitebox.getInternalState(result, "msg").toString();
        assertTrue(msg.contains("Ad not found for last period."));
        assertTrue(msg.contains("we want to win"));
        assertTrue(msg.contains("we need to make"));
    }
    
    @Test
    public void negativeFlow_startsOnIsAfterNow() throws Exception {
        startsOnStr = getBidderCalendar().currentDateTime().plusDays(1).toString();
        
        String resultMessage = testReserveMoney_prevAdIsNull(false, null);
        
        assertThat(resultMessage, containsString("Starts on date " + startsOnStr + " is after now"));
    }
    
    @Test
    public void negativeFlow_startsOnIsInvalid() throws Exception {
        startsOnStr = "Invalid start datetime";
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString(), Mockito.any());
        
        String resultMessage = testReserveMoney_prevAdIsNull(false, null);
        
        assertThat(resultMessage, containsString("Could not parse start datetime " + startsOnStr + ", not starting."));
    }
    
    @Test
    public void negativeFlow_endsOnIsBeforeNow() throws Exception {
        endsOnStr = getBidderCalendar().currentDateTime().minusDays(1).toString();
        
        String resultMessage = testReserveMoney_prevAdIsNull(false, null);
        
        assertThat(resultMessage, containsString("Ends on date " + endsOnStr + " is before now"));
    }
    
    @Test
    public void positiveFlow_endsOnIsASAP() throws Exception {
        endsOnStr = KVKeysValues.PACING_ASAP;
        
        ControlValues cv = new ControlValues(3000, 700, 0.2, 5, 25);
        
        String resultMessage = testReserveMoney_prevAdIsNull(true, cv);
        
        assertThat(resultMessage, containsString("Ad to end ASAP - greedy strategy."));
    }
    
    @Test
    public void positiveFlow_endsOnIsValid() throws Exception {
        ttlMinutes = 1000; /// set big value for ttlMinutes to avoid the impact of the current time difference in test and in real method
        endsOnStr = getBidderCalendar().currentDateTime().plusDays(3).toString();
        
        ControlValues cv = new ControlValues(750, 700, 0.2, 2, 10);
        
        testReserveMoney_prevAdIsNull(true, cv);
    }
    
    @Test
    public void positiveFlow_endsOnIsNull() throws Exception {
        ttlMinutes = 1;
        endsOnStr = null;
        
        ControlValues cv = new ControlValues(2, 700, 0.2, 1, 5);
        
        testReserveMoney_prevAdIsNull(true, cv);
    }
    
    @Test
    public void positiveFlow_endsOnIsInvalid() throws Exception {
        endsOnStr = "Invalid end datetime";
        
        ControlValues cv = new ControlValues(3000, 700, 0.2, 5, 25);
        
        String resultMessage = testReserveMoney_prevAdIsNull(true, cv);
        
        assertThat(resultMessage, containsString("Could not parse end datetime " + endsOnStr));
    }
    
    @Test
    public void positiveFlow_bidPriceIsNull() throws Exception {
        bidPrice = 0; /// this is null bid price
        
        ControlValues cv = new ControlValues(3000, 700, 0.2, 5, 25);
        
        String resultMessage = testReserveMoney_prevAdIsNull(true, cv);
        
        assertThat(resultMessage, containsString("Cannot find bid price: "));
    }

    @Test
    public void positiveFlow_bidPriceIsInvalid() throws Exception {
        bidPrice = -50; /// this is invalid bid price
        
        ControlValues cv = new ControlValues(3000, 700, 0.2, 5, 25);
        
        String resultMessage = testReserveMoney_prevAdIsNull(true, cv);
        
        assertThat(resultMessage, containsString("Cannot parse bid price: "));
    }
    
    @Test
    public void negativeFlow_budgetIsNegative() throws Exception {
        budget = -3000;
        
        String resultMessage = testReserveMoney_prevAdIsNull(false, null);
        
        assertThat(resultMessage, containsString("No money left: "));
    }
    
    @Test
    public void positiveFlow_budgetIsUnlimited() throws Exception {
        budget = Long.MAX_VALUE;
        
        ControlValues cv = new ControlValues(Long.MAX_VALUE, 700, 0.2, Long.MAX_VALUE / 700, Math.round(Math.ceil(Long.MAX_VALUE / 700 / 0.2)));
        
        String resultMessage = testReserveMoney_prevAdIsNull(true, cv);
        
        assertThat(resultMessage, containsString("Setting unlimited budget: " + Long.MAX_VALUE));
    }
    
    private String testReserveMoney_prevAdIsNull(boolean positeveFlow, ControlValues cv) throws Exception {
        Ad ad_1001001 = new SharedSetUp.Ad_1001001_mock();
        Ad ad = Mockito.spy(ad_1001001);
        ad.setBidPrice(bidPrice > 0 ? bidPrice : 0);

        RedisDaoCounters rdc = createRedisDaoCountersMock(ad, wins, bids, spendAmount, 0);
        RedisConnection<String, String> rCon = createRedisConnectionMock(ad, budget, startsOnStr, endsOnStr, bidPrice);

        AdCache adc = createAdCache();
        Whitebox.setInternalState(adc, "winRateCounters", rdc);

        BudgetAllocationResult result = Whitebox.invokeMethod(adc, "reserveMoney", rCon, ad);
        String msg = Whitebox.getInternalState(result, "msg").toString();
        
        if (positeveFlow) {
            // cv = new ControlValuesCalculator(ad.getId()); /// to comment out if control values assigned manually
            
            commonAssertions(msg, cv);
            
            Mockito.verify(ad).setProjectedSpend(cv.projectedSpend);
            Mockito.verify(ad).setBidsToMake(cv.bidsNeeded);
        } else {
            Mockito.verify(ad).setBidsToMake(0);
        }
        
        return msg;
    }

    private void commonAssertions(String msg, ControlValues cv) {
        if (cv.avgWinPrice > 0)
        {
            assertThat(msg, containsString("Based on projected spend of " + cv.projectedSpend + ", and average win price " + cv.avgWinPrice +
                            ",  we want to win " + cv.projectedSpend + "/" + cv.avgWinPrice + "=" + cv.impsNeeded + " impressions."));
        } else {
            assertThat(msg, containsString("Based on projected spend of " + cv.projectedSpend + ", and bid price " + bidPrice + 
                            ",  we want to win " + cv.projectedSpend + "/" + bidPrice + "=" + cv.impsNeeded + " impressions."));
        }

        assertThat(msg, containsString("For " + cv.impsNeeded + " impressions at " + cv.winRate + " win rate we need to make " + cv.bidsNeeded + " bids."));
    }
    
    @Test
    public void positiveFlow_prevAdSpendAmountIsPositiveAndLessThanBudget() throws Exception {
        prevProjectedSpend = 0;
        prevSpendAmount = 2000;
        
        ControlValues cv = new ControlValues(1000, 700, 0.2, 2, 10);
        cv = null; /// control values will be calculated
        
        testReserveMoney_prevAdIsNotNull(true, cv);
    }

    @Test
    public void positiveFlow_prevAdSpendAmountIsPositiveAndGreaterThanBudget() throws Exception {
        prevProjectedSpend = 0;
        prevSpendAmount = 4000;
        
        ControlValues cv = new ControlValues(0, 700, 0.2, 0, 0);
        cv = null; /// control values will be calculated
        
        testReserveMoney_prevAdIsNotNull(true, cv);
    }

    private void testReserveMoney_prevAdIsNotNull(boolean positeveFlow, ControlValues cv) throws Lot49Exception, Exception {
        Ad ad_1001001 = new SharedSetUp.Ad_1001001_mock();
        Ad ad = Mockito.spy(ad_1001001);
        ad.setBidPrice(bidPrice > 0 ? bidPrice : 0);

        String adId = ad.getId();
        long adLoadedOn = ad.getLoadedOn();
        
        Ad prevAd = Mockito.mock(Ad.class);
        Mockito.when(prevAd.getLoadedOn()).thenReturn(adLoadedOn - 60000 * ttlMinutes);
        Mockito.when(prevAd.getProjectedSpend()).thenReturn(prevProjectedSpend);
        Mockito.when(prevAd.getSpendAmount()).thenReturn(prevSpendAmount);

        RedisDaoCounters rdc = createRedisDaoCountersMock(ad, wins, bids, spendAmount, 0);
        Mockito.when(rdc.addAndGet(KVKeysValues.PREVIOUS_SPEND_AMOUNT_PREFIX + adId, prevSpendAmount)).thenReturn(prevSpendAmount);
        Mockito.when(rdc.getAndSet(KVKeysValues.PREVIOUS_SPEND_AMOUNT_PREFIX + adId, 0)).thenReturn(prevSpendAmount);
        
        RedisConnection<String, String> rCon = createRedisConnectionMock(ad, budget, null, KVKeysValues.PACING_ASAP, bidPrice);

        AdCache adc = createAdCache();
        Whitebox.setInternalState(adc, "winRateCounters", rdc);
        
        AdCache adcSpy = Mockito.spy(adc);
        Mockito.doReturn(prevAd).when(adcSpy).getAd(adId);

        BudgetAllocationResult result = Whitebox.invokeMethod(adcSpy, "reserveMoney", rCon, ad);
        String msg = Whitebox.getInternalState(result, "msg").toString();
        
        if (positeveFlow) {
            cv = new CalcControlValues(adId); /// to comment out if control values assigned manually
            
            assertTrue(msg.contains("Spend: Added last period spend " + prevSpendAmount));
            assertTrue(msg.contains(ttlMinutes + " minutes passed since last load, we will use this number."));
            assertTrue(msg.contains("Real unspent amount is " + prevProjectedSpend + "-" + prevSpendAmount + "=" + (prevProjectedSpend - prevSpendAmount)));
            
            if (prevProjectedSpend - prevSpendAmount < 0) {
                assertTrue(msg.contains("Unspent amount is too low: " + (prevProjectedSpend - prevSpendAmount) + ", will adjust to 0"));
            }

            commonAssertions(msg, cv);
            
            Mockito.verify(ad).setProjectedSpend(cv.projectedSpend);
            Mockito.verify(ad).setBidsToMake(cv.bidsNeeded);
        } else {
            Mockito.verify(ad).setBidsToMake(0);
        }
    }
    
    private BidderCalendar getBidderCalendar() {
        return BidderCalendar.getInstance();
    }
}
