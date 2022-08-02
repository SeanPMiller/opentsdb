package net.opentsdb.query.processor.expressions2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import net.opentsdb.query.processor.expressions2.nodes.*;
import net.opentsdb.query.processor.expressions2.nodes.Double;
import net.opentsdb.query.processor.expressions2.nodes.Long;
import org.junit.Test;

public class TestExpressionParser {

    @Test
    public void testParseByExample() {
        Map<String, ExpressionNode> arithmeticExamples = new HashMap<String, ExpressionNode>() {{
            put("0", new Long(0));
            put("2.5", new Double(2.5));
            put("1 + 2", new Addition(new Long(1), new Long(2)));
            put("3.14159 + 2.71828", new Addition(new Double(3.14159), new Double(2.71828)));
            put("m1 + 0", new Addition(new Metric("m1"), new Long(0)));
            put("m1 + 1 + 2", new Addition(new Addition(new Metric("m1"), new Long(1)), new Long(2)));
            put("25 + m3", new Addition(new Long(25), new Metric("m3")));
            put("1 + (2 + 3)", new Addition(new Long(1), new Addition(new Long(2), new Long(3))));
            put("-42", new NumericNegation(new Long(42)));
            put("-foo", new NumericNegation(new Metric("foo")));
            put("-(m1 + 12.5)", new NumericNegation(new Addition(new Metric("m1"), new Double(12.5))));
            put("m1 + m2", new Addition(new Metric("m1"), new Metric("m2")));
            put("m1 * m2", new Multiplication(new Metric("m1"), new Metric("m2")));
            put("5 ^ 2", new Power(new Long(5), new Long(2)));
            put("5 ^ 2 + 2", new Addition(new Power(new Long(5), new Long(2)), new Long(2)));
            put("2 + 5 ^ 2", new Addition(new Long(2), new Power(new Long(5), new Long(2))));
            put("5 ^ (2 + 2)", new Power(new Long(5), new Addition(new Long(2), new Long(2))));
        }};

        for (final Map.Entry<String, ExpressionNode> example : arithmeticExamples.entrySet()) {
            final ExpressionParser parser = new ExpressionParser();
            final ExpressionNode actual = parser.parse(example.getKey());
            assertEquals(example.getValue(), actual);
        }
    }

    @Test
    public void testUseCountTracking() {
        final String expression = "m1 - 2 + m1";
        final ExpressionParser parser = new ExpressionParser();
        final ExpressionNode actual = parser.parse(expression);
        final ExpressionNode expected = new Addition(new Subtraction(new Metric("m1"), new Long(2)), new Metric("m1"));
        assertEquals(expected, actual);

        final Addition add = (Addition) actual;
        final Metric secondUse = (Metric) add.getRightOperand();
        final Subtraction sub = (Subtraction) add.getLeftOperand();
        final Metric firstUse = (Metric) sub.getLeftOperand();
        assertTrue(firstUse == secondUse); // same object
        assertEquals(2, firstUse.getUses()); // used twice
    }


    @Test
    public void testParseComparisonExpressions() {
        Map<String, ExpressionNode> logicalExamples = new HashMap<String, ExpressionNode>() {{
            put("TrUe", Bool.TRUE);
            put("FaLsE", Bool.FALSE);
            put("!true", new LogicalNegation(Bool.TRUE));
            put("1 == 1", new Equal(new Long(1), new Long(1)));
            put("1 != 1", new NotEq(new Long(1), new Long(1)));
            put("!(1 != 1)", new LogicalNegation(new NotEq(new Long(1), new Long(1))));
            put("1 < 1", new Lt(new Long(1), new Long(1)));
            put("1 <= 1", new Lte(new Long(1), new Long(1)));
            put("1 > 1", new Gt(new Long(1), new Long(1)));
            put("1 >= 1", new Gte(new Long(1), new Long(1)));
            put("1 == 1 ? 2 : 3", new TernaryOperator(new Equal(new Long(1), new Long(1)), new Long(2), new Long(3)));
            put("(1 == 1 ? 2 : 3) + 2", new Addition(new TernaryOperator(new Equal(new Long(1), new Long(1)), new Long(2),
                    new Long(3)), new Long(2)));
            put("(1 == 1 ? 2 : 3) + (1 == 1 ? 2 : 3)", new Addition(
                    new TernaryOperator(new Equal(new Long(1), new Long(1)), new Long(2), new Long(3)),
                    new TernaryOperator(new Equal(new Long(1), new Long(1)), new Long(2), new Long(3))));
        }};

        for (final Map.Entry<String, ExpressionNode> example : logicalExamples.entrySet()) {
            final ExpressionParser parser = new ExpressionParser();
            final ExpressionNode actual = parser.parse(example.getKey());
            assertEquals(example.getValue(), actual);
        }
    }

    @Test
    public void testParseLogicalExpressions() {
        Map<String, ExpressionNode> logicalExamples = new HashMap<String, ExpressionNode>() {{
            put("TrUe && false", new And(Bool.TRUE, Bool.FALSE));
            put("falsE || true", new Or(Bool.FALSE, Bool.TRUE));
            put("!true && false", new And(new LogicalNegation(Bool.TRUE),Bool.FALSE));
            put("!true && false || true", new Or(new And(new LogicalNegation(Bool.TRUE),Bool.FALSE), Bool.TRUE));
            put("(1>2) || 2 <3", new Or(new Gt(new Long(1), new Long(2)), new Lt(new Long(2), new Long(3))));
        }};

        for (final Map.Entry<String, ExpressionNode> example : logicalExamples.entrySet()) {
            final ExpressionParser parser = new ExpressionParser();
            final ExpressionNode actual = parser.parse(example.getKey());
            assertEquals(example.getValue(), actual);
        }
    }
}
