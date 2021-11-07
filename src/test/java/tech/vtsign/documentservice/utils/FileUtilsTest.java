package tech.vtsign.documentservice.utils;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tech.vtsign.documentservice.repository.UserDocumentRepository;

import java.util.Arrays;

@SpringBootTest
@RequiredArgsConstructor
public class FileUtilsTest {
    private final UserDocumentRepository userDocumentRepository;

    @Test
    void testReadFileFromURL() {
        String url = "https://vtsign.blob.core.windows.net/user/0f8a4898-62ae-4322-9365-a09290a52573/0c88a565-87bd-4827-bc4a-9ad29629308a";
        byte[] data = FileUtil.readByteFromURL(url);
        System.out.println(Arrays.toString(data));
        Assertions.assertThat(data).isNotEmpty();
    }

//    @Test
//    void testFindDigitalSignatureByIdAndStatus() {
//        List<UserContract> userContractList = userDocumentRepository.findByUserUUIDAndStatus(UUID.fromString("1234"), "SEND");
//        System.out.println(userContractList);
//        assertThat(userContractList.size()).isGreaterThan(0);
//    }
}
