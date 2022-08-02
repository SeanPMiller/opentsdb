package net.opentsdb.query.processor.expressions2.eval;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestLongValue extends FactoryBasedTest {
    public TestLongValue() {
        super(2);
    }

    @Test
    public void testGetValue() {
        final LongValue x = new LongValue(factory, 42L);

        assertEquals(42L, x.getValue());
    }

    @Test
    public void testMakeCopy() {
        final LongValue x = new LongValue(factory, 42L);
        final ExpressionValue y = x.makeCopy();

        assertEquals(x, y); // same underlying value
        assertTrue(x != y); // different object
    }

    @Test
    public void testAddLong() {
        final LongValue x = new LongValue(factory, 7L);
        final LongValue y = new LongValue(factory, 11L);
        x.add(y);

        assertEquals(18L, x.getValue()); // LHS mutated
        assertEquals(11L, y.getValue()); // RHS unchanged
    }

    @Test
    public void testAddLongArray() {
        final LongValue x = new LongValue(factory, 1L);
        final LongArrayValue u = new LongArrayValue(factory, new long[]{0L, 5L});
        x.add(u);

        assertEquals(1L, x.getValue()); // LHS unchanged
        assertEquals(2, u.getLength());
        assertEquals(1L, u.getValueAt(0)); // RHS mutated
        assertEquals(6L, u.getValueAt(1)); // RHS mutated
    }

    @Test
    public void testAddDouble() {
        final LongValue x = new LongValue(factory, 7L);
        final DoubleValue y = new DoubleValue(factory, 1.1);
        ExpressionValue sum = x.add(y);

        assertTrue(sum instanceof DoubleValue);
        assertEquals(8.1, ((DoubleValue) sum).getValue(), 1e-3);
        assertEquals(7L, x.getValue()); // LHS mutated
        assertEquals(8.1, y.getValue(), 1e-3); // RHS unchanged
    }

    @Test
    public void testAddDoubleArray() {
        final LongValue x = new LongValue(factory, 1L);
        final DoubleArrayValue u = new DoubleArrayValue(factory, new double[]{0.1, 0.5});

        ExpressionValue sum = x.add(u);
        assertTrue(sum instanceof DoubleArrayValue);
        assertEquals(1.1, ((DoubleArrayValue) sum).getValueAt(0), 1e-3);
        assertEquals(1.5, ((DoubleArrayValue) sum).getValueAt(1), 1e-3);
        assertEquals(1L, x.getValue()); // LHS unchanged
        assertEquals(2, u.getLength());
        assertEquals(1.1, u.getValueAt(0), 1e-3); // RHS mutated
        assertEquals(1.5, u.getValueAt(1), 1e-3); // RHS mutated
    }

    @Test
    public void testSubLongArray() {
        final LongValue x = new LongValue(factory, 1L);
        final LongArrayValue u = new LongArrayValue(factory, new long[]{1L, 5L});
        ExpressionValue diff = x.subtract(u);

        assertTrue(diff instanceof LongArrayValue);
        assertEquals(0L, ((LongArrayValue) diff).getValueAt(0));
        assertEquals(-4L, ((LongArrayValue) diff).getValueAt(1));
        assertEquals(1L, x.getValue()); // LHS unchanged
        assertEquals(2, u.getLength());
        assertEquals(0L, u.getValueAt(0)); // RHS mutated
        assertEquals(-4L, u.getValueAt(1)); // RHS mutated
    }

    @Test
    public void testMulLongArray() {
        final LongValue x = new LongValue(factory, 2L);
        final LongArrayValue u = new LongArrayValue(factory, new long[]{3L, 5L});
        ExpressionValue product = x.multiply(u);

        assertTrue(product instanceof LongArrayValue);
        assertEquals(6L, ((LongArrayValue) product).getValueAt(0));
        assertEquals(10L, ((LongArrayValue) product).getValueAt(1));
        assertEquals(2L, x.getValue()); // LHS unchanged
        assertEquals(2, u.getLength());
        assertEquals(6L, u.getValueAt(0)); // RHS mutated
        assertEquals(10L, u.getValueAt(1)); // RHS mutated
    }

    @Test
    public void testDivLongArray() {
        final LongValue x = new LongValue(factory, 8L);
        final LongArrayValue u = new LongArrayValue(factory, new long[]{2L, 3L});
        ExpressionValue quotient = x.divide(u);

        assertTrue(quotient instanceof LongArrayValue);
        assertEquals(4L, ((LongArrayValue) quotient).getValueAt(0));
        assertEquals(2L, ((LongArrayValue) quotient).getValueAt(1));
        assertEquals(8L, x.getValue()); // LHS unchanged
        assertEquals(2, u.getLength());
        assertEquals(4L, u.getValueAt(0)); // RHS mutated
        assertEquals(2L, u.getValueAt(1)); // RHS mutated
    }

    @Test
    public void testModLongArray() {
        final LongValue x = new LongValue(factory, 10L);
        final LongArrayValue u = new LongArrayValue(factory, new long[]{2L, 3L});
        ExpressionValue modulus = x.mod(u);

        assertTrue(modulus instanceof LongArrayValue);
        assertEquals(0L, ((LongArrayValue) modulus).getValueAt(0));
        assertEquals(1L, ((LongArrayValue) modulus).getValueAt(1));
        assertEquals(10L, x.getValue()); // LHS unchanged
        assertEquals(2, u.getLength());
        assertEquals(0L, u.getValueAt(0)); // RHS mutated
        assertEquals(1L, u.getValueAt(1)); // RHS mutated
    }

    @Test
    public void testPowLongArray() {
        final LongValue x = new LongValue(factory, 2L);
        final LongArrayValue u = new LongArrayValue(factory, new long[]{2L, 3L});
        ExpressionValue exponent = x.power(u);

        assertTrue(exponent instanceof LongArrayValue);
        assertEquals(4L, ((LongArrayValue) exponent).getValueAt(0));
        assertEquals(8L, ((LongArrayValue) exponent).getValueAt(1));
        assertEquals(2L, x.getValue()); // LHS unchanged
        assertEquals(2, u.getLength());
        assertEquals(4L, u.getValueAt(0)); // RHS mutated
        assertEquals(8L, u.getValueAt(1)); // RHS mutated
    }
}
