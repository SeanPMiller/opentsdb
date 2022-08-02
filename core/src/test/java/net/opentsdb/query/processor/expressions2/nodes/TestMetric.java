package net.opentsdb.query.processor.expressions2.nodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import net.opentsdb.query.processor.expressions2.types.TypeLiteral;
import org.junit.Test;

public class TestMetric {
    @Test
    public void testGetName() {
        final Metric m = new Metric("m1");
        assertEquals("m1", m.getName());
    }

    @Test
    public void testGetType() {
        final Metric m = new Metric("e2");
        assertEquals(TypeLiteral.NUMERIC, m.getType());
    }

    @Test
    public void testEquals() {
        final Metric m2 = new Metric("m2");
        assertEquals(m2, m2);

        final Metric m2copy = new Metric("m2");
        assertEquals(m2, m2copy);

        final Metric e1 = new Metric("e1");
        assertNotEquals(m2, e1);
        assertNotEquals(e1, m2);
    }
}
