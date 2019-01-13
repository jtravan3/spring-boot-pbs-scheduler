package com.jtravan.pbs.generator;

import com.jtravan.pbs.model.Category;
import com.jtravan.pbs.model.Operation;
import com.jtravan.pbs.model.ResourceOperation;
import com.jtravan.pbs.model.TestCase;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.services.ResourceNotificationManager;
import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import com.techprimers.reactive.reactivemongoexample1.repository.EmployeeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("ALL")
@Component
public class TransactionGenerator {

    private static final int RANDOM_BOUND = 1000;
    private int methodCallCount = 0;

    private final ResourceNotificationManager resourceNotificationManager;
    private final EmployeeRepository employeeRepository;
    private final AbortPercentageFactory abortPercentageFactory;

    public TransactionGenerator(ResourceNotificationManager resourceNotificationManager, AbortPercentageFactory abortPercentageFactory,
                                EmployeeRepository employeeRepository) {

        this.resourceNotificationManager = resourceNotificationManager;
        this.abortPercentageFactory = abortPercentageFactory;
        this.employeeRepository = employeeRepository;
    }

    public List<Transaction> setCategoriesByTestCase(List<Transaction> transactionList, TestCase testCase) {

        // TC #1
        if (testCase.getHchePercentage() == 100) {
            for(Transaction t : transactionList) {
                t.setCategory(Category.HCHE);
            }
            return transactionList;
        }

        // TC #7
        if (testCase.getLclePercentage() == 100) {
            for(Transaction t : transactionList) {
                t.setCategory(Category.LCLE);
            }
            return transactionList;
        }

        // TC #2
        if (testCase.getHchePercentage() == 75) {

            for (Transaction t : transactionList) {
                t.setCategory(null);
            }

            long size = transactionList.size();
            long percentHCHE = Math.round(size * 0.75);

            for (int i = 0; i < percentHCHE; i++) {
                transactionList.get(i).setCategory(Category.HCHE);
            }

            for (Transaction t : transactionList) {
                if (t.getCategory() == null) {
                    t.setCategory(Category.HCLE);
                }
            }

            return transactionList;
        }

        // TC #6
        if (testCase.getLclePercentage() == 75) {

            for (Transaction t : transactionList) {
                t.setCategory(null);
            }

            long size = transactionList.size();
            long percentLCLE = Math.round(size * 0.75);

            for (int i = 0; i < percentLCLE; i++) {
                transactionList.get(i).setCategory(Category.LCLE);
            }

            for (Transaction t : transactionList) {
                if (t.getCategory() == null) {
                    t.setCategory(Category.LCHE);
                }
            }

            return transactionList;
        }

        // TC #3
        if (testCase.getHchePercentage() == 50) {

            for (Transaction t : transactionList) {
                t.setCategory(null);
            }

            long size = transactionList.size();
            long percentHCHE = Math.round(size * 0.50);

            for (int i = 0; i < percentHCHE; i++) {
                transactionList.get(i).setCategory(Category.HCHE);
            }

            int count = 0;
            for (Transaction t : transactionList) {
                if (t.getCategory() == null) {
                    if (count % 2 == 0) {
                        t.setCategory(Category.HCLE);
                    } else {
                        t.setCategory(Category.LCHE);
                    }
                    count++;
                }
            }

            return transactionList;
        }

        // TC #5
        if (testCase.getLclePercentage() == 50) {

            for (Transaction t : transactionList) {
                t.setCategory(null);
            }

            long size = transactionList.size();
            long percentLCLE = Math.round(size * 0.50);

            for (int i = 0; i < percentLCLE; i++) {
                transactionList.get(i).setCategory(Category.LCLE);
            }

            int count = 0;
            for (Transaction t : transactionList) {
                if (t.getCategory() == null) {
                    if (count % 2 == 0) {
                        t.setCategory(Category.HCLE);
                    } else {
                        t.setCategory(Category.LCHE);
                    }
                    count++;
                }
            }

            return transactionList;
        }

        // TC #4
        if (testCase.getHchePercentage() == 25 && testCase.getLclePercentage() == 25) {

            for (Transaction t : transactionList) {
                t.setCategory(null);
            }

            long size = transactionList.size();
            long aQuarterOfTheList = Math.round(size * 0.25);
            long halfOfTheList = aQuarterOfTheList * 2;
            long threeQuartersOfTheList = aQuarterOfTheList * 3;

            for (long l = 0; l < aQuarterOfTheList; l++) {
                int m = Math.toIntExact(l);
                transactionList.get(m).setCategory(Category.HCHE);
            }

            for (long l = aQuarterOfTheList; l < halfOfTheList; l++) {
                int m = Math.toIntExact(l);
                transactionList.get(m).setCategory(Category.HCLE);
            }

            for (long l = halfOfTheList; l < threeQuartersOfTheList; l++) {
                int m = Math.toIntExact(l);
                transactionList.get(m).setCategory(Category.LCHE);
            }

            for (long l = threeQuartersOfTheList; l < size; l++) {
                int m = Math.toIntExact(l);
                transactionList.get(m).setCategory(Category.LCLE);
            }

            return transactionList;
        }

        return transactionList;
    }

    public List<String> getAllPossibleIds() {

        Iterable<Employee> employeeIterable = employeeRepository.findAll(Sort.by("id"));
        List<String> employeeIdList = new LinkedList<>();

        for(Employee employee : employeeIterable) {
            employeeIdList.add(employee.getId());
        }

        return employeeIdList;
    }

