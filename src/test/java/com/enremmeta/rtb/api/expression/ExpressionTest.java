package com.enremmeta.rtb.api.expression;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.test.cases.Lot49Test;

public class ExpressionTest extends Lot49Test {

    @Test
    public void testParser() {
        String exprStr = ("AND(OR(\"rock\", \"paper\", \"scissors\"), noT(\"foo\"))");

        Expression<String> parsedExpr = null;
        try {
            parsedExpr = Helpers.parseStringExpression(exprStr);
        } catch (Lot49Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        Expression<String> expectedExpr = new And<String>(
                        new Or<String>("rock", "paper", "scissors"), new Not<String>("foo"));
        System.out.println(expectedExpr + " == " + parsedExpr + " = "
                        + expectedExpr.equals(parsedExpr));
        // TODO Have to still fix this equality business. It's an order thing.
        // Assert.assertEquals(expectedExpr, parsedExpr);

        exprStr = "AND(\"X\", OR(\"A\",\"B\",\"C\"), NOT(\"Z\"), NOT(AND(\"1\",\"2\",\"3\")))";
        expectedExpr = new And("X").and(new Or("A", "B", "C"), new Not("Z"),
                        new Not(new And("1", "2", "3")));

        // exprStr = "not(and(\"22\",\"33\",\"44\",or(\"55\",\"66\"))";
    }

    // Real life expressions
    @Test
    public void testParser2() {
        String exprStr = "AND(OR(\"268:fp:180\", \"269:fp:180\", \"272:fp:180\", \"274:fp:180\", \"276:fp:180\"),NOT(\"134:fp:180\"),NOT(\"137:fp:180\"),NOT(\"138:fp:180\"),NOT(\"139:fp:180\"),NOT(\"140:fp:180\"))";
        Expression<String> parsedExpr = null;
        try {
            parsedExpr = Helpers.parseStringExpression(exprStr);
        } catch (Lot49Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        System.out.println(parsedExpr);

    }

    @Test
    public void testEvaluator() {

        Expression<String> and;
        Set<String> test;
        String exprStr = ("AND(OR(\"rock\", \"paper\", \"scissors\"), \"foo\")");

        Expression<String> expr = null;
        try {
            expr = Helpers.parseStringExpression(exprStr);
        } catch (Lot49Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        test = new HashSet<String>() {
            {
                add("rock");
                add("foo");
            }
        };
        Assert.assertTrue(expr.eval(test));

        test = new HashSet<String>() {
            {
                add("rock");
                add("paper");
                add("foo");
            }
        };
        Assert.assertTrue(expr.eval(test));

        test = new HashSet<String>() {
            {
                add("rock");
                add("paper");
            }
        };
        Assert.assertFalse(expr.eval(test));

        exprStr = ("AND(OR(\"rock\", \"paper\", \"scissors\"), NOT(\"foo\"))");

        expr = null;
        try {
            expr = Helpers.parseStringExpression(exprStr);
        } catch (Lot49Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        test = new HashSet<String>() {
            {
                add("rock");
                add("foo");
            }
        };
        Assert.assertFalse(expr.eval(test));

        test = new HashSet<String>() {
            {
                add("rock");
                add("paper");
                add("foo");
            }
        };
        Assert.assertFalse(expr.eval(test));

        test = new HashSet<String>() {
            {
                add("rock");
                add("paper");
            }
        };
        Assert.assertTrue(expr.eval(test));

        test = new HashSet<String>() {
            {
                add("rock");
                add("paper");
                add("bar");
            }
        };
        Assert.assertTrue(expr.eval(test));

    }

}
