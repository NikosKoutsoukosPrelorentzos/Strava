package org.example.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class Utils {
    public static int getRandomPort() {
        Random random = new Random();
        return random.nextInt(65535 - 49152) + 49152;
    }

    public static byte[] gpxArray(File file) throws IOException {
        FileInputStream fileIn = new FileInputStream(file);
        byte[] fileArray = Files.readAllBytes(file.toPath());
        fileIn.close();
        return fileArray;
    }
}