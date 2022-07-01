package eblo.example.batch.config.datasource.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configuration
@PropertySource({ "classpath:datasource-default.properties", "classpath:datasource-default-common.properties" })
@ConfigurationProperties(prefix = "datasource.default")
@Getter
@Setter
@ToString
public class DataSourceProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private int minimumIdle;
    private int maximumPoolSize;
    private long idleTimeout;
    private String poolName;
    private long maxLifetime;
    private long connectionTimeout;
    private DataSourceConfig config;

    @Getter
    @Setter
    public static class DataSourceConfig {
        private String cachePrepStmts;
        private String prepStmtCacheSize;
        private String prepStmtcacheSqlLimit;
        private String useserverPrepStmts;
    }
}
