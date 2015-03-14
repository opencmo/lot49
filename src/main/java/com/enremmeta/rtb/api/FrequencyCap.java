package com.enremmeta.rtb.api;

import com.enremmeta.util.Jsonable;

/**
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class FrequencyCap implements Jsonable {
    private int hours;

    private int max;

    public FrequencyCap() {
        super();
    }

    public FrequencyCap(int max, int hours) {
        super();
        this.max = max;
        this.hours = hours;
    }

    public int getMax() {
        return max;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
