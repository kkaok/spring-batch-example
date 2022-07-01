package eblo.example.batch.job;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;

import eblo.example.batch.configuration.DataSourceConfiguration;
import eblo.example.batch.configuration.DataSourceProperties;
import eblo.example.batch.configuration.MybatisConfiguration;
import eblo.example.batch.configuration.MybatisProperties;
import eblo.example.batch.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableConfigurationProperties({MybatisProperties.class, DataSourceProperties.class})
@Import({DataSourceConfiguration.class, MybatisConfiguration.class})
@RequiredArgsConstructor
public class UserBatchJobConfiguration {

    private static final String OUTPUT_FILE_DIRECTORY = System.getProperty("java.io.tmpdir")+"/eblo/batch";
    
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final SqlSessionFactory sqlSessionFactory;

    @Bean(name="mybatisToJsonJob")
    public Job ExampleJob() throws Exception {
        return jobBuilderFactory.get("mybatisToJsonJob")
                .start(Step())
                .build();
    }
    
    @Bean
    @JobScope
    public Step Step() throws Exception {
        return stepBuilderFactory.get("Step")
                .<User, User>chunk(10000)
                //.transactionManager(transactionManager)
                .reader(reader(null, null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    @StepScope
    public MyBatisPagingItemReader<User> reader(@Value("#{jobParameters[age]}") Integer age, @Value("#{jobParameters[requestTime]}") String requestTime) throws Exception {
        log.info("jobParameters age : " + age);
        log.info("jobParameters requestTime : " + requestTime);
        Map<String, Object> params = new HashMap<>();
        params.put("age", age);
        return new MyBatisPagingItemReaderBuilder<User>()
                .pageSize(10000)
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("eblo.example.batch.mapper.UserMapper.findAll")
                .parameterValues(params)
                .build();
    }
    
    @Bean
    public JsonFileItemWriter<User> itemWriter() {
        return new JsonFileItemWriterBuilder<User>()
                .resource(new FileSystemResource(OUTPUT_FILE_DIRECTORY + "/eblo_users.json")).lineSeparator("\n")
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .name("ebloUsersJsonFileItemWriter")
                .build();
    }
    
//    public static void main(String[] args) {
//        System.out.println("@@@@ :" +System.getProperty("java.io.tmpdir"));
//    }
}