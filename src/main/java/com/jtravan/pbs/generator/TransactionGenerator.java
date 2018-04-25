package com.jtravan.pbs.generator;

import com.jtravan.pbs.model.Category;
import com.jtravan.pbs.model.Operation;
import com.jtravan.pbs.model.ResourceOperation;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.services.ResourceNotificationManager;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("ALL")
@Component
public class TransactionGenerator {

    private static final int RANDOM_BOUND = 500;
    private int methodCallCount = 0;

    private final ResourceNotificationManager resourceNotificationManager;

    public TransactionGenerator(ResourceNotificationManager resourceNotificationManager) {
        this.resourceNotificationManager = resourceNotificationManager;
    }

    public List<Transaction> generateRandomTransactions(int numOfOperations, int numOfTransactions, boolean controlCategory) {

        if(numOfOperations <= 0 || numOfTransactions <= 0) {
            return Collections.emptyList();
        }

        List<Transaction> transactions = new LinkedList<Transaction>();

        for(int i = 0; i < numOfTransactions; i++) {

            Transaction transaction = new Transaction();

            for(int j = 0; j < numOfOperations; j++) {

                Random random = new Random();
                int randomInt = random.nextInt(200);
                int operation = randomInt % 2;
                int resource = randomInt % 26;

                ResourceOperation resourceOperation = new ResourceOperation();
                resourceOperation.setExecutionTime(random.nextInt(500));
                resourceOperation.setIsCommitOperation(false);
                resourceOperation.setOperation(Operation.getOperationByOperationNum(operation));
                resourceOperation.setResource(resourceNotificationManager.getResourceByResourceNum(resource));
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

            for(int j = 0; j < numOfOperations; j++) {

                Random random = new Random();
                int randomInt = random.nextInt(200);
                int operation = randomInt % 2;
                int resource = randomInt % 26;

                ResourceOperation resourceOperation = new ResourceOperation();
                resourceOperation.setExecutionTime(random.nextInt(500));
                resourceOperation.setIsCommitOperation(false);
                resourceOperation.setOperation(Operation.getOperationByOperationNum(operation));
                resourceOperation.setResource(resourceNotificationManager.getResourceByResourceNum(resource));
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

            Random random = new Random();
            int randomOpNumber = random.nextInt(RANDOM_BOUND);

            for(int j = 0; j < randomOpNumber; j++) {

                int randomInt = random.nextInt(RANDOM_BOUND);
                int operation = randomInt % 2;
                int resource = randomInt % 26;

                ResourceOperation resourceOperation = new ResourceOperation();
                resourceOperation.setExecutionTime(random.nextInt(RANDOM_BOUND));
                resourceOperation.setIsCommitOperation(false);
                resourceOperation.setOperation(Operation.getOperationByOperationNum(operation));
                resourceOperation.setResource(resourceNotificationManager.getResourceByResourceNum(resource));
                resourceOperation.setAssociatedTransaction(transaction);

                transaction.addResourceOperation(resourceOperation);

            }

            transactions.add(transaction);
        }

        return transactions;
    }



}
