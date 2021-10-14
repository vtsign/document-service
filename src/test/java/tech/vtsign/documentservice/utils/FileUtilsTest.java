package tech.vtsign.documentservice.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
public class FileUtilsTest {
    @Test
    void testReadFileFromURL() {
        String url = "https://vtsign.blob.core.windows.net/user/0f8a4898-62ae-4322-9365-a09290a52573/0c88a565-87bd-4827-bc4a-9ad29629308a";
        byte[] data = FileUtil.readByteFromURL(url);
        System.out.println(Arrays.toString(data));
        Assertions.assertThat(data).isNotEmpty();
    }
}
