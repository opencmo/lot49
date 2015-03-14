package com.enremmeta.rtb.api.apps;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;

/**
 * An App that can augment an incoming {@link OpenRtbRequest request}, placing augmented data into
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public interface RequestAugmentor extends App {

}
