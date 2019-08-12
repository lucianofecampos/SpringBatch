package br.com.htecweb.springbatch.item.writer;

import br.com.htecweb.springbatch.entity.Customer;
import org.springframework.batch.item.ItemWriter;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

public class CustomerItemWriter implements ItemWriter<Customer>, Closeable {

    private final PrintWriter writer;

    public CustomerItemWriter(final String outputPath) {
        OutputStream out;
        try {
            out = new FileOutputStream(outputPath);
        } catch (FileNotFoundException e) {
            out = System.err;
        }
        this.writer = new PrintWriter(out);
    }

    @Override
    public void write(final List<? extends Customer> items) {
        for (Customer item : items) {
            writer.println(item.toString());
        }
    }

    @PreDestroy
    @Override
    public void close() {
        writer.close();
    }
}
