package sentiments;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import com.google.common.base.Predicates;

@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurationSupport {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                //.apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework.boot")))
                .apis(RequestHandlerSelectors.basePackage("sentiments.ApplicationController"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET, newArrayList(new ResponseMessageBuilder().code(500)
                                .message("500 message")
                                .responseModel(new ModelRef("Error"))
                                .build(),
                        new ResponseMessageBuilder().code(403)
                                .message("Forbidden!!!!!")
                                .build()));


    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo("Sentiment 2019", "Eine API zur Gefühlsanalyse von Tweets", "1.0", "", new Contact("", "", ""), "", "", Collections.emptyList());
        return apiInfo;
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}