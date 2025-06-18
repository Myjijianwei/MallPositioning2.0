package com.project.mapapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication()
//@MapperScan("com.project.mapapp.mapper")
@EnableTransactionManagement
public class MapAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MapAppApplication.class, args);
    }

}
