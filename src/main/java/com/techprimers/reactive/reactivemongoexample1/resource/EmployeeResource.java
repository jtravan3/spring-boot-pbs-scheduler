package com.techprimers.reactive.reactivemongoexample1.resource;

import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import com.techprimers.reactive.reactivemongoexample1.model.EmployeeEvent;
import com.techprimers.reactive.reactivemongoexample1.repository.EmployeeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/rest/employee")
public class EmployeeResource {


    private EmployeeRepository employeeRepository;

    public EmployeeResource(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/all")
    public List<Employee> getAll() {
        return employeeRepository
                .findAll();
    }

    @GetMapping("/{id}")
    public Mono<Employee> getId(@PathVariable("id") final String empId) {
        return Mono.just(employeeRepository.findById(empId).get());
    }


    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmployeeEvent> getEvents(@PathVariable("id") final String empId) {
        Employee employee = employeeRepository.findById(empId).get();
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(2));

        Flux<EmployeeEvent> employeeEventFlux =
                Flux.fromStream(
                        Stream.generate(() -> new EmployeeEvent(employee,
                                new Date()))
                );


        return Flux.zip(interval, employeeEventFlux)
                .map(Tuple2::getT2);
    }

    @GetMapping(value = "/all/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmployeeEvent> getAllEvents() {
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(2));

        Iterable<Employee> employeeIterable = employeeRepository.findAll(Sort.by("id"));

        LinkedList<Employee> employeeList = new LinkedList<>();
        for (Employee employee : employeeIterable) {
            employeeList.add(employee);
        }

        Flux<EmployeeEvent> employeeEventFlux =
                Flux.fromStream(
                        Stream.generate(() -> {
                            EmployeeEvent e = new EmployeeEvent(employeeList.poll(), new Date());
                            return e;
                        })
                );

        return Flux.zip(interval, employeeEventFlux)
                .map(Tuple2::getT2);
    }

    @GetMapping(value = "/all/events2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmployeeEvent> getAllEvents2() {
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(2));

        Iterable<Employee> employeeIterable = employeeRepository.findAll(Sort.by("id"));
        List<EmployeeEvent> employeeEventIterable = new LinkedList<>();

        for (Employee employee : employeeIterable) {
            employeeEventIterable.add(new EmployeeEvent(employee, new Date()));
        }

        Flux<EmployeeEvent> employeeEventFlux = Flux.fromIterable(employeeEventIterable);

        return Flux.zip(interval, employeeEventFlux)
                .map(Tuple2::getT2);
    }

}
