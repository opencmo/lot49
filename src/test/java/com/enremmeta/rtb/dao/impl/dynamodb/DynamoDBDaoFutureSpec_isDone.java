package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.caches.KnownFuture;
import com.enremmeta.util.ServiceRunner;

@Ignore("DynamoDBDaoFuture was removed")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class, KnownFuture.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoFutureSpec_isDone {
    @Before
    public void setUp() throws Exception {
        ServiceRunner serviceRunnerMock = PowerMockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        Mockito.doReturn(Mockito.mock(ThreadPoolExecutor.class, Mockito.RETURNS_DEEP_STUBS)).when(serviceRunnerMock).getExecutor();
        
        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerMock);
        
        PowerMockito.mockStatic(LogUtils.class);
    }

    @Test
    public void returnsExpectedValue() {
        boolean expectedResult = true;
        
        @SuppressWarnings("unchecked")
        Future<GetItemResult> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.isDone()).thenReturn(expectedResult);
        
        testIsDone(futureMock, expectedResult);
    }

    @Test
    public void returnsTrueForKnownFuture() {
        Future<GetItemResult> knownFuture = new KnownFuture<GetItemResult>(null);
        Future<GetItemResult> futureMock = PowerMockito.spy(knownFuture);
        
        testIsDone(futureMock, true);
    }

    private void testIsDone(Future<GetItemResult> futureMock, boolean expectedResult) {
        DynamoDBService dynamoDBService = new DynamoDBService();
        //DynamoDBDaoFuture<String> dynamoDBDaoFuture = new DynamoDBDaoFuture<String>(dynamoDBService, futureMock, "Key");
        
        //boolean result = dynamoDBDaoFuture.isDone();
        
        Mockito.verify(futureMock).isDone();
        
        PowerMockito.verifyStatic();
        //LogUtils.trace(contains(" is " + (result ? "DONE" : "NOT DONE")));

        //assertThat(result, equalTo(expectedResult));
    }
}
