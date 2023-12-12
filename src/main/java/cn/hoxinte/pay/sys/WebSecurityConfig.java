package cn.hoxinte.pay.sys;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WEB 安全配置
 *
 * @author dominate
 * @date 2021/11/26
 */
@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    private static final String ALLOW_ALL_PATH_PATTERNS = "/**";
    private static final String ALLOW_ALL_PATTERNS = "*";

    @Bean
    public CorsFilter corsFilter() {
        // 1 创建CorsConfiguration对象后添加配置
        CorsConfiguration config = new CorsConfiguration();
        // 设置放行原始域
        config.addAllowedOriginPattern(ALLOW_ALL_PATTERNS);
        // 放行原始请求头部信息
        config.addAllowedHeader(ALLOW_ALL_PATTERNS);
        // 暴露哪些头部信息
        config.addExposedHeader(ALLOW_ALL_PATTERNS);
        // 放行哪些请求方式
        config.addAllowedMethod(ALLOW_ALL_PATTERNS);
        // 是否发送Cookie
        config.setAllowCredentials(true);
        // 2 添加映射路径
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration(ALLOW_ALL_PATH_PATTERNS, config);
        return new CorsFilter(corsConfigurationSource);
    }

}
