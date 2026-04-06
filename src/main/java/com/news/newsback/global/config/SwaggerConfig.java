package com.news.newsback.global.config;
// Swagger : API 문서 자동화

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class SwaggerConfig {

	@Bean
	public OpenAPI newsBackOpenApi() {
		return new OpenAPI().info(new Info()
			.title("NewsBack API")
			.description("NewsBack 인증/사용자 API 문서")
			.version("v1"));
	}
}
