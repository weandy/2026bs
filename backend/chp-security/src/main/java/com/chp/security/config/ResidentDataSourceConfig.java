package com.chp.security.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 居民域数据源配置
 */
@Configuration
@MapperScan(basePackages = {"com.chp.resident.mapper", "com.chp.security.mapper.resident"},
        sqlSessionFactoryRef = "residentSqlSessionFactory")
public class ResidentDataSourceConfig {

    @Primary
    @Bean("residentDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.resident")
    public DataSource residentDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean("residentSqlSessionFactory")
    public SqlSessionFactory residentSqlSessionFactory(
            @Qualifier("residentDataSource") DataSource dataSource,
            MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setPlugins(mybatisPlusInterceptor);
        com.baomidou.mybatisplus.core.MybatisConfiguration config = new com.baomidou.mybatisplus.core.MybatisConfiguration();
        config.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(config);
        return factoryBean.getObject();
    }

    @Primary
    @Bean("residentTransactionManager")
    public PlatformTransactionManager residentTransactionManager(
            @Qualifier("residentDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
