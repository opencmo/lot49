package com.enremmeta.rtb.caches;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public interface DynamoDBDecoder<T> {
    T decode(Map<String, AttributeValue> map);
}
