package com.TSEngine.TSEngine;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class TimeSeriesBenchmark {

    private final TimeSeriesService service;
    private final Random random;
    private final String[] metrics = {"cpu.usage", "memory.used", "disk.io", "network.in", "network.out"};
    private final String[] hosts = {"server1", "server2", "server3", "server4", "server5"};
    private final String[] datacenters = {"us-west", "us-east", "eu-west", "ap-south"};

    public TimeSeriesBenchmark(TimeSeriesService service) {
        this.service = service;
        this.random = new Random(42);
    }

    public void benchmarkInsert() throws InterruptedException {
        System.out.println("\n--- Insert Performance ---");
        
        // Single-threaded
        System.out.println("Single-threaded (100K records):");
        long start = System.nanoTime();
        long baseTs1 = System.currentTimeMillis();
        
        for (int i = 0; i < 100_000; i++) {
            service.insert(baseTs1 + i, randomMetric(), randomValue(), generateTags());
        }
        
        double duration = (System.nanoTime() - start) / 1e9;
        double throughput = 100_000 / duration;
        System.out.println("  Throughput: " + String.format("%.0f", throughput) + " writes/sec");
        
        // Multi-threaded
        System.out.println("Multi-threaded (4 threads, 100K records each):");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        start = System.nanoTime();
        final long baseTs2 = System.currentTimeMillis();
        
        for (int t = 0; t < 4; t++) {
            int threadId = t;
            executor.submit(() -> {
                for (int i = 0; i < 100_000; i++) {
                    service.insert(
                        baseTs2 + (threadId * 100_000) + i,
                        randomMetric(),
                        randomValue(),
                        generateTags()
                    );
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        
        duration = (System.nanoTime() - start) / 1e9;
        throughput = 400_000 / duration;
        System.out.println("  Throughput: " + String.format("%.0f", throughput) + " writes/sec");
    }

    public void benchmarkQuery() {
        System.out.println("\n--- Query Performance ---");
        
        long baseTimestamp = System.currentTimeMillis();
        
        // Preload
        System.out.println("Preloading 100K records...");
        for (int i = 0; i < 100_000; i++) {
            service.insert(baseTimestamp + i, randomMetric(), randomValue(), generateTags());
        }
        
        // Range query
        System.out.println("Range query (1000 runs):");
        long[] latencies = new long[1000];
        
        for (int i = 0; i < 1000; i++) {
            long queryStart = System.nanoTime();
            service.query("cpu.usage", baseTimestamp, baseTimestamp + 100_000, null);
            latencies[i] = (System.nanoTime() - queryStart) / 1_000;
        }
        
        Arrays.sort(latencies);
        System.out.println("  P50: " + latencies[500] + " µs");
        System.out.println("  P99: " + latencies[990] + " µs");
        System.out.println("  Avg: " + String.format("%.0f", Arrays.stream(latencies).average().orElse(0)) + " µs");
        
        // Filtered query
        System.out.println("Filtered query (1000 runs):");
        for (int i = 0; i < 1000; i++) {
            long queryStart = System.nanoTime();
            service.query("cpu.usage", baseTimestamp, baseTimestamp + 100_000, 
                         Map.of("host", hosts[random.nextInt(hosts.length)]));
            latencies[i] = (System.nanoTime() - queryStart) / 1_000;
        }
        
        Arrays.sort(latencies);
        System.out.println("  P50: " + latencies[500] + " µs");
        System.out.println("  P99: " + latencies[990] + " µs");
        System.out.println("  Avg: " + String.format("%.0f", Arrays.stream(latencies).average().orElse(0)) + " µs");
    }

    public void benchmarkScalability() {
        System.out.println("\n--- Scalability ---");
        
        for (int volume : new int[]{100_000, 500_000, 1_000_000}) {
            long start = System.nanoTime();
            long baseTimestamp = System.currentTimeMillis();
            
            for (int i = 0; i < volume; i++) {
                service.insert(baseTimestamp + i, randomMetric(), randomValue(), generateTags());
            }
            
            double duration = (System.nanoTime() - start) / 1e9;
            double throughput = volume / duration;
            
            System.out.println(String.format("%,d records: %.0f writes/sec", volume, throughput));
        }
    }

    public void runAllBenchmarks() throws InterruptedException {
        System.out.println("\n--- TimeSeriesEngine Benchmarks ---");

        benchmarkInsert();
        benchmarkQuery();
        benchmarkScalability();

        System.out.println("\n--- Complete ---\n");
    }

    private String randomMetric() {
        return metrics[random.nextInt(metrics.length)];
    }

    private double randomValue() {
        return random.nextDouble() * 100;
    }

    private Map<String, String> generateTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put("host", hosts[random.nextInt(hosts.length)]);
        tags.put("dc", datacenters[random.nextInt(datacenters.length)]);
        return tags;
    }

    public static void main(String[] args) throws Exception {
        Path walDir = Path.of("data/wal-bench");
        WalConfig cfg = WalConfig.defaultDurable(walDir);
        WalWriter writer = new WalWriter(cfg);
        TimeSeriesService service = new TimeSeriesServiceImpl(writer);

        TimeSeriesBenchmark benchmark = new TimeSeriesBenchmark(service);
        benchmark.runAllBenchmarks();

        writer.close();
    }
}
