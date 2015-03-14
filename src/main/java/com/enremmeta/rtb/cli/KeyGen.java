package com.enremmeta.rtb.cli;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;

import com.enremmeta.rtb.SegmentEncoder;

public class KeyGen {

    public static void main(String[] args) throws Throwable {
        KeyGenerator keyGen = KeyGenerator.getInstance(SegmentEncoder.ENCRYPTION_ALGORITHM);
        keyGen.init(128);
        SecretKey secKey = keyGen.generateKey();
        byte[] keyBytes = secKey.getEncoded();
        String keyString = Base64.encodeBase64String(keyBytes);
        System.out.println("Base64: " + keyString);
        System.out.print("Byte array: [");
        boolean first = true;
        for (byte b : keyBytes) {
            if (first) {
                first = false;
            } else {
                System.out.print(", ");
            }
            System.out.print(b);
        }
        System.out.println("]");
    }
}
