package com.enremmeta.rtb.dao;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.FutureTestTemplate;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.util.Utils;

@SuppressWarnings("rawtypes")
@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class JsonFutureSpec_ALL extends FutureTestTemplate<Map, String> {
    private String key = "Key";
    private String value = "Value";
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Before
    public void setUp() throws Exception {
        set_up();
    }

    @After
    public void tearDown() throws Exception {
        tear_down();
    }
    
    protected Future<Map> createTestFuture(Future<String> innerFuture) {
        return new JsonFuture<Map>(innerFuture, Map.class);
    }
    
    @Test
    public void cancel_returnsExpectedResultsIfTaskIsFinished() throws Exception {
        test_cancel_ifTaskIsFinished();
    }

    @Test
    public void cancel_returnsExpectedResultsIfTaskIsRunning() throws Exception {
        test_cancel_ifTaskIsRunning();
    }

    @Test
    public void get_returnsNullIfInnerFutureGetReturnsNull() throws Exception {
        String innerFutureGetResult = null;

        @SuppressWarnings("unchecked")
        Map<String, String> result = test_get(innerFutureGetResult);
        
        assertThat(result, equalTo(null));
    }

    @Test
    public void get_returnsExpectedResultsIfInnerFutureGetReturnsIt() throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        data.put(key, value);
        
        String innerFutureGetResult = Utils.MAPPER.writeValueAsString(data);
        
        PowerMockito.mockStatic(LogUtils.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> result = test_get(innerFutureGetResult);
        
        PowerMockito.verifyStatic();
        LogUtils.debug("Dao: " + getTestFuture().getClass().getName() + ":  " + innerFutureGetResult + " -> " + result);

        assertThat(result, not(equalTo(null)));
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(key), equalTo(value));
    }

    @Test
    public void get_throwsExecutionExceptionIfInnerFutureGetReturnsBadJson() throws Exception {
        exceptionRule.expect(ExecutionException.class);
        
        String innerFutureGetResult = "Bad json data";
        
        test_get(innerFutureGetResult);
    }

    @Test
    public void get_long_TimeUnit_returnsExpectedResultsIfInnerFutureGetReturnsIt() throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        data.put(key, value);

        String innerFutureGetResult = Utils.MAPPER.writeValueAsString(data);

        PowerMockito.mockStatic(LogUtils.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> result = test_getWithTimeout(innerFutureGetResult);

        PowerMockito.verifyStatic();
        LogUtils.debug("Dao: " + getTestFuture().getClass().getName() + ":  " + innerFutureGetResult + " -> " + result);

        assertThat(result, not(equalTo(null)));
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(key), equalTo(value));
    }

    @Test
    public void get_long_TimeUnit_throwsTimeoutExceptionIfInnerFutureGetWaitTimeElapsed() throws Exception {
        exceptionRule.expect(TimeoutException.class);
        
        Map<String, String> data = new HashMap<String, String>();
        String innerFutureGetResult = Utils.MAPPER.writeValueAsString(data);

        test_getWithTimeout_ifWaitTimeElapsed(innerFutureGetResult);
    }
    
    @Test
    public void get_long_TimeUnit_throwsExecutionExceptionIfInnerFutureGetReturnsBadJson() throws Exception {
        exceptionRule.expect(ExecutionException.class);
        
        String innerFutureGetResult = "Bad json data";
        
        test_getWithTimeout(innerFutureGetResult);
    }
}
