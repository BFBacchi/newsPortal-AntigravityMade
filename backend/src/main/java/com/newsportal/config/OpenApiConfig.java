package com.newsportal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI newsPortalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NewsPortal API")
                        .description("API Documentation for Automated News Portal")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NewsPortal Team")
                                .email("support@newsportal.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
