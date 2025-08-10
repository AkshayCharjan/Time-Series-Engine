package com.TSEngine.TSEngine;

import java.util.List;
import java.util.Map;

public interface TimeSeriesService {
    boolean insert(long timestamp, String metric, double value, Map<String, String> tags);
    public List<DataPoint> query(String metric, long timeStart, long timeEnd, Map<String,String> filters);
}
