package gew.nodemanager.server.config;

import gew.nodemanager.server.NodeManagerServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Jason/GeW
 */
@Configuration
@EnableSwagger2
@Profile({"dev", "test"})
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("gew.nodemanager.server.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Node Manager Server")
                .description("Distributed System Node Management Service")
                .termsOfServiceUrl("https://github.com/Jason-Gew/Java_Modules/blob/master/LICENSE")
                .contact(new Contact("Jason/GeW", "https://github.com/Jason-Gew", "jason.ge.wu@gmail.com"))
                .version(NodeManagerServer.VERSION)
                .license("Apache License 2.0")
                .build();
    }
}