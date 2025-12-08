package com.financial.mcp.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.financial.mcp.core",
        "com.financial.mcp.rest",
        "com.financial.mcp.security",
        "com.financial.mcp.redis",
        "com.financial.mcp.postgres",
        "com.financial.mcp.elasticsearch",
        "com.financial.mcp.autoconfigure",
        "com.financial.mcp.sample"
})
public class SampleSpokeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SampleSpokeApplication.class, args);
    }
}
