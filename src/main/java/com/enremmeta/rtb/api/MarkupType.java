package com.enremmeta.rtb.api;

import com.enremmeta.rtb.proto.adx.AdXAdapter;

/**
 * @see AdXAdapter#convertResponse(com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest, com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse)
 * 
 * @see TagImpl#getNUrl(com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest, com.enremmeta.rtb.api.proto.openrtb.Bid, String)
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public enum MarkupType {
    
    VAST_PLAIN_FLASH_ONLY, VAST_PLAIN_MULTIPLE, VAST_VPAID, VAST_WRAPPER_PLAIN_FLASH_ONLY, VAST_WRAPPER_PLAIN_MULTIPLE, VAST_WRAPPER_VPAID, THIRD_PARTY_FLASH, THIRD_PARTY_HTML, THIRD_PARTY_HTML5, OWN_FLASH, OWN_HTML, OWN_HTML5;
}
