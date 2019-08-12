package br.com.htecweb.springbatch.item.reader;

import br.com.htecweb.springbatch.entity.Customer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Slf4j
public class CustomerItemReader implements ItemReader<Customer> {

    private final String filename;

    private ItemReader<Customer> delegate;

    public CustomerItemReader(final String filename) {
        this.filename = filename;
    }

    @Override
    public Customer read() throws Exception {
        if (delegate == null) {
            delegate = new IteratorItemReader<>(customers());
        }

        log.info("Reading next customer");
        return delegate.read();
    }

    private List<Customer> customers() throws IOException {
        String jsonString = Files.readString(ResourceUtils.getFile(filename).toPath(),
                                       StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, new TypeReference<List<Customer>>(){});
    }
}
