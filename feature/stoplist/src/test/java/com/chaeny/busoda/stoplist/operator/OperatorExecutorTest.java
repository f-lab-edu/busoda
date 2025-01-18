package com.chaeny.busoda.stoplist.operator;

import static org.junit.Assert.*;

import org.junit.Test;

public class OperatorExecutorTest {

    @Test
    public void testPlusOperator() {
        PlusOperator plus = new PlusOperator();
        int result = plus.operate(2, 2);
        assertEquals(4, result);
    }
}
