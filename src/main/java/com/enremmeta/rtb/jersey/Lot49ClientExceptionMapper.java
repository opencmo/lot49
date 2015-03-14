package com.enremmeta.rtb.jersey;

import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;

@Provider
public class Lot49ClientExceptionMapper implements ExceptionMapper<ClientErrorException> {

    @Context
    private HttpServletRequest svltReq;

    @Override
    public Response toResponse(ClientErrorException e) {
        // All we want is to log this.
        final Response resp = e.getResponse();
        final String url = svltReq.getRequestURL().toString();
        final String qs = svltReq.getQueryString() == null ? "" : "?" + svltReq.getQueryString();

        final String status = resp.getStatus() + " " + resp.getStatusInfo().getReasonPhrase();
        final String method = svltReq.getMethod();
        String host = "UNKNOWN";
        try {
            URL realUrl = new URL(url);
            host = realUrl.getHost();
        } catch (Exception e2) {
            //
        }
        final String msg = "Error in " + method + " to " + url + qs + ": " + status + " ("
                        + e.getClass() + "[" + e.getMessage() + "])";
        // Do not log the whole stack trace if it's not our host anyway.
        if (!Bidder.getInstance().getConfig().getHost().equalsIgnoreCase(host)) {
            LogUtils.warn(msg + " - NOT OUR HOST: " + host);
        } else {
            LogUtils.error(msg, e);
        }

        String debugMsg = msg + "\n";
        final Enumeration<String> headers = svltReq.getHeaderNames();
        while (headers.hasMoreElements()) {
            final String headerName = headers.nextElement();
            final String headerValue = svltReq.getHeader(headerName);
            debugMsg += "\t" + headerName + ": " + headerValue + "\n";
        }
        LogUtils.debug(debugMsg);

        return resp;
    }
}
