package jep.function;

import org.nfunk.jep.function.PostfixMathCommandI;

import java.util.Stack;

public class JepAttrFunction implements PostfixMathCommandI {

    @Override
//    @SuppressWarnings("unchecked")
    public void run(Stack stack) {
        Object o1 = stack.pop();
    }

    @Override
    public int getNumberOfParameters() {
        return 2;
    }

    @Override
    public void setCurNumberOfParameters(int i) {
    }
}
