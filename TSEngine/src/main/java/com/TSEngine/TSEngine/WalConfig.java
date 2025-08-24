package com.TSEngine.TSEngine;

import java.nio.file.Path;

public final class WalConfig {
    public final Path walDir;
    public final long maxSegmentBytes;
    public final WalSyncMode walSyncMode;
    public final long fsyncEveryMillis;
    public final long fsyncEveryBytes;

    public WalConfig(Path walDir, long maxSegmentBytes,
                     WalSyncMode walSyncMode, long fsyncEveryMillis, long fsyncEveryBytes) {
        this.walDir = walDir;
        this.maxSegmentBytes = maxSegmentBytes;
        this.walSyncMode = walSyncMode;
        this.fsyncEveryMillis = fsyncEveryMillis;
        this.fsyncEveryBytes = fsyncEveryBytes;
    }

    public static WalConfig defaultDurable(Path dir) {
        return new WalConfig(dir, 256L << 20, WalSyncMode.ALWAYS, 0, 0);
    }
}
