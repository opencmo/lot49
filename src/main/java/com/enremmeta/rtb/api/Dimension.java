package com.enremmeta.rtb.api;

import com.enremmeta.rtb.api.proto.openrtb.CompanionAd;
import com.enremmeta.rtb.api.proto.openrtb.Video;
import com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest.Banner;

/**
 * Class to specify dimension (width and height) of an ad. This is provided for flexibility of
 * allowing dimensions to be exact, or one of a list, or within a range, or with an aspect ratio.
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public interface Dimension {
    /**
     * Whether this dimension can be served in a slot with the specified width and height (from
     * {@link Banner} or {@link Video} or {@link CompanionAd}, etc.)
     */
    boolean check(int width, int height);

}
