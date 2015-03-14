package com.enremmeta.rtb.test.cases;

import java.net.URLEncoder;

import org.junit.Test;

import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.testexchange.Test1ExchangeAdapter;
import com.enremmeta.util.Utils;

import junit.framework.Assert;
import net.sf.uadetector.ReadableUserAgent;

public class UtilsTest {

    @Test
    public void testEncodeUrl() {
        ExchangeAdapter a = new Test1ExchangeAdapter();
        String before = "http://foo.bar.com/?x=1&y=2&wp=";
        String wp = a.getWinningPriceMacro();
        String after = "&q=3&xyz|zyx";
        String url = before + wp + after;
        String expected = URLEncoder.encode(before) + wp + URLEncoder.encode(after);
        String actual = Utils.encodeUrl(url, a);
        System.out.println("Encoded " + url + " to " + actual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testParseUa() {
        String uaChrome =
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36";
        ReadableUserAgent info = Utils.getBrowserInfoFromUa(uaChrome);
        Assert.assertEquals(info.getName().toLowerCase(), "chrome");
        info = Utils.getBrowserInfoFromUa(uaChrome);
        Assert.assertEquals(info.getFamily().getName().toLowerCase(), "chrome");

        OpenRtbRequest req = new OpenRtbRequest();
        req.setDevice(new Device());
        req.getDevice().setUa(uaChrome);

        Test1ExchangeAdapter adapter = new Test1ExchangeAdapter();
        adapter.fillPlatformInfoFromUa(req);
        Assert.assertEquals(req.getLot49Ext().getBrowserFamily(), "chrome");
        Assert.assertEquals(req.getLot49Ext().getBrowserName(), "chrome");
    }


    @Test
    public void testNotTest2() {
        try {
            String[] hex = new String[] {"9F001EACD2EC9B56EF0AC948025EFBA6",
                            "9F001EAC20CF8356CD4D43140221D694", "9F001EACAAAD9656886A4F280244F655",
                            "9F001EAC56D297568D6A3A36028DD6B0", "9F001EAC50359056CD63050402D303B7",
                            "9F001EAC6AF39756816A661402E53BB9", "9F001EAC65CA8056CD4D431402FEB6B6"};
            for (String h : hex) {
                String c = Utils.logToCookieModUid(h);
                System.out.println(h + "\t==>\t" + c + " (" + c.length() + ")");
            }
            System.out.println("Ok...");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // Not much of a test more to see what conversion results are.
    @Test
    public void testNotTest1() {
        try {
            String expectedM = "1705D40A9D3154DD6F81CCE12484C762";
            String c = "CtQFF91UMZ3hzIFvYseEJAg==";
            String m = Utils.cookieToLogModUid(c);
            System.out.println(c + "\t==>\t" + m + " (" + c.length() + ")");
            Assert.assertEquals(m, expectedM);

            c = "CtQFF91UMZ3hzIFvYseEJAg";
            m = Utils.cookieToLogModUid(c);
            System.out.println(c + "\t==>\t" + m + " (" + c.length() + ")");
            Assert.assertEquals(m, expectedM);

            c = "CtQFF91UMZ3hzIFvYseEJA";
            m = Utils.cookieToLogModUid(c);
            System.out.println(c + "\t==>\t" + m + " (" + c.length() + ")");
            Assert.assertEquals(m, expectedM);

            c = Utils.logToCookieModUid(expectedM);
            System.out.println(m + "\t==>\t" + c + " (" + c.length() + ")");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Test
    public void testDecodeModUi() {
        String[] cookieValues = new String[] {Lot49Constants.TEST_MOD_UID_COOKIE_1,
                        Lot49Constants.TEST_MOD_UID_COOKIE_2, Lot49Constants.TEST_MOD_UID_COOKIE_3,
                        Lot49Constants.TEST_MOD_UID_COOKIE_4,};
        String[] logValues = new String[] {Lot49Constants.TEST_MOD_UID_LOG_1,
                        Lot49Constants.TEST_MOD_UID_LOG_2, Lot49Constants.TEST_MOD_UID_LOG_3,
                        Lot49Constants.TEST_MOD_UID_LOG_4};
        String[] suffixValues = new String[] {"", "=="};

        for (int i = 0; i < cookieValues.length; i++) {
            for (int j = 0; j < suffixValues.length; j++) {
                String cookie = cookieValues[i] + suffixValues[j];
                String log = Utils.cookieToLogModUid(cookie);

                System.out.println(
                                "cookieToLogModUid(): Comparing\n\t" + log + "\n\t" + logValues[i]);

                Assert.assertEquals(log, logValues[i]);
            }
        }

        for (int i = 0; i < logValues.length; i++) {
            String log = logValues[i];
            String cookie = Utils.logToCookieModUid(log);
            System.out.println("Comparing\n\t" + cookie + " to\n\t" + cookieValues[i]);
            Assert.assertEquals(cookie, cookieValues[i]);

            for (int j = 0; j < suffixValues.length; j++) {
                String toCompare = cookieValues[i] + suffixValues[j];
                if (!cookie.endsWith("==")) {
                    cookie += "==";
                }
                if (!toCompare.endsWith("==")) {
                    toCompare += "==";
                }
                System.out.println("logToCookieModUid(" + log + "): Comparing\n\t" + cookie
                                + " to\n\t" + toCompare);
                Assert.assertEquals(cookie, toCompare);
            }
        }
    }
}
