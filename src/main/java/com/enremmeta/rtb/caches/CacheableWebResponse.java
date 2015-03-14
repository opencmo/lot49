package com.enremmeta.rtb.caches;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.enremmeta.rtb.dao.impl.hazelcast.HazelcastService;
import com.enremmeta.util.Jsonable;

/**
 * Information enough to create a {@link Response}, capable to be stored in {@link HazelcastService
 * distributed cache}.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class CacheableWebResponse implements Jsonable {

    /**
     * 
     */
    private static final long serialVersionUID = 3614836058597403638L;

    public CacheableWebResponse() {
        // TODO Auto-generated constructor stub
    }

    public CacheableWebResponse(String entity, String mediaType) {
        this.entity = entity;
        this.mediaType = mediaType;
    }

    public CacheableWebResponse(Response response) {
        super();
        this.entity = response.getEntity().toString();
        this.mediaType = response.getMediaType().toString();
    }

    public CacheableWebResponse(ResponseBuilder rb) {
        this(rb.build());
    }

    private String entity;
    private String mediaType;

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public Response getResponse() {
        return Response.ok(entity, mediaType).build();
    }

}
