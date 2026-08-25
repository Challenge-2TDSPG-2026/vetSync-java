package br.com.fiap.VetSync.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "JornadaPet API",
                description = "API para continuidade do cuidado e engajamento na jornada de saúde do pet — Clyvo Vet Challenge 2026",
                version = "1.0"
        )
)
public class SwaggerConfig {
}