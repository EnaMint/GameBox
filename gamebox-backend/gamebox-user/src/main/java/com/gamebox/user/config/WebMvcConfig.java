package com.gamebox.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileProperties fileProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 资源定位路径统一为正斜杠且以 / 结尾，兼容 Windows 本地目录
        String dir = fileProperties.getDir().replace('\\', '/');
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + dir);
    }
}
