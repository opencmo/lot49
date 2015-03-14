package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.util.BidderCalendar;
import com.lambdaworks.redis.RedisConnection;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_isValidAd {
    private AdCacheConfig adCacheConfig;
    private AdCache adCache;
    private RedisConnection<String, String> redisConnectionMock;
    
    private String adId = "78";
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        adCache = new AdCache(adCacheConfig);
        
        redisConnectionMock = Mockito.mock(RedisConnection.class);
        
        PowerMockito.mockStatic(LogUtils.class);
    }

    @Test
    public void negativeFlow_returnsFalseIfBudgetIsZero() throws Exception {
        String remainingInStoreStr = "0";
        Mockito.when(redisConnectionMock.get(KVKeysValues.BUDGET_PREFIX + adId)).thenReturn(remainingInStoreStr);
        
        AdCache.ValidationResultHolder result = Whitebox.invokeMethod(adCache, "isValidAd", adId, redisConnectionMock);
        
        PowerMockito.verifyStatic();
        LogUtils.debug(contains("No money left: "));
        
        assertThat(result.getValid(), is(false));
    }

    @Test
    public void positiveFlow_returnsTrueIfBudgetIsPositive() throws Exception {
        String remainingInStoreStr = "1000";
        Mockito.when(redisConnectionMock.get(KVKeysValues.BUDGET_PREFIX + adId)).thenReturn(remainingInStoreStr);

        AdCache.ValidationResultHolder result = Whitebox.invokeMethod(adCache, "isValidAd", adId, redisConnectionMock);
        
        assertThat(result.getValid(), is(true));
    }

    @Test
    public void positiveFlow_returnsTrueIfBudgetIsUnlimited() throws Exception {
        setUnlimitedBudget();

        AdCache.ValidationResultHolder result = Whitebox.invokeMethod(adCache, "isValidAd", adId, redisConnectionMock);
        
        PowerMockito.verifyStatic();
        LogUtils.debug(contains("Could not find end datetime, defaulting to same time tomorrow: "));
        
        assertThat(result.getValid(), is(true));
    }

    @Test
    public void negativeFlow_returnsFalseIfStartsOnIsAfterNow() throws Exception {
        setUnlimitedBudget();

        DateTime startsOn = getBidderCalendar().currentDateTime().plusDays(1);
        Mockito.when(redisConnectionMock.get(KVKeysValues.STARTS_ON_PREFIX + adId)).thenReturn(startsOn.toString());
        
        AdCache.ValidationResultHolder result = Whitebox.invokeMethod(adCache, "isValidAd", adId, redisConnectionMock);
        
        PowerMockito.verifyStatic();
        LogUtils.warn(contains("Starts on date " + startsOn + " is after now"));
        
        assertThat(result.getValid(), is(false));
    }

    @Test
    public void negativeFlow_returnsFalseIfStartsOnIsInvalid() throws Exception {
        setUnlimitedBudget();

        String startsOnStr = "Invalid starts on date";
        Mockito.when(redisConnectionMock.get(KVKeysValues.STARTS_ON_PREFIX + adId)).thenReturn(startsOnStr);
        
        AdCache.ValidationResultHolder result = Whitebox.invokeMethod(adCache, "isValidAd", adId, redisConnectionMock);
        
        PowerMockito.verifyStatic();
        LogUtils.error(contains("Could not parse start datetime " + startsOnStr + ", not starting."), any(Throwable.class));
        
        assertThat(result.getValid(), is(false));
    }

    @Test
    public void positiveFlow_returnsTrueIfEndsOnIsASAP() throws Exception {
        setUnlimitedBudget();

        String endsOnStr = KVKeysValues.PACING_ASAP;
        Mockito.when(redisConnectionMock.get(KVKeysValues.ENDS_ON_PREFIX + adId)).thenReturn(endsOnStr);
        
        AdCache.ValidationResultHolder result = Whitebox.invokeMethod(adCache, "isValidAd", adId, redisConnectionMock);
        
        PowerMockito.verifyStatic();
        LogUtils.debug(contains("Ad to end ASAP - greedy strategy."));
        
        assertThat(result.getValid(), is(true));
    }

    @Test
    public void positiveFlow_returnsTrueIfEndsOnIsInvalid() throws Exception {
        setUnlimitedBudget();

        String endsOnStr = "Invalid ends on date";
        Mockito.when(redisConnectionMock.get(KVKeysValues.ENDS_ON_PREFIX + adId)).thenReturn(endsOnStr);
        
        AdCache.ValidationResultHolder result = Whitebox.invokeMethod(adCache, "isValidAd", adId, redisConnectionMock);
        
        PowerMockito.verifyStatic();
        LogUtils.warn(contains("Could not parse end datetime " + endsOnStr + ", defaulting to "), any(Throwable.class));
        
        assertThat(result.getValid(), is(true));
    }

    @Test
    public void positiveFlow_returnsTrueIfEndsOnNotFound() throws Exception {
        setUnlimitedBudget();

        AdCache.ValidationResultHolder result = Whitebox.invokeMethod(adCache, "isValidAd", adId, redisConnectionMock);
        
        PowerMockito.verifyStatic();
        LogUtils.debug(contains("Could not find end datetime, defaulting to same time tomorrow: "));
        
        assertThat(result.getValid(), is(true));
    }

    @Test
    public void negativeFlow_returnsFalseIfEndsOnIsBeforeNow() throws Exception {
        setUnlimitedBudget();

        DateTime endsOn = getBidderCalendar().currentDateTime().minusDays(1);
        Mockito.when(redisConnectionMock.get(KVKeysValues.ENDS_ON_PREFIX + adId)).thenReturn(endsOn.toString());
        
        AdCache.ValidationResultHolder result = Whitebox.invokeMethod(adCache, "isValidAd", adId, redisConnectionMock);
        
        PowerMockito.verifyStatic();
        LogUtils.warn(contains("Ends on date " + endsOn + " is before now"));
        
        assertThat(result.getValid(), is(false));
    }

    private void setUnlimitedBudget() {
        Mockito.when(redisConnectionMock.get(KVKeysValues.BUDGET_PREFIX + adId)).thenReturn(KVKeysValues.BUDGET_UNLIMITED);
    }

    private BidderCalendar getBidderCalendar() {
        return BidderCalendar.getInstance();
    }
}
