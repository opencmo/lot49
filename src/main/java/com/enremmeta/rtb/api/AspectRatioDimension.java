package com.enremmeta.rtb.api;

/**
 * As long as aspect ratio is kept exact width and height are not relevant.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class AspectRatioDimension extends FixedDimension {

    public AspectRatioDimension(int width, int height) {
        super(width, height);
    }

    @Override
    public boolean check(int width, int height) {
        return (getWidth() / (float) getHeight()) == (width / (float) height);
    }

}
