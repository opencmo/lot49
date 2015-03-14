package com.enremmeta.rtb.constants;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StateSpec {

    @Test
    public void shouldGetNameByAbbreviation() {
       
        assertEquals("New York", State.nameByAbbreviation("NY"));
        
    }
    
    @Test
    public void shouldGetStateByAbbreviation() {
       
        assertEquals(State.CALIFORNIA, State.valueOfAbbreviation("CA"));
        
    }
    
    @Test
    public void shouldGetStateByName() {
       
        assertEquals(State.DELAWARE, State.valueOfName("Delaware"));
        
        
        
    }

}
