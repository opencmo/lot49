package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;

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
import com.enremmeta.rtb.SharedSetUp;
import com.lambdaworks.redis.protocol.Command;
import com.lambdaworks.redis.protocol.CommandOutput;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisJsonFutureSpec_getSafe {
    private RedisService redisService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Before
    public void setUp() throws Exception {
        redisService = SharedSetUp.createRedisServiceMock();
    }
    
    @SuppressWarnings("unchecked")
    private Command<?, ?, Integer> createCommandMock(String commandOutputError, Integer expectedResult) {
        @SuppressWarnings({"rawtypes"})
        CommandOutput commandOutputMock = Mockito.mock(CommandOutput.class);
        Mockito.when(commandOutputMock.getError()).thenReturn(commandOutputError);
        
        Command<?, ?, Integer> commandMock = Mockito.mock(Command.class);
        Mockito.when(commandMock.getOutput()).thenReturn(commandOutputMock);
        if (commandOutputError == null) {
            Mockito.when(commandMock.get()).thenReturn(expectedResult);
        }
        
        return commandMock;
    }

    @Test
    public void positiveFlow_returnsNullIfFirstParameterIsNull() throws Exception {
        Integer result = RedisJsonFuture.getSafe(null, redisService); /// act
        
        assertThat(result, equalTo(null));
    }

    @Test
    public void positiveFlow_returnsExpectedValueIfFirstParameterIsCommandAndCommandOutputErrorIsNull() throws Exception {
        String commandOutputError = null;
        Integer expectedResult = 123;
        
        Command<?, ?, Integer> commandMock = createCommandMock(commandOutputError, expectedResult);
        
        Integer result = RedisJsonFuture.getSafe(commandMock, redisService); /// act
        
        assertThat(result, equalTo(expectedResult));
    }

    @Test
    public void negativeFlow_throwsExceptionIfFirstParameterIsCommandAndCommandOutputErrorIsNotNull() throws Exception {
        exceptionRule.expect(Lot49Exception.class);
        exceptionRule.expectMessage("Error executing Redis operation on ");
        
        Command<?, ?, Integer> commandMock = createCommandMock("CommandOutputError", null);
        
        RedisJsonFuture.getSafe(commandMock, redisService); /// act
    }

    @Test
    public void positiveFlow_returnsExpectedValueIfFirstParameterIsFuture() throws Exception {
        Integer expectedResult = 123;
        
        @SuppressWarnings("unchecked")
        Future<Integer> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenReturn(expectedResult);
        
        Integer result = RedisJsonFuture.getSafe(futureMock, redisService); /// act
        
        assertThat(result, equalTo(expectedResult));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void negativeFlow_throwsExceptionIfFirstParameterIsFutureAndFutureGetThrowsInterruptedException() throws Exception {
        Future<Integer> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenThrow(InterruptedException.class);
        
        PowerMockito.mockStatic(LogUtils.class);
        
        try {
            RedisJsonFuture.getSafe(futureMock, redisService); /// act
            fail("Expected test to throw an instance of com.enremmeta.rtb.Lot49Exception");
        } catch(Lot49Exception ex) {
        } finally {
            Thread.interrupted(); /// to clear interrupted status
        }
        
        PowerMockito.verifyStatic();
        LogUtils.error(isA(InterruptedException.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void negativeFlow_throwsExceptionIfFirstParameterIsFutureAndFutureGetThrowsExecutionException() throws Exception {
        Future<Integer> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenThrow(ExecutionException.class);
        
        PowerMockito.mockStatic(LogUtils.class);
        
        try {
            RedisJsonFuture.getSafe(futureMock, redisService); /// act
            fail("Expected test to throw an instance of com.enremmeta.rtb.Lot49Exception");
        } catch(Lot49Exception ex) {
        }
        
        PowerMockito.verifyStatic();
        LogUtils.error(isA(ExecutionException.class));
    }
}
