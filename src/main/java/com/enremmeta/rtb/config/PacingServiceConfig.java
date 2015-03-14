package com.enremmeta.rtb.config;

import com.enremmeta.rtb.Orchestrator;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;

/**
 * Config for pacing service. For actual information on the pacing algorithms, see {@link AdCache}.
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class PacingServiceConfig implements Config {

    public PacingServiceConfig() {
        // TODO Auto-generated constructor stub
    }

    public double getWinRateMin() {
        return winRateMin;
    }

    public void setWinRateMin(double winRateMin) {
        this.winRateMin = winRateMin;
    }

    private double winRateMin = 0.01;

    private double effectiveWinRateIfLessThanMin = 0.1;

    public double getEffectiveWinRateIfLessThanMin() {
        return effectiveWinRateIfLessThanMin;
    }

    public void setEffectiveWinRateIfLessThanMin(double effectiveWinRateIfLessThanMin) {
        this.effectiveWinRateIfLessThanMin = effectiveWinRateIfLessThanMin;
    }

    private RedisServiceConfig redis;

    public static final long DEFAULT_MESSAGE_TTL_MINUTES = 3 * 60;

    private long messageTtlMinutes = DEFAULT_MESSAGE_TTL_MINUTES;

    public long getMessageTtlMinutes() {
        return messageTtlMinutes;
    }

    public void setMessageTtlMinutes(long messageTtlMinutes) {
        this.messageTtlMinutes = messageTtlMinutes;
    }



    private long winRateTtlMinutes;

    public long getWinRateTtlMinutes() {
        return winRateTtlMinutes;
    }

    public void setWinRateTtlMinutes(long winRateTtlMinutes) {
        this.winRateTtlMinutes = winRateTtlMinutes;
    }

    /**
     * This strategy will allocate money to a current Ad as follows:
     * <ol>
     * <li>Figure out how many {@link AdCacheConfig#getTtlMinutes() refresh periods} there are
     * between now and end of {@link Ad}'s flight.</li>
     * <li>Divide total remaining budget in the pacing cache by this number, and also by the number
     * of {@link Orchestrator#getNumberOfPeers() number of servers servicing this bid stream}.</li>
     * </ol>
     */
    public final static String BUDGET_ALLOCATION_STRATEGY_NAIVE = "naive";

    /**
     * This strategy will allocate the budget as {@link #BUDGET_ALLOCATION_STRATEGY_NAIVE} first,
     * and then calculate the win rate (wins/bids) and divide the budget by the win rate. In other
     * words, if the naive strategy resulted in allocation of micro$ 10000 and the win rate is 20%
     * (0.2), the new budget will be adjusted to 50000.
     * 
     */
    public final static String BUDGET_ALLOCATION_STRATEGY_WIN_RATE_BASED = "winRateBased";

    public final static String DEFAULT_BUDGET_ALLOCATION_STRATEGY =
                    BUDGET_ALLOCATION_STRATEGY_WIN_RATE_BASED;

    private String budgetAllocationStrategy;

    /**
     * How to allocated money from budget. Possible choices are:
     * <ol>
     * <li>{@link #BUDGET_ALLOCATION_STRATEGY_NAIVE naive}</li>
     * <li>{@link #BUDGET_ALLOCATION_STRATEGY_NAIVE winRateBased}</li>
     * </ol>
     * <p>
     * <b>NOTE:</b> This applies to allocating new money from the budget, and does not apply to what
     * to do with money allocated previously that was unspent. For that, see
     * {@link #getBudgetAllocationStrategy()}.
     * </p>
     */
    public String getBudgetAllocationStrategy() {
        if (budgetAllocationStrategy == null || budgetAllocationStrategy.trim().equals("")) {
            this.budgetAllocationStrategy = DEFAULT_BUDGET_ALLOCATION_STRATEGY;
        }
        return this.budgetAllocationStrategy;

    }

    public void setBudgetAllocationStrategy(String budgetAllocationStrategy) {
        if (budgetAllocationStrategy == null || budgetAllocationStrategy.trim().equals("")) {
            this.budgetAllocationStrategy = DEFAULT_BUDGET_ALLOCATION_STRATEGY;
        } else {
            this.budgetAllocationStrategy = budgetAllocationStrategy;
        }

    }

    public RedisServiceConfig getRedis() {
        return redis;
    }

    public void setRedis(RedisServiceConfig redis) {
        this.redis = redis;
    }

    public int getLockExpireSeconds() {
        return lockExpireSeconds;
    }

    public void setLockExpireSeconds(int lockExpireSeconds) {
        this.lockExpireSeconds = lockExpireSeconds;
    }

    private int lockExpireSeconds;

}
