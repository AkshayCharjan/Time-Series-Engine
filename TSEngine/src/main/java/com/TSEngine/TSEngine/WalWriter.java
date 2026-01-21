package com.TSEngine.TSEngine;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;

import static java.nio.file.StandardOpenOption.*;

public class WalWriter{
    private final WalConfig walConfig;
    private int segmentIndex = 1;
    private Path currentPath;
    private FileChannel ch;
    private long currentSize;


    public WalWriter(WalConfig walConfig) throws IOException {
        this.walConfig = walConfig;
        Files.createDirectories(walConfig.walDir);
        initFromDisk();
    }

    private void initFromDisk() throws IOException {
        if (!Files.exists(walConfig.walDir)) {
            openNewSegment();
            return;
        }

        try (var s = Files.list(walConfig.walDir)) {
            OptionalInt max = s
                .map(p -> p.getFileName().toString())
                .filter(n -> n.startsWith("wal-") && n.endsWith(".log"))
                .mapToInt(n -> Integer.parseInt(n.substring(4, 10)))
                .max();

            if (max.isPresent()) {
                segmentIndex = max.getAsInt();
                currentPath = walConfig.walDir.resolve(
                    String.format("wal-%06d.log", segmentIndex)
                );
                ch = FileChannel.open(currentPath, CREATE, WRITE, APPEND);
                currentSize = ch.size();
                return;
            }
        }

        openNewSegment();
    }

    public void openNewSegment() throws IOException {
        String fin = String.format("wal-%06d.log", segmentIndex);
        currentPath = walConfig.walDir.resolve(fin);
        ch = FileChannel.open(currentPath, CREATE, WRITE, APPEND);
        currentSize = ch.size();
    }
    
    public synchronized void append(WalRecord rec) throws IOException {
        byte[] payload = WalCodec.serialize(rec);

        int crc = WalCodec.crc32(payload);
        int len = payload.length;

        writeInt(len);
        writeInt(crc);
        writeBytes(payload);

        currentSize += 8L + len;

        if (currentSize >= walConfig.maxSegmentBytes) {
            rotate();
        }
    }


    private void rotate() throws IOException {
        ch.force(true);
        ch.close();
        segmentIndex++;
        openNewSegment();
    }

    private void writeInt(int v) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(v).flip();
        while (buf.hasRemaining()) {
            ch.write(buf);
        }
    }

    private void writeBytes(byte[] bytes) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        while (buf.hasRemaining()) {
            ch.write(buf);
        }
    }

    public void close() throws IOException {
        if (ch != null) {
            ch.force(true);
            ch.close();
        }
    }
}
