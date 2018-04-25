package com.jtravan.pbs.generator;

import com.jtravan.pbs.model.ResourceOperation;
import com.jtravan.pbs.model.Transaction;

import java.util.Random;

public class BatchFactory {

    public static Transaction setAbortPercentage(Transaction transaction, int percentage) {

        Random random = new Random();
        float chance = random.nextInt(100);

        if (chance <= percentage) {
            int randomOperation = random.nextInt(transaction.getResourceOperationList().size());

            ResourceOperation resourceOperation = transaction.getResourceOperationList().get(randomOperation);
            resourceOperation.setAbortOperation(true);
        }

        return transaction;

    }

    public static Transaction setAbortPercentageBasedOnCategory(Transaction transaction) {

        if (transaction == null) {
            return transaction;
        }

        switch (transaction.getCategory()) {
            case HCHE:
            case HCLE:
                setAbortPercentage(transaction, 0);
                break;
            case LCHE:
            case LCLE:
                setAbortPercentage(transaction, getRandomNumberInRange(50, 100));
                break;
            default:
                break;
        }

        return transaction;

    }

    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

}
