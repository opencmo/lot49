package com.enremmeta.rtb.config;

public class DynamoDBServiceConfig extends DbConfig {

    public DynamoDBServiceConfig() {
        // TODO Auto-generated constructor stub
    }

    private String table;

    private String storeAs = "json";

    public static final int DEFAULT_POOL_SIZE = 256;

    private int poolSize = DEFAULT_POOL_SIZE;

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getStoreAs() {
        return storeAs;
    }

    public void setStoreAs(String storeAs) {
        this.storeAs = storeAs;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    private String keyField;

    public String getKeyField() {
        return keyField;
    }

    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }

    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }

    private String awsAccessKey;
    private String awsSecretKey;

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }
}
