package br.com.htecweb.springbatch.item.processor;

import br.com.htecweb.springbatch.SpringbatchApplication;
import br.com.htecweb.springbatch.configuration.BatchTestConfiguration;
import br.com.htecweb.springbatch.entity.Customer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.GregorianCalendar;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, StepScopeTestExecutionListener.class})
@ContextConfiguration(classes = {SpringbatchApplication.class, BatchTestConfiguration.class})
public class BirthdayFilterProcessorTest {

    @Autowired
    private BirthdayFilterProcessor processor;

    public StepExecution getStepExecution() {
        return MetaDataInstanceFactory.createStepExecution();
    }

    @Test
    public void filter() {
        final Customer customer = new Customer();
        customer.setId(1);
        customer.setName("name");
        customer.setBirthday(new GregorianCalendar());
        assertNotNull(processor.process(customer));
    }
}
