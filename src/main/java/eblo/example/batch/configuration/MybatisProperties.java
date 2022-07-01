package eblo.example.batch.configuration;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.type.JdbcType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configuration
@PropertySource({ "classpath:mybatis.properties" })
@ConfigurationProperties(prefix = "mybatis")
@Getter
@Setter
@ToString
public class MybatisProperties {

    private String packagesToScan;
    private String typeAliasesPackage;
    private String typeHandlerPackage;
    private String mapperLocations;
    private MybatisConfig config;

    @Getter
    @Setter
    public static class MybatisConfig {
        private boolean mapUnderscoreToCamelcase;
        private boolean callSettersOnNulls;
        private boolean lazyLoadingEnabled;
        private int defaultStatementTimeout;
        private ExecutorType defaultExecutorType;
        private boolean useGeneratedKeys;
        private boolean cacheEnabled;
        private JdbcType jdbcTypeForNull;
    }
}