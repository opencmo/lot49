package com.enremmeta.rtb.impl.netty;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.jersey.AuctionsSvc;
import com.enremmeta.rtb.jersey.protobuf.ProtobufMessageReader;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * https://raw.githubusercontent.com/Jotschi/netty-examples/master/netty-5-
 * jersey-example/src/main/java/de/jotschi/example/netty/init/ HttpServerPipelineInitializer.java
 */
public class HttpServerPipelineInitializer extends ChannelInitializer<SocketChannel> {

    private NettyHttpContainer container;

    public HttpServerPipelineInitializer() {
        super();
        this.container = makeContainer();
    }

    private static final NettyHttpContainer makeContainer() {
        final Map<String, Object> props = new HashMap<String, Object>();
        String svcPkgs = AuctionsSvc.class.getPackage().getName() + ";"
                        + OpenRtbRequest.class.getPackage().getName() + ";"
                        + ProtobufMessageReader.class.getPackage().getName();
        props.put(ServerProperties.PROVIDER_PACKAGES,
                        "com.fasterxml.jackson.jaxrs.json;" + svcPkgs);
        props.put("com.sun.jersey.api.json.POJOMappingFeature", "true");

        props.put(ServerProperties.MOXY_JSON_FEATURE_DISABLE, "true");

        props.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, "true");
        ResourceConfig rcf = new ResourceConfig();
        rcf.addProperties(props);
        return ContainerFactory.createContainer(NettyHttpContainer.class, rcf);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast("codec", new HttpServerCodec());
        p.addLast("jerseyHandler", container);
    }
}
