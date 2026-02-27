package com.katya.quoterestapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        Schema<?> tokenRequestSchema = new ObjectSchema()
                .addProperty("client_id", new StringSchema().example("quote-api"))
                .addProperty("grant_type", new StringSchema().example("password"))
                .addProperty("username", new StringSchema().example("api-user"))
                .addProperty("password", new StringSchema().example("password"));

        Operation tokenOperation = new Operation()
                .summary("Get access token (Keycloak)")
                .description("Keycloak token endpoint. Use application/x-www-form-urlencoded.")
                .requestBody(new RequestBody()
                        .required(true)
                        .content(new Content().addMediaType(
                                "application/x-www-form-urlencoded",
                                new io.swagger.v3.oas.models.media.MediaType().schema(tokenRequestSchema)
                        ))
                )
                .responses(new ApiResponses().addApiResponse("200", new ApiResponse().description("Token response")))
                .servers(java.util.List.of(new Server().url("http://localhost:8081/realms/quote/protocol/openid-connect")));

        Paths paths = new Paths().addPathItem("/token", new PathItem().post(tokenOperation));

        return new OpenAPI()
                .info(new Info().title("Quote REST API").version("v1"))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .paths(paths);
    }
}
