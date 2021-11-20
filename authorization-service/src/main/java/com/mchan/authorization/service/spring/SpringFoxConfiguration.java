package com.mchan.authorization.service.spring;

import io.swagger.annotations.SwaggerDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * http://localhost:8094/auth-hub/swagger-ui/index.html.
 * http://localhost:8094/auth-hub/v2/api-docs
 */
@SwaggerDefinition(schemes = SwaggerDefinition.Scheme.HTTP)
@EnableSwagger2
@Configuration
public class SpringFoxConfiguration {

    /**
     * .
     *
     * @return .
     */
    @Bean
    public Docket api() {
        List<SecurityScheme> securitySchemes = new ArrayList<>();
        securitySchemes.add(new BasicAuth("basic"));

        return new Docket(DocumentationType.SWAGGER_2)
            .securitySchemes(securitySchemes)
            .forCodeGeneration(true)
            .protocols(Set.of("http"))
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build();
    }

}
