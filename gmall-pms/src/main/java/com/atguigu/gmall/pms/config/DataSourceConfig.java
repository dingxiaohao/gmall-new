package com.atguigu.gmall.pms.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {


    /**
     * 需要将 DataSourceProxy 设置为主数据源，否则事务无法回滚
     *
     * @param The HikariDataSource
     * @return The default datasource
     */
    @Primary
    @Bean("dataSource")
    public DataSource dataSource(@Value("${spring.datasource.url}")String url,
                                 @Value("${spring.datasource.password}")String password,
                                 @Value("${spring.datasource.username}")String username,
                                 @Value("${spring.datasource.driver-class-name}")String driver) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(url);
        hikariDataSource.setPassword(password);
        hikariDataSource.setUsername(username);
        hikariDataSource.setDriverClassName(driver);
        return hikariDataSource;
    }
}