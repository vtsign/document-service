package tech.vtsign.documentservice.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class FileUtil {
    public static boolean writeByte(String filePath, byte[] bytes) {
        try {
            Files.write(Paths.get(filePath), bytes);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static byte[] readByte(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] readByteFromURL(String link) {
        try {
            URL url = new URL(link);
            InputStream inputStream = url.openStream();
            return inputStream.readAllBytes();
        } catch (IOException e) {
            log.error("Invalid file link {}", link);
            e.printStackTrace();
            return null;
        }
    }

}
