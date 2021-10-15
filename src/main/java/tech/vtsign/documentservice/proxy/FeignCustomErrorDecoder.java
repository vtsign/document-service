package tech.vtsign.documentservice.proxy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import tech.vtsign.documentservice.exception.RequestException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FeignCustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        //START DECODING ORIGINAL ERROR MESSAGE
        RequestException requestException;
        Reader reader = null;
        //capturing error message from response body.
        try {
            reader = response.body().asReader(StandardCharsets.UTF_8);
            String result = IOUtils.toString(reader);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            requestException = mapper.readValue(result, RequestException.class);
        } catch (IOException e) {
            log.error("IO Exception on reading exception message feign client" + e);
            throw new RuntimeException("Internal error");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("IO Exception on reading exception message feign client" + e);
            }
        }

        return requestException;
    }

}