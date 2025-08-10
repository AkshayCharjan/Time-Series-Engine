package com.TSEngine.TSEngine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TimeSeriesServiceImpl implements TimeSeriesService {

    private final Map<String, ConcurrentSkipListMap<Long, List<DataPoint>>> metricMap = new ConcurrentHashMap<>();

    @Override
    public boolean insert(long timestamp, String metric, double value, Map<String, String> tags){
        DataPoint dp = new DataPoint(timestamp, metric, value, tags);

        ConcurrentSkipListMap<Long, List<DataPoint>> series =
                metricMap.computeIfAbsent(metric, m -> new ConcurrentSkipListMap<>());

        series.compute(timestamp,(ts,existingList) -> {
            if(existingList == null){
                List <DataPoint> newList = new CopyOnWriteArrayList<>();
                newList.add(dp);
                return newList;
            }
            else{
                existingList.add(dp);
                return existingList;
            }
                });

        return false;
    }

    @Override
    public List<DataPoint> query(String metric, long timeStart, long timeEnd, Map<String, String> filters) {
        ConcurrentSkipListMap<Long, List<DataPoint>> series = metricMap.get(metric);
        if(series == null) return Collections.emptyList();

        NavigableMap<Long, List<DataPoint>> range = series.subMap(timeStart,true, timeEnd, false);
        List<DataPoint> results = new ArrayList<>();
        for(List<DataPoint> dataPoints: range.values()){
            for(DataPoint dp : dataPoints){
                if(filters == null || filters.isEmpty() || dp.getTags().entrySet().containsAll(filters.entrySet())){
                    results.add(dp);
                }
            }
        }

        return results;
    }

    public void printAllData(){
        for(Map.Entry<String, ConcurrentSkipListMap<Long, List<DataPoint>>> entry: metricMap.entrySet()){
            String metric = entry.getKey();
            ConcurrentSkipListMap<Long, List<DataPoint>> series = entry.getValue();
            System.out.println("Metric: " + metric);
            for(Map.Entry<Long, List<DataPoint>> dataPointEntry: series.entrySet()){
                Long timestamp = dataPointEntry.getKey();
                List<DataPoint> dataPoints = dataPointEntry.getValue();
                System.out.println("  Timestamp: " + timestamp);
                for(DataPoint dp: dataPoints){
                    System.out.println("    "+dp);
                }
            }
        }
    }

}
