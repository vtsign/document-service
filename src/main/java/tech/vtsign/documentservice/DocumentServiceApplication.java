package tech.vtsign.documentservice;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class DocumentServiceApplication {
    @Value("${tech.vtsign.api-url}")
    private String apiUrl;
    @Value("${server.servlet.context-path}")
    private String path;

    public static void main(String[] args) {
        SpringApplication.run(DocumentServiceApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info().title("Document Service API")
                        .description("API documentation Document Service")
                        .version(appVersion)
                )
                .addServersItem(new Server().url(apiUrl+ path));
    }
}
