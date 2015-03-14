package com.enremmeta.rtb.impl.netty;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.spi.ContainerProvider;

public class NettyHttpContainerProvider implements ContainerProvider {


    @Override
    public <T> T createContainer(final Class<T> type, final Application application)
                    throws ProcessingException {
        if (NettyHttpContainer.class == type) {

            return type.cast(new NettyHttpContainer(application));
        }
        return null;
    }

}
