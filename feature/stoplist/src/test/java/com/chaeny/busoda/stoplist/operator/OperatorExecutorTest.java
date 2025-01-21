package com.chaeny.busoda.stoplist.operator;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class OperatorExecutorTest {

    OperatorExecutor operatorExe = new OperatorExecutor();

    private int getResult(String... args) {
        return operatorExe.executeOperation(args);
    }

    private ThrowingRunnable getThrowingOperation(String... args) {
        return () -> operatorExe.executeOperation(args);
    }

    @Test
    public void testPlusOperator() {
        assertEquals(4, getResult("2", "+", "2"));
    }

    @Test
    public void testMinusOperator() {
        assertEquals(2, getResult("3", "-", "1"));
    }

    @Test
    public void testMultiplyOperator() {
        assertEquals(20, getResult("4", "x", "5"));
    }

    @Test
    public void testDivideOperator() {
        assertEquals(5, getResult("10", "/", "2"));
    }

    @Test
    public void testDivideOperatorByZero() {
        assertThrows(IllegalArgumentException.class, getThrowingOperation("10", "/", "0"));
    }

    @Test
    public void testMissingArguments() {
        assertThrows(IllegalArgumentException.class, getThrowingOperation("10", "x"));
    }

    @Test
    public void testExtraArguments() {
        assertThrows(IllegalArgumentException.class, getThrowingOperation("10", "x", "9", "x"));
    }

    @Test
    public void testNotAllowedOperator() {
        assertThrows(IllegalArgumentException.class, getThrowingOperation("10", "*", "9"));
    }
}
