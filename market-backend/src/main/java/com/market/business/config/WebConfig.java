package com.market.business.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * web配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    /**
     * cors 跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 放行首页接口
                        "/api/market/findList",
                        "/api/market/options",
                        // 放行用户认证接口
                        "/api/user/nonce",
                        "/api/user/login",
                        // 放行管理员接口
                        "/api/admin/**"
                );
    }
}
