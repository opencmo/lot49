package com.enremmeta.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.FormattedMessage;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

public class DelimLayoutSpec {

    @Test
    public void testConstructor() {
        DelimLayout dl = new DelimLayout();
        assertEquals("-", dl.getNullChar());
        assertEquals("text/plain", dl.getContentType());
        assertEquals(EnremmetaConstants.LOG_DELIMITER, Whitebox.getInternalState(dl, "delimiter"));
        assertEquals(',', Whitebox.getInternalState(dl, "secondaryDelimiter"));
        assertFalse((boolean) Whitebox.getInternalState(dl, "waitForFutures"));
        assertEquals(0L, Whitebox.getInternalState(dl, "waitForFuturesTimeoutMillis"));
    }
    
    @Test
    public void testConstructor2() {
        DelimLayout dl = new DelimLayout('f', 's', "9");
        assertEquals("9", dl.getNullChar());
        assertEquals("text/plain", dl.getContentType());
        assertEquals('f', Whitebox.getInternalState(dl, "delimiter"));
        assertEquals('s', Whitebox.getInternalState(dl, "secondaryDelimiter"));
        assertFalse((boolean) Whitebox.getInternalState(dl, "waitForFutures"));
        assertEquals(0L, Whitebox.getInternalState(dl, "waitForFuturesTimeoutMillis"));
    }
    
    @Test
    public void testToSerializable() {
        DelimLayout dl = new DelimLayout();
        LogEvent event = Mockito.mock(LogEvent.class);
        Mockito.when(event.getMessage()).thenReturn(new SimpleMessage("TEST_MESSAGE"));
        assertEquals("TEST_MESSAGE\n", dl.toSerializable(event));
        Mockito.when(event.getMessage()).thenReturn(new ObjectMessage("TEST_MESSAGE2"));
        assertEquals("TEST_MESSAGE2\n", dl.toSerializable(event));
        Mockito.when(event.getMessage()).thenReturn(new FormattedMessage("TEST_MESSAGE3", null));
        assertEquals("TEST_MESSAGE3\n", dl.toSerializable(event));
        Mockito.when(event.getMessage()).thenReturn(new ParameterizedMessage("TEST_MESSAGE4", 10));
        assertEquals("10\n", dl.toSerializable(event));
    }

}
