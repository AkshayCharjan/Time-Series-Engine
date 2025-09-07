package com.TSEngine.TSEngine;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


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

}