package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.util.BidderCalendar;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class CacheObjectSpec_ALL {
    private final String testObject = "TestObject"; /// is not null
    private long now = BidderCalendar.getInstance().currentTimeMillis();

    @Before
    public void setUp() throws Exception {}

    @Test
    public void constructor_T_ifParameterIsNotNull() {
        CacheObject<String> cacheObject = new CacheObject<String>(testObject); /// act
        
        assertThat(cacheObject.getFetchedTime() >= now, is(true));
        assertThat(cacheObject.getObject(), equalTo(testObject));
        assertThat(cacheObject.isNull(), is(false));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void constructor_T_ifParameterIsNull() {
        // TODO in CacheObject.CacheObject(T): should set value of the field 'isNull' to true if parameter is null
        CacheObject<String> cacheObject = new CacheObject<String>(null); /// act
        
        assertThat(cacheObject.getObject(), equalTo(null));
        assertThat(cacheObject.isNull(), is(true));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void constructor() {
        // TODO in CacheObject.CacheObject(): should set value of the field 'isNull' to true
        CacheObject<String> cacheObject = new CacheObject<String>(); /// act
        
        assertThat(cacheObject.getFetchedTime(), equalTo(0L));
        assertThat(cacheObject.getObject(), equalTo(null));
        assertThat(cacheObject.isNull(), is(true));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void setNull() {
        // TODO in CacheObject.setNull(): should set value of the field 'object' to null
        CacheObject<String> cacheObject = new CacheObject<String>(testObject);
        
        cacheObject.setNull(); /// act
        
        assertThat(cacheObject.isNull(), is(true));
        assertThat(cacheObject.getObject(), equalTo(null));
    }

    @Test
    public void setFetchedTimeNow() {
        CacheObject<String> cacheObject = new CacheObject<String>();
        cacheObject.setObject(testObject);
        
        cacheObject.setFetchedTimeNow(); /// act
        
        assertThat(cacheObject.getFetchedTime() >= now, is(true));
    }

    @Test
    public void setFetchedTime() throws InterruptedException {
        CacheObject<String> cacheObject = new CacheObject<String>();
        cacheObject.setObject(testObject);
        
        cacheObject.setFetchedTime(now); /// act
        
        assertThat(cacheObject.getFetchedTime(), equalTo(now));
    }

    @Test
    public void setObject_ifParameterIsNotNull() {
        CacheObject<String> cacheObject = new CacheObject<String>();
        
        cacheObject.setObject(testObject); /// act
        
        assertThat(cacheObject.getObject(), equalTo(testObject));
        assertThat(cacheObject.isNull(), is(false));
        // TODO: uncomment below row if setObject() with not null parameter must set value of the field 'fetchedTime' to the current time
        // assertThat(cacheObject.getFetchedTime() >= now, is(true));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void setObject_ifParameterIsNull() {
        // TODO in CacheObject.setObject(): should set value of the field 'isNull' to true if parameter is null
        CacheObject<String> cacheObject = new CacheObject<String>(testObject);
        
        cacheObject.setObject(null); /// act
        
        assertThat(cacheObject.getObject(), equalTo(null));
        assertThat(cacheObject.isNull(), is(true));
    }
}
