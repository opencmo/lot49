package com.enremmeta.rtb.api.proto.openrtb;

import java.util.List;

import com.enremmeta.rtb.RtbBean;

/**
 * OpenRTB Content object.
 * 
 * @see <a href= "http://www.iab.net/media/file/OpenRTB-API-Specification-Version-2-3.pdf"> OpenRTB
 *      2.3 (section 3.2.9)</a>
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class Content implements RtbBean {

    public Content() {
        // TODO Auto-generated constructor stub
    }

    private String id;
    private int episode;
    private String title;
    private String series;
    private String season;
    private String url;
    public List<String> cat;
    private int videoquality;
    private String keywords;
    private String contentrating;
    private String userrating;
    private String context;
    private int livestream;
    private int sourcerelationship;
    private Producer producer;
    private int len;
    private int qagmediarating;
    private int embeddable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getCat() {
        return cat;
    }

    public void setCat(List<String> cat) {
        this.cat = cat;
    }

    public int getVideoquality() {
        return videoquality;
    }

    public void setVideoquality(int videoquality) {
        this.videoquality = videoquality;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getContentrating() {
        return contentrating;
    }

    public void setContentrating(String contentrating) {
        this.contentrating = contentrating;
    }

    public String getUserrating() {
        return userrating;
    }

    public void setUserrating(String userrating) {
        this.userrating = userrating;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getLivestream() {
        return livestream;
    }

    public void setLivestream(int livestream) {
        this.livestream = livestream;
    }

    public int getSourcerelationship() {
        return sourcerelationship;
    }

    public void setSourcerelationship(int sourcerelationship) {
        this.sourcerelationship = sourcerelationship;
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getQagmediarating() {
        return qagmediarating;
    }

    public void setQagmediarating(int qagmediarating) {
        this.qagmediarating = qagmediarating;
    }

    public int getEmbeddable() {
        return embeddable;
    }

    public void setEmbeddable(int embeddable) {
        this.embeddable = embeddable;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    private String language;
    private String ext;

}
