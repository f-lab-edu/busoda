package com.chaeny.busoda.stoplist.operator;

import static org.junit.Assert.*;

import org.junit.Test;

public class OperatorExecutorTest {

    OperatorExecutor operatorExe = new OperatorExecutor();

    @Test
    public void testPlusOperator() {
        String[] args = {"2", "+", "2"};
        int result = operatorExe.executeOperation(args);
        assertEquals(4, result);
    }

    @Test
    public void testMinusOperator() {
        String[] args = {"3", "-", "1"};
        int result = operatorExe.executeOperation(args);
        assertEquals(2, result);
    }

    @Test
    public void testMultiplyOperator() {
        String[] args = {"4", "x", "5"};
        int result = operatorExe.executeOperation(args);
        assertEquals(20, result);
    }

    @Test
    public void testDivideOperator() {
        String[] args = {"10", "/", "2"};
        int result = operatorExe.executeOperation(args);
        assertEquals(5, result);
    }

    @Test
    public void testDivideOperatorByZero() {
        String[] args = {"10", "/", "0"};
        assertThrows(IllegalArgumentException.class, () -> operatorExe.executeOperation(args));
    }

    @Test
    public void testMissingArguments() {
        String[] args = {"10", "x"};
        assertThrows(IllegalArgumentException.class, () -> operatorExe.executeOperation(args));
    }

    @Test
    public void testExtraArguments() {
        String[] args = {"10", "x", "9", "x"};
        assertThrows(IllegalArgumentException.class, () -> operatorExe.executeOperation(args));
    }

    @Test
    public void testNotAllowedOperator() {
        String[] args = {"10", "*", "9"};
        assertThrows(IllegalArgumentException.class, () -> operatorExe.executeOperation(args));
    }
}
