package eblo.example.batch.elasticsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import eblo.example.batch.elasticsearch.property.ESBulkProcessorProperties;
import eblo.example.batch.elasticsearch.property.ESProperties;

@Configuration
@PropertySource({ "classpath:es-default.properties" })
@Import({ESProperties.class, ESBulkProcessorProperties.class})
@EnableElasticsearchRepositories(basePackages = "eblo.example.batch.elasticsearch")
@ComponentScan(basePackages = { "eblo.example.batch.elasticsearch" })
public class ElasticSearchConfiguration {

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient(ESProperties esProperties) {
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                new HttpHost(esProperties.getHost(), esProperties.getPort(), esProperties.getScheme())
        ));
        return client;
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient restHighLevelClient) {
        return new ElasticsearchRestTemplate(restHighLevelClient);
    }

    /* version 
    @Bean
    public RestClient restClient() {
        return RestClient.builder(new HttpHost("localhost", 9200)).build();
    }

    @Bean
    public RestClient httpClient(RestClient restClient) {
        return new RestHighLevelClientBuilder(restClient).setApiCompatibilityMode(true).build();
    }
    
    @Bean
    public ElasticsearchTransport transport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient esClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
    */    
}

