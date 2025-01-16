package com.chaeny.busoda.stoplist.operator;

import java.util.ArrayList;
import java.util.List;

public class OperatorExecutor {
    public static void main(String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("모두 입력해주세요.");
        }

        if (args.length > 4) {
            throw new IllegalArgumentException("5 x 5 형식으로 입력해주세요");
        }

        int a = Integer.parseInt(args[0]);
        String operatorSymbol = args[1];
        int b = Integer.parseInt(args[2]);

        List<Operator> operators = new ArrayList<>();
        operators.add(new PlusOperator());
        operators.add(new MinusOperator());
        operators.add(new MultiplyOperator());
        operators.add(new DivideOperator());

        StringBuilder allowedOperators = new StringBuilder();

        for (Operator op : operators) {
            allowedOperators.append(op.getOperator()).append(" ");

            if (op.getOperator().equals(operatorSymbol)) {
                getValue(op.operate(a, b));
                return;
            }
        }
        throw new IllegalArgumentException("허용되는 연산자는 " + allowedOperators + "가 있습니다.");
    }

    public static int getValue(int result) {
        return result;
    }
}
