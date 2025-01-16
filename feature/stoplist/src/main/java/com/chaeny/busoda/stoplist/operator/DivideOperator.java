package com.chaeny.busoda.stoplist.operator;

public class DivideOperator implements Operator {
    @Override
    public int operate(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("나누기의 경우에는 0 이 아닌 숫자로 나누어 주세요.");
        }
        return a / b;
    }

    @Override
    public String getOperator() {
        return "/";
    }
}
