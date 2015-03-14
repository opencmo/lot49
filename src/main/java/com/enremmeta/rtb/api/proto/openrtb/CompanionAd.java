package com.enremmeta.rtb.api.proto.openrtb;

import com.enremmeta.rtb.RtbBean;

/**
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. This code is licensed under
 *         <a href="http://www.gnu.org/licenses/agpl-3.0.html">Affero GPL 3.0</a>
 *
 */
public class CompanionAd implements RtbBean {

    public CompanionAd() {
        // TODO Auto-generated constructor stub
    }

    private int w;
    private int h;
    private int id;

    private int pos;

    private int topframe;

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getTopframe() {
        return topframe;
    }

    public void setTopframe(int topframe) {
        this.topframe = topframe;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
