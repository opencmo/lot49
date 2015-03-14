package com.enremmeta.rtb.spi.codecs;

public class DefaultSegmentToOwnerCodec implements SegmentToOwnerCodec {

    public DefaultSegmentToOwnerCodec() {

    }

    @Override
    public String getOwner(String segment) {
        String[] elts = segment.split(":");
        if (elts.length != 3) {
            return null;
        }
        if (!elts[1].equals("fp")) {
            return null;
        }
        return elts[2];
    }

}
