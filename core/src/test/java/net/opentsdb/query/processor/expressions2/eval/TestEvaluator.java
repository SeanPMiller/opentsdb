package net.opentsdb.query.processor.expressions2.eval;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import net.opentsdb.query.processor.expressions2.ExpressionParser;
import net.opentsdb.query.processor.expressions2.nodes.ExpressionNode;
import org.junit.Test;

public class TestEvaluator extends FactoryBasedTest {

    final EvaluationContext context = new EvaluationContext.Builder().
            define("nat", factory.makeValueFrom(new long[]{0, 1, 2, 3, 4})).
            define("natSquared", factory.makeValueFrom(new long[]{0, 1, 4, 9, 16})).
            define("ieee754", factory.makeValueFrom(new double[]{1.25, 3d, 0.7, 8.1, 4.75})).
            build();

    final Evaluator evaluator = new Evaluator(factory, context);

    final ExpressionParser parser = new ExpressionParser();

    @Test
    public void testByExample() throws Exception {
        // nat:         {0, 1, 2, 3, 4}
        // natSquared:  {0, 1, 4, 9, 16}
        // ieee754:     {1.25, 3d, 0.7, 8.1, 4.75}
        final Map<String, ExpressionValue> examples = new HashMap<String, ExpressionValue>() {{
            put("true", BooleanConstantValue.TRUE);
            put("FaLsE", BooleanConstantValue.FALSE);
            put("!TrUe", BooleanConstantValue.FALSE);
            put("!false", BooleanConstantValue.TRUE);

            put("1 + 2", factory.makeValueFrom(3L));
            put("2 + 3.14159265", factory.makeValueFrom(5.14159265));
            put("3.14159265 + 2", factory.makeValueFrom(5.14159265));
            put("7 - 3", factory.makeValueFrom(4L));
            put("4 - 5.7", factory.makeValueFrom(-1.7));

            // addition: long array and single value
            put("nat + 1", factory.makeValueFrom(new long[]{1, 2, 3, 4, 5}));
            put("nat + 2.5", factory.makeValueFrom(new double[]{2.5, 3.5, 4.5, 5.5, 6.5}));
            put("nat + 2.0", factory.makeValueFrom(new long[]{2, 3, 4, 5, 6}));
            put("1 + nat", factory.makeValueFrom(new long[]{1, 2, 3, 4, 5}));
            put("2.5 + nat", factory.makeValueFrom(new double[]{2.5, 3.5, 4.5, 5.5, 6.5}));
            put("2.0 + nat", factory.makeValueFrom(new long[]{2, 3, 4, 5, 6}));

            // addition: double array and single value
            put("ieee754 + 1", factory.makeValueFrom(new double[]{2.25, 4d, 1.7, 9.1, 5.75}));
            put("ieee754 + 1.5", factory.makeValueFrom(new double[]{2.75, 4.5, 2.2, 9.6, 6.25}));
            put("1 + ieee754", factory.makeValueFrom(new double[]{2.25, 4d, 1.7, 9.1, 5.75}));
            put("1.5 + ieee754", factory.makeValueFrom(new double[]{2.75, 4.5, 2.2, 9.6, 6.25}));

            // addition: long arrays
            put("nat + natSquared", factory.makeValueFrom(new long[]{0, 2, 6, 12, 20}));
            put("nat + nat + nat", factory.makeValueFrom(new long[]{0, 3, 6, 9, 12}));

            // addition: arrays of mixed type
            put("natSquared + ieee754 + nat", factory.makeValueFrom(new double[]{1.25, 5d, 6.7, 20.1, 24.75}));

            // subtraction: long array and single value
            put("nat - 2", factory.makeValueFrom(new long[]{-2, -1, 0, 1, 2}));
            put("-(nat - 2)", factory.makeValueFrom(new long[]{2, 1, 0, -1, -2}));
            put("nat - 1.5", factory.makeValueFrom(new double[]{-1.5, -0.5, 0.5, 1.5, 2.5}));
            put("nat - 1.0", factory.makeValueFrom(new long[]{-1, 0, 1, 2, 3}));
            put("2 - nat", factory.makeValueFrom(new long[]{2, 1, 0, -1, -2}));
            put("1.5 - nat", factory.makeValueFrom(new double[]{1.5, 0.5, -0.5, -1.5, -2.5}));
            put("1.0 - nat", factory.makeValueFrom(new long[]{1, 0, -1, -2, -3}));

            // subtraction: double array and single value
            put("ieee754 - 3", factory.makeValueFrom(new double[]{-1.75, 0d, -2.3, 5.1, 1.75}));
            put("-(ieee754 - 3)", factory.makeValueFrom(new double[]{1.75, -0d, 2.3, -5.1, -1.75}));
            put("ieee754 - 1.25", factory.makeValueFrom(new double[]{0d, 1.75, -0.55, 6.85, 3.5}));
            put("3 - ieee754", factory.makeValueFrom(new double[]{1.75, 0d, 2.3, -5.1, -1.75}));
            put("1.25 - ieee754", factory.makeValueFrom(new double[]{0d, -1.75, 0.55, -6.85, -3.5}));

            // subtraction: long arrays
            put("natSquared - nat", factory.makeValueFrom(new long[]{0, 0, 2, 6, 12}));
            put("nat - natSquared", factory.makeValueFrom(new long[]{0, 0, -2, -6, -12}));

            // subtraction: arrays of mixed type
            put("natSquared - ieee754 + nat", factory.makeValueFrom(new double[]{-1.25, -1d, 5.3, 3.9, 15.25}));
        }};

        for (final Map.Entry<String, ExpressionValue> example : examples.entrySet()) {
            final ExpressionNode parseTree = parser.parse(example.getKey());
            try (final ExpressionValue result = evaluator.evaluate(parseTree)) {
                System.out.println("expr: " + example.getKey());
                assertEquals(example.getValue(), result);
                System.out.println("done");
            }
        }
    }


