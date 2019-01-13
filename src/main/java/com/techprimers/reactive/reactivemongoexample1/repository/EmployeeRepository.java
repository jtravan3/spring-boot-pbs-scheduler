package com.techprimers.reactive.reactivemongoexample1.repository;

import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
}
