package com.enremmeta.rtb.spi.providers;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.SimpleCallback;

/**
 * Must:
 * <ol>
 * <li>Provide a public constructor taking the following arguments:
 * <ol>
 * <li>Instance of {@link ServiceRunner} (usually {@link Bidder}).
 * <li><tt>Map</tt></li>
 * </ol>
 * </ol>
 * May:
 * <ol>
 * <li>Include a CLI (command-line) interface</li>
 * </ol>
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public interface Provider {
    ProviderInfoReceived getProviderInfo(OpenRtbRequest req);

    void init() throws Lot49Exception;

    void initAsync(SimpleCallback callback) throws Lot49Exception;

    boolean isInitAsync();

    boolean isEnabled();

    String getName();

    ProviderInfoRequired parse(String json) throws Lot49Exception;

    boolean match(ProviderInfoReceived received, ProviderInfoRequired required);

}
