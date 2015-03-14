package com.enremmeta.rtb.test.cases;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import com.enremmeta.rtb.api.AspectRatioDimension;

/**
 * 
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class DimensionTest {

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testAspect() {
        AspectRatioDimension dim4x3 = new AspectRatioDimension(4, 3);
        assertTrue(dim4x3.check(640, 480));
        assertTrue(dim4x3.check(480, 360));
        assertFalse(dim4x3.check(640, 360));
        AspectRatioDimension dim16x9 = new AspectRatioDimension(16, 9);
        assertFalse(dim16x9.check(640, 480));
        assertFalse(dim16x9.check(480, 360));
        assertTrue(dim16x9.check(640, 360));
    }

}
