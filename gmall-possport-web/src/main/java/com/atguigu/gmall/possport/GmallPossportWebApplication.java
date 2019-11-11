package com.atguigu.gmall.possport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.atguigu.gmall")
public class GmallPossportWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPossportWebApplication.class, args);
    }

}
