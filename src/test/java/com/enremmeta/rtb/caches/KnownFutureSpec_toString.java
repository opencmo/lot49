package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class KnownFutureSpec_toString {
    @Test
    public void returnsNullIfConstructorParameterIsNull() {
        String ctorParam = null;
        KnownFuture<String> knownFuture = new KnownFuture<String>(ctorParam);
        
        String result = knownFuture.toString();
        
        assertThat(result, equalTo(null));
    }

    @Test
    public void returnsEmptyStringIfConstructorParameterIsWhiteSpaceOrEmpty() {
        String ctorParam = "   ";
        KnownFuture<String> knownFuture = new KnownFuture<String>(ctorParam);
        
        String result = knownFuture.toString();
        
        assertThat(result, equalTo(""));
    }

    @Test
    public void returnsExpectedValueIfConstructorParameterIsNotWhiteSpaceOrEmpty() {
        String ctorParam = "Test";
        KnownFuture<String> knownFuture = new KnownFuture<String>(ctorParam);
        
        String result = knownFuture.toString();
        
        String expectedResult = "Future[" + ctorParam + "]";
        assertThat(result, equalTo(expectedResult));
    }

    @Ignore("UserCache was removed")
    @Test
    public void returnsExpectedValueIfIfKnownFutureIsEmptyUserInfoFuture() {
        //KnownFuture<Set<String>> knownFuture = UserCache.EMPTY_USER_INFO_FUTURE;
        
        //String result = knownFuture.toString();
        
        //assertThat(result, equalTo("EMPTY_USER_INFO_FUTURE"));
    }
}
