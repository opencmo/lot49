package com.enremmeta.rtb;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.enremmeta.rtb.api.UserSegments;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.spi.codecs.NoopSegmentToOwnerCodec;
import com.enremmeta.rtb.spi.codecs.SegmentToOwnerCodec;
import com.enremmeta.util.ServiceRunner;

/**
 * To encode segments owned by particular owners when writing to logs. (And encode all segments by
 * master key available to us).
 * 
 * @see Lot49Config#getLogKey()
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public interface SegmentEncoder {

    String ENCRYPTION_ALGORITHM = "AES";

    String getEncoded();

    default String encode(String plainText, SecretKeySpec key) {
        Cipher AesCipher;
        try {
            AesCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            byte[] byteText = plainText.getBytes();
            AesCipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] byteCipherText = AesCipher.doFinal(byteText);
            return new String(Base64.encodeBase64(byteCipherText));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
                        | IllegalBlockSizeException | BadPaddingException e) {
            LogUtils.error(e);
            return e.getMessage();
        }
    }
}


/**
 * 
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
class InternalSegmentEncoder implements SegmentEncoder {

    private final static SecretKeySpec key = getKey();

    private final static SecretKeySpec getKey() {
        SecretKeySpec retval = null;
        ServiceRunner bidder = Bidder.getInstance();
        if (bidder != null) {
            Lot49Config config = bidder.getConfig();
            if (config != null) {
                byte k[] = config.getLogKey();
                if (k != null && k.length > 0) {
                    retval = new SecretKeySpec(k, ENCRYPTION_ALGORITHM);
                }
            }
        }
        if (retval == null) {
            LogUtils.info("InternalSegmentEncoder: No key specified, will NOT encrypt segment information.");
        } else {
            LogUtils.info("InternalSegmentEncoder: Using internal key: " + retval);
        }
        return retval;
    }

    private final UserSegments segments;

    public InternalSegmentEncoder(UserSegments s) {
        super();
        this.segments = s;

    }

    public String getEncoded() {
        if (segments == null) {
            return null;
        }
        Set<String> segSet = segments.getSegmentsSet();
        if (segSet == null || segSet.size() == 0) {
            return null;
        }
        String str = String.join(",", segSet);
        if (key == null) {
            // If no key,
            return str;
        }
        return encode(str, key);
    }
}


class ExternalSegmentEncoder implements SegmentEncoder {

    private final UserSegments userSegments;

    private static ServiceRunner bidder;

    private static AdCache adCache;

    private final static AdCache getAdCache() {
        if (bidder == null) {
            return null;
        }
        return bidder.getAdCache();
    }

    private static SegmentToOwnerCodec s2o;

    private final static SegmentToOwnerCodec getS2O() {
        SegmentToOwnerCodec retval = null;

        if (bidder != null) {
            retval = bidder.getSegmentToOwnerCodec();
        }
        LogUtils.info("Using codec: " + retval);
        if (retval == null) {
            retval = new NoopSegmentToOwnerCodec();
        }
        return retval;
    }

    public ExternalSegmentEncoder(UserSegments s) {
        super();
        this.userSegments = s;

        if (bidder == null) {
            bidder = Bidder.getInstance();
        }

        if (s2o == null || s2o instanceof NoopSegmentToOwnerCodec) {
            s2o = getS2O();
        }

        if (adCache == null) {
            adCache = getAdCache();
        }

    }

    public String getEncoded() {

        if (userSegments == null) {
            return null;
        }

        Set<String> segSet = userSegments.getSegmentsSet();

        if (segSet == null || segSet.size() == 0) {
            return null;
        }
        Map<String, Set<String>> ownerToStr = new HashMap<String, Set<String>>();
        for (String segment : segSet) {
            String owner = s2o.getOwner(segment);
            ownerToStr.putIfAbsent(owner, new HashSet<String>());
            ownerToStr.get(owner).add(segment);
        }
        String retval = "";
        for (String owner : ownerToStr.keySet()) {
            byte[] key = adCache == null ? null : adCache.getOwnerKey(owner);
            if (key == null) {
                continue;
            }
            Set<String> curSegments = ownerToStr.get(owner);
            if (!retval.equals("")) {
                retval += ",";
            }
            retval += encode(String.join(",", curSegments),
                            new SecretKeySpec(key, ENCRYPTION_ALGORITHM));
        }
        return retval;
    }

}
