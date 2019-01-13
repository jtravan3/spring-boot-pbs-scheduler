package com.techprimers.reactive.reactivemongoexample1.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Entity
public class Employee {

    @Id
    private String id;
    private String name;
    private Long salary;
    private AtomicBoolean isLocked;

    public Employee() {
        isLocked = new AtomicBoolean(false);
    }

    public Employee(String id, String name, Long salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        isLocked = new AtomicBoolean(false);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }

    public AtomicBoolean isLocked() {
        return isLocked;
    }

    public void setLocked(AtomicBoolean locked) {
        isLocked = locked;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", salary=" + salary +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(getId(), employee.getId()) &&
                Objects.equals(getName(), employee.getName()) &&
                Objects.equals(getSalary(), employee.getSalary());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getSalary());
    }
}
