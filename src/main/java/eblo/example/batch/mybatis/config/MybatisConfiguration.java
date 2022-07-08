package eblo.example.batch.mybatis.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import eblo.example.batch.datasource.config.DataSourceConfiguration;
import eblo.example.batch.mybatis.property.MybatisProperties;

@Configuration
@EnableConfigurationProperties({MybatisProperties.class})
@Import({DataSourceConfiguration.class})
public class MybatisConfiguration {

    @Bean(name="sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactoryBean(DataSource dataSource, MybatisProperties mybatisProperties, ApplicationContext applicationContext)
            throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setConfiguration(getMyBatisConfiguration(mybatisProperties));
        factoryBean.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage());
        factoryBean.setMapperLocations(applicationContext.getResources(mybatisProperties.getMapperLocations()));
        return factoryBean.getObject();
    }
    
    private org.apache.ibatis.session.Configuration getMyBatisConfiguration(MybatisProperties mybatisProperties) {
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(mybatisProperties.getConfig().isMapUnderscoreToCamelcase());
        configuration.setCallSettersOnNulls(mybatisProperties.getConfig().isCallSettersOnNulls());
        configuration.setLazyLoadingEnabled(mybatisProperties.getConfig().isLazyLoadingEnabled());
        configuration.setDefaultStatementTimeout(mybatisProperties.getConfig().getDefaultStatementTimeout());
        configuration.setDefaultExecutorType(mybatisProperties.getConfig().getDefaultExecutorType());
        configuration.setUseGeneratedKeys(mybatisProperties.getConfig().isUseGeneratedKeys());
        configuration.setCacheEnabled(mybatisProperties.getConfig().isCacheEnabled());
        configuration.setJdbcTypeForNull(mybatisProperties.getConfig().getJdbcTypeForNull());
        return configuration;
    }

    @Primary
    public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}

