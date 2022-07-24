package com.kakao.pretest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableCaching
@EnableAsync
@SpringBootApplication
public class PretestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PretestApplication.class, args);
    }

}
