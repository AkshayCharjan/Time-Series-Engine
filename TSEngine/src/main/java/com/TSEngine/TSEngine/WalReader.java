package com.TSEngine.TSEngine;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

public final class WalReader {
    private static final OpenOption READ = StandardOpenOption.READ;
    private final Path walDir;

    public WalReader(Path walDir) {
        this.walDir = walDir;
    }

    public void replay(Consumer<WalRecord> consumer) throws IOException {
        if (!Files.exists(walDir)) return;

        try (var stream = Files.list(walDir)) {
            stream
                .filter(p -> p.getFileName().toString().endsWith(".log"))
                .sorted()
                .forEach(p -> {
                    try {
                        replayFile(p, consumer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        }
    }

    private void replayFile(Path file, Consumer<WalRecord> consumer) throws IOException {
        try (FileChannel fc = FileChannel.open(file, READ);
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

                if (WalCodec.crc32(payload) != crc) break;

                try (DataInputStream pin = new DataInputStream(new ByteArrayInputStream(payload))) {
                    WalRecord r = WalCodec.deserialize(pin);
                    consumer.accept(r);
                }
            }
        }
    }
}

