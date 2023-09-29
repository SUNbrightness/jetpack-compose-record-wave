package com.example.record_wave.utils;

import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/* loaded from: classes.dex */
public class WaveRecorder {
    public static String logfile;
    private static BufferedOutputStream outputStream;

    public static int open(String str) {
        Log.i("WaveRecorder", "OPEN:" + str);
        logfile = str;
        try {
            File file = new File(logfile);
            if (file.exists()) {
                file.delete();
            }
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }



    public static void close() {
        Log.i("WaveRecorder", "close -====");
        BufferedOutputStream bufferedOutputStream = outputStream;
        if (bufferedOutputStream == null || bufferedOutputStream == null) {
            return;
        }
        try {
            bufferedOutputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeShort(short s) throws IOException {
        BufferedOutputStream bufferedOutputStream = outputStream;
        if (bufferedOutputStream != null) {
            bufferedOutputStream.write(s >> 0);
            outputStream.write(s >> 8);
        }
    }

    public static void write(int i, short[] sArr) {
        if (outputStream == null) {
            return;
        }
        for (int i2 = 0; i2 < i; i2++) {
            try {
                writeShort(sArr[i2]);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}