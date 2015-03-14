package com.enremmeta.util;

import java.nio.ByteOrder;

import javolution.io.Struct;

/**
 * Structure for holding mod_uid.
 * 
 * @see <A href="http://nginx.org/en/docs/http/ngx_http_userid_module.html#variables">Module
 *      ngx_http_userid_module</a>
 * 
 * @see <a href="http://www.lexa.ru/programs/mod-uid-eng.html">http://www.lexa.ru/programs/mod-uid-
 * 
 *      eng.html</a>
 * 
 * @see <a href="https://github.com/debedb/microput">Microput</a>
 * 
 * @see Utils#cookieToLogModUid(String)
 * 
 * @see Utils#logToCookieModUid(String)
 * 
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class ModUidStruct extends Struct {
    public ByteOrder byteOrder() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    public final Unsigned32 serviceNumber = new Unsigned32();
    public final Unsigned32 issueTime = new Unsigned32();
    public final Unsigned32 pid = new Unsigned32();
    public final Unsigned32 cookie3 = new Unsigned32();
}
