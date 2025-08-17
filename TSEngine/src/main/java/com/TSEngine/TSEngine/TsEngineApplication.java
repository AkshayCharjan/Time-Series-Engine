package com.TSEngine.TSEngine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class TsEngineApplication {

	public static void main(String[] args) {

//		SpringApplication.run(TsEngineApplication.class, args);
		TimeSeriesServiceImpl timeSeriesServiceImpl = new TimeSeriesServiceImpl();

		long now = System.currentTimeMillis();

		Map<String,String> tags1 = new HashMap<>();
		tags1.put("host", "server1");
		tags1.put("dc", "us-west");

		Map<String, String> tags2 = new HashMap<>();
		tags2.put("host", "server2");
		tags2.put("dc", "us-east");

		timeSeriesServiceImpl.insert(now,"cpu.usage", 67.5, tags1);
		timeSeriesServiceImpl.insert(now , "cpu.usage", 69.2, tags1);
		timeSeriesServiceImpl.insert(now, "memory.used", 512.0, tags1);

		timeSeriesServiceImpl.printAllData();

		List<DataPoint> results = timeSeriesServiceImpl.query("cpu.usage", now, now+5000,
				Map.of("host", "server1"));
		
		for (DataPoint dp : results) {
			System.out.println(dp);
		}
	}

}
