# spring-batch-example

스프링부트 기반에서 스프링배치를 구현하는 샘플 예제입니다.   
  
예제 내용   
1. DB에 있는 사용자 정보를 읽어서 json 파일을 생성합니다. 
2. 엘라스틱 서치에 인덱스를 생성합니다. 
3. json 파일을 읽어서 엘라스틱 서치에 색인을 합니다. 
  
### Technologies Used  
- Java 1.8 or above
- Apache Maven 3.3 or above
- Eclipse
- Mysql 
- Elasticsearch 7.13.4 
- Spring Boot 2.7.1
- Spring Batch 4.3.6

### schema-mysql.sql
```
CREATE TABLE BATCH_JOB_INSTANCE  (
    JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
    VERSION BIGINT ,
    JOB_NAME VARCHAR(100) NOT NULL,
    JOB_KEY VARCHAR(32) NOT NULL,
    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION  (
    JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
    VERSION BIGINT  ,
    JOB_INSTANCE_ID BIGINT NOT NULL,
    CREATE_TIME DATETIME(6) NOT NULL,
    START_TIME DATETIME(6) DEFAULT NULL ,
    END_TIME DATETIME(6) DEFAULT NULL ,
    STATUS VARCHAR(10) ,
    EXIT_CODE VARCHAR(2500) ,
    EXIT_MESSAGE VARCHAR(2500) ,
    LAST_UPDATED DATETIME(6),
    JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
    references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
    JOB_EXECUTION_ID BIGINT NOT NULL ,
    TYPE_CD VARCHAR(6) NOT NULL ,
    KEY_NAME VARCHAR(100) NOT NULL ,
    STRING_VAL VARCHAR(250) ,
    DATE_VAL DATETIME(6) DEFAULT NULL ,
    LONG_VAL BIGINT ,
    DOUBLE_VAL DOUBLE PRECISION ,
    IDENTIFYING CHAR(1) NOT NULL ,
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
    references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION  (
    STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
    VERSION BIGINT NOT NULL,
    STEP_NAME VARCHAR(100) NOT NULL,
    JOB_EXECUTION_ID BIGINT NOT NULL,
    START_TIME DATETIME(6) NOT NULL ,
    END_TIME DATETIME(6) DEFAULT NULL ,
    STATUS VARCHAR(10) ,
    COMMIT_COUNT BIGINT ,
    READ_COUNT BIGINT ,
    FILTER_COUNT BIGINT ,
    WRITE_COUNT BIGINT ,
    READ_SKIP_COUNT BIGINT ,
    WRITE_SKIP_COUNT BIGINT ,
    PROCESS_SKIP_COUNT BIGINT ,
    ROLLBACK_COUNT BIGINT ,
    EXIT_CODE VARCHAR(2500) ,
    EXIT_MESSAGE VARCHAR(2500) ,
    LAST_UPDATED DATETIME(6),
    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
    references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
    STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT ,
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
    references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
    JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT ,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
    references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_SEQ (
    ID BIGINT NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_STEP_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_EXECUTION_SEQ (
    ID BIGINT NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_SEQ (
    ID BIGINT NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_SEQ);
```


스프링 배치 실행 형태  
- org.springframework.batch.core.launch.support.CommandLineJobRunner 실행
- CommandLineRunner로 실행

### CommandLineJobRunner로 배치 실행하기 
###### 실행 예시 
```
//java org.springframework.batch.core.launch.support.CommandLineJobRunner [JobConfiguration] [Job 이] [파라미터key1=value1] [파라미터key2=value2] ...
java org.springframework.batch.core.launch.support.CommandLineJobRunner eblo.example.batch.job.UserBatchJobConfiguration exampleJob date=20220705 executeTime=202207031317
```

###### pom.xml 설정 
```
<plugins>
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <excludes>
                <exclude>
                    <groupId>org.projectlombok</groupId>
                    <artifactId>lombok</artifactId>
                </exclude>
            </excludes>
            <!-- 메인 클래스 변경 -->
            <mainClass>org.springframework.batch.core.launch.support.CommandLineJobRunner</mainClass>
        </configuration>
    </plugin>
</plugins>
```

