package com.enremmeta.rtb.spi.providers.integral.result.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class IntegralScoresDto {

    private Integer adt;
    private Integer alc;
    private Integer arf;
    private Integer dlm;
    private Integer drg;
    private Integer hat;
    private Integer iab_business;
    private Integer iab_news;
    private Integer iab_religion;
    private Integer iab_travel;
    private Integer iv2;
    private Integer iv3;
    private Integer iviab;
    @JsonProperty("iviab_160x600")
    @SerializedName("iviab_160x600")
    private Integer iviab_160x600;
    @JsonProperty("iviab_300x250")
    @SerializedName("iviab_300x250")
    private Integer iviab_300x250;
    @JsonProperty("iviab_728x90")
    @SerializedName("iviab_728x90")
    private Integer iviab_728x90;
    private Integer ivl;
    @JsonProperty("ivl_160x600")
    @SerializedName("ivl_160x600")
    private Integer ivl_160x600;
    @JsonProperty("ivl_300x250")
    @SerializedName("ivl_300x250")
    private Integer ivl_300x250;
    @JsonProperty("ivl_728x90")
    @SerializedName("ivl_728x90")
    private Integer ivl_728x90;
    private Integer ivp;
    @JsonProperty("ivp_160x600")
    @SerializedName("ivp_160x600")
    private Integer ivp_160x600;
    @JsonProperty("ivp_300x250")
    @SerializedName("ivp_300x250")
    private Integer ivp_300x250;
    @JsonProperty("ivp_728x90")
    @SerializedName("ivp_728x90")
    private Integer ivp_728x90;
    private Integer ivt;
    private Integer ivu;
    private Integer jdl0426;
    private Integer jha0420;
    private Integer jof0427;
    private Integer lang;
    private Integer niv;
    private Integer off;
    private Integer off1220;
    private Integer pac;
    private Integer par;
    private Integer pol;
    private Integer pro;
    private Integer rsa;
    private Integer sam;
    private Integer top;
    private Integer trq;
    private Integer ugb;

    private Integer ugc;

    private Integer ugd;

    private Integer ugf;

    private Integer ugm;

    private Integer ugs;

    private Integer ugt;

    @SerializedName("v_ap")
    private Integer v_ap;
    @SerializedName("v_c")
    private Integer v_c;

    @SerializedName("v_h250")
    private Integer v_h250;

    @SerializedName("v_r6_5")
    private Integer v_r6_5;
    @SerializedName("v_s1")
    private Integer v_s1;

    @SerializedName("v_w300")
    private Integer v_w300;

    private Integer vio;

    private Integer vio1029;

    private Integer visibility;
    private Integer viv2;
    private Integer webmail;
    private Integer zacc;
    private Integer zboa;
    private Integer zcke;
    private Integer zcmt;
    private Integer zfos;
    private Integer zgp;
    private Integer zibm;
    private Integer zmer;
    private Integer ztraf;
    private Integer zult;
    private Integer zver;
    private Integer zvzn;

    public IntegralScoresDto() {

    }

    public Integer getAdt() {
        return adt;
    }

    public Integer getAlc() {
        return alc;
    }

    public Integer getArf() {
        return arf;
    }

    public BscDto getBsc() {
        BscDto bscDto = new BscDto();
        bscDto.setAdt(adt);
        bscDto.setAlc(alc);
        bscDto.setDim(dlm);
        bscDto.setDrg(drg);
        bscDto.setHat(hat);
        bscDto.setOff(off);
        bscDto.setVio(vio);
        bscDto.setSam(sam);
        return bscDto;
    }

    public Integer getDlm() {
        return dlm;
    }

    public Integer getDrg() {
        return drg;
    }

    public Integer getHat() {
        return hat;
    }

    @JsonProperty("iab_business")
    @SerializedName("iab_business")
    public Integer getIab_business() {
        return iab_business;
    }

    @JsonProperty("iab_news")
    @SerializedName("iab_news")
    public Integer getIab_news() {
        return iab_news;
    }

    @JsonProperty("iab_religion")
    @SerializedName("iab_religion")
    public Integer getIab_religion() {
        return iab_religion;
    }

    @JsonProperty("iab_travel")
    @SerializedName("iab_travel")
    public Integer getIab_travel() {
        return iab_travel;
    }

    public Integer getIv2() {
        return iv2;
    }

    public Integer getIv3() {
        return iv3;
    }

    public Integer getIviab() {
        return iviab;
    }

    @JsonProperty("iviab_160x600")
    @SerializedName("iviab_160x600")
    public Integer getIviab_160x600() {
        return iviab_160x600;
    }

    @JsonProperty("iviab_300x250")
    @SerializedName("iviab_300x250")
    public Integer getIviab_300x250() {
        return iviab_300x250;
    }

    @JsonProperty("iviab_728x90")
    @SerializedName("iviab_728x90")
    public Integer getIviab_728x90() {
        return iviab_728x90;
    }

    public Integer getIvl() {
        return ivl;
    }

    @JsonProperty("ivl_160x600")
    @SerializedName("ivl_160x600")
    public Integer getIvl_160x600() {
        return ivl_160x600;
    }

    @JsonProperty("ivl_300x250")
    @SerializedName("ivl_300x250")
    public Integer getIvl_300x250() {
        return ivl_300x250;
    }

    @JsonProperty("ivl_728x90")
    @SerializedName("ivl_728x90")
    public Integer getIvl_728x90() {
        return ivl_728x90;
    }

    public Integer getIvp() {
        return ivp;
    }

    @JsonProperty("ivp_160x600")
    @SerializedName("ivp_160x600")
    public Integer getIvp_160x600() {
        return ivp_160x600;
    }

    @JsonProperty("ivp_300x250")
    @SerializedName("ivp_300x250")
    public Integer getIvp_300x250() {
        return ivp_300x250;
    }

    @JsonProperty("ivp_728x90")
    @SerializedName("ivp_728x90")
    public Integer getIvp_728x90() {
        return ivp_728x90;
    }

    public Integer getIvt() {
        return ivt;
    }

    public Integer getIvu() {
        return ivu;
    }

    public Integer getJdl0426() {
        return jdl0426;
    }

    public Integer getJha0420() {
        return jha0420;
    }

    public Integer getJof0427() {
        return jof0427;
    }

    public Integer getLang() {
        return lang;
    }

    public Integer getNiv() {
        return niv;
    }

    public Integer getOff() {
        return off;
    }

    public Integer getOff1220() {
        return off1220;
    }

    public Integer getPac() {
        return pac;
    }

    public Integer getPar() {
        return par;
    }

    public Integer getPol() {
        return pol;
    }

    public Integer getPro() {
        return pro;
    }

    public Integer getRsa() {
        return rsa;
    }

    public Integer getSam() {
        return sam;
    }

    public Integer getTop() {
        return top;
    }

    public Integer getTrq() {
        return trq;
    }

    public Integer getUgb() {
        return ugb;
    }

    public Integer getUgc() {
        return ugc;
    }

    public Integer getUgd() {
        return ugd;
    }

    public Integer getUgf() {
        return ugf;
    }

    public Integer getUgm() {
        return ugm;
    }

    public Integer getUgs() {
        return ugs;
    }

    public Integer getUgt() {
        return ugt;
    }

    @JsonProperty("v_ap")
    @SerializedName("v_ap")
    public Integer getV_ap() {
        return v_ap;
    }

    @JsonProperty("v_c")
    public Integer getV_c() {
        return v_c;
    }

    @JsonProperty("v_h250")
    @SerializedName("v_h250")
    public Integer getV_h250() {
        return v_h250;
    }

    @JsonProperty("v_r6_5")
    @SerializedName("v_r6_5")
    public Integer getV_r6_5() {
        return v_r6_5;
    }

    @JsonProperty("v_s1")
    @SerializedName("v_s1")
    public Integer getV_s1() {
        return v_s1;
    }

    @JsonProperty("v_w300")
    @SerializedName("v_w300")
    public Integer getV_w300() {
        return v_w300;
    }

    public Integer getVio() {
        return vio;
    }

    public Integer getVio1029() {
        return vio1029;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public Integer getViv2() {
        return viv2;
    }

    public Integer getWebmail() {
        return webmail;
    }

    public Integer getZacc() {
        return zacc;
    }

    public Integer getZboa() {
        return zboa;
    }

    public Integer getZcke() {
        return zcke;
    }

    public Integer getZcmt() {
        return zcmt;
    }

    public Integer getZfos() {
        return zfos;
    }

    public Integer getZgp() {
        return zgp;
    }

    public Integer getZibm() {
        return zibm;
    }

    public Integer getZmer() {
        return zmer;
    }

    public Integer getZtraf() {
        return ztraf;
    }

    public Integer getZult() {
        return zult;
    }

    public Integer getZver() {
        return zver;
    }

    public Integer getZvzn() {
        return zvzn;
    }

    public void setAdt(Integer adt) {
        this.adt = adt;
    }

    public void setAlc(Integer alc) {
        this.alc = alc;
    }

    public void setArf(Integer arf) {
        this.arf = arf;
    }

    public void setDlm(Integer dlm) {
        this.dlm = dlm;
    }

    public void setDrg(Integer drg) {
        this.drg = drg;
    }

    public void setHat(Integer hat) {
        this.hat = hat;
    }

    @JsonProperty("iab_business")
    @SerializedName("iab_business")
    public void setIab_business(Integer iab_business) {
        this.iab_business = iab_business;
    }

    @JsonProperty("iab_news")
    @SerializedName("iab_news")
    public void setIab_news(Integer iab_news) {
        this.iab_news = iab_news;
    }

    @JsonProperty("iab_religion")
    @SerializedName("iab_religion")
    public void setIab_religion(Integer iab_religion) {
        this.iab_religion = iab_religion;
    }

    @JsonProperty("iab_travel")
    @SerializedName("iab_travel")
    public void setIab_travel(Integer iab_travel) {
        this.iab_travel = iab_travel;
    }

    public void setIv2(Integer iv2) {
        this.iv2 = iv2;
    }

    public void setIv3(Integer iv3) {
        this.iv3 = iv3;
    }

    public void setIviab(Integer iviab) {
        this.iviab = iviab;
    }

    @JsonProperty("iviab_160x600")
    @SerializedName("iviab_160x600")
    public void setIviab_160x600(Integer iviab_160x600) {
        this.iviab_160x600 = iviab_160x600;
    }

    @JsonProperty("iviab_300x250")
    @SerializedName("iviab_160x600")
    public void setIviab_300x250(Integer iviab_300x250) {
        this.iviab_300x250 = iviab_300x250;
    }

    @JsonProperty("iviab_728x90")
    @SerializedName("iviab_160x600")
    public void setIviab_728x90(Integer iviab_728x90) {
        this.iviab_728x90 = iviab_728x90;
    }

    public void setIvl(Integer ivl) {
        this.ivl = ivl;
    }

    @JsonProperty("ivl_160x600")
    public void setIvl_160x600(Integer ivl_160x600) {
        this.ivl_160x600 = ivl_160x600;
    }

    @JsonProperty("ivl_300x250")
    public void setIvl_300x250(Integer ivl_300x250) {
        this.ivl_300x250 = ivl_300x250;
    }

    @JsonProperty("ivl_728x90")
    public void setIvl_728x90(Integer ivl_728x90) {
        this.ivl_728x90 = ivl_728x90;
    }

    public void setIvp(Integer ivp) {
        this.ivp = ivp;
    }

    @JsonProperty("ivp_160x600")
    @SerializedName("ivp_160x600")
    public void setIvp_160x600(Integer ivp_160x600) {
        this.ivp_160x600 = ivp_160x600;
    }

    @JsonProperty("ivp_300x250")
    @SerializedName("ivp_300x250")
    public void setIvp_300x250(Integer ivp_300x250) {
        this.ivp_300x250 = ivp_300x250;
    }


    @JsonProperty("ivp_728x90")
    @SerializedName("ivp_728x90")
    public void setIvp_728x90(Integer ivp_728x90) {
        this.ivp_728x90 = ivp_728x90;
    }

    public void setIvt(Integer ivt) {
        this.ivt = ivt;
    }

    public void setIvu(Integer ivu) {
        this.ivu = ivu;
    }

    public void setJdl0426(Integer jdl0426) {
        this.jdl0426 = jdl0426;
    }

    public void setJha0420(Integer jha0420) {
        this.jha0420 = jha0420;
    }

    public void setJof0427(Integer jof0427) {
        this.jof0427 = jof0427;
    }

    public void setLang(Integer lang) {
        this.lang = lang;
    }

    public void setNiv(Integer niv) {
        this.niv = niv;
    }

    public void setOff(Integer off) {
        this.off = off;
    }

    public void setOff1220(Integer off1220) {
        this.off1220 = off1220;
    }

    public void setPac(Integer pac) {
        this.pac = pac;
    }

    public void setPar(Integer par) {
        this.par = par;
    }

    public void setPol(Integer pol) {
        this.pol = pol;
    }

    public void setPro(Integer pro) {
        this.pro = pro;
    }

    public void setRsa(Integer rsa) {
        this.rsa = rsa;
    }

    public void setSam(Integer sam) {
        this.sam = sam;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public void setTrq(Integer trq) {
        this.trq = trq;
    }

    public void setUgb(Integer ugb) {
        this.ugb = ugb;
    }

    public void setUgc(Integer ugc) {
        this.ugc = ugc;
    }

    public void setUgd(Integer ugd) {
        this.ugd = ugd;
    }

    public void setUgf(Integer ugf) {
        this.ugf = ugf;
    }

    public void setUgm(Integer ugm) {
        this.ugm = ugm;
    }

    public void setUgs(Integer ugs) {
        this.ugs = ugs;
    }

    public void setUgt(Integer ugt) {
        this.ugt = ugt;
    }

    @JsonProperty("v_ap")
    @SerializedName("v_ap")
    public void setV_ap(Integer v_ap) {
        this.v_ap = v_ap;
    }

    @JsonProperty("v_c")
    public void setV_c(Integer v_c) {
        this.v_c = v_c;
    }

    @JsonProperty("v_h250")
    @SerializedName("v_h250")
    public void setV_h250(Integer v_h250) {
        this.v_h250 = v_h250;
    }

    @JsonProperty("v_r6_5")
    @SerializedName("v_r6_5")
    public void setV_r6_5(Integer v_r6_5) {
        this.v_r6_5 = v_r6_5;
    }

    @JsonProperty("v_s1")
    @SerializedName("v_s1")
    public void setV_s1(Integer v_s1) {
        this.v_s1 = v_s1;
    }

    @JsonProperty("v_w300")
    @SerializedName("v_w300")
    public void setV_w300(Integer v_w300) {
        this.v_w300 = v_w300;
    }

    public void setVio(Integer vio) {
        this.vio = vio;
    }

    public void setVio1029(Integer vio1029) {
        this.vio1029 = vio1029;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    public void setViv2(Integer viv2) {
        this.viv2 = viv2;
    }

    public void setWebmail(Integer webmail) {
        this.webmail = webmail;
    }

    public void setZacc(Integer zacc) {
        this.zacc = zacc;
    }

    public void setZboa(Integer zboa) {
        this.zboa = zboa;
    }

    public void setZcke(Integer zcke) {
        this.zcke = zcke;
    }

    public void setZcmt(Integer zcmt) {
        this.zcmt = zcmt;
    }

    public void setZfos(Integer zfos) {
        this.zfos = zfos;
    }

    public void setZgp(Integer zgp) {
        this.zgp = zgp;
    }

    public void setZibm(Integer zibm) {
        this.zibm = zibm;
    }

    public void setZmer(Integer zmer) {
        this.zmer = zmer;
    }

    public void setZtraf(Integer ztraf) {
        this.ztraf = ztraf;
    }

    public void setZult(Integer zult) {
        this.zult = zult;
    }

    public void setZver(Integer zver) {
        this.zver = zver;
    }

    public void setZvzn(Integer zvzn) {
        this.zvzn = zvzn;
    }
}
