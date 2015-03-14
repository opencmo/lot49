package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.util.Utils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisJsonFutureSpec_get {
    @SuppressWarnings("unchecked")
    private Future<String> futureMock = Mockito.mock(Future.class);
    private RedisService redisService = Mockito.mock(RedisService.class);
    private RedisJsonFuture<Integer[]> redisJsonFuture;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        redisJsonFuture = new RedisJsonFuture<Integer[]>(futureMock, Integer[].class, redisService);
    }

    @Test
    public void positiveFlow_returnsNullIfFutureGetReturnsNull() throws Exception {
        Mockito.when(futureMock.get()).thenReturn(null);
        
        PowerMockito.mockStatic(LogUtils.class);
        
        Integer[] result = redisJsonFuture.get(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.debug("Dao: " + redisJsonFuture.getClass().getName() + ":  get(): " + result);
        
        assertThat(result, equalTo(null));
    }

    @Test
    public void positiveFlow_returnsExpectedValueIfFutureGetReturnsItsJsonRepresentation() throws Exception {
        Integer[] expectedResult = new Integer[] { 123, 456, 789 };
        String expectedResultJson = Utils.MAPPER.writeValueAsString(expectedResult);
        
        Mockito.when(futureMock.get()).thenReturn(expectedResultJson);
        
        PowerMockito.mockStatic(LogUtils.class);
        
        Integer[] result = redisJsonFuture.get(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.debug("Dao: " + redisJsonFuture.getClass().getName() + ":  get(): " + expectedResultJson);
        
        PowerMockito.verifyStatic();
        LogUtils.debug("Dao: " + redisJsonFuture.getClass().getName() + ":  " + expectedResultJson + " -> " + result);

        assertThat(result, equalTo(expectedResult));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void negativeFlow_throwsExceptionIfFutureGetThrowsException() throws Exception {
        exceptionRule.expect(ExecutionException.class);
        
        Mockito.when(futureMock.get()).thenThrow(Lot49Exception.class);
        
        redisJsonFuture.get(); /// act
    }
}
