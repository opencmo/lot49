
package com.enremmeta.rtb

import groovy.transform.InheritConstructors

import com.enremmeta.rtb.api.FixedDimension
import com.enremmeta.rtb.api.MarkupType
import com.enremmeta.rtb.api.TagImpl
import com.enremmeta.rtb.api.proto.openrtb.Bid
import com.enremmeta.rtb.api.proto.openrtb.Impression
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest
import com.enremmeta.rtb.constants.Macros
import com.enremmeta.rtb.proto.ExchangeAdapter

@InheritConstructors
public class Tag_178_MYIP_78_testMyIPnewAdservice2 extends TagImpl {
    private String myTagId

    @Override
    public void init() {
	//	MatchingAdId matchingAdId = new MatchingAdId(537256973,537427467,537683535);
	//	OpenXTargeting oxTargeting = new OpenXTargeting();
	//	oxTargeting.getRequiredMatchingAdIds().add(matchingAdId);
	//	this.exchangeTargeting = new ExchangeTargeting();
	//	this.exchangeTargeting.setOpenxTargeting(oxTargeting)
	markupType = MarkupType.OWN_HTML;


	video = false
	banner = true

	int width = 300
	int height = 250
	dim =  new FixedDimension(width, height);

    }

    @Override
    public String getClickRedir(final OpenRtbRequest req, final Bid bid) {
	String brId = req.getId()
	String bId = bid.getId();
	String iId = bid.getImpid()
	final ExchangeAdapter adapter = req.getLot49Ext().getAdapter();
	final String exchange = adapter.getName();
	final String ssp = adapter.isAggregator() ? req.getLot49Ext().getSsp()
		: exchange;

	return "http://s.opendsp.com/man/click/?cid=" + adId +
	"&crid=" + id +
	"&iid=" + iId +
	"&bid=" + bId +
	"&brid=" + brId +
	"&comment=GGTEST&xch=$exchange&ssp=$ssp";
    }

    public String getTagTemplate(OpenRtbRequest req, Impression imp, Bid bid) {
	ExchangeAdapter adapter = req.getLot49Ext().getAdapter();
	String exchange = adapter.getName();
	String ssp = adapter.isAggregator() ? req.getLot49Ext().getSsp()
		: exchange;


	String brId = req.getId()
	String bId = bid.getId();
	String iId = imp.getId();
	int width = imp.getBanner().getW()
	int height = imp.getBanner().getH()
        
        Random rand1 = new Random(System.currentTimeMillis());
    
    //make the click - exchange click encoded + b.opendsp double encoded    
        
        
	String tagTemplate = """
 <!--/*
*
* Revive Adserver Javascript Tag
* - Generated with Revive Adserver v3.1.0
*
*/-->
<!--/*
  * The backup image section of this tag has been generated for use on a
  * non-SSL page. If this tag is to be placed on an SSL page, change the
  *   'http://ads.opendsp.com/service/...'
  * to
  *   'https://ads.opendsp.com/service/...'
  *
  * This noscript section of this tag only shows image banners. There
  * is no width or height in these banners, so if you want these tags to
  * allocate space for the ad before it shows, you will need to add this
  * information to the <img> tag.
  *
  * If you do not want to deal with the intricities of the noscript
  * section, delete the tag (from <noscript>... to </noscript>). On
  * average, the noscript tag is called from less than 1% of internet
  * users.
*/-->
<script type='text/javascript'><!--//<![CDATA[
    var exchClickMacro = '$Macros.MACRO_LOT49_EXCHANGE_CLICK_ENC';
    var clickMacro = '$Macros.MACRO_LOT49_CLICK_ENC';
    if (exchClickMacro) {
   	clickMacro = exchClickMacro + '$Macros.MACRO_LOT49_CLICK_ENC_ENC';
    }
    var m3_3rdPclickEncoded = \"\";
    var m3_u = (location.protocol=='https:'?'https://ads.opendsp.com/service/adservice2.php':'http://ads.opendsp.com/service/adservice2.php');
    var m3_r = Math.floor(Math.random()*99999999999);
    if (!document.MAX_used) document.MAX_used = ',';
    document.write (\"<scr\"+\"ipt type='text/javascript' src='\"+m3_u);
    document.write (\"?cb=\" + m3_r);
    document.write (\"&amp;cr=myIpN01\");
    document.write (\"&amp;loc=jsr\");
    document.write (\"&amp;tagID=myTagID\");  
    document.write (\"&amp;w=$width\");
    document.write (\"&amp;h=$height\");
    document.write (\"&amp;click=\"+m3_3rdPclickEncoded);
    document.write (\"&amp;clickb=\"+clickMacro);
    document.write (\"&amp;iid=$iId\");
    document.write (\"&amp;cid=$adId\");
    document.write (\"&amp;crid=$id\");
    document.write (\"&amp;bid=$bId\");

    document.write (\"&amp;brId=$brId\");
    document.write (\"&amp;comment=GGTEST\");
    document.write (\"&amp;xch=$exchange\");
    document.write (\"&amp;ssp=$ssp\");

    if (document.MAX_used != ',') document.write (\"&amp;exclude=\" + document.MAX_used);
    document.write (document.charset ? '&amp;charset='+document.charset : (document.characterSet ? '&amp;charset='+document.characterSet : ''));
    document.write (\"&amp;locUrl=\" + escape(window.location));
    if (document.referrer) document.write (\"&amp;referer=\" + escape(document.referrer));
    if (document.context) document.write (\"&context=\" + escape(document.context));
    if (document.mmm_fo) document.write (\"&amp;mmm_fo=1\");
    document.write (\"'><\\/scr\"+\"ipt>\");
//]]>--></script>
<img src=\"http://s.opendsp.com/man/impression/?iid=$iId&cid=$adId&crid=$id&bid=$bId&brId=$brId&comment=GGTEST&xch=$exchange&ssp=$ssp\"></img>
<img src=\"$Macros.MACRO_LOT49_IMPRESSION\"></img>

<noscript>
    <a href='$Macros.MACRO_LOT49_CLICK%%http%3A%2F%2Fs.opendsp.com%2Fman%2Fclick%2F%3Fiid%3D%26cid%3D%26crid%3D%26bid%3D%26r%3Dhttps%253A%252F%252Fwww.myip.io%252F%253Futm_source%253Dopendsp%2526utm_medium%253Dopendsp%252520link%2526utm_campaign%253DOpendsp' target='_blank'>
        <img src='http://ads.opendsp.com/service/adservice.php?cb=$rand1&cr=myIpN01&loc=ns&tagID=myTagID&w=$width&h=$height' border='0' alt=''  width='$width' height='$height' />
    </a>
</noscript>

"""
	return tagTemplate
    }
}