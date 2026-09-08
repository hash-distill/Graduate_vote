package com.bluemsun.config;

import com.bluemsun.interceptor.AuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 *
 * 设计依据：docs/投票系统鉴权与身份投票设计.md §4.2
 * 注册管理员鉴权拦截器：/admin/** 全部要求会话（放行 /admin/login 登录入口）。
 * 此前拦截器从未被注册（无 WebMvcConfigurer），导致管理接口匿名可达。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;

    public WebConfig(AuthenticationInterceptor authenticationInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");
    }
}
