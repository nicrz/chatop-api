package com.openclassrooms.chatop.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
  type = SecuritySchemeType.HTTP,
  bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfiguration {

  @Bean
  public GroupedOpenApi api() {
    return GroupedOpenApi.builder()
      .group("app")
      .pathsToMatch("/**")
      .build();
  }

 
}
