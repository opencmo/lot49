package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisStringDaoShortLivedMapSpec_ALL {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisStringDaoShortLivedMap redisStringDaoShortLivedMapSpy;

    private String key = "Key";
    private String value = "Value";
    private Long expiration = 60 * 1000L; /// milliseconds
    
    @Before
    public void setUp() throws Exception {
        RedisService redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        redisStringDaoShortLivedMapSpy = Mockito.spy(new RedisStringDaoShortLivedMap(redisServiceMock));
    }

    @Test
    public void positiveFlow_putAsync_String_String() {
        PowerMockito.mockStatic(LogUtils.class);
        
        redisStringDaoShortLivedMapSpy.putAsync(key, value); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.debug("putAsync(" + key + "," + value + ")");

        Mockito.verify(redisAsyncConnectionMock).set(key, value);
        Mockito.verify(redisStringDaoShortLivedMapSpy).setExpiry(key);
    }

    @Test
    public void positiveFlow_putAsync_String_String_Long() {
        PowerMockito.mockStatic(LogUtils.class);
        
        redisStringDaoShortLivedMapSpy.putAsync(key, value, expiration); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.debug("putAsync(" + key + "," + value + ")");

        Mockito.verify(redisAsyncConnectionMock).set(key, value);
        Mockito.verify(redisStringDaoShortLivedMapSpy).setExpiry(key, expiration);
    }

    @Test
    public void positiveFlow_replace() throws Lot49Exception, Exception {
        String oldValue = "OldValue";
        
        @SuppressWarnings("unchecked")
        Future<String> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenReturn(oldValue);
        
        Mockito.when(redisAsyncConnectionMock.getset(key, value)).thenReturn(futureMock);
        
        String result = redisStringDaoShortLivedMapSpy.replace(key, value); /// act
        
        Mockito.verify(redisAsyncConnectionMock).getset(key, value);
        Mockito.verify(redisStringDaoShortLivedMapSpy).setExpiry(key);
        Mockito.verify(redisStringDaoShortLivedMapSpy).getSafe(futureMock);

        assertThat(result, equalTo(oldValue));
    }

    @Test
    public void positiveFlow_getAsync() {
        @SuppressWarnings("unchecked")
        Future<String> futureMock = Mockito.mock(Future.class);
        
        Mockito.when(redisAsyncConnectionMock.get(key)).thenReturn(futureMock);
        
        Future<String> result = redisStringDaoShortLivedMapSpy.getAsync(key); /// act
        
        Mockito.verify(redisAsyncConnectionMock).get(key);
        
        assertThat(result, equalTo(futureMock));
    }

    @Test
    public void positiveFlow_get() throws Exception {
        @SuppressWarnings("unchecked")
        Future<String> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenReturn(value);
        
        Mockito.when(redisAsyncConnectionMock.get(key)).thenReturn(futureMock);
        
        String result = redisStringDaoShortLivedMapSpy.get(key); /// act
        
        Mockito.verify(redisStringDaoShortLivedMapSpy).getAsync(key);
        Mockito.verify(redisStringDaoShortLivedMapSpy).getSafe(futureMock);
        
        assertThat(result, equalTo(value));
    }

    @Test
    public void positiveFlow_put() {
        redisStringDaoShortLivedMapSpy.put(key, value); /// act
        
        Mockito.verify(redisStringDaoShortLivedMapSpy).putAsync(key, value);
    }

    @Test
    public void positiveFlow_remove() throws Exception {
        @SuppressWarnings("unchecked")
        Future<String> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenReturn(value);
        
        Mockito.when(redisAsyncConnectionMock.get(key)).thenReturn(futureMock);
        
        String result = redisStringDaoShortLivedMapSpy.remove(key); /// act
        
        Mockito.verify(redisAsyncConnectionMock).get(key);
        Mockito.verify(redisAsyncConnectionMock).del(key);
        Mockito.verify(redisStringDaoShortLivedMapSpy).getSafe(futureMock);
        
        assertThat(result, equalTo(value));
    }
}
