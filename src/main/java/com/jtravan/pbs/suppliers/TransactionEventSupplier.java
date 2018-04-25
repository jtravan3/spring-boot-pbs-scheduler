package com.jtravan.pbs.suppliers;

import com.jtravan.pbs.model.TransactionEvent;
import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import com.techprimers.reactive.reactivemongoexample1.repository.EmployeeRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Supplier;

@Component
public class TransactionEventSupplier implements Supplier<TransactionEvent> {

    private final EmployeeRepository employeeRepository;

    public TransactionEventSupplier(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public TransactionEvent get() {
        Employee e = employeeRepository.findById("1000").block();
        return new TransactionEvent(e.toString(), new Date());
    }

}
