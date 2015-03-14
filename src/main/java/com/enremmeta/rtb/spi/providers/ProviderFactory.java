package com.enremmeta.rtb.spi.providers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.spi.providers.ip.blacklist.IpBlackListProvider;
import com.enremmeta.rtb.spi.providers.skyhook.SkyhookProvider;
import com.enremmeta.util.ServiceRunner;

public class ProviderFactory {
    private static final Map<String, Class<? extends Provider>> providerMap =
                    new HashMap<String, Class<? extends Provider>>() {
                        {
                            put(SkyhookProvider.SKYHOOK_PROVIDER_NAME, SkyhookProvider.class);
                            put(IpBlackListProvider.IP_BLACKLIST_PROVIDER_NAME,
                                            IpBlackListProvider.class);
                        }
                    };

    public static Provider getProvider(ServiceRunner runner, String name, Map config)
                    throws Lot49Exception {
        // This special case is worth it - monadic;)
        if (name == null) {
            return null;
        }
        final Class<? extends Provider> klass = providerMap.get(name);
        if (klass == null) {
            throw new IllegalArgumentException("Unknown provider: " + name);
        }
        try {
            Constructor<?>[] ctors = klass.getConstructors();
            LogUtils.debug("Available constructors: " + Arrays.asList(ctors));
            Constructor<? extends Provider> con =
                            klass.getConstructor(ServiceRunner.class, Map.class);

            final Provider retval = con.newInstance(runner, config);
            return retval;
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException
                        | IllegalAccessException e) {
            LogUtils.error("Cannot instantiate Provider for " + name, e);
            throw new IllegalArgumentException("Unknown provider: " + name, e);
        }

    }
}
