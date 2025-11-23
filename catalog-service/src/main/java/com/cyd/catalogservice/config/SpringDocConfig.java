package com.cyd.catalogservice.config;


import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
@Configuration
public class SpringDocConfig {
    @Bean
    public OpenAPI campusCourseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园选课系统 API 文档")
                        .description("单体版校园选课系统的接口说明，包含课程、学生、选课管理")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("项目说明文档")
                        .url("https://github.com/anhechangming/Course/tree/main/README.md"));
    }
}