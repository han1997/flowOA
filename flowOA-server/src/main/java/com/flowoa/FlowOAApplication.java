package com.flowoa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.flowoa.mapper")
public class FlowOAApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowOAApplication.class, args);
    }
}
