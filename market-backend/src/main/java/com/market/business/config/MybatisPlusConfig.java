package com.market.business.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * @description: mybatis-plus配置
 */
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    /**
     * @description: 分页插件
     */
    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        return new MybatisPlusInterceptor();
    }

    /**
     * @description: 插入策略
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 以后只要是插入操作就会自动控制
        this.setFieldValByName("createdTime", new Date(), metaObject);
        this.setFieldValByName("updatedTime", new Date(), metaObject);
    }

    /**
     * @description: 更新策略
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updatedTime", new Date(), metaObject);
    }

}
