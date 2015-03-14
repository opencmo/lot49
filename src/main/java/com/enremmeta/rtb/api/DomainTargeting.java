package com.enremmeta.rtb.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.util.Utils;

/**
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 */



public class DomainTargeting {

    private Ad ad;

    public DomainTargeting() {
        super();
    }

    public DomainTargeting(Ad ad, Set<String> list) {
        super();
        this.ad = ad;
        this.originalList = list;
        normalizeList(list);
    }

    public Ad getAd() {
        return ad;
    }

    public void setAd(Ad ad) {
        this.ad = ad;
    }

    private Set<String> originalList;

    public Set<String> getOriginalList() {
        return originalList;
    }

    public void setOriginalList(Set<String> originalList) {
        this.originalList = originalList;
    }

    public Set<String> getExactList() {
        return exactList;
    }

    public void setExactList(Set<String> exactList) {
        this.exactList = exactList;
    }

    public Set<Pattern> getGlobList() {
        return globList;
    }

    public void setGlobList(Set<Pattern> globList) {
        this.globList = globList;
    }


    private void normalizeList(Set<String> list) {
        String msg = "";
        if (list == null || list.size() == 0) {
            return;
        }
        int nullCount = 0;
        int wwwCount = 0;
        int dupCount = 0;
        for (String d : list) {
            String orig = d;
            if (d == null) {
                nullCount++;
                continue;
            }
            d = d.trim();
            if (d.length() == 0) {
                nullCount++;
                continue;
            }
            d = d.toLowerCase();
            if (d.startsWith("www.")) {
                d = d.substring(4);
                wwwCount++;
            }
            if (Utils.validateDomain(d)) {
                if (!d.equals(orig)) {
                    // msg += "\n\tNormalized " + orig + " to " + d;
                }
                if (!this.exactList.add(d)) {
                    dupCount++;
                }
                continue;
            }
            if (d.startsWith("//")) {
                d = "http:" + d;
            }
            // Logic is like this: if we don't have the pattern for the domain,
            // this may be a partial URL.
            if (!d.startsWith("http://") && !d.startsWith("https://")) {
                d = "http://" + d;
            }
            try {
                d = new URL(d).getHost();
                if (d.startsWith("www.")) {
                    d = d.substring(4);
                    wwwCount++;
                }
                // TODO is probably faster to only allow * to be least top
                // (like *.yahoo.com) and match using a Map...
                if (d.indexOf('*') > -1) {
                    String newD = d.replaceAll("\\.", "\\\\.");
                    newD = newD.replaceAll("\\*", ".*");
                    msg += "\n\tReplaced '" + d + " with '" + newD + "'";
                    // System.out.println("\n\tReplaced '" + d + " with '" + newD + "'");
                    Pattern p = Pattern.compile(newD);
                    if (!this.globList.add(p)) {
                        dupCount++;
                    }
                } else if (!this.exactList.add(d)) {
                    dupCount++;
                }
                continue;
            } catch (MalformedURLException e) {
                msg += "\n\tCould not normalize " + d;
                continue;
            }
        }
        if (nullCount > 0) {
            msg += "\n\tIgnored " + nullCount + " null or empty entries";
        }
        if (dupCount > 0) {
            msg += "\n\t " + nullCount + " duplicates";
        }
        if (wwwCount > 0) {
            msg += "\n\tRemoved leading www. " + wwwCount + " times";
        }
    }

    private Set<String> exactList = new HashSet<String>();
    private Set<Pattern> globList = new HashSet<Pattern>();

    public boolean match(String domain) {
        if (domain == null) {
            return false;
        }
        domain = domain.toLowerCase();
        if (domain.startsWith("www.")) {
            domain = domain.substring(4);
        }
        boolean retval = false;
        if (exactList.contains(domain)) {
            retval = true;
        } else {
            for (Pattern p : globList) {
                if (p.matcher(domain).matches()) {
                    retval = true;
                    break;
                }
            }
        }
        // LogUtils.debug(ad.getId() + ": XXX: matching " + domain + " vs " + exactList + "; " +
        // globList
        // + ": " + retval);
        return retval;
    }

    public static void main(String[] argv) throws Exception {
        Set<String> list = new HashSet<String>();
        list.add("*.yahoo.com");
        list.add("www.google.com");
        list.add("finance.google.com");
        DomainTargeting dt = new DomainTargeting(new AdImpl() {}, list);
        String[] domains = new String[] {"mailyahoo.com", "mail.yahoo.com", "ca.finance.yahoo.com",
                        "google.com", "www.google.com", "finance.google.com", "mail.google.com"};

        for (String domain : domains) {
            System.out.println(domain + ": " + dt.match(domain));
        }
    }
}
