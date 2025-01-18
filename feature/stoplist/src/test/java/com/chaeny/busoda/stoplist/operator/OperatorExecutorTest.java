package com.chaeny.busoda.stoplist.operator;

import static org.junit.Assert.*;
import org.junit.Test;

public class OperatorExecutorTest {

    OperatorExecutor operatorExecutor = new OperatorExecutor();

    @Test
    public void testPlusOperator() {
        String[] args = {"2", "+", "2"};
        int result = operatorExecutor.executeOperation(args);
        assertEquals(result, 4);
    }
}
