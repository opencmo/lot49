package com.enremmeta.rtb.api;

/**
 * Fixed dimension - exact width and height.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class FixedDimension extends DimensionImpl {

    public FixedDimension(int width, int height) {
        super();
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean check(int width, int height) {
        return this.width == width && this.height == height;
    }

    private final int width;
    private final int height;

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
