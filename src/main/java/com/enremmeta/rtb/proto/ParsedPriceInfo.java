package com.enremmeta.rtb.proto;

import javax.ws.rs.core.Response.Status;

import com.enremmeta.rtb.jersey.StatsSvc;

/**
 * Holds information as returned from
 * {@link StatsSvc#parsePriceInformation(ExchangeAdapter, String, String, String, String, String)} .
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class ParsedPriceInfo {

    public ParsedPriceInfo() {
        super();
    }

    public ParsedPriceInfo(double wpDouble, long wpMicro, long bpLong) {
        super();
        this.wpDouble = wpDouble;
        this.wpMicro = wpMicro;
        this.bpLong = bpLong;
    }

    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    private Double wpDouble;
    private long wpMicro;
    private long bpLong;

    public Double getWpDouble() {
        return wpDouble;
    }

    public void setWpDouble(Double wpDouble) {
        this.wpDouble = wpDouble;
    }

    public long getWpMicro() {
        return wpMicro;
    }

    public void setWpMicro(long wpMicro) {
        this.wpMicro = wpMicro;
    }

    public long getBpLong() {
        return bpLong;
    }

    public void setBpLong(long bpLong) {
        this.bpLong = bpLong;
    }
}
