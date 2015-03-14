package com.enremmeta.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.CorruptedUserIdException;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.config.Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.constants.RtbConstants;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.brx.BrxRtb095.Mimes;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.jndi.toolkit.url.UrlUtil;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

/**
 * Various utilities. All methods here are static.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 *
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class Utils implements Lot49Constants {

    private static final Map<String, ReadableUserAgent> UA_TO_BROWSER_INFO =
                    new HashMap<String, ReadableUserAgent>();

    private static final ReadableUserAgent parseUa(String ua) {
        if (ua == null) {
            return null;
        }
        final UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
        final ReadableUserAgent agent = parser.parse(ua);
        return agent;
    }

    public static final ReadableUserAgent getBrowserInfoFromUa(String ua) {
        return UA_TO_BROWSER_INFO.computeIfAbsent(ua, Utils::parseUa);
    }

    public static final String REGEXP_DOMAIN_NAME_PATTERN_STR =
                    "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";

    public static final Pattern REGEXP_DOMAIN_NAME_PATTERN =
                    Pattern.compile(REGEXP_DOMAIN_NAME_PATTERN_STR);

    /**
     * URL-encodes provided URL keeping Winning price macro alone
     */
    public static String encodeUrl(String url, ExchangeAdapter adapter) {
        String wpMacro = adapter.getWinningPriceMacro();
        int wpLen = wpMacro.length();
        int wpIdx = url.indexOf(wpMacro);
        String encoded;
        if (wpIdx > -1) {
            String before = url.substring(0, wpIdx);
            String after = url.substring(wpIdx + wpLen);
            encoded = URLEncoder.encode(before) + wpMacro + URLEncoder.encode(after);
        } else {
            encoded = URLEncoder.encode(url);
        }
        return encoded;
    }

    /**
     * Add optout headers to the response if {@link Lot49Ext#isDebug()}.
     */
    public static final ResponseBuilder addOptoutHeaders(final Lot49Ext ext,
                    ResponseBuilder builder) {
        if (ext.isDebug()) {
            Map<String, String> reasons = ext.getOptoutReasons();
            LogUtils.debug("Writing optout headers, should be " + reasons.size() + " entries.");

            for (String adId : reasons.keySet()) {
                String r = reasons.get(adId);
                LogUtils.trace("x-opendsp-optout-" + adId + ": " + r);
                r = StringUtils.replace(r, "\"", "_");
                r = r.replaceAll(REGEXP_REDUNDANT_WHITESPACE_STR, " ");
                r = "\"" + r + "\"";
                builder = builder.header("x-opendsp-optout-" + adId, r);
            }
        }
        return builder;
    }

    public static boolean validateDomain(String d) {
        return REGEXP_DOMAIN_NAME_PATTERN.matcher(d).find();
    }

    public static boolean isMagic() {
        if (Bidder.getInstance().getConfig().getMagic() != null) {
            LogUtils.info("Magic is on (config)!");
            return true;
        }
        Map<String, String> env = System.getenv();
        String envMagic = env.get(KVKeysValues.ENV_LOT49_MAGIC);
        if (envMagic == null) {
            return false;
        }
        if (isTrue(envMagic)) {
            LogUtils.info("Magic is on (environment)!");
            return true;
        } else {
            LogUtils.info("Strange value for Magic: " + envMagic
                            + ", assuming false - you better unset it, Muggle!");
        }
        return false;

    }

    public static final String REGEXP_REDUNDANT_WHITESPACE_STR = "\\s+";

    public static final Pattern REGEXP_REDUNDANT_WHITESPACE_PATTERN =
                    Pattern.compile(REGEXP_REDUNDANT_WHITESPACE_STR);

    public static final String REGEXP_COOKIE_VALUE_DISALLOWED_STR = "[\\s;,=]+";

    public static final Pattern REGEXP_COOKIE_VALUE_DISALLOWED_PATTERN =
                    Pattern.compile(REGEXP_COOKIE_VALUE_DISALLOWED_STR);

    public static String removeRedundantWhitespace(String s) {
        return s.replaceAll(REGEXP_REDUNDANT_WHITESPACE_STR, " ");
    }

    /**
     * Convert NGINX-set user ID from cookie to log format. NGINX writes to log a hex string, but
     * sends as cookie value a base-64 encoded something. As an example, value in the file of
     * <tt>uid_got</tt> is {@link Lot49Constants#TEST_MOD_UID_LOG_1} or
     * {@link Lot49Constants#TEST_MOD_UID_LOG_2} and value sent in a cookie field is, respectively,
     * {@link Lot49Constants#TEST_MOD_UID_COOKIE_1} and {@link Lot49Constants#TEST_MOD_UID_COOKIE_2}
     * .
     *
     * @see <A href= "http://nginx.org/en/docs/http/ngx_http_userid_module.html#variables"> Module
     *      ngx_http_userid_module</a>
     *
     * @see <a href="http://www.lexa.ru/programs/mod-uid-eng.html">http://www.lexa.
     *
     *      ru/programs/mod-uid-eng.html</a>
     *
     * @see <a href="https://github.com/debedb/microput">Microput</a>
     *
     * @see User#getCustomdata()
     */
    public static final String cookieToLogModUid(final String encUid) {
        if (encUid == null) {
            return null;
        }
        if (encUid.equals("pushkin")) {
            return encUid;
        }
        // Maybe it's not encoded...
        if (encUid.startsWith("odsp=")) {
            return encUid.substring(5);
        }
        // http://stackoverflow.com/questions/3092019/can-a-base64-encoded-string-contain-whitespace
        final int len = encUid.length();
        if (len < Lot49Constants.MOD_UID_COOKIE_LENGTH_MIN
                        || len > Lot49Constants.MOD_UID_COOKIE_LENGTH_MAX) {
            final String errMsg = "Corrupted user ID " + encUid + ": length is " + encUid.length()
                            + "; expected length between  "
                            + Lot49Constants.MOD_UID_COOKIE_LENGTH_MIN + " and "
                            + Lot49Constants.MOD_UID_COOKIE_LENGTH_MAX;
            LogUtils.debug(errMsg);
            throw new CorruptedUserIdException(encUid, errMsg);
        }
        final StringBuilder encUid2 = new StringBuilder(len);
        for (int i = 0; i < encUid.length(); i++) {
            final char curChar = encUid.charAt(i);
            if (curChar == ' ') {
                encUid2.append('+');
            } else {
                encUid2.append(curChar);
            }
        }
        try {
            final byte[] uidBytes = Base64.decodeBase64(encUid2.toString());
            ModUidStruct modUid = new ModUidStruct();
            ByteBuffer uidBb = ByteBuffer.wrap(uidBytes);
            uidBb.order(ByteOrder.LITTLE_ENDIAN);
            modUid.setByteBuffer(uidBb, 0);
            // F705D40A12F77454D05E782B02473F10

            String h1 = toHexString(modUid.serviceNumber.get());
            String h2 = toHexString(modUid.issueTime.get());
            String h3 = toHexString(modUid.pid.get());
            String h4 = toHexString(modUid.cookie3.get());
            String logValue = h1 + h2 + h3 + h4;
            logValue = logValue.toUpperCase();
            return logValue;
        } catch (IndexOutOfBoundsException ioobe) {
            throw new CorruptedUserIdException(encUid, ioobe.getMessage());
        }
    }

    public static <T> Set<T> set(T... items) {
        Set<T> set = new HashSet<T>();
        for (T item : items) {
            if (item == null) {
                continue;
            }
            set.add(item);
        }
        return set;
    }

    private static final String toHexString(final long l) {
        final String s = Long.toHexString(l);
        final int charsShort = 8 - s.length();

        switch (charsShort) {
            case 0:
                return s;
            case 1:
                return "0" + s;
            case 2:
                return "00" + s;
            case 3:
                return "000" + s;
            case 4:
                return "0000" + s;
            case 5:
                return "00000" + s;
            case 6:
                return "000000" + s;
            case 7:
                return "0000000" + s;
        }
        return "00000000";
    }

    /**
     * The opposite of {@link #cookieToLogModUid(String)}.
     */
    public static final String logToCookieModUid(String encUid) {
        return logToCookieModUid(encUid, true);
    }

    public static final String logToCookieModUid(String encUid, boolean urlSafe) {
        if (encUid.length() != 32) {
            throw new CorruptedUserIdException(encUid, "Expected 32 characters in user ID, got "
                            + encUid + " (" + encUid.length() + ")");

        }
        ModUidStruct modUid = new ModUidStruct();
        Long l1 = Long.parseLong(encUid.substring(0, 8), 16);
        modUid.serviceNumber.set(l1);
        Long l2 = Long.parseLong(encUid.substring(8, 16), 16);
        modUid.issueTime.set(l2);
        Long l3 = Long.parseLong(encUid.substring(16, 24), 16);
        modUid.pid.set(l3);
        Long l4 = Long.parseLong(encUid.substring(24, 32), 16);
        modUid.cookie3.set(l4);
        ByteBuffer bb = modUid.getByteBuffer();
        byte byteArr[] = new byte[16];
        int i = 0;
        while (bb.hasRemaining()) {
            byteArr[i++] = bb.get();
        }
        String cookie;
        if (urlSafe) {
            cookie = Base64.encodeBase64URLSafeString(byteArr);
        } else {
            cookie = Base64.encodeBase64String(byteArr);
        }
        return cookie;
    }

    /**
     * For use in {@link #createModUidCookie()}.
     */
    private static AtomicLong COOKIE_SEQUENCER = new AtomicLong(0x030303);

    /**
     * Creates cookie based on mod_uid cookie rules.
     *
     * @see #logToCookieModUid(String)
     *
     * @see #cookieToLogModUid(String)
     */
    public static final String createModUidCookie() {
        return createModUidCookie(false);
    }

    public static final String createModUidCookie(boolean urlSafe) {
        ModUidStruct modUid = new ModUidStruct();
        Long serviceNumber = Bidder.SERVICE_NUMBER;
        modUid.serviceNumber.set(serviceNumber);
        Long issueTime = BidderCalendar.getInstance().currentTimeMillis();
        modUid.issueTime.set(issueTime);
        Long pid = Bidder.PID;
        modUid.pid.set(pid);

        long nextSeq = COOKIE_SEQUENCER.incrementAndGet();
        Long cookie3 = (nextSeq << 8) | 2;
        modUid.cookie3.set(cookie3);

        ByteBuffer bb = modUid.getByteBuffer();
        byte byteArr[] = new byte[16];
        int i = 0;
        while (bb.hasRemaining()) {
            byteArr[i++] = bb.get();
        }
        String cookie;
        if (urlSafe) {
            cookie = Base64.encodeBase64URLSafeString(byteArr);
        } else {
            cookie = Base64.encodeBase64String(byteArr);
        }
        return cookie;
    }

    public static final Random RANDOM =
                    new Random(BidderCalendar.getInstance().currentTimeMillis());

    /**
     * All methods here are static.
     */
    private Utils() {
        // Boo!
    }

    public static final JsonFactory JSON_FACTORY = new JsonFactory();

    static {
        JSON_FACTORY.configure(Feature.ALLOW_SINGLE_QUOTES, true);
        JSON_FACTORY.configure(Feature.ALLOW_COMMENTS, true);
        JSON_FACTORY.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    public static final ObjectMapper MAPPER = new ObjectMapper(JSON_FACTORY);

    static {
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

    }

    /**
     * Parse query string (of the format "k1=v1&amp;k2=v2&amp;k3=v3").
     */
    public static Map<String, String> parseQueryString(String s) {
        Map<String, String> retval = new HashMap<String, String>();
        String[] kvs = s.split("&");
        for (String kvStr : kvs) {
            String[] kv = kvStr.split("=");
            switch (kv.length) {
                case 0:
                    break;
                case 1:
                    retval.put(kv[0], "");
                    break;
                case 2:
                    retval.put(kv[0], kv[1]);
                    break;
                default:
                    retval.put(kv[0],
                                    String.join("=", Arrays.asList(kv).subList(1, kv.length - 1)));
            }
        }
        return retval;
    }

    /**
     * Whether the specified argument can be interpreted as true. The rules as of now are the
     * follows
     * <ol>
     * <li><tt>null</tt> is <tt>false</tt></li>
     * <li>If the object is {@link Boolean}, the answer is obvious</li>
     * <li>If the object is a {@link String}, then it is lower-cased and leading/trailing whitespace
     * is trimmed. After that, any of the following values are interpreted as <tt>false</tt> :
     * <ol>
     * <li>Empty string</li>
     * <li><tt>false</tt></li>
     * <li><tt>0</tt></li>
     * <li><tt>no</tt></li>
     * <li><tt>off</tt></li>
     * </ol>
     * Everything else is true</li>
     * <li>If the object is a {@link Number}, then a 0 value is <tt>false</tt>, everything else is
     * <tt>true</tt>.</li>
     * <li>If the object is a {@link Collection}, then an empty one is <tt>false</tt> , everything
     * else is <tt>true</tt>.</li>
     * <li>If the object is a {@link File}, the result is the same as calling {@link File#exists()}.
     * </li>
     * <li>Everything else is <tt>true</tt></li>
     * </ol>
     */
    public static final boolean isTrue(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue();
        }
        if (o instanceof Number) {
            return ((Number) o).longValue() != 0;
        }
        if (o instanceof String) {
            String strVal = ((String) o).toLowerCase();
            if (strVal.length() == 0) {
                return false;
            }
            boolean realFalse = strVal.equals("no") || strVal.equals("false")
                            || strVal.equals("off") || strVal.equals("0") || strVal.equals("n");
            return !realFalse;
        }
        if (o instanceof Collection) {
            return ((Collection) o).size() > 0;
        }
        if (o instanceof File) {
            return ((File) o).exists();
        }
        return true;
    }

    public static String readFile(String s) throws Lot49Exception {
        return readFile(new File(s));
    }

    public static String readUrl(String url) throws IOException {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();

        return response.toString();
    }

    public static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }


    public static Config loadConfig(String fileName, Class<? extends Config> configClass)
                    throws Lot49Exception {
        // System.out.println("Entering Utils.loadConfig()");
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(Feature.ALLOW_COMMENTS, true);
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        Config config;
        File file = new File(fileName);
        try {
            config = mapper.readValue(file, configClass);
        } catch (IOException e) {
            throw new Lot49Exception(e);
        }
        return config;
    }

    public static String readFile(File f) throws Lot49Exception {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(f.toPath());
            return new String(encoded);
        } catch (IOException e) {
            throw new Lot49Exception(e);
        }
    }

    /**
     * Convert CPM value in dollars to micro-dollars per impression.
     * <p>
     * </p>
     * Example: $12.45 CPM means that each impression is priced at $ <tt>$12.45/1,000=0.01245</tt>.
     * In micro-dollars, that would be <tt>0.01245*1,000,000=12450</tt>.
     */
    public static final long cpmToMicro(double cpm) {
        return (long) (cpm / 1000 * 1000000);
    }

    /**
     * Convert micro-dollar per impression to CPM in dollars.
     * <p>
     * </p>
     * Example: 2,500 micro-dollars per-impression mean that I am willing to pay $
     * <tt>2,500/1,000,000=0.0025</tt> for each individual impression. That means that for the
     * thousand impressions, my price (CPM) is $ <tt>0.0025*1,000=2.5</tt>.
     */
    public static final double microToCpm(long micro) {
        return (micro / 1000000. * 1000.);
    }

    public static final double microToCpm(double micro) {
        return (micro / 1000000. * 1000.);
    }

    public static final List<String> apisToString(List<Integer> apis) {
        List<String> retval = new ArrayList<String>();
        if (apis == null || apis.size() == 0) {
            return retval;
        }
        apis.stream().forEach(x -> apiToString(x));
        for (Integer api : apis) {
            retval.add(apiToString(api));
        }
        return retval;
    }

    public static final List<String> protosToString(List<Integer> protos) {
        List<String> retval = new ArrayList<String>();
        if (protos == null || protos.size() == 0) {
            return retval;
        }
        for (Integer proto : protos) {
            retval.add(protoToString(proto));
        }
        return retval;
    }

    public static final String apiToString(int api) {
        switch (api) {
            case RtbConstants.API_VPAID_1:
                return "VPAID1";
            case RtbConstants.API_VPAID_2:
                return "VPAID2";
            case RtbConstants.API_MRAID:
                return "MRAID";
            case RtbConstants.API_ORMMA:
                return "ORMMA";
            default:
                return null;
        }
    }

    public static final String brxMimeToString(Mimes mime) {
        switch (mime) {
            case FLV:
                return "application/flv";
            case GIF:
                return "image/gif";
            case MP4:
                return "video/mp4";
            case JPG:
                return "image/jpeg";
            case SHOCKWAVE_FLASH:
                return Lot49Constants.MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH;
            case TEXT_HTML:
                return "text/html";
            case PNG:
                return "image/png";
            default:
                return null;
        }
    }

    public static final String protoToString(int proto) {
        switch (proto) {
            case RtbConstants.VIDEO_PROTOCOL_VAST_1:
                return "VAST1";
            case RtbConstants.VIDEO_PROTOCOL_VAST_2:
                return "VAST2";
            case RtbConstants.VIDEO_PROTOCOL_VAST_3:
                return "VAST3";
            case RtbConstants.VIDEO_PROTOCOL_VAST_WRAPPER_1:
                return "VASTWrapper1";
            case RtbConstants.VIDEO_PROTOCOL_VAST_WRAPPER_2:
                return "VASTWrapper2";
            case RtbConstants.VIDEO_PROTOCOL_VAST_WRAPPER_3:
                return "VASTWrapper3";
            default:
                return null;
        }
    }

    /**
     * No-op method to insert for a breakpoint.
     */
    public static void noop() {

    }

    public static final PeriodFormatter DEFAULT_PERIOD_FORMATTER = new PeriodFormatterBuilder()
                    .appendDays().appendSuffix(" day", " days").appendSeparator(" ").appendHours()
                    .appendSuffix(" hour", " hours").appendSeparator(" ").appendMinutes()
                    .appendSuffix(" min", " mins").appendSeparator(" ").appendSeconds()
                    .appendSuffix(" sec", " secs").appendSeparator(" ").toFormatter();

    public static long incrMapCounter(Map<String, AtomicLong> map, String key) {
        map.putIfAbsent(key, new AtomicLong(0));
        AtomicLong c = map.get(key);
        return c.incrementAndGet();
    }

    public static Set<String> incrMapSet(Map<String, Set<String>> map, String key, String value) {
        map.putIfAbsent(key, new HashSet<String>(0));
        Set<String> s = map.get(key);
        s.add(value);
        return s;
    }

    /**
     * No-op method to insert for a breakpoint, if you want to examine the objects in the debugger.
     * Silly, yes.
     */
    public static void noop(Object... objects) {

    }

    /**
     * Convert the provided objects to delimiter-separated string of their String representations.
     */
    public static void delimFormat(StringBuilder buf, final char delim, final char delim2,
                    final String nullChar, boolean waitForFutures, long waitForFuturesTimeoutMillis,
                    List obj) {

        if (obj == null) {
            return;
        }

        for (Object o : obj) {
            if (buf.length() > 0) {
                buf.append(delim);
            }

            if (o == null) {
                buf.append(nullChar);
            } else if (o instanceof byte[]) {
                final byte[] byteArr = (byte[]) o;
                for (int i = 0; i < byteArr.length; i++) {
                    if ((0xff & byteArr[i]) < 0x10) {
                        buf.append("0").append(Integer.toHexString((0xFF & byteArr[i])));
                    } else {
                        buf.append(Integer.toHexString(0xFF & byteArr[i]));
                    }
                }
            } else if (o instanceof Object[]) {
                delimFormat(buf, delim, delim2, nullChar, waitForFutures,
                                waitForFuturesTimeoutMillis, Arrays.asList((Object[]) o));
            } else if (o instanceof Collection) {
                final Collection oc = (Collection) o;
                if (oc.isEmpty()) {
                    buf.append(nullChar);
                } else {
                    if (oc instanceof Set) {
                        // TODO TEMPORARY FIX
                        final Set os = (Set) oc;
                        final Set<Object> set2 = new HashSet<Object>(os.size());
                        for (final Object s : os) {
                            if (s != null) {
                                if (s instanceof String) {
                                    set2.add(((String) s).trim());
                                } else {
                                    set2.add(s);
                                }
                            }
                        }
                        o = set2;
                    }

                    try {
                        buf.append(MAPPER.writeValueAsString(o));
                    } catch (JsonProcessingException e) {
                        buf.append(o.toString()).append(" (").append(e).append(")");
                    }
                }
            } else if (o instanceof Jsonable) {
                try {
                    buf.append(MAPPER.writeValueAsString(o));
                } catch (JsonProcessingException e) {
                    buf.append(o.toString()).append(" (").append(e).append(")");
                }
            } else if (o instanceof Future) {

                final Future f = (Future) o;
                try {
                    if (f.isDone()) {
                        o = f.get();
                    } else {
                        if (waitForFutures) {
                            if (waitForFuturesTimeoutMillis > 0) {
                                o = f.get(waitForFuturesTimeoutMillis, TimeUnit.MILLISECONDS);
                            } else {
                                o = f.get();
                            }

                        } else {
                            o = "FUTURE_NOT_DONE(" + f.getClass() + ")";
                        }
                    }
                    // Not a future anymore
                    delimFormat(buf, delim, delim2, nullChar, waitForFutures,
                                    waitForFuturesTimeoutMillis, o);
                } catch (TimeoutException te) {
                    buf.append("FUTURE_TIMEOUT");
                } catch (Throwable t) {
                    buf.append("FUTURE_ERROR[").append(f.getClass()).append(": ")
                                    .append(t.getMessage()).append(": ")
                                    .append(Arrays.asList(t.getStackTrace())).append(")");
                }
            } else if (o instanceof Map) {
                Map map = (Map) o;
                try {
                    Set keySet = new HashSet(map.keySet());
                    buf.append("{");
                    boolean isFirst = true;
                    for (Object key : keySet) {
                        Object val = map.get(key);
                        if (val != null) {
                            if (!isFirst) {
                                buf.append("; ");
                            } else {
                                isFirst = false;
                            }
                            buf.append(key).append("=").append(val);
                        }
                    }
                    buf.append("}");
                } catch (ConcurrentModificationException cme) {
                    LogUtils.error("Error serializing " + o.getClass(), cme);
                }
            } else {
                String s = o.toString().trim();
                if (s.length() == 0) {
                    buf.append(nullChar);
                } else {
                    if (delim == '\t') {
                        s = s.replace('\t', ' ');
                    }
                    buf.append(s);
                }
            }
        }
    }


    /**
     * Convert the provided objects to tab-separated string of their String representations (TSV).
     *
     * @param delim
     *            delimiter
     *
     * @param delim2
     *            delimiter within fields that are of list format
     *
     * @param nullChar
     *            character to use for <tt>null</tt> (should default to XXX).
     *
     * @param waitForFutures
     *            whether to wait for futures or to log future not found.
     */
    public static String delimFormat(char delim, char delim2, String nullChar,
                    boolean waitForFutures, long waitForFuturesTimeoutMillis, Object... obj) {
        StringBuilder sb = new StringBuilder();
        delimFormat(sb, delim, delim2, nullChar, waitForFutures, waitForFuturesTimeoutMillis,
                        Arrays.asList(obj));

        return sb.toString();
    }

    public static void delimFormat(StringBuilder sb, char delim, char delim2, String nullChar,
                    boolean waitForFutures, long waitForFuturesTimeoutMillis, Object... obj) {
        delimFormat(sb, delim, delim2, nullChar, waitForFutures, waitForFuturesTimeoutMillis,
                        Arrays.asList(obj));
    }

    /**
     * Same as {@link LogUtils#debug(Object)}, but also writes information about current stack
     * frames.
     */
    public static void trace(Object msg) {
        // java.lang.Thread.getStackTrace (Thread.java:1552
        // com.enremmeta.rtb.LogUtils.trace (LogUtils.java:783
        // com.enremmeta.rtb.caches.RedisPacingCacheCallback.cancelReservation
        // (RedisPacing
        // Cache.java:69
        // com.enremmeta.rd.TargetingFailTask.run (ReclaimBudgetTaskImpl.java:44
        // java.util.concurrent.Executors$RunnableAdapter.call
        // (Executors.java:511

        String msgStr = msg + " at\n";
        StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        StackTraceElement ste = stes[3];
        msgStr += "\t" + ste.getClassName() + "." + ste.getMethodName() + " (" + ste.getFileName()
                        + ":" + ste.getLineNumber() + "\n";

        LogUtils.debug(msgStr);
    }

    // public static String tsvFormat(Object... obj) {
    // return delimFormat(
    // EnremmetaConstants.LOG_DELIMITER,
    // EnremmetaConstants.LOG_LIST_DELIMITER,
    // EnremmetaConstants.LOG_NULL_CHAR,
    // true,
    // 100,
    // obj);
    // }

    public final static String toStringKeyValue(final String key, final Object value) {
        return "\"" + key + "\" : " + (value == null ? "" : "\"" + value + "\"");
    }

    public static final String getId() {
        return InsecureUuid.randomInsecureUuid().toString();
    }

    public static String fixStringEncode(String target) {
        try {
            return UrlUtil.decode(target.replaceAll("%(25)+", "%"));
        } catch (MalformedURLException e) {
            return target;
        }
    }

    public static StringBuilder replace(StringBuilder sb, String searched, String replaceTo) {
        if (sb != null && sb.length() > 0 && searched != null && !searched.isEmpty()
                        && replaceTo != null) {
            int start = sb.indexOf(searched);
            int end = sb.indexOf(searched) + searched.length();
            if (start >= 0 && end >= 0 && end > start) {
                sb.replace(start, end, replaceTo);
            }
        }
        return sb;
    }

    public static StringBuilder replaceAll(StringBuilder sb, String searched, String replaceTo) {
        if (sb != null && sb.length() > 0 && searched != null && !searched.isEmpty()
                        && replaceTo != null) {
            while (sb.indexOf(searched) >= 0) {
                sb = replace(sb, searched, replaceTo);
            }
        }

        return sb;
    }

    public static long ipToLong(String ipAddress) {
        long result = 0;
        ipAddress = ipAddress.trim();
        if (ipAddress.length() == 0) {
            return 0;
        }
        String[] ipAddressInArray = ipAddress.split("\\.");
        if (ipAddressInArray.length == 4) {
            try {
                for (int i = 3; i >= 0; i--) {
                    long ip = Long.parseLong(ipAddressInArray[3 - i]);
                    result |= ip << (i * 8);
                }
            } catch (NumberFormatException e) {
                LogUtils.debug("Wrong IP address: " + ipAddress, e);
                result = 0;
            }
        } else {
            LogUtils.debug("Wrong IP address: " + ipAddress);
        }

        return result;
    }

    public static BigInteger ipv6ToBigInteger(String ipAddress) {
        int startIndex = ipAddress.indexOf("::");

        if (startIndex != -1) {
            String firstStr = ipAddress.substring(0, startIndex);
            String secondStr = ipAddress.substring(startIndex + 2, ipAddress.length());

            int x = countChar(ipAddress, ':');
            int numOfColonsInSecondPart = countChar(secondStr, ':');

            BigInteger first = ipv6ToBigIntegerInt(firstStr);
            first = first.shiftLeft(16 * (7 - x + numOfColonsInSecondPart))
                            .add(ipv6ToBigIntegerInt(secondStr));
            return first;
        } else if (countChar(ipAddress, ':') != 7) {
            return BigInteger.valueOf(0);
        }

        return ipv6ToBigIntegerInt(ipAddress);
    }

    private static BigInteger ipv6ToBigIntegerInt(String ipAddress) {
        String[] strArr = ipAddress.split(":");
        BigInteger retValue = BigInteger.valueOf(0);
        for (int i = 0; i < strArr.length; i++) {
            BigInteger bi = BigInteger.valueOf(0);
            if (!strArr[i].isEmpty()) {
                bi = new BigInteger(strArr[i], 16);
            }
            retValue = retValue.shiftLeft(16).add(bi);
        }

        return retValue;
    }

    public static int countChar(String str, char reg) {
        char[] ch = str.toCharArray();
        int count = 0;
        for (int i = 0; i < ch.length; ++i) {
            if (ch[i] == reg) {
                if (ch[i + 1] == reg) {
                    ++i;
                    continue;
                }
                ++count;
            }
        }
        return count;
    }
}
