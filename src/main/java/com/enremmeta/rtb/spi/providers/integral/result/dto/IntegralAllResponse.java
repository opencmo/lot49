package com.enremmeta.rtb.spi.providers.integral.result.dto;

import java.util.Date;
import java.util.List;

import com.enremmeta.util.Jsonable;

/**
 * Holds information returned from Integral Ad service
 */

public class IntegralAllResponse implements Jsonable {

    public Integer getClu() {
        return clu;
    }

    public void setClu(Integer clu) {
        this.clu = clu;
    }

    public void setBrandSafetyDto(BrandSafetyDto brandSafetyDto) {
        this.brandSafetyDto = brandSafetyDto;
    }

    private String country;
    private String dma;
    private String lang;
    private List<String> iab1;
    private List<String> iab2;


    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDma() {
        return dma;
    }

    public void setDma(String dma) {
        this.dma = dma;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public List<String> getIab1() {
        return iab1;
    }

    public void setIab1(List<String> iab1) {
        this.iab1 = iab1;
    }

    public List<String> getIab2() {
        return iab2;
    }

    public void setIab2(List<String> iab2) {
        this.iab2 = iab2;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Integer clu;

    private String action;

    private String risk;

    private String si;

    private String state;

    private Date ttl;

    private Integer traq;

    private IntegralScoresDto scores;

    private ViewabilityDto uem;

    private BrandSafetyDto brandSafetyDto;

    public Integer getTraq() {
        return traq;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public String getSi() {
        return si;
    }

    public void setSi(String si) {
        this.si = si;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getTtl() {
        return ttl;
    }

    public void setTtl(Date ttl) {
        this.ttl = ttl;
    }

    public void setTraq(Integer traq) {
        this.traq = traq;
    }

    public IntegralScoresDto getScores() {
        return scores;
    }

    public void setScores(IntegralScoresDto scores) {
        this.scores = scores;
    }

    public ViewabilityDto getUem() {
        return uem;
    }

    public void setUem(ViewabilityDto uem) {
        this.uem = uem;
    }

    public BrandSafetyDto getBrandSafetyDto() {
        if (brandSafetyDto == null) {
            brandSafetyDto = new BrandSafetyDto();
            brandSafetyDto.setAction(action);
            brandSafetyDto.setTtl(ttl);
            brandSafetyDto.setRisk(risk);
            if (scores != null) {
                brandSafetyDto.setVis(scores.getVisibility());
                brandSafetyDto.setBsc(scores.getBsc());
            }
        }

        return brandSafetyDto;
    }
}
