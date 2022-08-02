package net.opentsdb.query.processor.expressions2.eval;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestDoubleValue extends FactoryBasedTest {
    public TestDoubleValue() {
        super(2);
    }

    @Test
    public void testGetValue() {
        final DoubleValue x = new DoubleValue(factory, 3.14159265);

        assertEquals(3.14159265, x.getValue(), 1e-9);
    }

    @Test
    public void testMakeCopy() {
        final DoubleValue x = new DoubleValue(factory, 2.71828);
        final ExpressionValue y = x.makeCopy();

        assertEquals(x, y); // same underlying value
        assertTrue(x != y); // different object
    }

    @Test
    public void testAddDouble() {
        final DoubleValue x = new DoubleValue(factory, 2.5);
        final DoubleValue y = new DoubleValue(factory, 9.6);
        x.add(y);

        assertEquals(12.1, x.getValue(), 1e-2); // LHS mutated
        assertEquals(9.6, y.getValue(), 1e-2); // RHS unchanged
    }

    @Test
    public void testAddDoubleArray() {
        final DoubleValue x = new DoubleValue(factory, 7d);
        final DoubleArrayValue u = new DoubleArrayValue(factory, new double[] {0.65, 4.3});
        x.add(u);

        assertEquals(7d, x.getValue(), 1e-1); // LHS unchanged
        assertEquals(2, u.getLength());
        assertEquals(7.65, u.getValueAt(0), 1e-3); // RHS mutated
        assertEquals(11.3, u.getValueAt(1), 1e-3); // RHS mutated
    }

    @Test
    public void testAddLong() {
        final DoubleValue x = new DoubleValue(factory, 2.5);
        final LongValue y = new LongValue(factory, 3L);
        x.add(y);

        assertEquals(5.5, x.getValue(), 1e-2); // LHS mutated
        assertEquals(3L, y.getValue()); // RHS unchanged
    }

    @Test
    public void testAddLongArray() {
        final DoubleValue x = new DoubleValue(factory, 7.1);
        final LongArrayValue u = new LongArrayValue(factory, new long[] {1, 2});
        ExpressionValue sum = x.add(u);
        assertTrue(sum instanceof DoubleArrayValue);
        assertEquals(8.1, ((DoubleArrayValue)sum).getValueAt(0), 1e-3);
        assertEquals(9.1, ((DoubleArrayValue)sum).getValueAt(1), 1e-3);
        assertEquals(7.1, x.getValue(), 1e-1); // LHS unchanged
        assertFalse(u.isLive()); // closed

        // Double can be casted to long
        final DoubleValue x2 = new DoubleValue(factory, 7d);
        final LongArrayValue u2 = new LongArrayValue(factory, new long[] {1, 2});
        ExpressionValue sum2 = x2.add(u2);
        assertTrue(sum2 instanceof LongArrayValue);
        assertEquals(8L, ((LongArrayValue)sum2).getValueAt(0));
        assertEquals(9L, ((LongArrayValue)sum2).getValueAt(1));
        assertEquals(7d, x.getValue(), 1e-1); // LHS unchanged
        assertEquals(2, u2.getLength());
        assertEquals(8L, u2.getValueAt(0)); // RHS mutated
        assertEquals(9L, u2.getValueAt(1)); // RHS mutated
    }

    @Test
    public void testSubDouble() {
        final DoubleValue x = new DoubleValue(factory, 7d);
        final DoubleValue u = new DoubleValue(factory, 3d);

        ExpressionValue diff = x.subtract(u);
        assertTrue(diff instanceof DoubleValue);
        assertEquals(4d, ((DoubleValue)diff).getValue(), 1e-3);
        assertEquals(4d, x.getValue(), 1e-1); // LHS mutated
        assertEquals(3d, u.getValue(), 1e-3); // RHS unchanged
    }

    @Test
    public void testSubDoubleArray() {
        final DoubleValue x = new DoubleValue(factory, 7.65);
        final DoubleArrayValue u = new DoubleArrayValue(factory, new double[] {0.65, 4.3});

        ExpressionValue diff = x.subtract(u);
        assertTrue(diff instanceof DoubleArrayValue);
        assertEquals(7d, ((DoubleArrayValue)diff).getValueAt(0), 1e-3);
        assertEquals(3.35, ((DoubleArrayValue)diff).getValueAt(1), 1e-3);
        assertEquals(7.65, x.getValue(), 1e-1); // LHS unchanged
        assertEquals(2, u.getLength());
        assertEquals(7d, u.getValueAt(0), 1e-3); // RHS mutated
        assertEquals(3.35, u.getValueAt(1), 1e-3); // RHS mutated
    }

    @Test
    public void testMulDouble() {
        final DoubleValue x = new DoubleValue(factory, 7d);
        final DoubleValue u = new DoubleValue(factory, 3d);

        ExpressionValue product = x.multiply(u);
        assertTrue(product instanceof DoubleValue);
        assertEquals(21d, ((DoubleValue)product).getValue(), 1e-3);
        assertEquals(21d, x.getValue(), 1e-1); // LHS mutated
        assertEquals(3d, u.getValue(), 1e-3); // RHS unchanged
    }

    @Test
    public void testMulDoubleArray() {
        final DoubleValue x = new DoubleValue(factory, 7d);
        final DoubleArrayValue u = new DoubleArrayValue(factory, new double[] {0.65, 4.3});

        ExpressionValue product = x.multiply(u);
        assertTrue(product instanceof DoubleArrayValue);
        assertEquals(4.55, ((DoubleArrayValue)product).getValueAt(0), 1e-3);
        assertEquals(30.1, ((DoubleArrayValue)product).getValueAt(1), 1e-3);
        assertEquals(7d, x.getValue(), 1e-1); // LHS unchanged
        assertEquals(2, u.getLength());
        assertEquals(4.55, u.getValueAt(0), 1e-3); // RHS mutated
        assertEquals(30.1, u.getValueAt(1), 1e-3); // RHS mutated
    }



    @Test
    public void testDivDouble() {
        final DoubleValue x = new DoubleValue(factory, 9.3d);
        final DoubleValue u = new DoubleValue(factory, 3d);

        ExpressionValue quotient = x.divide(u);
        assertTrue(quotient instanceof DoubleValue);
        assertEquals(3.1, ((DoubleValue)quotient).getValue(), 1e-3);
        assertEquals(3.1, x.getValue(), 1e-1); // LHS mutated
        assertEquals(3d, u.getValue(), 1e-3); // RHS unchanged
    }

    @Test
    public void testDivDoubleArray() {
        final DoubleValue x = new DoubleValue(factory, 7d);
        final DoubleArrayValue u = new DoubleArrayValue(factory, new double[] {0.5, 1.0});

        ExpressionValue quotient = x.divide(u);
        assertTrue(quotient instanceof DoubleArrayValue);
        assertEquals(14.0, ((DoubleArrayValue)quotient).getValueAt(0), 1e-3);
        assertEquals(7.0, ((DoubleArrayValue)quotient).getValueAt(1), 1e-3);
        assertEquals(2, u.getLength());
        assertEquals(14.0, u.getValueAt(0), 1e-3); // RHS mutated
        assertEquals(7.0, u.getValueAt(1), 1e-3); // RHS mutated
    }

    @Test
    public void testModDouble() {
        final DoubleValue x = new DoubleValue(factory, 9.3d);
        final DoubleValue u = new DoubleValue(factory, 3d);

        ExpressionValue modulus = x.mod(u);
        assertTrue(modulus instanceof DoubleValue);
        assertEquals(0.3, ((DoubleValue)modulus).getValue(), 1e-3);
        assertEquals(0.3, x.getValue(), 1e-1); // LHS mutated
        assertEquals(3d, u.getValue(), 1e-3); // RHS unchanged
    }

    @Test
    public void testModDoubleArray() {
        final DoubleValue x = new DoubleValue(factory, 7d);
        final DoubleArrayValue u = new DoubleArrayValue(factory, new double[] {0.8, 6.0});

        ExpressionValue modulus = x.mod(u);
        assertTrue(modulus instanceof DoubleArrayValue);
        assertEquals(0.6, ((DoubleArrayValue)modulus).getValueAt(0), 1e-3);
        assertEquals(1.0, ((DoubleArrayValue)modulus).getValueAt(1), 1e-3);
        assertEquals(2, u.getLength());
        assertEquals(0.6, u.getValueAt(0), 1e-3); // RHS mutated
        assertEquals(1.0, u.getValueAt(1), 1e-3); // RHS mutated
    }

    @Test
    public void testPowLong() {
        final DoubleValue x = new DoubleValue(factory, 2d);
        final LongValue u = new LongValue(factory, 3L);

        ExpressionValue exponent = x.power(u);
        assertTrue(exponent instanceof DoubleValue);
        assertEquals(8d, ((DoubleValue)exponent).getValue(), 1e-3);
        assertEquals(8d, x.getValue(), 1e-1); // LHS mutated
        assertEquals(3d, u.getValue(), 1e-3); // RHS unchanged
    }

    @Test
    public void testPowDouble() {
        final DoubleValue x = new DoubleValue(factory, 2d);
        final DoubleValue u = new DoubleValue(factory, 3d);

        ExpressionValue exponent = x.power(u);
        assertTrue(exponent instanceof DoubleValue);
        assertEquals(8d, ((DoubleValue)exponent).getValue(), 1e-3);
        assertEquals(8d, x.getValue(), 1e-1); // LHS mutated
        assertEquals(3d, u.getValue(), 1e-3); // RHS unchanged
    }

    @Test
    public void testPowDoubleArray() {
        final DoubleValue x = new DoubleValue(factory, 7d);
        final DoubleArrayValue u = new DoubleArrayValue(factory, new double[] {1.0, 2.0});

        ExpressionValue exponent = x.power(u);
        assertTrue(exponent instanceof DoubleArrayValue);
        assertEquals(7d, ((DoubleArrayValue)exponent).getValueAt(0), 1e-3);
        assertEquals(49d, ((DoubleArrayValue)exponent).getValueAt(1), 1e-3);
        assertEquals(2, u.getLength());
        assertEquals(7d, u.getValueAt(0), 1e-3); // RHS mutated
        assertEquals(49d, u.getValueAt(1), 1e-3); // RHS mutated
    }

    // TODO: more permutations of unit tests
}
