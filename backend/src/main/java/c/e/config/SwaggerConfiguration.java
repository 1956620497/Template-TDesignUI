package c.e.config;

import c.e.entity.RestBean;
import c.e.entity.vo.response.AuthorizeVO;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Swagger开发文档相关配置
 * &#064;SecurityScheme   定义安全方案，说明API的认证方式
 * type = SecuritySchemeType.HTTP   指定认证类型为HTTP
 * scheme = "Bearer"   指认证方式为Bearer token
 * name = "Authorization"   指定HTTP请求头中的认证字段名称为"Authorization"
 * in = SecuritySchemeIn.HEADER   指定Bearer token会放在HTTP头部。
 * &#064;OpenAPIDefinition   定义全局API文档配置
 * security =  { @SecurityRequirement(name="Authorization")}  全局要求所有API请求都带有"Authorization"认证信息
 */
@Configuration
@SecurityScheme(type = SecuritySchemeType.HTTP,scheme = "Bearer"
        ,name = "Authorization",in = SecuritySchemeIn.HEADER)
@OpenAPIDefinition(security =  { @SecurityRequirement(name="Authorization")})
public class SwaggerConfiguration {
    /**
     * 配置文档介绍以及详细信息
     * @return OpenAPI
     */
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("API 文档")
                        .description("欢迎来到本项目API测试文档，在这里可以快速进行接口调试")
                        .version("1.0")
                        .license(new License()
                                .name("项目开源地址")
                                .url("https://github.com/1956620497/template-TDesignUI")
                        )
                )
                .externalDocs(new ExternalDocumentation()
                        .description("我的个人网站")
                        .url("https://github.com/1956620497")
                );
    }

    /**
     * 配置自定义的OpenApi相关信息
     * @return OpenApiCustomizer
     */
    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
        return api -> this.authorizePathItems().forEach(api.getPaths()::addPathItem);
    }

    /**
     * 登录接口和退出登录接口手动添加一下
     * @return PathItems
     */
    private Map<String, PathItem> authorizePathItems(){
        Map<String, PathItem> map = new HashMap<>();
        map.put("/api/auth/login", new PathItem()
                .post(new Operation()
                        .tags(List.of("登录校验相关"))
                        .summary("登录验证接口")
                        .addParametersItem(new QueryParameter()
                                .name("username")
                                .required(true)
                        )
                        .addParametersItem(new QueryParameter()
                                .name("password")
                                .required(true)
                        )
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content().addMediaType("*/*", new MediaType()
                                                .example(RestBean.success(new AuthorizeVO()).asJsonString())
                                        ))
                                )
                        )
                )
        );
        map.put("/api/auth/logout", new PathItem()
                .get(new Operation()
                        .tags(List.of("登录校验相关"))
                        .summary("退出登录接口")
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content().addMediaType("*/*", new MediaType()
                                                .example(RestBean.success())
                                        ))
                                )
                        )
                )

        );
        return map;
    }
}
