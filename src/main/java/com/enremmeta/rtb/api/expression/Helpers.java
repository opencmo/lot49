package com.enremmeta.rtb.api.expression;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import com.enremmeta.rtb.Lot49Exception;

public class Helpers {

    private static void println(Object s) {
        System.out.println(s);
    }

    // not(and(22,23,24,or(33,44))
    public static Expression<String> parseStringExpression(String s) throws Lot49Exception {
        Stack stack = new Stack();
        boolean allDone = false;
        try {

            StreamTokenizer st = new StreamTokenizer(new StringReader(s));
            st.quoteChar('"');
            while (true) {
                if (allDone) {
                    if (stack.size() != 1) {
                        throw new Lot49Exception("Exception parsing " + s
                                        + " -- all done but stack is" + stack);
                    }
                    Expression<String> retval = (Expression<String>) stack.get(0);
                    return retval;
                }
                int token = st.nextToken();
                switch (token) {

                    case '"':
                        println("Pushing terminal " + st.sval);
                        stack.push(new Literal<String>(st.sval));
                        break;
                    case ',':
                    case '(':
                        // Do nothing...
                        break;
                    case StreamTokenizer.TT_EOF:
                        allDone = true;
                        break;
                    case ')':
                        List args = new ArrayList();
                        boolean done = false;
                        while (true) {
                            if (done) {
                                break;
                            }
                            if (stack.isEmpty()) {
                                break;
                            }
                            Object popped = stack.pop();
                            if (popped instanceof Expression) {
                                args.add(0, popped);
                            } else {
                                String poppedStr = popped.toString();
                                println("Popped " + popped + ", args " + args);

                                switch (poppedStr.toLowerCase()) {
                                    case "not":
                                        if (args.size() != 1) {
                                            throw new Lot49Exception(
                                                            "not takes exactly one argument, have "
                                                                            + args + ", stack "
                                                                            + stack);
                                        }
                                        Class klass = args.get(0).getClass();
                                        if (Expression.class.isAssignableFrom(klass)) {
                                            Not<String> not = new Not<String>(
                                                            (Expression<String>) args.get(0));
                                            println("Pushing " + not + ", stack: " + stack);
                                            stack.push(not);
                                        } else {
                                            throw new Lot49Exception("Unexpected " + args.get(0)
                                                            + ", stack " + stack);
                                        }
                                        done = true;
                                        break;
                                    case "or":
                                        if (args.size() < 2) {
                                            throw new Lot49Exception(popped
                                                            + " takes two or more arguments, have "
                                                            + args + ", stack " + stack);
                                        }

                                        klass = args.get(0).getClass();
                                        if (Expression.class.isAssignableFrom(klass)) {
                                            Collection<Expression<String>> castArgs =
                                                            (Collection<Expression<String>>) args;
                                            Or<String> or = new Or<String>(castArgs);
                                            println("Pushing " + or + ", stack: " + stack);
                                            stack.push(or);
                                        } else {
                                            throw new Lot49Exception("Unexpected " + args + " ("
                                                            + klass + ")");
                                        }
                                        done = true;
                                        break;
                                    case "and":
                                        if (args.size() < 2) {
                                            throw new Lot49Exception(popped
                                                            + " takes two or more arguments, have "
                                                            + args + ", stack " + stack);
                                        }

                                        klass = args.get(0).getClass();
                                        if (Expression.class.isAssignableFrom(klass)) {
                                            Collection<Expression<String>> castArgs =
                                                            (Collection<Expression<String>>) args;
                                            And<String> and = new And<String>(castArgs);
                                            println("Pushing " + and + ", stack: " + stack);
                                            stack.push(and);
                                        } else {
                                            throw new Lot49Exception("Unexpected " + args);
                                        }
                                        done = true;
                                        break;

                                    case "literal":
                                        args.add(0, popped);
                                        break;
                                    default:
                                        throw new Lot49Exception("Unknown expression: " + popped);
                                }
                            }
                        }
                        break;
                    case StreamTokenizer.TT_WORD:
                        stack.push(st.sval.toLowerCase());
                        break;
                    default:
                        throw new Lot49Exception("Error parsing " + s);
                }

            }
        } catch (Throwable t) {
            throw new Lot49Exception("Error parsing " + s + ", stack: " + stack, t);
        }

    }

}
