package com.jtravan.pbs.services;

import com.jtravan.pbs.model.TestCase;

public class TestCaseFactory {

    public static final TestCase getTestCaseByTestCaseNumber(Long testCaseNumber) {
        if (testCaseNumber == null) {
            return null;
        }

        if(testCaseNumber == 1) {
            return new TestCase(100,0,0,0);
        }
        if(testCaseNumber == 2) {
            return new TestCase(75,25,0,0);
        }
        if(testCaseNumber == 3) {
            return new TestCase(50,25,25,0);
        }
        if(testCaseNumber == 4) {
            return new TestCase(25,25,25,25);
        }
        if(testCaseNumber == 5) {
            return new TestCase(0,25,25,50);
        }
        if(testCaseNumber == 6) {
            return new TestCase(0,0,25,75);
        }
        if(testCaseNumber == 7) {
            return new TestCase(0,0,0,100);
        }

        return null;
    }
}
