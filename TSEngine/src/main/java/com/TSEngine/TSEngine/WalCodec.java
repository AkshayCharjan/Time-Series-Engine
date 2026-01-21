package com.TSEngine.TSEngine;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;


public class WalCodec {
    private WalCodec() {};

    public static byte[] serialize(WalRecord walRecord) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        DataOutputStream dataOutputStream = new DataOutputStream(baos);
        dataOutputStream.writeLong(walRecord.timestamp);
        writeString(dataOutputStream ,walRecord.metric);
        dataOutputStream.writeDouble(walRecord.value);
        dataOutputStream.writeInt(walRecord.tags.size());
        for(var e : walRecord.tags.entrySet()){
            writeString(dataOutputStream, e.getKey());
            writeString(dataOutputStream, e.getValue());
        }
        dataOutputStream.flush();
        return baos.toByteArray();
    }

    public static void writeString(DataOutput out, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    public static WalRecord deserialize(DataInput in) throws IOException {
        long ts = in.readLong();
        String metric = readString(in);
        double val = in.readDouble();

        int n = in.readInt();
        Map<String, String> tags = (n == 0) ? Map.of() : new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            tags.put(readString(in), readString(in));
        }

        return new WalRecord(ts, metric, val, tags);
    }
    public static int crc32(byte[] payload) {
        CRC32 c = new CRC32();
        c.update(payload);
        return (int) c.getValue(); // fits in 32 bits
    }

    private static String readString(DataInput in) throws IOException {
        int len = in.readInt();
        byte[] b = new byte[len];
        in.readFully(b);
        return new String(b, StandardCharsets.UTF_8);
    }
}