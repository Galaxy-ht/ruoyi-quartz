package com.haotao.quartz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.haotao.**.mapper")
public class RuoyiQuartzApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuoyiQuartzApplication.class, args);
    }

}