    @Test
    public void testMulDivModPow() throws Exception {
        // nat:         {0, 1, 2, 3, 4}
        // natSquared:  {0, 1, 4, 9, 16}
        // ieee754:     {1.25, 3d, 0.7, 8.1, 4.75}

        final Map<String, ExpressionValue> examples = new HashMap<String, ExpressionValue>() {{
            // multiplication: long value and long value
            put("1 * 2 * 3", factory.makeValueFrom(6L));
            // multiplication: double value and double value
            put("1.1 * 2.2 * 3.3", factory.makeValueFrom(7.986));
            // multiplication: long array and long array
            put("nat * nat", factory.makeValueFrom(new long[]{0, 1, 4, 9, 16}));
            // multiplication: long array and long value
            put("nat * 3", factory.makeValueFrom(new long[]{0, 3, 6, 9, 12}));
            // multiplication: long array and double value
            put("nat * 3.5", factory.makeValueFrom(new double[]{0, 3.5, 7d, 10.5, 14d}));
            // multiplication: long array and double array
            put("nat * ieee754", factory.makeValueFrom(new double[]{0, 3d, 1.4, 24.3, 19}));
            // multiplication: double array and long value
            put("2 * ieee754", factory.makeValueFrom(new double[]{2.5, 6d, 1.4, 16.2, 9.5}));
            // multiplication: double array and double value
            put("2.5 * ieee754", factory.makeValueFrom(new double[]{3.125, 7.5, 1.75, 20.25, 11.875}));

            // division: long value and long value
            put("6 / 2 / 2", factory.makeValueFrom(1L));
            // division: double value and double value
            put("6.6 / 2.2", factory.makeValueFrom(3d));
            // division: long array and long array
            put("(nat + 1) / (nat + 1)", factory.makeValueFrom(new long[]{1, 1, 1, 1, 1}));
            // division: long array and long value
            put("nat / 3", factory.makeValueFrom(new long[]{0, 0, 0, 1, 1}));
            // division: long array and double value
            put("nat / 0.5", factory.makeValueFrom(new double[]{0, 2d, 4d, 6d, 8d}));
            // division: long array and double array
            put("nat / ieee754", factory.makeValueFrom(new double[]{0, 0.3333333333333333, 2.857142857142857, 0.3703703703703704, 0.8421052631578947}));
            // division: double array and long value
            put("2 / ieee754", factory.makeValueFrom(new double[]{1.6, 0.6666666666666666, 2.857142857142857, 0.2469135802469136, 0.42105263157894735}));
            // division: double array and double value
            put("2.5 / ieee754", factory.makeValueFrom(new double[]{2.0, 0.8333333333333334, 3.5714285714285716, 0.308641975308642, 0.5263157894736842}));

            // modulus: long value and long value
            put("7 % 4 % 2", factory.makeValueFrom(1L));
            // modulus: double value and double value
            put("4.3 % 2.0 % 0.2", factory.makeValueFrom(0.1));
            // modulus: long array and long array
            put("(nat + 1) % (nat + 1)", factory.makeValueFrom(new long[]{0, 0, 0, 0, 0}));
            // modulus: long array and long value
            put("nat % 3", factory.makeValueFrom(new long[]{0, 1, 2, 0, 1}));
            // modulus: long array and double value
            put("nat % 3.5", factory.makeValueFrom(new double[]{0, 1, 2, 3.0, 0.5}));
            // modulus: long array and double array
            put("nat % ieee754", factory.makeValueFrom(new double[]{0d, 1d, 0.6, 3d, 4d}));
            // modulus: double array and long value
            put("2 % ieee754", factory.makeValueFrom(new double[]{0.75,2.0,0.6,2.0,2.0}));
            // modulus: double array and double value
            put("2.5 % ieee754", factory.makeValueFrom(new double[]{0.0,2.5,0.4,2.5,2.5}));

            // power: long value and long value
            put("2 ^ 2 ^ 2", factory.makeValueFrom(16L));
            // power: double value and double value
            put("2.5 ^ 2", factory.makeValueFrom(6.25));
            // power: long array and long array
            put("nat ^ nat", factory.makeValueFrom(new long[]{1, 1, 4, 27, 256}));
            // power: long array and long value
            put("nat ^ 3", factory.makeValueFrom(new long[]{0, 1, 8, 27, 64}));
            // power: long array and double value
            put("nat ^ 2.5", factory.makeValueFrom(new double[]{0d, 1d, 5.65685424949238, 15.588457268119896, 32d}));

            // miscellaneous
            put("nat ^ (1 + nat) * 2 / ieee754 % 2", factory.makeValueFrom(new double[]{0d,0.6666666666666666,0.8571428571428577,0d,1.1578947368420813}));
        }};

        for (final Map.Entry<String, ExpressionValue> example : examples.entrySet()) {
            final ExpressionNode parseTree = parser.parse(example.getKey());
            try (final ExpressionValue result = evaluator.evaluate(parseTree)) {
                System.out.println("expr: " + example.getKey());
                assertEquals(example.getValue(), result);
                System.out.println("done");
            }
        }
    }

