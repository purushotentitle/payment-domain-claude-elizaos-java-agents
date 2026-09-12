package com.example.payments.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI paymentAgentOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Domain Agent API")
                        .version("1.0.0")
                        .description("Java backend APIs for Claude-assisted, approval-gated payment agents."));
    }
}
