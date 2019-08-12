package br.com.htecweb.springbatch.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Calendar;

@Data
public class Customer implements Serializable {

    private int id;
    private String name;
    private Calendar birthday;
    private int transactions;

    @Override
    public String toString() {
        return String.format(
                "#%s, %s born on %3$tb %3$te, %3$tY, finished %4$s transactions",
                id,
                name,
                birthday.getTime(),
                transactions
        );
    }

}
