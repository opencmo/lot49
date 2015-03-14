package com.enremmeta.rtb;

import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.util.Utils;

/**
 * An exception for cases when a {@link User#getBuyeruid() demand-side user ID} is corrupted (e.g.,
 * cannot be decoded by {@link Utils#cookieToLogModUid(String)}). Since this can happen quite
 * frequently, this follows a common pattern and overrides {@link #fillInStackTrace()} with a no-op
 * in order to avoid an expensive operation.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 *
 */
public final class CorruptedUserIdException extends Lot49RuntimeException {

    /**
     * Generated
     */
    private static final long serialVersionUID = 6955977081079328676L;

    public CorruptedUserIdException(String userId, String reason) {
        super("Corrupted user ID '" + userId + "': " + reason);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
