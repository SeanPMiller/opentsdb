package net.opentsdb.query.processor.expressions2;

import net.opentsdb.data.types.numeric.NumericType;
import net.opentsdb.query.QueryFillPolicy;
import net.opentsdb.query.interpolation.types.numeric.NumericInterpolatorConfig;
import net.opentsdb.query.joins.JoinConfig;
import net.opentsdb.query.pojo.FillPolicy;
import net.opentsdb.query.processor.expressions2.ExpressionConfig;
import net.opentsdb.query.processor.expressions2.nodes.Addition;
import net.opentsdb.query.processor.expressions2.nodes.Long;
import net.opentsdb.query.processor.expressions2.nodes.Metric;
import net.opentsdb.query.processor.expressions2.nodes.Power;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

public class TestExpressionConfig extends ConfigBasedTest {

    @Test
    public void builder() throws Exception {
        ExpressionConfig config = ExpressionConfig.newBuilder()
                .setExpression("a + b")
                .setJoinConfig(JOIN_CONFIG)
                .addVariableInterpolator("a", NUMERIC_CONFIG)
                .setAs("some.metric.name")
                .addInterpolatorConfig(NUMERIC_CONFIG)
                .setId("e1")
                .setAllowMetricReuse(true)
                .setForceFloatingPointDivision(true)
                .setInfectiousNan(false)
                .build();

        assertEquals("e1", config.getId());
        assertEquals("some.metric.name", config.getAs());
        assertEquals("a + b", config.getExpression());
        assertEquals(JoinConfig.JoinType.INNER, config.getJoin().getJoinType());
        assertEquals("host", config.getJoin().getJoins().get("host"));
        assertSame(NUMERIC_CONFIG, config.interpolatorConfig(NumericType.TYPE));
        assertSame(NUMERIC_CONFIG, config.getVariableInterpolators().get("a").get(0));
        assertTrue(config.isAllow_metric_reuse());
        assertFalse(config.isInfectious_nan());
        assertTrue(config.isForce_floating_point_division());
        assertEquals(new Addition(new Metric("a"), new Metric("b")), config.getParseTree());

        config = ExpressionConfig.newBuilder()
                .setExpression("a + 5")
                .setJoinConfig(JOIN_CONFIG)
                //.setAs("some.metric.name") // defaults
                .addVariableInterpolator("a", NUMERIC_CONFIG)
                .addInterpolatorConfig(NUMERIC_CONFIG)
                .setId("e1")
                .build();
        assertEquals("e1", config.getId());
        assertEquals("e1", config.getAs());
        assertEquals("a + 5", config.getExpression());
        assertEquals(JoinConfig.JoinType.INNER, config.getJoin().getJoinType());
        assertEquals("host", config.getJoin().getJoins().get("host"));
        assertSame(NUMERIC_CONFIG, config.interpolatorConfig(NumericType.TYPE));
        assertSame(NUMERIC_CONFIG, config.getVariableInterpolators().get("a").get(0));
        assertEquals(new Addition(new Metric("a"), new Long(5)), config.getParseTree());

        try {
            ExpressionConfig.newBuilder()
                    //.setExpression("a + b")
                    .setJoinConfig((JoinConfig) JoinConfig.newBuilder()
                            .addJoins("host", "host")
                            .setJoinType(JoinConfig.JoinType.INNER)
                            .setId("jc")
                            .build())
                    .setAs("e1")
                    .addInterpolatorConfig(NUMERIC_CONFIG)
                    .setId("e1")
                    .build();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        try {
            ExpressionConfig.newBuilder()
                    .setExpression("")
                    .setJoinConfig(JOIN_CONFIG)
                    .setAs("e1")
                    .addInterpolatorConfig(NUMERIC_CONFIG)
                    .setId("e1")
                    .build();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        try {
            ExpressionConfig.newBuilder()
                    .setExpression("a + b")
                    //.setJoinConfig(JOIN_CONFIG)
                    .setAs("e1")
                    .addInterpolatorConfig(NUMERIC_CONFIG)
                    .setId("e1")
                    .build();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        try {
            ExpressionConfig.newBuilder()
                    .setExpression("a + b")
                    .setJoinConfig(JOIN_CONFIG)
                    .setAs("e1")
                    //.addInterpolatorConfig(NUMERIC_CONFIG)
                    .setId("e1")
                    .build();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
}