    public Transaction generateRandomTransaction(int numOfOperations) {

        Transaction transaction = new Transaction();
        List<String> employeeIdList = getAllPossibleIds();

        for(int i = 0; i < numOfOperations; i++) {

            Random random = new Random();
            int randomInt = random.nextInt(RANDOM_BOUND);
            int operation = randomInt % 2;
            int resourceIndex = randomInt % employeeIdList.size();

            ResourceOperation resourceOperation = new ResourceOperation();
            resourceOperation.setExecutionTime(random.nextInt(RANDOM_BOUND));
            resourceOperation.setIsCommitOperation(false);
            resourceOperation.setOperation(Operation.getOperationByOperationNum(operation));

            resourceOperation.setResource(employeeRepository.findById(employeeIdList.get(resourceIndex)).get());
            resourceOperation.setAssociatedTransaction(transaction);

            transaction.addResourceOperation(resourceOperation);

        }

        Random random = new Random();
        int randomInt2 = random.nextInt(RANDOM_BOUND);
        int category = randomInt2 % 4;
        transaction.setCategory(Category.getCategoryByCategoryNum(category));
        abortPercentageFactory.setAbortPercentageBasedOnCategory(transaction);

        return transaction;
    }

    public List<Transaction> generateRandomTransactions(int numOfOperations, int numOfTransactions, boolean controlCategory) {

        if(numOfOperations <= 0 || numOfTransactions <= 0) {
            return Collections.emptyList();
        }

        List<Transaction> transactions = new LinkedList<Transaction>();

        for(int i = 0; i < numOfTransactions; i++) {

            Transaction transaction = new Transaction();
            List<String> employeeIdList = getAllPossibleIds();

            for(int j = 0; j < numOfOperations; j++) {

                Random random = new Random();
                int randomInt = random.nextInt(RANDOM_BOUND);
                int operation = randomInt % 2;
                int resourceIndex = randomInt % employeeIdList.size();

                ResourceOperation resourceOperation = new ResourceOperation();
                resourceOperation.setExecutionTime(random.nextInt(RANDOM_BOUND));
                resourceOperation.setIsCommitOperation(false);
                resourceOperation.setOperation(Operation.getOperationByOperationNum(operation));
                resourceOperation.setResource(employeeRepository.findById(employeeIdList.get(resourceIndex)).get());
                resourceOperation.setAssociatedTransaction(transaction);

                transaction.addResourceOperation(resourceOperation);

            }

            if(controlCategory) {
                if(methodCallCount % 2 == 0) {
                    transaction.setCategory(Category.HCHE);
                } else {
                    transaction.setCategory(Category.LCHE);
                }
                methodCallCount++;
            } else {
                Random random = new Random();
                int randomInt2 = random.nextInt(RANDOM_BOUND);
                int category = randomInt2 % 4;
                transaction.setCategory(Category.getCategoryByCategoryNum(category));
            }

            transactions.add(transaction);
        }

        return transactions;
    }

    public List<Transaction> generateRandomTransactions(int numOfOperations, int numOfTransactions) {

        if(numOfOperations <= 0 || numOfTransactions <= 0) {
            return Collections.emptyList();
        }

        List<Transaction> transactions = new LinkedList<Transaction>();

        for(int i = 0; i < numOfTransactions; i++) {

            Transaction transaction = new Transaction();
            List<String> employeeIdList = getAllPossibleIds();

            for(int j = 0; j < numOfOperations; j++) {

                Random random = new Random();
                int randomInt = random.nextInt(RANDOM_BOUND);
                int operation = randomInt % 2;
                int resourceIndex = randomInt % employeeIdList.size();

                ResourceOperation resourceOperation = new ResourceOperation();
                resourceOperation.setExecutionTime(random.nextInt(RANDOM_BOUND));
                resourceOperation.setIsCommitOperation(false);
                resourceOperation.setOperation(Operation.getOperationByOperationNum(operation));
                resourceOperation.setResource(employeeRepository.findById(employeeIdList.get(resourceIndex)).get());
                resourceOperation.setAssociatedTransaction(transaction);

                transaction.addResourceOperation(resourceOperation);

            }

            transactions.add(transaction);
        }

        return transactions;
    }

    public List<Transaction> generateRandomTransactions(int numOfTransactions) {

        if(numOfTransactions <= 0) {
            return Collections.emptyList();
        }

        List<Transaction> transactions = new LinkedList<Transaction>();

        for(int i = 0; i < numOfTransactions; i++) {

            Transaction transaction = new Transaction();
            List<String> employeeIdList = getAllPossibleIds();

            Random random = new Random();
            int randomOpNumber = random.nextInt(RANDOM_BOUND);

            for(int j = 0; j < randomOpNumber; j++) {

                int randomInt = random.nextInt(RANDOM_BOUND);
                int operation = randomInt % 2;
                int resourceIndex = randomInt % employeeIdList.size();

                ResourceOperation resourceOperation = new ResourceOperation();
                resourceOperation.setExecutionTime(random.nextInt(RANDOM_BOUND));
                resourceOperation.setIsCommitOperation(false);
                resourceOperation.setOperation(Operation.getOperationByOperationNum(operation));
                resourceOperation.setResource(employeeRepository.findById(employeeIdList.get(resourceIndex)).get());
                resourceOperation.setAssociatedTransaction(transaction);

                transaction.addResourceOperation(resourceOperation);

            }

            transactions.add(transaction);
        }

        return transactions;
    }



}
