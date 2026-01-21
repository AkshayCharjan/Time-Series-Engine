package com.TSEngine.TSEngine;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class WalDumper {

    public static void dumpFile(Path file) throws IOException {
        if (!Files.exists(file)) {
            System.out.println("File not found: " + file);
            return;
        }

        System.out.println("=== WAL Log: " + file.getFileName() + " ===");
        System.out.println();

        int recordCount = 0;

        try (FileChannel fc = FileChannel.open(file, StandardOpenOption.READ);
             DataInputStream in = new DataInputStream(Channels.newInputStream(fc))) {

            while (true) {
                int len;
                try {
                    len = in.readInt();
                } catch (EOFException eof) {
                    break;
                }

                int crc = in.readInt();
                byte[] payload = in.readNBytes(len);
                if (payload.length < len) break;

                int calculatedCrc = (int) WalCodec.crc32(payload);
                boolean crcValid = calculatedCrc == crc;

                System.out.println("Record #" + (recordCount + 1));
                System.out.println("  Length: " + len + " bytes");
                System.out.println("  CRC: " + crc + " (calculated: " + calculatedCrc + ", " + (crcValid ? "VALID" : "INVALID") + ")");

                if (!crcValid) {
                    System.out.println("  [CRC mismatch - data may be corrupted]");
                    break;
                }

                try {
                    WalRecord rec = WalCodec.deserialize(new java.io.DataInputStream(
                            new java.io.ByteArrayInputStream(payload)));
                    
                    System.out.println("  Timestamp: " + rec.timestamp);
                    System.out.println("  Metric: " + rec.metric);
                    System.out.println("  Value: " + rec.value);
                    System.out.println("  Tags: " + formatTags(rec.tags));
                    System.out.println();

                    recordCount++;
                } catch (Exception e) {
                    System.out.println("  [Error deserializing record: " + e.getMessage() + "]");
                    System.out.println();
                }
            }
        }

        System.out.println("=== Total Records: " + recordCount + " ===");
    }

    public static void dumpDir(Path walDir) throws IOException {
        if (!Files.exists(walDir)) {
            System.out.println("Directory not found: " + walDir);
            return;
        }

        System.out.println("=== WAL Directory: " + walDir + " ===");
        System.out.println();

        try (var stream = Files.list(walDir)) {
            stream
                .filter(p -> p.getFileName().toString().endsWith(".log"))
                .sorted()
                .forEach(p -> {
                    try {
                        dumpFile(p);
                        System.out.println("\n" + "=".repeat(60) + "\n");
                    } catch (IOException e) {
                        System.err.println("Error reading " + p + ": " + e.getMessage());
                    }
                });
        }
    }

    private static String formatTags(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        tags.forEach((k, v) -> sb.append(k).append("=").append(v).append(", "));
        sb.setLength(sb.length() - 2); // Remove last ", "
        sb.append("}");
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        Path path = Path.of("data/wal");

        if (Files.isDirectory(path)) {
            dumpDir(path);
        } else {
            dumpFile(path);
        }
    }
}
