package br.com.htecweb.springbatch.configuration;

import br.com.htecweb.springbatch.SpringbatchApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpringbatchApplication.class, BatchTestConfiguration.class})
public class CustomerReportJobConfigTest {

    @Autowired
    private JobLauncherTestUtils testUtils;

    @Autowired
    private CustomerReportJobConfig config;

    @Test
    public void testEntireJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        final JobExecution result = testUtils.getJobLauncher().run(config.customerReportJob(),
                                                                   testUtils.getUniqueJobParameters());
        assertNotNull(result);
        assertEquals(BatchStatus.COMPLETED, result.getStatus());
    }

    @Test
    public void testSpecificStep() {
        assertEquals(BatchStatus.COMPLETED, testUtils.launchStep("taskletStep").getStatus());
    }
}
