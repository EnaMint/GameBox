package com.gamebox.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// 只扫描网关自己的包：避免 common 中的 Servlet 组件（UserContextFilter、RestControllerAdvice）混入 WebFlux
@SpringBootApplication(scanBasePackages = "com.gamebox.gateway")
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
