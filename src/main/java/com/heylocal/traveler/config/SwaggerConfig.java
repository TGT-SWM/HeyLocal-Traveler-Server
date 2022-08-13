package com.heylocal.traveler.config;

import com.fasterxml.classmate.TypeResolver;
import com.heylocal.traveler.dto.ErrorMessageResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.time.LocalDate;
import java.time.LocalDateTime;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-08-12T04:12:44.357Z[GMT]")
@Configuration
public class SwaggerConfig {

  @Bean
  public Docket customImplementation(TypeResolver typeResolver){
    return new Docket(DocumentationType.OAS_30)
        .additionalModels(
            typeResolver.resolve(ErrorMessageResponse.class)
        )
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.heylocal.traveler.controller"))
        .build()
        .directModelSubstitute(LocalDate.class, java.sql.Date.class)
        .directModelSubstitute(LocalDateTime.class, java.util.Date.class)
        .apiInfo(apiInfo())
        .tags(
            new Tag("Signup", "회원가입", 1),
            new Tag("Signin", "로그인", 2),
            new Tag("User", "사용자", 3),
            new Tag("OrderSheets", "여행 의뢰서", 4),
            new Tag("Orders", "매칭", 5),
            new Tag("Travels", "여행", 6),
            new Tag("Managers", "매니저", 7),
            new Tag("Posts", "포스트", 8),
            new Tag("Search", "검색", 9),
            new Tag("ChatRooms", "채팅방", 10),
            new Tag("Policies", "정책", 11)
        );
  }

  ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("현지야")
        .description("현지야 서버 Application API Document")
        .license("Apache 2.0")
        .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
        .termsOfServiceUrl("")
        .version("1.0.0")
        .contact(new Contact("","", "dnxprbs@gmail.com"))
        .build();
  }

  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
        .info(new Info()
            .title("현지야")
            .description("현지야 서버 Application API Document")
            .termsOfService("")
            .version("1.0.0")
            .license(new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0.html"))
            .contact(new io.swagger.v3.oas.models.info.Contact()
                .email("dnxprbs@gmail.com")))
            .components(new Components());
  }

}