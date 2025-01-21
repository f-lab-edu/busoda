package com.chaeny.busoda.stoplist.operator;

public class PlusOperator implements Operator {
    @Override
    public int operate(int a, int b) {
        return a + b;
    }

    @Override
    public String getOperator() {
        return "+";
    }
}
