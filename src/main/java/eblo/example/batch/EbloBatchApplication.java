package eblo.example.batch;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class EbloBatchApplication implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

	public static void main(String[] args) {
	    // 이렇게만 한다면 run 메소드 하단에 System.exit(SpringApplication.exit(applicationContext)); 처리 필요 
		//SpringApplication.run(EbloBatchApplication.class, args);
		// 배치 실행 후 system exit 하는 경우 
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
        
        JobLauncher jobLauncher = applicationContext.getBean("jobLauncher", JobLauncher.class);
        Job job = applicationContext.getBean(jobName, Job.class);

        Map<String, JobParameter> parameterMap = new HashMap<>();
        parameterMap.put("date", new JobParameter(date));
        parameterMap.put("executeTime", new JobParameter(executeTime));

        JobExecution jobExecution = null;
        try {
            jobExecution = jobLauncher.run(job,  new JobParameters(parameterMap));
            if (jobExecution.getStatus() == BatchStatus.COMPLETED ) {
                log.info("배치 완료 !!!");    
            } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
                log.error("배치 실패 !!!");    
            }
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
//        } finally {
        }
    }

}