    @Test
    public void testComparison() throws Exception {
        // nat:         {0, 1, 2, 3, 4}
        // natSquared:  {0, 1, 4, 9, 16}
        // ieee754:     {1.25, 3d, 0.7, 8.1, 4.75}

        final Map<String, ExpressionValue> examples = new HashMap<String, ExpressionValue>() {{
            // Comparison between numbers
            put("1 < 2", BooleanConstantValue.TRUE);
            put("1 <= 1", BooleanConstantValue.TRUE);
            put("1 <= 0", BooleanConstantValue.FALSE);
            put("1 <= 0.9", BooleanConstantValue.FALSE);
            put("1 == 1.0", BooleanConstantValue.TRUE);
            put("1 == 1.0 && 2 > 1", BooleanConstantValue.TRUE);
            put("1 >= 2 && 2 > 1", BooleanConstantValue.FALSE);
            put("1 >= 2 || 2 > 1", BooleanConstantValue.TRUE);
            put("1 != 1", BooleanConstantValue.FALSE);
            put("1 == 1", BooleanConstantValue.TRUE);
            put("true == true", BooleanConstantValue.TRUE);
            put("false == true", BooleanConstantValue.FALSE);

            // Comparison with metric involved
            // GT and GTE
            put("5 > 2", BooleanConstantValue.TRUE);
            put("2 >= 2", BooleanConstantValue.TRUE);
            put("2.0 >= 2", BooleanConstantValue.TRUE);
            put("2.0 >= 2.0", BooleanConstantValue.TRUE);
            put("2.0 >= 2.1", BooleanConstantValue.FALSE);
            put("nat > 2", BooleanConstantValue.FALSE);
            put("nat > 0", BooleanConstantValue.FALSE);
            put("nat >= 0", BooleanConstantValue.TRUE);
            put("4 >= nat", BooleanConstantValue.TRUE);
            put("4.0 >= nat", BooleanConstantValue.TRUE);
            put("3 >= nat", BooleanConstantValue.FALSE);
            put("3 > nat", BooleanConstantValue.FALSE);
            put("4 > nat", BooleanConstantValue.FALSE);
            put("4.0 > nat", BooleanConstantValue.FALSE);
            put("ieee754 >= 4", BooleanConstantValue.FALSE);
            put("ieee754 >= 0.7", BooleanConstantValue.TRUE);
            put("ieee754 > 0.7", BooleanConstantValue.FALSE);
            put("8.1 > ieee754", BooleanConstantValue.FALSE);
            put("8 > ieee754", BooleanConstantValue.FALSE);
            put("8.1 >= ieee754", BooleanConstantValue.TRUE);

            // LT and LTE
            put("5 < 2", BooleanConstantValue.FALSE);
            put("1 < 2", BooleanConstantValue.TRUE);
            put("2 <= 2", BooleanConstantValue.TRUE);
            put("2.0 <= 2", BooleanConstantValue.TRUE);
            put("2.0 <= 2.0", BooleanConstantValue.TRUE);
            put("2.0 <= 2.1", BooleanConstantValue.TRUE);
            put("nat < 2", BooleanConstantValue.FALSE);
            put("nat < 4", BooleanConstantValue.FALSE);
            put("nat <= 4", BooleanConstantValue.TRUE);
            put("0 <= nat", BooleanConstantValue.TRUE);
            put("0 < nat", BooleanConstantValue.FALSE);
            put("1 < nat", BooleanConstantValue.FALSE);
            put("-1 <= nat", BooleanConstantValue.TRUE);
            put("ieee754 <= 4", BooleanConstantValue.FALSE);
            put("ieee754 < 0.7", BooleanConstantValue.FALSE);
            put("ieee754 > 0.7", BooleanConstantValue.FALSE);
            put("0.7 <= ieee754", BooleanConstantValue.TRUE);
            put("8.1 < ieee754", BooleanConstantValue.FALSE);
            put("8 < ieee754", BooleanConstantValue.FALSE);
            put("-1 <= ieee754", BooleanConstantValue.TRUE);
        }};

        for (final Map.Entry<String, ExpressionValue> example : examples.entrySet()) {
            final ExpressionNode parseTree = parser.parse(example.getKey());
            try (final ExpressionValue result = evaluator.evaluate(parseTree)) {
                System.out.println("expr: " + example.getKey());
                assertEquals(example.getValue(), result);
                System.out.println("done");
            }
        }
    }

