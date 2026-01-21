package com.TSEngine.TSEngine;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class TsEngineApplication {

    public static void main(String[] args) throws Exception {

        Path walDir = Path.of("data/wal");
        WalConfig cfg = WalConfig.defaultDurable(walDir);

        TimeSeriesServiceImpl mem = new TimeSeriesServiceImpl();

        WalReader reader = new WalReader(walDir);
        reader.replay(mem::replayInsert);

        WalWriter writer = new WalWriter(cfg);

        TimeSeriesService service = new TimeSeriesServiceImpl(writer);

        long now = System.currentTimeMillis();

        Map<String, String> tags1 = new HashMap<>();
        tags1.put("host", "server1");
        tags1.put("dc", "us-west");

        Map<String, String> tags2 = new HashMap<>();
        tags2.put("host", "server2");
        tags2.put("dc", "us-east");

        service.insert(now, "cpu.usage", 67.5, tags1);
        service.insert(now + 1000, "cpu.usage", 69.2, tags1);
        service.insert(now + 2000, "memory.used", 512.0, tags2);

        ((TimeSeriesServiceImpl) service).printAllData();

        List<DataPoint> results = service.query(
                "cpu.usage",
                now,
                now + 5000,
                Map.of("host", "server1")
        );

        for (DataPoint dp : results) {
            System.out.println(dp);
        }
    }
}

