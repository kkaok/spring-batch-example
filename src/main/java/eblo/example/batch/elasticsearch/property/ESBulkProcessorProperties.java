package eblo.example.batch.elasticsearch.property;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Setter;
import lombok.ToString;

@Configuration
@PropertySource({ "classpath:es-default.properties" })
@ConfigurationProperties(prefix = "es.bulk.processor")
@Setter
@ToString
public class ESBulkProcessorProperties {

    private Integer bulkActions; // default 1000, 5000건 
    private Integer bulkSize; // default 5mb, 50mb
    private Integer concurrentRequests; // default 0, 0이면 하나의 request. 
    private Integer flushInterval; // default null, 
    private Integer backoffMillis; // BackoffPolicy.exponentialBackoff(TimeValue.timeValueSeconds(1), 3)) 
    private Integer retries; // BackoffPolicy.exponentialBackoff(TimeValue.timeValueSeconds(1), 3)) 

    public Integer getBulkActions() {
        return this.bulkActions;
    }

    public ByteSizeValue getBulkSize() {
        return new ByteSizeValue(bulkSize, ByteSizeUnit.MB);
    }
    
    public Integer getConcurrentRequests() {
        return this.concurrentRequests;
    }
    
    public TimeValue getFushInterval() {
        return TimeValue.timeValueSeconds(this.flushInterval);
    }
    
    public BackoffPolicy getBackoffPolicy() {
        return BackoffPolicy.exponentialBackoff(TimeValue.timeValueSeconds(this.backoffMillis), this.retries);
    }
}