###### Maven Build 
```
$ mvn clean compile package -Dmaven.test.skip=true -Plocal
```

###### 실행 예시 
```
java -jar eblo.example.batch-0.0.1-SNAPSHOT.jar eblo.example.batch.job.UserBatchJobConfiguration exampleJob date=20220705 executeTime=202207031317
```


### CommandLineRunner로 실행

```
@Slf4j
@SpringBootApplication
public class EbloBatchApplication implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(EbloBatchApplication.class, args).close();
    }

    @Override
    public void run(String... args) throws Exception {
        
        if(args == null || args.length < 3){
            return;
        }
        
        String jobName = args[0];
        String date = args[1];
        String executeTime = args[2];
        
        System.out.println(jobName);
        System.out.println(date);
        System.out.println(executeTime);

        JobLauncher jobLauncher = applicationContext.getBean("jobLauncher", JobLauncher.class);
        Job job = applicationContext.getBean(jobName, Job.class);

        Map<String, JobParameter> parameterMap = new HashMap<>();
        parameterMap.put("date", new JobParameter(date));
        parameterMap.put("executeTime", new JobParameter(executeTime));

        JobExecution jobExecution = null;
        try {
            jobExecution = jobLauncher.run(job,  new JobParameters(parameterMap));
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
            log.error("Batch Job이 이미 실행 중 입니다. {}", e);    
        } catch (JobRestartException e) {
            e.printStackTrace();
            log.error("동일 Batch Job이 다시 시작 되었습니다. {}", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
            log.error("이미 완료된 Batch Job입니다. {}", e);    
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
            log.error("파라미터를 확인해 주세요. {}", e);    
        } catch(Exception e) {
            e.printStackTrace();
            log.error("Batch Job 실행 중 오류가 발생했습니다. {}", e);   
        } finally {
            if (jobExecution.getStatus() == BatchStatus.COMPLETED ) {
                log.info("배치 완료 !!!");    
            } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
                log.error("배치 실패 !!!");    
            }
        }
    }
}
```
아래처럼 배치 완료 시점에 시스템을 종료하는 형태로 할 수도 있습니다. 

```
@Slf4j
@SpringBootApplication
public class EbloBatchApplication implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(EbloBatchApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        
        if(args == null || args.length < 3){
            return;
        }
        
        String jobName = args[0];
        String date = args[1];
        String executeTime = args[2];
        
        System.out.println(jobName);
        System.out.println(date);
        System.out.println(executeTime);

        JobLauncher jobLauncher = applicationContext.getBean("jobLauncher", JobLauncher.class);
        Job job = applicationContext.getBean(jobName, Job.class);

        Map<String, JobParameter> parameterMap = new HashMap<>();
        parameterMap.put("date", new JobParameter(date));
        parameterMap.put("executeTime", new JobParameter(executeTime));

        JobExecution jobExecution = null;
        try {
            jobExecution = jobLauncher.run(job,  new JobParameters(parameterMap));
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
            log.error("Batch Job이 이미 실행 중 입니다. {}", e);    
        } catch (JobRestartException e) {
            e.printStackTrace();
            log.error("동일 Batch Job이 다시 시작 되었습니다. {}", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
            log.error("이미 완료된 Batch Job입니다. {}", e);    
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
            log.error("파라미터를 확인해 주세요. {}", e);    
        } catch(Exception e) {
            e.printStackTrace();
            log.error("Batch Job 실행 중 오류가 발생했습니다. {}", e);   
        } finally {
            if (jobExecution.getStatus() == BatchStatus.COMPLETED ) {
                log.info("배치 완료 !!!");    
            } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
                log.error("배치 실패 !!!");    
            }
            System.exit(SpringApplication.exit(applicationContext));
        }
    }
}
```

###### pom.xml 설정 - 기존 Spring boot에서 사용하는 그대로 두면 됩니다.  
```
<plugins>
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
    </plugin>
</plugins>
```
###### Maven Build 
```
$ mvn clean compile package -Dmaven.test.skip=true -Plocal
```

###### 실행 예시 
```
java -jar eblo.example.batch-0.0.1-SNAPSHOT.jar exampleJob 20220705 202207031317
```

