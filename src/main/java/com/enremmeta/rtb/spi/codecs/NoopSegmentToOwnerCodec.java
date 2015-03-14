package com.enremmeta.rtb.spi.codecs;

public class NoopSegmentToOwnerCodec implements SegmentToOwnerCodec {

    public NoopSegmentToOwnerCodec() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getOwner(String segment) {
        return null;
    }
}
