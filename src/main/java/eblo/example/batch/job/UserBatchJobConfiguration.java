package eblo.example.batch.job;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;

import eblo.example.batch.elasticsearch.config.ElasticSearchConfiguration;
import eblo.example.batch.elasticsearch.item.ElasticseachItemWriter;
import eblo.example.batch.elasticsearch.mapper.JsonLineMapper;
import eblo.example.batch.elasticsearch.model.JsonDataSet;
import eblo.example.batch.elasticsearch.property.ESBulkProcessorProperties;
import eblo.example.batch.elasticsearch.support.ESIndexHandler;
import eblo.example.batch.mybatis.config.MybatisConfiguration;
import eblo.example.batch.user.model.User;
import eblo.example.batch.user.model.UserJsonDataSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
@Import({MybatisConfiguration.class, ElasticSearchConfiguration.class})
@RequiredArgsConstructor
public class UserBatchJobConfiguration {

    private static final String OUTPUT_FILE_DIRECTORY = System.getProperty("java.io.tmpdir")+"/eblo/batch";
    private static final int DEFAULT_CHUNGK_SIZE = 5000; //chunk size, page size, bulk size 등은 performance 측면에 일치 시켜주는 것이 좋다. 
    
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final SqlSessionFactory sqlSessionFactory;
    private final ESIndexHandler esIndexHandler;
    private final RestHighLevelClient restHighLevelClient;
    private final ESBulkProcessorProperties bulkProcessorProperties;
    
    @Bean(name="exampleJob")
    public Job ExampleJob() throws Exception {
        return jobBuilderFactory.get("exampleJob")
                .preventRestart() // 에러가 나서 다시 실행하는거 차단. 에러 나고 다시 실행하면 실패 된건 실패로 끝... 
                .start(stepReadDataFromDB())
                .next(stepAddIndex(null))
                .next(stepWriteES())
                .build();
    }
    
    @Bean
    @JobScope
    public Step stepReadDataFromDB() throws Exception {
        return stepBuilderFactory.get("stepReadDataFromDB")
                .<User, User>chunk(DEFAULT_CHUNGK_SIZE)
                //.transactionManager(transactionManager)
                .reader(userDBItemReader(null, null))
                .writer(userJsonItemWriter())
                .build();
    }
    
    @Bean(destroyMethod="close")
    @StepScope
    public MyBatisPagingItemReader<User> userDBItemReader(@Value("#{jobParameters[age]}") Integer age, @Value("#{jobParameters[requestTime]}") String requestTime) throws Exception {
        log.info("jobParameters age : " + age);
        log.info("jobParameters requestTime : " + requestTime);
        Map<String, Object> params = new HashMap<>();
        params.put("age", age);
        return new MyBatisPagingItemReaderBuilder<User>()
                .pageSize(DEFAULT_CHUNGK_SIZE)
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("eblo.example.batch.user.mapper.UserMapper.findAll")
                .parameterValues(params)
                .build();
    }
    
    @Bean(destroyMethod="close")
    public JsonFileItemWriter<User> userJsonItemWriter() {
        return new JsonFileItemWriterBuilder<User>()
                .resource(new FileSystemResource(OUTPUT_FILE_DIRECTORY + "/eblo_users.json")).lineSeparator("\n")
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .name("ebloUsersJsonFileItemWriter")
                .build();
    }

    @Bean
    @JobScope
    public Step stepWriteES() {
        return stepBuilderFactory.get("stepWriteES")
                .<JsonDataSet, JsonDataSet>chunk(3) //DEFAULT_CHUNGK_SIZE
                .reader(userJsonItemReader())
//                .writer(new ConsoleItemWriter<>())
                .writer(elasticseachItemWriter(null))
                .build();
    }

    @Bean
    @JobScope
    public Step stepAddIndex(@Value("#{jobParameters[date]}") String date) throws Exception {
        String indexName = "idx_user_"+date;
        return stepBuilderFactory.get("stepAddIndex")
                .tasklet(new EsCreateIndex(esIndexHandler, indexName))
                .build();
    }

    @Bean(destroyMethod="close")
    public FlatFileItemReader<JsonDataSet> userJsonItemReader() {
      FlatFileItemReader<JsonDataSet> reader = new FlatFileItemReader<>();
      reader.setResource(new FileSystemResource(OUTPUT_FILE_DIRECTORY + "/eblo_users.json"));
      reader.setLinesToSkip(1);
      reader.setLineMapper(new JsonLineMapper(UserJsonDataSet.class));
      return reader;
    }
    
    @Bean
    @JobScope
    public ElasticseachItemWriter elasticseachItemWriter(@Value("#{jobParameters[date]}") String date) {
        String indexName = "idx_user_"+date;
        ElasticseachItemWriter writer = new ElasticseachItemWriter(restHighLevelClient, bulkProcessorProperties, indexName);
        return writer;
    }

    @RequiredArgsConstructor
    public static class EsCreateIndex implements Tasklet{
        private final ESIndexHandler esIndexHandler;
        private final String indexName;
        @Override
        public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
            // 인덱스 생성 
            esIndexHandler.addIndex(indexName);
            return RepeatStatus.FINISHED;
        }
    }

    
//    public static void main(String[] args) {
//        System.out.println("@@@@ :" +System.getProperty("java.io.tmpdir"));
//    }
}