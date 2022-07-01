package eblo.example.batch.configuration;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement()
public class DataSourceConfiguration {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSourceProperties.getUrl());
        config.setDriverClassName(dataSourceProperties.getDriverClassName());
        config.setUsername(dataSourceProperties.getUsername());
        config.setPassword(dataSourceProperties.getPassword());
        config.setMinimumIdle(dataSourceProperties.getMinimumIdle());
        config.setMaximumPoolSize(dataSourceProperties.getMaximumPoolSize());
        config.setConnectionTimeout(dataSourceProperties.getConnectionTimeout());
        config.setPoolName(dataSourceProperties.getPoolName());
        config.setIdleTimeout(dataSourceProperties.getIdleTimeout());
        config.setMaxLifetime(dataSourceProperties.getMaxLifetime());
        Properties props = new Properties();
        props.put("cachePrepStmts", dataSourceProperties.getConfig().getCachePrepStmts());
        props.put("prepStmtCacheSize", dataSourceProperties.getConfig().getPrepStmtCacheSize());
        props.put("prepStmtCacheSqlLimit", dataSourceProperties.getConfig().getPrepStmtcacheSqlLimit());
        props.put("useServerPrepStmts", dataSourceProperties.getConfig().getUseserverPrepStmts());
        config.setDataSourceProperties(props);
        return new HikariDataSource(config);
    }

    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}

