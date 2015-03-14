package com.enremmeta.rtb.api;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of {@link FixedDimension}s, any number of which could fit.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class ListDimension extends DimensionImpl {

    public ListDimension() {
        // TODO Auto-generated constructor stub
    }

    public ListDimension(List<FixedDimension> dims) {
        this.dims = dims;
    }

    @Override
    public boolean check(int width, int height) {
        for (FixedDimension dim : dims) {
            if (dim.check(width, height)) {
                return true;
            }
        }
        return false;
    }

    private List<FixedDimension> dims = new ArrayList<FixedDimension>();

    public List<FixedDimension> getDims() {
        return dims;
    }

    public void setDims(List<FixedDimension> dims) {
        this.dims = dims;
    }

    @Override
    public String toString() {
        String retval = "";
        for (FixedDimension fDim : dims) {
            if (retval.length() > 0) {
                retval += ", ";
            }
            retval += fDim;
        }
        retval = "_ListDimension([" + retval + "])_";
        return retval;
    }
}
