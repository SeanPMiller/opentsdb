package net.opentsdb.query.processor.expressions2.nodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.opentsdb.query.processor.expressions2.OperatorTypeError;
import net.opentsdb.query.processor.expressions2.types.TypeLiteral;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestAddition {
    @Rule
    public ExpectedException exnRule = ExpectedException.none();

    @Test
    public void testChildren() {
        final Addition a = new Addition(new Long(16), new Long(8));
        assertEquals(new Long(16), a.getLeftOperand());
        assertEquals(new Long(8), a.getRightOperand());
    }

    @Test
    public void testParents() {
        final Addition a = new Addition(new Long(16), new Long(8));
        assertNull(a.getParent());
        assertTrue(a.getLeftOperand().getParent() == a);
        assertTrue(a.getRightOperand().getParent() == a);
    }

    @Test
    public void testArity() {
        final Addition a = new Addition(new Long(7), new Long(11));
        assertEquals(2, a.getArity());
    }

    @Test
    public void testGetType() {
        final Addition a = new Addition(new Double(3.14159), new Double(2.71828));
        assertEquals(TypeLiteral.NUMERIC, a.getType());
    }

    @Test
    public void testTypeErrorBooleanRHS() {
        exnRule.expect(OperatorTypeError.class);
        exnRule.expectMessage("for operator '+': type mismatch\n" +
            "given operand(s) of type: NUMERIC * BOOLEAN\n" +
            "expected any of the following: NUMERIC * NUMERIC");
        new Addition(new Double(3.14159), Bool.TRUE);
    }

    @Test
    public void testTypeErrorBooleanLHS() {
        exnRule.expect(OperatorTypeError.class);
        exnRule.expectMessage("for operator '+': type mismatch\n" +
            "given operand(s) of type: BOOLEAN * NUMERIC\n" +
            "expected any of the following: NUMERIC * NUMERIC");
        new Addition(Bool.FALSE, new Double(2.71828));
    }

    @Test
    public void testEquals() {
        final Addition a = new Addition(new Double(3.14159), new Double(2.71828));
        assertEquals(a, a);

        final Addition b = new Addition(new Long(7), new Long(11));
        assertEquals(b, b);
        assertNotEquals(a, b);
        assertNotEquals(b, a);

        final Addition c = new Addition(new Long(7), new Long(11));
        assertEquals(c, c);
        assertEquals(b, c);
    }
}
