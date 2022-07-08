package eblo.example.batch.elasticsearch.item;


import java.util.List;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.batch.item.ItemWriter;

import eblo.example.batch.elasticsearch.model.JsonDataSet;
import eblo.example.batch.elasticsearch.property.ESBulkProcessorProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ElasticseachItemWriter implements ItemWriter<JsonDataSet> { //, InitializingBean {
    
    private final RestHighLevelClient restHighLevelClient;
    private final ESBulkProcessorProperties bulkProcessorProperties;
    private final String indexName;
//
//    public void setRestHighLevelClient(RestHighLevelClient restHighLevelClient) {
//        this.restHighLevelClient = restHighLevelClient;
//    }
//
//    public void setBulkProcessorProperties(ESBulkProcessorProperties bulkProcessorProperties) {
//        this.bulkProcessorProperties = bulkProcessorProperties;
//    }
//
//    public void setIndexName(String indexName) {
//        this.indexName = indexName;
//    }

    /**
     * bulk insert 
     * @param lists
     * @throws Exception
     */
    public void bulkRequest(List<? extends JsonDataSet> lists) throws Exception {
        log.debug("bulkRequest start.");
        if(lists == null || lists.size() == 0) return ;
        BulkProcessor bulkProcessor = builder(restHighLevelClient).build();
        for (int i = 0; i < lists.size(); i++) {
            String id = lists.get(i).getId();
            bulkProcessor.add(new IndexRequest(indexName).id(id).source(lists.get(i).getJsonData(), XContentType.JSON));
        }
        bulkProcessor.flush();
        // Or close the bulkProcessor if you don't need it anymore
        bulkProcessor.awaitClose(60, TimeUnit.MINUTES);
        // Refresh your indices
        restHighLevelClient.indices().refresh(new RefreshRequest(indexName), RequestOptions.DEFAULT);
        log.debug("bulkRequest end.");
    }
    
    
    /**
     * Delete Index Document
     * @param indexName
     * @param lists
     * @throws Exception
     */
    public void deleteBulkRequest(List<String> lists) throws Exception {
        log.debug("deleteBulkRequest start.");

        if(lists == null || lists.size() == 0) return ;
        BulkProcessor bulkProcessor = builder(restHighLevelClient).build();
        for (String itemId : lists) {
            bulkProcessor.add(new DeleteRequest(indexName).id(itemId));
        }
        bulkProcessor.flush();
        // Or close the bulkProcessor if you don't need it anymore
        bulkProcessor.awaitClose(600, TimeUnit.MINUTES);

        restHighLevelClient.indices().refresh(new RefreshRequest(indexName), RequestOptions.DEFAULT);

        log.debug("deleteBulkRequest end.");
    }

    public BulkProcessor.Listener listener() {
        return new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                int numberOfActions = request.numberOfActions(); 
                log.debug("Executing bulk [{}] with {} requests",
                        executionId, numberOfActions);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                    BulkResponse response) {
                if (response.hasFailures()) { 
                    log.warn("Bulk [{}] executed with failures", executionId);
                } else {
                    log.debug("Bulk [{}] completed in {} milliseconds",
                            executionId, response.getTook().getMillis());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                    Throwable failure) {
                log.error("Failed to execute bulk", failure); 
            }
        };
    }
    
    public BulkProcessor.Builder builder(RestHighLevelClient restHighLevelClient) {
        BulkProcessor.Builder builder = BulkProcessor.builder( (request, bulkListener) -> restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener(), "bulk-processor");
        builder.setBulkActions(this.bulkProcessorProperties.getBulkActions()); // default 1000
        builder.setBulkSize(this.bulkProcessorProperties.getBulkSize()); // default 5MB 
        builder.setConcurrentRequests(this.bulkProcessorProperties.getConcurrentRequests()); // default 1  
        builder.setFlushInterval(this.bulkProcessorProperties.getFushInterval());  // default 10 
        builder.setBackoffPolicy(this.bulkProcessorProperties.getBackoffPolicy()); // default 50, 8 
        return builder;
    }
    
    @Override
    public void write(List<? extends JsonDataSet> items) throws Exception {
        bulkRequest(items); 
    }

}

