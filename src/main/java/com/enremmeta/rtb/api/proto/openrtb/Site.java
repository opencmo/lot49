package com.enremmeta.rtb.api.proto.openrtb;

import java.util.List;
import java.util.Map;

import com.enremmeta.rtb.RtbBean;

/**
 * OpenRTB Site object.
 * 
 * @see <a href="http://www.iab.net/media/file/OpenRTB-API-Specification-Version-2-3.pdf">OpenRTB
 *      2.3 (section 3.2.6)</a>
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class Site implements RtbBean {

    public Site() {
        // TODO Auto-generated constructor stub
    }

    private String id;
    private String name;
    private String domain;
    private List<String> cat;
    private List<String> sectioncat;
    private List<String> pagecat;
    private String page;
    private Integer privacypolicy;
    private String ref;
    private Integer search;
    private Publisher publisher;
    private Content content = new Content();
    private String keywords;
    private Map ext;
    private Integer mobile;

    public Integer getMobile() {
        return mobile;
    }

    public void setMobile(Integer mobile) {
        this.mobile = mobile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getCat() {
        return cat;
    }

    public void setCat(List<String> cat) {
        this.cat = cat;
    }

    public List<String> getSectioncat() {
        return sectioncat;
    }

    public void setSectioncat(List<String> sectioncat) {
        this.sectioncat = sectioncat;
    }

    public List<String> getPagecat() {
        return pagecat;
    }

    public void setPagecat(List<String> pagecat) {
        this.pagecat = pagecat;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public Integer getPrivacypolicy() {
        return privacypolicy;
    }

    public void setPrivacypolicy(Integer privacypolicy) {
        this.privacypolicy = privacypolicy;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Integer getSearch() {
        return search;
    }

    public void setSearch(Integer search) {
        this.search = search;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Map getExt() {
        return ext;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }

}
