package com.news.newsback.global.config;
// Swagger : API 문서 자동화

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("dev")
public class SwaggerConfig {

	@Value("${server.servlet.context-path:}")
	private String contextPath;

	@Bean
	public OpenAPI newsBackOpenApi() {
		String jwtSchemeName = "jwtAuth";
		SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
		Components components = new Components()
			.addSecuritySchemes(jwtSchemeName, new SecurityScheme()
				.name(jwtSchemeName)
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT"));

		return new OpenAPI()
			.info(new Info()
				.title("BRIEFY API")
				.description("BRIEFY 인증/사용자 API 문서")
				.version("v1"))
			.servers(List.of(new Server()
				.url(contextPath == null || contextPath.isBlank() ? "/" : contextPath)
				.description("BRIEFY API Server")))
			.components(components);
	}
}
