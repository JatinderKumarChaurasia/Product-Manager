package com.jkc.microservices.composite.product.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Response;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@ComponentScan(basePackages = {"com.jkc.microservices.composite.product"})
@EnableSwagger2
public class SwaggerUIConfig {

    @Value("${api.common.version}")
    String apiVersion;
    @Value("${api.common.title}")
    String apiTitle;
    @Value("${api.common.description}")
    String apiDescription;
    @Value("${api.common.termsOfServiceUrl}")
    String apiTermsOfServiceUrl;
    @Value("${api.common.license}")
    String apiLicense;
    @Value("${api.common.licenseUrl}")
    String apiLicenseUrl;
    @Value("${api.common.contact.name}")
    String apiContactName;
    @Value("${api.common.contact.url}")
    String apiContactUrl;
    @Value("${api.common.contact.email}")
    String apiContactEmail;

    @Bean
    public Docket apiDocumentation() {
        final List<Response> responses = new ArrayList<>();
        responses.add(new ResponseBuilder().code("200").description("OK").build());
        responses.add(new ResponseBuilder().code("404").description("Not Found: server can not find the requested resource").build());
        responses.add(new ResponseBuilder().code("400").description("Bad Request: The server could not understand the request due to invalid syntax.").build());
        responses.add(new ResponseBuilder().code("401").description("Unauthorized: client must authenticate itself to get the requested response.").build());
        responses.add(new ResponseBuilder().code("403").description("Forbidden: not have access rights to the content").build());
        responses.add(new ResponseBuilder().code("405").description("Method Not Allowed: request method is known by the server but has been disabled and cannot be used").build());
        responses.add(new ResponseBuilder().code("406").description("Not Acceptable: This response is sent when the web server, after performing server-driven content negotiation, doesn't find any content that conforms to the criteria given by the user agent.").build());
        responses.add(new ResponseBuilder().code("408").description("Request Timeout: This response is sent on an idle connection by some servers, even without any previous request by the client.").build());
        responses.add(new ResponseBuilder().code("422").description("Unprocessable Entity: The request was well-formed but was unable to be followed due to semantic errors.").build());
        responses.add(new ResponseBuilder().code("500").description("Internal Server Error: The server has encountered a situation it doesn't know how to handle.").build());
        responses.add(new ResponseBuilder().code("501").description("Not Implemented: The request method is not supported by the server and cannot be handled.").build());
        responses.add(new ResponseBuilder().code("502").description("Bad Gateway: The server got an invalid response.").build());
        responses.add(new ResponseBuilder().code("503").description("Service Unavailable: The server is not ready to handle the request and under maintenance.").build());
        responses.add(new ResponseBuilder().code("504").description("Gateway Timeout: The server as a gateway cannot get a response in time.").build());
        responses.add(new ResponseBuilder().code("505").description("HTTP Version Not Supported: The HTTP version used in the request is not supported by the server.").build());

        return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors
                .basePackage("com.jkc.microservices.composite.product"))
                .paths(PathSelectors.any()).build()
                .globalResponses(HttpMethod.GET, responses)
                .globalResponses(HttpMethod.POST, responses)
                .globalResponses(HttpMethod.DELETE, responses)
                .apiInfo(new ApiInfo(
                        apiTitle, apiDescription, apiVersion, apiTermsOfServiceUrl, new Contact(apiContactName, apiContactUrl, apiContactEmail), apiLicense, apiLicenseUrl, Collections.emptyList()
                )).enable(true);
    }
}
