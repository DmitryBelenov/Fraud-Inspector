package jep.function;

import org.nfunk.jep.function.PostfixMathCommandI;

import java.util.Stack;

public class JepStatFunction implements PostfixMathCommandI {

    @Override
//    @SuppressWarnings("unchecked")
    public void run(Stack stack) {
        Object o2 = stack.pop();
        Object o1 = stack.pop();
    }

    @Override
    public int getNumberOfParameters() {
        return 1;
    }

    @Override
    public void setCurNumberOfParameters(int i) {

    }
}