    @Test
    public void testTernary() throws Exception {
        // nat:         {0, 1, 2, 3, 4}
        // natSquared:  {0, 1, 4, 9, 16}
        // ieee754:     {1.25, 3d, 0.7, 8.1, 4.75}

        final Map<String, ExpressionValue> examples = new HashMap<String, ExpressionValue>() {{
            put("1 < 2 ? 1 : 2", factory.makeValueFrom(1L));
            put("1 > 2 ? 1 : 2", factory.makeValueFrom(2L));
            put("1 > 2 || 2 < 3 ? 1 : 2", factory.makeValueFrom(1L));
            put("1 > 2 && 2 < 3 ? 1 : 2", factory.makeValueFrom(2L));
            put("((1 > 2) && (2 < 3)) ? 1 : 2", factory.makeValueFrom(2L));
            put("(((1 > 2) && (2 < 3)) ? 1 : 2)", factory.makeValueFrom(2L));
            put("(((1 > 2) && (2 < 3)) ? nat + 1 : nat + 2)", factory.makeValueFrom(new long[]{2, 3, 4, 5, 6}));
            put("(((1 > 2) && (2 < 3)) ? (nat + 1) : (nat + 2))", factory.makeValueFrom(new long[]{2, 3, 4, 5, 6}));
            put("(2.28 < 5.30 ? (nat + 1) : (nat + 2))", factory.makeValueFrom(new long[]{1, 2, 3, 4, 5}));
            put("(2.28 < 5.30 ? ieee754 * 2 : (nat + 2))", factory.makeValueFrom(new double[]{2.5, 6d, 1.4, 16.2, 9.5}));
            put("truE ? ieee754 * 2 : (nat + 2)", factory.makeValueFrom(new double[]{2.5, 6d, 1.4, 16.2, 9.5}));
            put("1 < 2 ? 2<3?2+3:4+5 : 2", factory.makeValueFrom(5L));
            put("1 >= 2 ? (2<3?2+3:4+5) : (2>3?100:200.2)", factory.makeValueFrom(200.2));
        }};

        for (final Map.Entry<String, ExpressionValue> example : examples.entrySet()) {
            final ExpressionNode parseTree = parser.parse(example.getKey());
            try (final ExpressionValue result = evaluator.evaluate(parseTree)) {
                System.out.println("expr: " + example.getKey());
                assertEquals(example.getValue(), result);
                System.out.println("done");
            }
        }
    }
}
