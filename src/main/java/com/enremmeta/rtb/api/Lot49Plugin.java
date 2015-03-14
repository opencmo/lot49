package com.enremmeta.rtb.api;

import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.constants.RtbConstants;

public interface Lot49Plugin extends Lot49Constants, KVKeysValues, RtbConstants {
    String getId();

    String getName();
}
