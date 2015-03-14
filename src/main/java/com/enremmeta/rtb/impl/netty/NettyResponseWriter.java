package com.enremmeta.rtb.impl.netty;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

import com.enremmeta.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

final class NettyResponseWriter implements ContainerResponseWriter {

    // private final Response response;
    // private final Continuation continuation;
    // private final boolean configSetStatusOverSendError;

    // public NettyResponseWriter(final Request request, final Response
    // response,
    // final boolean configSetStatusOverSendError) {
    // this.response = response;
    // this.continuation = ContinuationSupport.getContinuation(request);
    // this.configSetStatusOverSendError = configSetStatusOverSendError;
    // }

    public NettyResponseWriter() {

    }

    @Override
    public OutputStream writeResponseStatusAndHeaders(final long contentLength,
                    final ContainerResponse cResponse) throws ContainerException {

        ByteBuf buffer = Unpooled.buffer();
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.valueOf(cResponse.getStatus()), buffer);
        //
        // for (Map.Entry<String, List<Object>> e : cResponse.getHttpHeaders()
        // .entrySet()) {
        // List<String> values = new ArrayList<String>();
        // for (Object v : e.getValue())
        // values.add(ContainerResponse.getHeaderValue(v));
        // response.headers().set(e.getKey(), values);
        // }
        return new ByteBufOutputStream(buffer);
    }

    @Override
    public boolean suspend(final long timeOut, final TimeUnit timeUnit,
                    final TimeoutHandler timeoutHandler) {
        return true;
    }

    @Override
    public void setSuspendTimeout(final long timeOut, final TimeUnit timeUnit)
                    throws IllegalStateException {
        Utils.noop();
    }

    @Override
    public void commit() {
        Utils.noop();
    }

    @Override
    public void failure(final Throwable error) {
        Utils.noop();
    }

    @Override
    public boolean enableResponseBuffering() {
        return false;
    }

    /**
     * Rethrow the original exception as required by JAX-RS, 3.3.4.
     *
     * @param error
     *            throwable to be re-thrown
     */
    private void rethrow(final Throwable error) {
        Utils.noop();
    }

}
