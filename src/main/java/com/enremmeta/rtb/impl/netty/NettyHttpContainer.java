package com.enremmeta.rtb.impl.netty;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.Container;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.jersey.AuctionsSvc;
import com.enremmeta.rtb.jersey.protobuf.ProtobufMessageReader;
import com.enremmeta.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

@Sharable
public class NettyHttpContainer extends
                // SimpleChannelInboundHandler<HttpRequest> {
                ChannelHandlerAdapter implements Container {
    public static final SecurityContext NO_SECURITY_CONTEXT = new SecurityContext() {

        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public String getAuthenticationScheme() {
            return null;
        }
    };

    public static final String PROPERTY_BASE_URI =
                    "com.sun.jersey.server.impl.container.netty.baseUri";

    private Application application;
    private String baseUri;

    private ApplicationHandler appHandler;

    public NettyHttpContainer(Application application) {
        super();
        final Map<String, Object> props = new HashMap<String, Object>();
        String svcPkgs = AuctionsSvc.class.getPackage().getName() + ";"
                        + OpenRtbRequest.class.getPackage().getName() + ";"
                        + ProtobufMessageReader.class.getPackage().getName();
        props.put(ServerProperties.PROVIDER_PACKAGES,
                        "com.fasterxml.jackson.jaxrs.json;" + svcPkgs);
        props.put("com.sun.jersey.api.json.POJOMappingFeature", "true");

        props.put(ServerProperties.MOXY_JSON_FEATURE_DISABLE, "true");

        props.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, "true");
        config = new ResourceConfig();
        config.addProperties(props);
        this.application = application;
        this.appHandler = new ApplicationHandler(application);

    }

    // @Override
    // public void channelReadComplete(ChannelHandlerContext ctx) {
    // ctx.flush();
    // }


    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {

            HttpRequest req = (HttpRequest) msg;

            if (is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }

            // boolean keepAlive = isKeepAlive(req);
            // FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
            // OK);
            String base = getBaseUri(req);
            final URI baseUri = new URI(base);
            final URI requestUri = new URI(base.substring(0, base.length() - 1) + req.getUri());

            final ContainerRequest cRequest =
                            new ContainerRequest(baseUri, requestUri, req.getMethod().name(),
                                            NO_SECURITY_CONTEXT, new SimplePropertiesDelegate());
            NettyResponseWriter writer = new NettyResponseWriter();
            cRequest.setWriter(writer);
            appHandler.handle(cRequest);
            //

        } else if (msg instanceof LastHttpContent) {
            LastHttpContent lhc = (LastHttpContent) msg;
            ByteBuf content = lhc.content();

            Utils.noop();
        } else {
            Utils.noop();
        }
    }

    private String getBaseUri(HttpRequest request) {
        if (baseUri != null) {
            return baseUri;
        }

        return "http://" + request.headers().get(HttpHeaders.Names.HOST) + "/";
    }

    @Override
    public ResourceConfig getConfiguration() {
        return this.config;
    }

    private final ResourceConfig config;

    @Override
    public ApplicationHandler getApplicationHandler() {
        return this.appHandler;
    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub

    }

    @Override
    public void reload(ResourceConfig configuration) {
        // TODO Auto-generated method stub

    }

}
