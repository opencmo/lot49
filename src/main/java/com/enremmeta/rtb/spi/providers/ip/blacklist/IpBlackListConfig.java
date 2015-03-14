package com.enremmeta.rtb.spi.providers.ip.blacklist;

import java.util.Map;

import com.enremmeta.rtb.spi.providers.ProviderConfig;

/**
 * Created by amiroshn on 4/22/2016.
 */
public class IpBlackListConfig extends ProviderConfig {

    private String filePath;

    private Boolean initAsync;

    public IpBlackListConfig(Map map) {
        super(map);
        this.filePath = (String) map.get("filePath");
        this.initAsync = map.get("initAsync") != null || "false".equals(map.get("initAsync"))
                        ? false : true;
    }

    public String getFilePath() {
        return filePath;
    }

    public Boolean getInitAsync() {
        return initAsync;
    }
}
