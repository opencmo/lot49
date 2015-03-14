package com.enremmeta.rtb.config;

public abstract class DbConfig implements Config {

    public static final long DEFAULT_SHORT_LIVED_MAP_TTL_SECONDS = 30 * 60;

    private long shortLivedMapTtlSeconds = DEFAULT_SHORT_LIVED_MAP_TTL_SECONDS;

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getShortLivedMapTtlSeconds() {
        return shortLivedMapTtlSeconds;
    }

    public void setShortLivedMapTtlSeconds(long shortLivedMapTtlSecs) {
        this.shortLivedMapTtlSeconds = shortLivedMapTtlSecs;
    }

}
