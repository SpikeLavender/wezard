package com.natsumes.wezard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author  hetengjiao
 * @date    2020-10-30
 */
@SpringBootApplication
@MapperScan("com.natsumes.wezard.mapper")
@RefreshScope
@EnableDiscoveryClient
public class DbAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbAdapterApplication.class, args);
    }

}
