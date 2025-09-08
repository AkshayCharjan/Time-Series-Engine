package com.TSEngine.TSEngine;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
        openNewSegment();
    }

    public void openNewSegment() throws IOException {
        String tmp = String.format("wal-%06d.log.tmp", segmentIndex);
        String fin = String.format("wal-%06d.log",segmentIndex);
        currentPath = walConfig.walDir.resolve(tmp);
        ch = FileChannel.open(currentPath, CREATE, WRITE, APPEND);
        currentSize = ch.size();

        Path finalPath = walConfig.walDir.resolve(fin);
        Files.move(currentPath, finalPath, StandardCopyOption.ATOMIC_MOVE);
        currentPath = finalPath;
    }



}
