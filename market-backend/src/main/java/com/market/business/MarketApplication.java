package com.market.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

/**
 * market后台启动类
 */
@SpringBootApplication
@MapperScan("com.market.business.mapper")
public class MarketApplication {
    public static void main(String[] args){
        try {
            SpringApplication.run(MarketApplication.class, args);
        } catch (Exception e) {
            // ExceptionUtil.getStackMessage(e);
        }
    }
}