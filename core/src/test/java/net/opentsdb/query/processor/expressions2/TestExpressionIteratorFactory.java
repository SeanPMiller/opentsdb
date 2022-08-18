package net.opentsdb.query.processor.expressions2;

import com.google.common.collect.Lists;
import net.opentsdb.core.DefaultRegistry;
import net.opentsdb.core.MockTSDB;
import net.opentsdb.core.TSDBPlugin;
import net.opentsdb.data.TimeSeriesDataSource;
import net.opentsdb.data.TimeSeriesDataSourceFactory;
import net.opentsdb.data.types.numeric.NumericArrayType;
import net.opentsdb.data.types.numeric.NumericType;
import net.opentsdb.query.*;
import net.opentsdb.query.filter.MetricLiteralFilter;
import net.opentsdb.query.interpolation.types.numeric.NumericInterpolatorConfig;
import net.opentsdb.query.joins.JoinConfig;
import net.opentsdb.query.plan.DefaultQueryPlanner;
import net.opentsdb.query.pojo.FillPolicy;
import net.opentsdb.query.processor.downsample.DownsampleConfig;
import net.opentsdb.query.processor.expressions2.nodes.Addition;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestExpressionIteratorFactory {
    protected static NumericInterpolatorConfig NUMERIC_CONFIG;
    protected static JoinConfig JOIN_CONFIG;
    private static MockTSDB TSDB;
    private static MockTSDSFactory STORE_FACTORY;
    protected static QueryNode SINK;
    private static List<TimeSeriesDataSource> STORE_NODES;
    private static MockTSDSFactory S1;
    private static MockTSDSFactory S2;

    private QueryPipelineContext context;

    @BeforeClass
    public static void beforeClass() throws Exception {
        NUMERIC_CONFIG =
                (NumericInterpolatorConfig) NumericInterpolatorConfig.newBuilder()
                        .setFillPolicy(FillPolicy.NOT_A_NUMBER)
                        .setRealFillPolicy(QueryFillPolicy.FillWithRealPolicy.NONE)
                        .setDataType(NumericType.TYPE.toString())
                        .build();

        JOIN_CONFIG = JoinConfig.newBuilder()
                .setJoinType(JoinConfig.JoinType.NATURAL)
                .setId("expression")
                .build();

        TSDB = new MockTSDB();
        STORE_FACTORY = new MockTSDSFactory("Default");
        STORE_FACTORY.setupGraph = true;

        SINK = mock(QueryNode.class);
        STORE_NODES = Lists.newArrayList();
        STORE_FACTORY.store_nodes = STORE_NODES;
        S1 = new MockTSDSFactory("s1");
        S1.setupGraph = true;
        S2 = new MockTSDSFactory("s2");
        S2.setupGraph = true;

        TSDB.registry = new DefaultRegistry(TSDB);
        ((DefaultRegistry) TSDB.registry).initialize(true);
        ((DefaultRegistry) TSDB.registry).registerPlugin(
                TimeSeriesDataSourceFactory.class, null, (TSDBPlugin) STORE_FACTORY);
        ((DefaultRegistry) TSDB.registry).registerPlugin(
                TimeSeriesDataSourceFactory.class, "s1", (TSDBPlugin) S1);
        ((DefaultRegistry) TSDB.registry).registerPlugin(
                TimeSeriesDataSourceFactory.class, "s2", (TSDBPlugin) S2);

        QueryNodeConfig sink_config = mock(QueryNodeConfig.class);
        when(sink_config.getId()).thenReturn("SINK");
        when(SINK.config()).thenReturn(sink_config);
    }

    @Before
    public void before() throws Exception {
        context = mock(QueryPipelineContext.class);
        when(context.tsdb()).thenReturn(TSDB);
        when(context.queryContext()).thenReturn(mock(QueryContext.class));

        STORE_NODES.clear();
    }

    @Test
    public void ctor() throws Exception {
        ExpressionIteratorFactory factory = new ExpressionIteratorFactory();
        assertTrue(factory.types().contains(NumericArrayType.TYPE));
        assertEquals(1, factory.types().size());
    }

    @Test
    public void testWithPlanner() throws Exception {
        List<QueryNodeConfig> graph = Lists.newArrayList(
                ExpressionConfig.newBuilder()
                        .setExpression("m1 + 42")
                        .setJoinConfig(JOIN_CONFIG)
                        .addInterpolatorConfig(NUMERIC_CONFIG)
                        .setId("expression")
                        .addSource("m1")
                        .build(),
                DefaultTimeSeriesDataSourceConfig.newBuilder()
                        .setMetric(MetricLiteralFilter.newBuilder()
                                .setMetric("sys.cpu.user")
                                .build())
                        .setFilterId("f1")
                        .setId("m1")
                        .build());
        SemanticQuery query = SemanticQuery.newBuilder()
                .setMode(QueryMode.SINGLE)
                .setStart("1514764800")
                .setEnd("1514768400")
                .setExecutionGraph(graph)
                .build();

        when(context.query()).thenReturn(query);

        DefaultQueryPlanner planner =
                new DefaultQueryPlanner(context, SINK);
        planner.plan(null).join();

        assertSame(STORE_NODES.get(0), planner.sources().get(0));
        assertEquals(3, planner.graph().nodes().size());
        assertEquals(1, planner.serializationSources().size());

        // graph: sink -> expression -> m1
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("expression"),
                planner.nodeForId("m1")));
        assertTrue(planner.graph().hasEdgeConnecting(SINK, planner.nodeForId("expression")));
        assertFalse(planner.graph().hasEdgeConnecting(SINK, planner.nodeForId("m1")));

        QueryNodeConfig b1 = planner.configNodeForId("expression");
        assertEquals("Expression", b1.getType());

        assertTrue((b1 instanceof ExpressionConfig));
        ExpressionConfig p1 = (ExpressionConfig) b1;
        assertEquals("expression", p1.getId());
        assertEquals("m1 + 42", p1.getExpression());
        assertTrue((p1.getParseTree() instanceof Addition));
        assertEquals(new DefaultQueryResultId("expression", "expression"),
                p1.resultIds().get(0));
    }


    @Test
    public void testWithComplexPlanning() throws Exception {
        List<QueryNodeConfig> graph = Lists.newArrayList(
                ExpressionConfig.newBuilder()
                        .setExpression("m1 + m2 + m3 + 28")
                        .setJoinConfig(JOIN_CONFIG)
                        .addInterpolatorConfig(NUMERIC_CONFIG)
                        .setId("e1")
                        // add sources
                        .addSource("m1")
                        .addSource("m2")
                        .addSource("m3")
                        .build(),
                DefaultTimeSeriesDataSourceConfig.newBuilder()
                        .setMetric(MetricLiteralFilter.newBuilder()
                                .setMetric("sys.cpu.user")
                                .build())
                        .setFilterId("f1")
                        .setId("m1")
                        .build(),
                DefaultTimeSeriesDataSourceConfig.newBuilder()
                        .setMetric(MetricLiteralFilter.newBuilder()
                                .setMetric("sys.cpu.foo1")
                                .build())
                        .setFilterId("f1")
                        .setId("m2")
                        .build(),
                DefaultTimeSeriesDataSourceConfig.newBuilder()
                        .setMetric(MetricLiteralFilter.newBuilder()
                                .setMetric("sys.cpu.foo2")
                                .build())
                        .setFilterId("f1")
                        .setId("m3")
                        .build());

        SemanticQuery query = SemanticQuery.newBuilder()
                .setMode(QueryMode.SINGLE)
                .setStart("1514764800")
                .setEnd("1514768400")
                .setExecutionGraph(graph)
                .build();

        when(context.query()).thenReturn(query);

        DefaultQueryPlanner planner =
                new DefaultQueryPlanner(context, SINK);
        planner.plan(null).join();

        assertSame(STORE_NODES.get(0), planner.sources().get(0));
        assertEquals(5, planner.graph().nodes().size());
        assertEquals(1, planner.serializationSources().size());

        // graph: sink -> expression -> (m1, m2, m3)
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("e1"),
                planner.nodeForId("m1")));
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("e1"),
                planner.nodeForId("m2")));
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("e1"),
                planner.nodeForId("m3")));
        assertTrue(planner.graph().hasEdgeConnecting(SINK, planner.nodeForId("e1")));

        assertFalse(planner.graph().hasEdgeConnecting(planner.nodeForId("m1"), planner.nodeForId("m2")));
        assertFalse(planner.graph().hasEdgeConnecting(planner.nodeForId("m2"), planner.nodeForId("m3")));
        assertFalse(planner.graph().hasEdgeConnecting(planner.nodeForId("m1"), planner.nodeForId("m3")));
        assertFalse(planner.graph().hasEdgeConnecting(SINK, planner.nodeForId("m1")));
        assertFalse(planner.graph().hasEdgeConnecting(SINK, planner.nodeForId("m2")));
        assertFalse(planner.graph().hasEdgeConnecting(SINK, planner.nodeForId("m3")));

        QueryNodeConfig b1 = planner.configNodeForId("e1");
        assertEquals("Expression", b1.getType());

        assertTrue((b1 instanceof ExpressionConfig));
        ExpressionConfig p1 = (ExpressionConfig) b1;
        assertEquals("e1", p1.getId());
        assertEquals("m1 + m2 + m3 + 28", p1.getExpression());
        assertTrue((p1.getParseTree() instanceof Addition));
        assertEquals(new DefaultQueryResultId("e1", "e1"),
                p1.resultIds().get(0));
    }


    @Test
    public void setupGraph1MetricThroughNode() throws Exception {
        List<QueryNodeConfig> graph = Lists.newArrayList(
                DefaultTimeSeriesDataSourceConfig.newBuilder()
                        .setMetric(MetricLiteralFilter.newBuilder()
                                .setMetric("sys.cpu.user")
                                .build())
                        .setFilterId("f1")
                        .setId("m1")
                        .build(),
                DownsampleConfig.newBuilder()
                        .setAggregator("sum")
                        .setInterval("1m")
                        .addInterpolatorConfig(NUMERIC_CONFIG)
                        .setId("downsample")
                        .addSource("m1")
                        .build(),
                ExpressionConfig.newBuilder()
                        .setExpression("m1 + 42")
                        .setJoinConfig((JoinConfig) JoinConfig.newBuilder()
                                .setJoinType(JoinConfig.JoinType.NATURAL)
                                .build())
                        .addInterpolatorConfig(NUMERIC_CONFIG)
                        .setId("e1")
                        .addSource("downsample")
                        .build());

        SemanticQuery query = SemanticQuery.newBuilder()
                .setMode(QueryMode.SINGLE)
                .setStart("1514764800")
                .setEnd("1514768400")
                .setExecutionGraph(graph)
                .build();

        when(context.query()).thenReturn(query);

        DefaultQueryPlanner planner =
                new DefaultQueryPlanner(context, SINK);
        planner.plan(null).join();

        assertSame(STORE_NODES.get(0), planner.sources().get(0));
        assertEquals(4, planner.graph().nodes().size());
        assertEquals(1, planner.serializationSources().size());

        // graph: sink -> e1 -> downsample -> m1
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("e1"),
                planner.nodeForId("downsample")));
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("downsample"),
                planner.nodeForId("m1")));
        assertTrue(planner.graph().hasEdgeConnecting(SINK, planner.nodeForId("e1")));
        assertFalse(planner.graph().hasEdgeConnecting(planner.nodeForId("e1"), planner.nodeForId("m1")));

        QueryNodeConfig b1 = planner.configNodeForId("e1");
        assertEquals("Expression", b1.getType());

        ExpressionConfig p1 = (ExpressionConfig) b1;
        assertEquals("e1", p1.getId());
        assertEquals(1, p1.resultIds().size());
        assertEquals(new DefaultQueryResultId("e1", "e1"),
                p1.resultIds().get(0));
    }


    @Test
    public void setupGraph3MetricsThroughNode() throws Exception {
        List<QueryNodeConfig> graph = Lists.newArrayList(
                // metrics
                DefaultTimeSeriesDataSourceConfig.newBuilder()
                        .setMetric(MetricLiteralFilter.newBuilder()
                                .setMetric("sys.cpu.user")
                                .build())
                        .setFilterId("f1")
                        .setId("m1")
                        .build(),
                DefaultTimeSeriesDataSourceConfig.newBuilder()
                        .setMetric(MetricLiteralFilter.newBuilder()
                                .setMetric("sys.cpu.sys")
                                .build())
                        .setFilterId("f1")
                        .setId("m2")
                        .build(),
                DefaultTimeSeriesDataSourceConfig.newBuilder()
                        .setMetric(MetricLiteralFilter.newBuilder()
                                .setMetric("sys.cpu.idle")
                                .build())
                        .setFilterId("f1")
                        .setId("m3")
                        .build(),

                // downsample
                DownsampleConfig.newBuilder()
                        .setAggregator("sum")
                        .setInterval("1m")
                        .addInterpolatorConfig(NUMERIC_CONFIG)
                        .setId("downsample")
                        .setSources(Lists.newArrayList("m1", "m2", "m3"))
                        .build(),

                // expression
                ExpressionConfig.newBuilder()
                        .setExpression("m1 + m2 + m3")
                        .setJoinConfig(JOIN_CONFIG)
                        .addInterpolatorConfig(NUMERIC_CONFIG)
                        .setId("e1")
                        .addSource("downsample")
                        .build());

        SemanticQuery query = SemanticQuery.newBuilder()
                .setMode(QueryMode.SINGLE)
                .setStart("1514764800")
                .setEnd("1514768400")
                .setExecutionGraph(graph)
                .build();

        when(context.query()).thenReturn(query);

        DefaultQueryPlanner planner =
                new DefaultQueryPlanner(context, SINK);
        planner.plan(null).join();

        assertSame(STORE_NODES.get(0), planner.sources().get(0));
        assertEquals(6, planner.graph().nodes().size());
        assertEquals(1, planner.serializationSources().size());

        // graph: sink -> expression -> downsample -> (m1, m2, m3)
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("e1"),
                planner.nodeForId("downsample")));
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("downsample"),
                planner.nodeForId("m1")));
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("downsample"),
                planner.nodeForId("m2")));
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("downsample"),
                planner.nodeForId("m3")));
        assertTrue(planner.graph().hasEdgeConnecting(SINK, planner.nodeForId("e1")));

        QueryNodeConfig b1 = planner.configNodeForId("e1");
        assertEquals("Expression", b1.getType());
        ExpressionConfig p1 = (ExpressionConfig) b1;

        assertEquals("e1", p1.getId());
        assertEquals(new DefaultQueryResultId("e1", "e1"),
                p1.resultIds().get(0));

        // It requires the correct result id to perform query
        List<QueryResultId> resultIds = p1.getResultsNeeded();
        assertEquals(3, resultIds.size());
        assertTrue(resultIds.contains(new DefaultQueryResultId("downsample", "m1")));
        assertTrue(resultIds.contains(new DefaultQueryResultId("downsample", "m2")));
        assertTrue(resultIds.contains(new DefaultQueryResultId("downsample", "m3")));

    }

    @Test
    public void setupGraph3MetricsComplexThroughNode() throws Exception {
        List<QueryNodeConfig> graph = Lists.newArrayList(
                DefaultTimeSeriesDataSourceConfig.newBuilder()
                        .setMetric(MetricLiteralFilter.newBuilder()
                                .setMetric("sys.cpu.user")
                                .build())
                        .setFilterId("f1")
                        .setId("m1")
                        .build(),
                DefaultTimeSeriesDataSourceConfig.newBuilder()
                        .setMetric(MetricLiteralFilter.newBuilder()
                                .setMetric("sys.cpu.sys")
                                .build())
                        .setFilterId("f1")
                        .setId("m2")
                        .build(),
                DefaultTimeSeriesDataSourceConfig.newBuilder()
                        .setMetric(MetricLiteralFilter.newBuilder()
                                .setMetric("sys.cpu.idle")
                                .build())
                        .setFilterId("f1")
                        .setId("m3")
                        .build(),
                DownsampleConfig.newBuilder()
                        .setAggregator("sum")
                        .setInterval("1m")
                        .addInterpolatorConfig(NUMERIC_CONFIG)
                        .setId("downsample")
                        .setSources(Lists.newArrayList("m1", "m3"))
                        .build(),
                ExpressionConfig.newBuilder()
                        .setExpression("(m1 * 1024) + (m2 * 1024) + (m3 * 1024)")
                        .setJoinConfig((JoinConfig) JoinConfig.newBuilder()
                                .setJoinType(JoinConfig.JoinType.NATURAL)
                                .build())
                        .addInterpolatorConfig(NUMERIC_CONFIG)
                        .addSource("downsample")
                        .addSource("m2")
                        .setId("e1")
                        .build());
        SemanticQuery query = SemanticQuery.newBuilder()
                .setMode(QueryMode.SINGLE)
                .setStart("1514764800")
                .setEnd("1514768400")
                .setExecutionGraph(graph)
                .build();

        when(context.query()).thenReturn(query);

        DefaultQueryPlanner planner =
                new DefaultQueryPlanner(context, SINK);
        planner.plan(null).join();

        assertSame(STORE_NODES.get(0), planner.sources().get(0));
        assertEquals(6, planner.graph().nodes().size());
        assertEquals(1, planner.serializationSources().size());

        // graph: sink -> expression -> downsample -> (m1, m3)
        //                           -> m2
        assertTrue(planner.graph().hasEdgeConnecting(SINK, planner.nodeForId("e1")));
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("e1"),
                planner.nodeForId("downsample")));
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("e1"),
                planner.nodeForId("m2")));
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("downsample"),
                planner.nodeForId("m1")));
        assertTrue(planner.graph().hasEdgeConnecting(planner.nodeForId("downsample"),
                planner.nodeForId("m3")));

        assertFalse(planner.graph().hasEdgeConnecting(planner.nodeForId("e1"),
                planner.nodeForId("m1")));
        assertFalse(planner.graph().hasEdgeConnecting(planner.nodeForId("e1"),
                planner.nodeForId("m3")));
        assertFalse(planner.graph().hasEdgeConnecting(planner.nodeForId("downsample"),
                planner.nodeForId("m2")));

        ExpressionConfig node = (ExpressionConfig) planner.configNodeForId("e1");
        // Make sure the config requires the downstream results
        assertEquals(3, node.getResultsNeeded().size());
        assertTrue(node.getResultsNeeded().contains(new DefaultQueryResultId("m2", "m2")));
        assertTrue(node.getResultsNeeded().contains(new DefaultQueryResultId("downsample", "m1")));
        assertTrue(node.getResultsNeeded().contains(new DefaultQueryResultId("downsample", "m3")));
    }
}
