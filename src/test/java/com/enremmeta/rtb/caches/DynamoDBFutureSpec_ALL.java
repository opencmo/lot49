package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.enremmeta.rtb.FutureTestTemplate;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBFutureSpec_ALL extends FutureTestTemplate<Set<String>, GetItemResult> {
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
    
    protected Future<Set<String>> createTestFuture(Future<GetItemResult> innerFuture) {
        return new DynamoDBFuture<Set<String>>(innerFuture, new UserDynamoDBDecoder());
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
    public void get_returnsExpectedResultsIfInnerFutureGetReturnsIt() throws Exception {
        Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
        attributes.put(DYN_DB_PREFIX_OBJECT_ATTR, InternalUtils.toAttributeValue("ObjectValue"));
        
        GetItemResult innerFutureGetResult = Mockito.mock(GetItemResult.class);
        Mockito.when(innerFutureGetResult.getItem()).thenReturn(attributes);

        Set<String> result = test_get(innerFutureGetResult);
        
        assertThat(result, not(equalTo(null)));
        assertThat(result.size(), equalTo(1));
        assertThat(result.contains(DYN_DB_PREFIX_OBJECT_ATTR), is(true));
    }

    @Test
    public void get_long_TimeUnit_returnsExpectedResultsIfInnerFutureGetReturnsIt() throws Exception {
        Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
        attributes.put(DYN_DB_PREFIX_OBJECT_ATTR, InternalUtils.toAttributeValue("ObjectValue"));

        GetItemResult innerFutureGetResult = Mockito.mock(GetItemResult.class);
        Mockito.when(innerFutureGetResult.getItem()).thenReturn(attributes);

        Set<String> result = test_getWithTimeout(innerFutureGetResult);

        assertThat(result, not(equalTo(null)));
        assertThat(result.size(), equalTo(1));
        assertThat(result.contains(DYN_DB_PREFIX_OBJECT_ATTR), is(true));
    }

    @Test
    public void get_long_TimeUnit_throwsTimeoutExceptionIfInnerFutureGetWaitTimeElapsed() throws Exception {
        exceptionRule.expect(TimeoutException.class);
        
        Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
        GetItemResult innerFutureGetResult = Mockito.mock(GetItemResult.class);
        Mockito.when(innerFutureGetResult.getItem()).thenReturn(attributes);

        test_getWithTimeout_ifWaitTimeElapsed(innerFutureGetResult);
    }
    
    /// Code below was present in the project formerly
    
    private static final String DYN_DB_PREFIX_OBJECT_ATTR = "doAttr";
    
    private class UserDynamoDBDecoder implements DynamoDBDecoder<Set<String>> {
        @Override
        public Set<String> decode(Map<String, AttributeValue> map) {
            return map.keySet();
        }
    }
}
