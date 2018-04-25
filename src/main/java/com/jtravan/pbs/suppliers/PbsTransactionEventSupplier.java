package com.jtravan.pbs.suppliers;

import com.jtravan.pbs.generator.TransactionGenerator;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.scheduler.PredictionBasedScheduler;
import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import com.techprimers.reactive.reactivemongoexample1.repository.EmployeeRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Supplier;

@Component
public class PbsTransactionEventSupplier implements Supplier<TransactionEvent> {

    private final EmployeeRepository employeeRepository;
    private final PredictionBasedScheduler predictionBasedScheduler;

    public PbsTransactionEventSupplier(EmployeeRepository employeeRepository, PredictionBasedScheduler predictionBasedScheduler) {
        this.employeeRepository = employeeRepository;
        this.predictionBasedScheduler = predictionBasedScheduler;
    }

    @Override
    public TransactionEvent get() {

        Employee e = employeeRepository.findById("1000").block();
        return new TransactionEvent(e.toString(), new Date());
    }

}
