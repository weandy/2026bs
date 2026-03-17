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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 管理域数据源配置
 */
@Configuration
@MapperScan(basePackages = {"com.chp.admin.mapper", "com.chp.medical.mapper", "com.chp.security.mapper.admin"},
        sqlSessionFactoryRef = "adminSqlSessionFactory")
public class AdminDataSourceConfig {

    @Bean("adminDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.admin")
    public DataSource adminDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean("adminSqlSessionFactory")
    public SqlSessionFactory adminSqlSessionFactory(
            @Qualifier("adminDataSource") DataSource dataSource,
            MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setPlugins(mybatisPlusInterceptor);
        com.baomidou.mybatisplus.core.MybatisConfiguration config = new com.baomidou.mybatisplus.core.MybatisConfiguration();
        config.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(config);
        return factoryBean.getObject();
    }

    @Bean("adminTransactionManager")
    public PlatformTransactionManager adminTransactionManager(
            @Qualifier("adminDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
