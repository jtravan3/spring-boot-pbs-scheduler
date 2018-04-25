package com.jtravan.pbs.testers;

import com.jtravan.pbs.generator.TransactionGenerator;
import com.jtravan.pbs.model.Transaction;

import java.util.List;

/**
 * Created by johnravan on 3/30/16.
 */
public class TransactionGeneratorTester {

    public static void main(String[] args) {

        TransactionGenerator generator = TransactionGenerator.getInstance();
        List<Transaction> transactions = generator.generateRandomTransactions(5, 10, false);

        for(Transaction transaction : transactions) {
            System.out.println(transaction.toString());
        }

    }

}
