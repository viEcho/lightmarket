package com.market.business.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;


/**
 * @description: druid配置
 */
@Configuration
public class DruidConfig {

    /**
     * @description: 绑定配置的bean
     */
    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource druidDateSource() {
        return new DruidDataSource();
    }


    /**
     * @description: 注册后台监控页面。SpringBoot 如何注册Servlet
     *  没有web.xml 的情况配置 Servlet 的方法:ServletRegistrationBean
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet() {
        ServletRegistrationBean<StatViewServlet> bean =
                new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");

        bean.addInitParameter("loginUsername", "admin");
        bean.addInitParameter("loginPassword", "admin");
        bean.addInitParameter("allow", "");
        bean.addInitParameter("deny", "");

        return bean;
    }



    /**
     * @description: 过滤器的配置，哪些请求需要被过滤
     */
    @Bean
    public FilterRegistrationBean webStatFilter() {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new WebStatFilter());
        // 配置内容
        // 配置哪些请求可以被过滤！
        HashMap<String, String> map = new HashMap<>();
        map.put("exclusions", "*.js,*.css,/druid/*");
        bean.setInitParameters(map);
        bean.setUrlPatterns(Arrays.asList("/*"));
        return bean;
    }

}
