package com.enremmeta.rtb.groovy.tc

import com.enremmeta.rtb.api.AdImpl


class Ad_78_testMyIPnewAdservice2 extends AdImpl {

	void init() {
		
		adomain = ["myip.io"]
		iurl = "http://creative.us.cf.opendsp.com/creatives/creative_openDSP_myIpN01_300_250/myip300x250_n.swf"
		bidAmount = 3000;

		tags = [
                        new Tag_178_MYIP_78_testMyIPnewAdservice2(this)
		]
	}
}