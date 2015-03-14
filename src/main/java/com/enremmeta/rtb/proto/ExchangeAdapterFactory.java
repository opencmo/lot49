package com.enremmeta.rtb.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.adaptv.AdaptvAdapter;
import com.enremmeta.rtb.proto.adx.AdXAdapter;
import com.enremmeta.rtb.proto.bidswitch.BidSwitchAdapter;
import com.enremmeta.rtb.proto.brx.BrxAdapter;
import com.enremmeta.rtb.proto.liverail.LiverailAdapter;
import com.enremmeta.rtb.proto.openx.OpenXAdapter;
import com.enremmeta.rtb.proto.pubmatic.PubmaticAdapter;
import com.enremmeta.rtb.proto.spotxchange.SpotXChangeAdapter;
import com.enremmeta.rtb.proto.testexchange.Lot49InternalAuctionExchangeAdapter;
import com.enremmeta.rtb.proto.testexchange.Test1ExchangeAdapter;
import com.enremmeta.rtb.proto.testexchange.Test2ExchangeAdapter;

/**
 * Generate {@link ExchangeAdapter} by name.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class ExchangeAdapterFactory {

    private static final Map<String, Class<? extends ExchangeAdapter>> adapterMap =
                    new HashMap<String, Class<? extends ExchangeAdapter>>() {
                        {
                            put(Lot49Constants.EXCHANGE_ADAPTV, AdaptvAdapter.class);
                            put(Lot49Constants.EXCHANGE_BIDSWITCH, BidSwitchAdapter.class);
                            put(Lot49Constants.EXCHANGE_BRX, BrxAdapter.class);
                            put(Lot49Constants.EXCHANGE_OPENX, OpenXAdapter.class);
                            put(Lot49Constants.EXCHANGE_PUBMATIC, PubmaticAdapter.class);
                            put(Lot49Constants.EXCHANGE_SPOTXCHANGE, SpotXChangeAdapter.class);
                            put(Lot49Constants.EXCHANGE_LIVERAIL, LiverailAdapter.class);
                            put(Lot49Constants.EXCHANGE_LOT49_INTERNAL_AUCTION,
                                            Lot49InternalAuctionExchangeAdapter.class);
                            put(Lot49Constants.EXCHANGE_TEST1, Test1ExchangeAdapter.class);
                            put(Lot49Constants.EXCHANGE_TEST2, Test2ExchangeAdapter.class);
                            put(Lot49Constants.EXCHANGE_ADX, AdXAdapter.class);

                        }
                    };

    public static ExchangeAdapter getExchangeAdapter(String exchangeName) {
        return getExchangeAdapter(exchangeName, false);
    }

    // TODO : Use SPI
    public static ExchangeAdapter getExchangeAdapter(String exchangeName, boolean quiet) {
        // This special case is worth it - monadic;)
        if (exchangeName == null) {
            return null;
        }
        final Class<? extends ExchangeAdapter> klass = adapterMap.get(exchangeName);
        if (klass == null) {
            String msg = "Cannot get ExchangeAdapter for " + exchangeName;
            if (quiet) {
                LogUtils.error(msg);
                return null;
            } else {
                throw new IllegalArgumentException(msg);
            }
        }
        try {
            final ExchangeAdapter retval = klass.newInstance();
            return retval;
        } catch (Throwable t) {
            String msg = "Cannot instantiate ExchangeAdapter for " + exchangeName;
            if (quiet) {
                LogUtils.error(msg, t);
                return null;
            } else {
                throw new IllegalArgumentException(msg, t);
            }
        }
    }

    public static List<String> getAllExchangeAdapterNames() {
        return new ArrayList(adapterMap.keySet());
    }

    public static List<ExchangeAdapter> getAllExchangeAdapters() {
        return getAllExchangeAdapters(false);
    }

    public static List<ExchangeAdapter> getAllExchangeAdapters(boolean quiet) {
        final List<ExchangeAdapter> retval = new ArrayList<ExchangeAdapter>();
        for (final String exchangeName : adapterMap.keySet()) {
            ExchangeAdapter adapter = getExchangeAdapter(exchangeName, quiet);
            if (adapter != null) {
                retval.add(adapter);
            }
        }
        return retval;
    }

}
