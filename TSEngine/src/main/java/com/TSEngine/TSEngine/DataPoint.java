package com.TSEngine.TSEngine;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DataPoint {
    private final long timestamp;
    private final String metric;
    private final double value;
    private final Map<String, String> tags;

    public DataPoint(long timestamp, String metric, double value, Map<String, String> tags) {
        this.timestamp = timestamp;
        this.metric = metric;
        this.value = value;
        this.tags = tags != null ? new HashMap<>(tags) : new HashMap<>();
    }
}
