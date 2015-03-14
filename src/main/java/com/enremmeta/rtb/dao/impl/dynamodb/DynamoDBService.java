package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.Map;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.nio.AmazonDynamoDBClient;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.config.DbConfig;
import com.enremmeta.rtb.config.DynamoDBServiceConfig;
import com.enremmeta.rtb.dao.DaoCache;
import com.enremmeta.rtb.dao.DaoCacheLoader;
import com.enremmeta.rtb.dao.DaoCounters;
import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;
import com.enremmeta.rtb.dao.DaoMapOfUserSegments;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.dao.DbService;

public class DynamoDBService implements DbService {

    public DynamoDBService() {
        super();

    }

    private DynamoDBServiceConfig config;

    private AmazonDynamoDBClient client;

    private String tableName;

    String getTableName() {
        return tableName;
    }

    String getKeyField() {
        return keyField;
    }

    @Override
    public void init(DbConfig c) throws Lot49Exception {
        try {
            this.config = (DynamoDBServiceConfig) c;
        } catch (ClassCastException cce) {
            throw new Lot49Exception("Expected 'DynamoDBServiceConfig', got " + c.getClass(), cce);
        }

        final Map<String, String> env = System.getenv();
        String dynEndpoint = env.get(KVKeysValues.ENV_DYNAMO_ENDPOINT);

        if (dynEndpoint == null) {
            throw new Lot49Exception("Expected a '" + KVKeysValues.ENV_DYNAMO_ENDPOINT
                            + "' environment variable");
        }

        if (config.getAwsAccessKey() != null) {
            LogUtils.info("AUTHENTICATION: DynamoDB is using KEYS.");
            BasicAWSCredentials bsc = new BasicAWSCredentials(config.getAwsAccessKey(),
                            config.getAwsSecretKey());

            ClientConfiguration cc = new ClientConfiguration();
            cc.setUseTcpKeepAlive(true);
            cc.setMaxConnections(config.getPoolSize());
            client = new AmazonDynamoDBClient(bsc, cc);

            LogUtils.info(this + " connecting to " + dynEndpoint);

            client.setEndpoint(dynEndpoint);

        } else {
            // http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/prog-services-sts.html
            initRoleBasedClient();
        }

        this.tableName = config.getTable();

        this.keyField = config.getKeyField();
    }

    private void initRoleBasedClient() {
        LogUtils.info("AUTHENTICATION: DynamoDB is using ROLES.");
        ClientConfiguration cc = new ClientConfiguration();
        cc.setUseTcpKeepAlive(true);
        cc.setMaxConnections(config.getPoolSize());
        InstanceProfileCredentialsProvider ipcp = new InstanceProfileCredentialsProvider();
        client = new AmazonDynamoDBClient(ipcp, cc);
        final Map<String, String> env = System.getenv();
        String dynEndpoint = env.get(KVKeysValues.ENV_DYNAMO_ENDPOINT);
        LogUtils.info(this + " connecting to " + dynEndpoint);
        client.setEndpoint(dynEndpoint);
        this.tableName = config.getTable();
    }

    private String keyField;

    @Override
    public <T> DaoShortLivedMap<T> getDaoShortLivedMap(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> DaoShortLivedMap<T> getDaoShortLivedMap() {
        throw new UnsupportedOperationException();
    }

    AmazonDynamoDBClient getClient() {
        return client;
    }

    @Override
    public <T> DaoCacheLoader<T> getDaoCacheLoader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> DaoCache<T> getDaoCache(DaoCacheLoader<T> loader, T nullValue, long ttl,
                    long maxItems) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DaoCounters getDaoCounters() {
        throw new UnsupportedOperationException();
    }

    private DynamoDBDaoMapOfUserAttributes daoMapOfUserAttributes = null;

    @Override
    public synchronized DaoMapOfUserAttributes getDaoMapOfUserAttributes() {
        if (daoMapOfUserAttributes == null) {
            daoMapOfUserAttributes = new DynamoDBDaoMapOfUserAttributes(this);
        }
        return daoMapOfUserAttributes;
    }

    private DynamoDBDaoMapOfUserSegments daoMapOfUserSegments = null;

    @Override
    public synchronized DaoMapOfUserSegments getDaoMapOfUserSegments() {
        if (daoMapOfUserSegments == null) {
            daoMapOfUserSegments = new DynamoDBDaoMapOfUserSegments(this);
        }
        return daoMapOfUserSegments;
    }
}
