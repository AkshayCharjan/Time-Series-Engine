package com.TSEngine.TSEngine;

import java.util.Map;

public interface TimeSeriesService {
    boolean insert(long timestamp, String metric, double value, Map<String, String> tags);
}
