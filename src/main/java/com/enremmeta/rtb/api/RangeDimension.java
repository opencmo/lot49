package com.enremmeta.rtb.api;

/**
 * Width and height in range from min to max.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class RangeDimension extends DimensionImpl {

    public boolean check(int width, int height) {
        return width >= minWidth && width <= maxWidth && height >= minHeight && height <= maxHeight;
    }

    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;

    public RangeDimension(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        super();
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    @Override
    public String toString() {
        return "_RangeDimension([" + minWidth + "," + maxWidth + "]x[" + minHeight + "," + maxHeight
                        + "])_";
    }
}
