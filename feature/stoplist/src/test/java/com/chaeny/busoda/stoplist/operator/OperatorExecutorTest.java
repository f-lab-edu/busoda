package com.chaeny.busoda.stoplist.operator;

import static org.junit.Assert.*;

import org.junit.Test;

public class OperatorExecutorTest {
    PlusOperator plus = new PlusOperator();

    @Test
    public void testPlusOperator() {
        int result = plus.operate(2, 2);
        assertEquals(4, result);
    }

    @Test
    public void testGetPlusOperator() {
        String operator = plus.getOperator();
        assertEquals("+", operator);
    }
}
