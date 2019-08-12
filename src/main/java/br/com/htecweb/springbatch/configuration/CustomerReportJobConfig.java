package br.com.htecweb.springbatch.configuration;

import br.com.htecweb.springbatch.entity.Customer;
import br.com.htecweb.springbatch.item.processor.BirthdayFilterProcessor;
import br.com.htecweb.springbatch.item.processor.TransactionValidatingProcessor;
import br.com.htecweb.springbatch.item.reader.CustomerItemReader;
import br.com.htecweb.springbatch.item.writer.CustomerItemWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PreDestroy;
import java.util.Arrays;

@Slf4j
@Configuration
@PropertySource("classpath:application.properties")
public class CustomerReportJobConfig {

    private final String jsonFile;
    private final String outputPath;
    private final int limit;
    private static final String TASKLET_STEP = "taskletStep";
    private static final String JOB_NAME = "customerReportJob";

    private final JobBuilderFactory jobBuilders;

    private final StepBuilderFactory stepBuilders;

    private final JobExplorer jobs;

    private final JobLauncher jobLauncher;

    @Autowired
    public CustomerReportJobConfig(JobBuilderFactory jobBuilders, StepBuilderFactory stepBuilders, JobExplorer jobs,
                                   JobLauncher jobLauncher, @Value("${json_path}") String jsonFile,
                                   @Value("${output_path}") String outputPath, @Value("${limit}") int limit) {
        this.jobBuilders = jobBuilders;
        this.stepBuilders = stepBuilders;
        this.jobs = jobs;
        this.jobLauncher = jobLauncher;
        this.jsonFile = jsonFile;
        this.outputPath = outputPath;
        this.limit = limit;
    }

    @PreDestroy
    public void destroy() throws NoSuchJobException {
        jobs.getJobNames().forEach(name -> log.info("job name: {}", name));
        jobs.getJobInstances(JOB_NAME, 0, jobs.getJobInstanceCount(JOB_NAME)).forEach(
                jobInstance -> log.info("job instance id {}", jobInstance.getInstanceId())
        );

    }

    @Scheduled(fixedRate = 5000)
    public void run() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException,
            JobInstanceAlreadyCompleteException {
        JobExecution execution = jobLauncher.run(
                customerReportJob(),
                new JobParametersBuilder().addLong("uniqueness", System.nanoTime()).toJobParameters()
        );
        log.info("Exit status: {}", execution.getStatus());
    }

    @Bean
    public Job customerReportJob() {
        return jobBuilders.get(JOB_NAME)
                .start(taskletStep())
                .next(chunkStep())
                .build();
    }

    @Bean
    public Step taskletStep() {
        return stepBuilders.get(TASKLET_STEP)
                .tasklet(tasklet())
                .build();
    }

    @Bean
    public Tasklet tasklet() {
        return (contribution, chunkContext) -> RepeatStatus.FINISHED;
    }

    @Bean
    public Step chunkStep() {
        return stepBuilders.get("chunkStep")
                .<Customer, Customer>chunk(20)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @StepScope
    @Bean
    public ItemReader<Customer> reader() {
        return new CustomerItemReader(jsonFile);
    }

    @StepScope
    @Bean
    public ItemProcessor<Customer, Customer> processor() {
        final CompositeItemProcessor<Customer, Customer> processor = new CompositeItemProcessor<>();
        processor.setDelegates(Arrays.asList(birthdayFilterProcessor(), transactionValidatingProcessor()));
        return processor;
    }

    @StepScope
    @Bean
    public BirthdayFilterProcessor birthdayFilterProcessor() {
        return new BirthdayFilterProcessor();
    }

    @StepScope
    @Bean
    public TransactionValidatingProcessor transactionValidatingProcessor() {
        return new TransactionValidatingProcessor(limit);
    }

    @StepScope
    @Bean
    public ItemWriter<Customer> writer() {
        return new CustomerItemWriter(outputPath);
    }
}
