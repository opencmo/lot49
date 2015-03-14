package com.enremmeta.rtb.impl.netty;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.util.Utils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Utils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class NettyHttpContainerSpec {

    @Test
    public void testCondtructor_shpuldSet_ServerProperties_PROVIDER_PACKAGES() {
        NettyHttpContainer nettyC = new NettyHttpContainer(new Application());

        ResourceConfig config = Whitebox.getInternalState(nettyC, "config");

        assertEquals("com.fasterxml.jackson.jaxrs.json;com.enremmeta.rtb.jersey;com.enremmeta.rtb.api.proto.openrtb;com.enremmeta.rtb.jersey.protobuf",
                        config.getProperties().get(ServerProperties.PROVIDER_PACKAGES));
    }

    @Test
    public void testCondtructor_shpuldSet_POJOMappingFeature() {
        NettyHttpContainer nettyC = new NettyHttpContainer(new Application());

        ResourceConfig config = Whitebox.getInternalState(nettyC, "config");

        assertEquals("true",
                        config.getProperties().get("com.sun.jersey.api.json.POJOMappingFeature"));
    }

    @Test
    public void testCondtructor_shpuldSet_MOXY_JSON_FEATURE_DISABLE() {
        NettyHttpContainer nettyC = new NettyHttpContainer(new Application());

        ResourceConfig config = Whitebox.getInternalState(nettyC, "config");

        assertEquals("true",
                        config.getProperties().get(ServerProperties.MOXY_JSON_FEATURE_DISABLE));
    }

    @Test
    public void testCondtructor_shpuldSet_PROCESSING_RESPONSE_ERRORS_ENABLED() {
        NettyHttpContainer nettyC = new NettyHttpContainer(new Application());

        ResourceConfig config = Whitebox.getInternalState(nettyC, "config");

        assertEquals("true", config.getProperties()
                        .get(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED));
    }

    @Test
    public void test_channelRead_shouldInvokeUtilsNoop_onWrongMsg() throws Exception {
        NettyHttpContainer nettyC = new NettyHttpContainer(new Application());

        PowerMockito.mockStatic(Utils.class);
        PowerMockito.doNothing().when(Utils.class);
        Utils.noop();

        String msg = "illegal type for message";

        nettyC.channelRead(Mockito.mock(ChannelHandlerContext.class), msg);

        PowerMockito.verifyStatic(Mockito.times(1));
        Utils.noop();
    }

    @Test
    public void test_channelRead_LastHttpContentMsg_shouldInvokeNoop() throws Exception {
        NettyHttpContainer nettyC = new NettyHttpContainer(new Application());

        PowerMockito.mockStatic(Utils.class);
        PowerMockito.doCallRealMethod().when(Utils.class);
        Utils.noop();

        LastHttpContent msg = Mockito.mock(LastHttpContent.class);

        nettyC.channelRead(Mockito.mock(ChannelHandlerContext.class), msg);

        PowerMockito.verifyStatic(Mockito.times(1));
        Utils.noop();
    }

    @Test
    public void test_channelRead_HttpRequestMsg_shouldSetApplicationField() throws Exception {
        Application appMarker = Mockito.mock(Application.class);

        NettyHttpContainer nettyC = new NettyHttpContainer(appMarker);

        HttpRequest msg = Mockito.mock(HttpRequest.class);
        Mockito.when(msg.protocolVersion()).thenReturn(HttpVersion.HTTP_1_0);
        HttpHeaders httpHeadersMock = Mockito.mock(HttpHeaders.class);
        Mockito.when(msg.headers()).thenReturn(httpHeadersMock);
        Mockito.when(msg.getMethod()).thenReturn(HttpMethod.GET);

        nettyC.channelRead(Mockito.mock(ChannelHandlerContext.class), msg);

        assertEquals(appMarker,
                        Whitebox.getInternalState(nettyC.getApplicationHandler(), "application"));
    }

}
