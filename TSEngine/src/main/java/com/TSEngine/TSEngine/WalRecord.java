package com.TSEngine.TSEngine;

import java.util.Map;

public final class WalRecord {
    public final long timestamp;
    public final String metric;
    public final double value;
    public final Map<String, String> tags; // can be empty

    public WalRecord(long ts, String metric, double value, Map<String,String> tags) {
        this.timestamp = ts;
        this.metric = metric;
        this.value = value;
        this.tags = (tags == null ? Map.of() : tags);
    }
}