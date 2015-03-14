package com.enremmeta.rtb.api;

import com.enremmeta.rtb.AwsOrchestrator;
import com.enremmeta.rtb.LostAuctionTask;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBService;

/**
 * Just to keep various keys in one place.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public interface KVKeysValues {

    /**
     * Это очень сильное колдунство.
     * 
     * @author Half-blood prince
     */
    public static final String ENV_LOT49_MAGIC = "LOT49_MAGIC";

    public static final String ENV_LOT49_CONFIG_FILE = "LOT49_CONFIG_FILE";
    public static final String ENV_LOT49_HOME = "LOT49_HOME";

    public static final String ENV_AWS_REGION = "AWS_REGION";

    /**
     * Environment variable specifying EC2 API endpoint
     */
    public static final String ENV_EC2_ENDPOINT = "LOT49_EC2_ENDPOINT";

    /**
     * Environment variable specifying ELB API endpoint
     * 
     * @see AwsOrchestrator
     */
    public static final String ENV_ELB_ENDPOINT = "LOT49_ELB_ENDPOINT";

    /**
     * Environment variable specifying STS API endpoint
     * 
     * @see AwsOrchestrator
     */
    public static final String ENV_STS_ENDPOINT = "LOT49_STS_ENDPOINT";

    /**
     * Environment variable specifying DynamoDB API endpoint
     * 
     * @see DynamoDBService
     */
    public static final String ENV_DYNAMO_ENDPOINT = "LOT49_DYNAMODB_ENDPOINT";

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is remaining budget, in
     * micro-dollars.
     * 
     * @see AdCache
     */
    public static final String BUDGET_PREFIX = "budget_";

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is the end date of the ad in an ISO
     * format.
     * 
     * @see AdCache
     */
    public static final String ENDS_ON_PREFIX = "endsOn_";

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is the start date of the ad in an ISO
     * format.
     * 
     * @see AdCache
     */
    public static final String STARTS_ON_PREFIX = "startsOn_";

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}.
     * 
     * @see AdCache
     */
    public static final String BID_PRICE_PREFIX = "bidPrice_";

    public static final String OWNER_KEY_PREFIX = "ownerKey_";

    /**
     * Special value for {@link #ENDS_ON_PREFIX}.
     * 
     * @see AdCache
     */
    public static final String PACING_ASAP = "ASAP";

    /**
     * Special value for {@link #BUDGET_PREFIX}
     * 
     * @see #PACING_ASAP
     */
    public static final String BUDGET_UNLIMITED = "UNLIMITED";

    /**
     * Increment this when a bug is fixed or logic changes, so that calculations are reset.
     */
    static final String PREFIX_VERSION = "_v7_";

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is total amount (in micro$) that has
     * been bid so far (reset every {@link PacingServiceConfig#getWinRateTtlMinutes()}).
     * 
     * @see AdCache
     */
    public static final String BID_COUNT_PREFIX = "bidsCount" + PREFIX_VERSION;

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is total count of wins so far (reset
     * every {@link PacingServiceConfig#getWinRateTtlMinutes()}).
     * 
     * @see AdCache
     */
    public static final String WIN_COUNT_PREFIX = "winsCount" + PREFIX_VERSION;

    /**
     * When some errors occur, we are going to lower bid probability for a while. TBD. TODO.
     */
    public static final String LOWER_BID_PROBABILITY = "lowerBidProbability" + PREFIX_VERSION;

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is spend amount for the previous
     * period
     * 
     * @see AdCache
     */
    public static final String PREVIOUS_SPEND_AMOUNT_PREFIX = "prevSpendAmount" + PREFIX_VERSION;

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is total spend amount so far (reset
     * every {@link PacingServiceConfig#getWinRateTtlMinutes()}).
     * 
     * @see AdCache
     */
    public static final String SPEND_AMOUNT_PREFIX = "spendAmount" + PREFIX_VERSION;

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is total amount (in micro$) that has
     * been bid so far (reset every {@link PacingServiceConfig#getWinRateTtlMinutes()}).
     * 
     * @see AdCache
     */
    public static final String BID_AMOUNT_PREFIX = "bidAmount" + PREFIX_VERSION;

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is total number of bids so far.
     * Unlike {@link #BID_COUNT_PREFIX} this is not reset, as this is for the life of the Ad (as
     * opposed for calculation of pacing/rates).
     * 
     * @see AdCache
     */
    public static final String BID_COUNT_DISPLAY_PREFIX = "bidsCountDisplay" + PREFIX_VERSION;

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is total number of wins so far.
     * Unlike {@link #WIN_COUNT_PREFIX} this is not reset, as this is for the life of the Ad (as
     * opposed for calculation of pacing/rates).
     * 
     * @see AdCache
     */
    public static final String WIN_COUNT_DISPLAY_PREFIX = "winsCountDisplay" + PREFIX_VERSION;

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is total amount spent so far. Unlike
     * {@link #SPEND_AMOUNT_PREFIX} this is not reset, as this is for the life of the Ad (as opposed
     * for calculation of pacing/rates).
     * 
     * @see AdCache
     */
    public static final String SPEND_AMOUNT_DISPLAY_PREFIX = "spendAmountDisplay_";

    /**
     * Prefix (followed by {@link Ad#getId() Ad ID}) of a data stored in
     * {@link PacingServiceConfig#getRedis() pacing DB}. Value is total amount bid so far. Unlike
     * {@link #BID_AMOUNT_PREFIX} this is not reset, as this is for the life of the Ad (as opposed
     * for calculation of pacing/rates).
     * 
     * @see AdCache
     */
    public static final String BID_AMOUNT_DISPLAY_PREFIX = "bidAmountDisplay" + PREFIX_VERSION;

    public static final String DECISION_DISPLAY_PREFIX = "decisionDisplay" + PREFIX_VERSION;

    public static final String COOKIE_TS_IMPRESSION = "its";
    public static final String COOKIE_TS_CLICK = "cts";

    /**
     * When win and bid counts have last been reset, as a Unix time stamp.
     */
    public static final String WIN_RATE_STARTED_TS = "winRateStarted_";

    public static final String PACING_LOG_PREFIX = "pacingLog_";

    // public static final String USER_PREFIX = "user_";

    public static final String SEGMENT_PREFIX = "segment_";

    public static final String NURL_PREFIX = "nurl_";

    public static final String DEBUG_NURL_PREFIX = "debugNurl_";

    /**
     * This is for ZScores
     */
    public static final String USER_PREFIX_V1 = "user_v1_";

    /**
     * For bids in flight. For logging as well as {@link LostAuctionTask}
     */
    public static final String BID_PREFIX = "bid_";
    public static final String BID_REQUEST_PREFIX = "bidRequest_";

    public static final String B_TOOK_CARE_OF_IT_WIN = "B_TOOK_CARE_OF_IT_WIN";

    public static final String B_TOOK_CARE_OF_IT_LOSS = "B_TOOK_CARE_OF_IT_LOSS";

    public static final String B_TOOK_CARE_OF_IT_ERROR = "B_TOOK_CARE_OF_IT_ERROR";

    public static final String BUDGET_CHUNK_SIZE_PREFIX = "budgetChunkSize_";

    public static final String BUDGET_MAP_KEY = "budgetMap";

    public static final String BIDDER_COUNT_KEY = "bidderCount";

    public static final String USER_DATA_SCORE_KEY = "score";
}
