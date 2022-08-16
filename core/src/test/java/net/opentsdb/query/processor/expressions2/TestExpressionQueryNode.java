package net.opentsdb.query.processor.expressions2;

import com.google.common.collect.Lists;
import net.opentsdb.core.TSDB;
import net.opentsdb.data.BaseTimeSeriesStringId;
import net.opentsdb.data.SecondTimeStamp;
import net.opentsdb.data.TimeSeries;
import net.opentsdb.data.TimeSeriesId;
import net.opentsdb.data.types.numeric.NumericArrayTimeSeries;
import net.opentsdb.query.*;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

public class TestExpressionQueryNode extends ConfigBasedTest {

    protected ExpressionConfig CONFIG;

    protected ExpressionIteratorFactory FACTORY;

    protected QueryPipelineContext CONTEXT;

    protected TSDB TSDB;

    private QueryNode UPSTREAM;

    protected TimeSeries M1;

    protected TimeSeries M2;

    protected  DefaultQueryResultId ID1;
    protected  DefaultQueryResultId ID2;

    public TestExpressionQueryNode() {
        CONFIG = ExpressionConfig.newBuilder()
                .setExpression("m1 + m2")
                .setJoinConfig(JOIN_CONFIG)
                .addVariableInterpolator("a", NUMERIC_CONFIG)
                .setAs("some.metric.name")
                .addInterpolatorConfig(NUMERIC_CONFIG)
                .setId("e1")
                .setAllowMetricReuse(true)
                .setForceFloatingPointDivision(true)
                .setInfectiousNan(false)
                .addNeededResult(new DefaultQueryResultId("m1", "m1"))
                .addNeededResult(new DefaultQueryResultId("m2", "m2"))
                .build();

        FACTORY = mock(ExpressionIteratorFactory.class);

        CONTEXT = mock(QueryPipelineContext.class);

        TSDB = mock(TSDB.class);

        when(CONTEXT.tsdb()).thenReturn(TSDB);

        TimeSeriesId M1_ID = BaseTimeSeriesStringId.newBuilder()
                .setMetric("m1")
                .build();
        TimeSeriesId M2_ID = BaseTimeSeriesStringId.newBuilder()
                .setMetric("m2")
                .build();

        M1 = new NumericArrayTimeSeries(M1_ID,
                new SecondTimeStamp(60));
        ((NumericArrayTimeSeries) M1).add(1);
        ((NumericArrayTimeSeries) M1).add(5);
        ((NumericArrayTimeSeries) M1).add(2);

        M2 = new NumericArrayTimeSeries(M2_ID,
                new SecondTimeStamp(60));
        ((NumericArrayTimeSeries) M2).add(4);
        ((NumericArrayTimeSeries) M2).add(10);
        ((NumericArrayTimeSeries) M2).add(8);

        ID1 = new DefaultQueryResultId("m1", "m1");
        ID2 = new DefaultQueryResultId("m2", "m2");
        UPSTREAM = mock(QueryNode.class);
        when(CONTEXT.upstream(any(QueryNode.class)))
                .thenReturn(Lists.newArrayList(UPSTREAM));
    }

    @Test
    public void ctor() throws Exception {
        ExpressionQueryNode node = new ExpressionQueryNode(FACTORY, CONTEXT, CONFIG);
        assertSame(FACTORY, node.factory());
        assertSame(CONTEXT, node.pipelineContext());
        assertSame(CONFIG, node.config());
        assertEquals(2, node.results().size());
        assertTrue(node.results().containsKey(ID1));
        assertTrue(node.results().containsKey(ID2));
    }

    @Test
    public void testOnNext() throws Exception {
        ExpressionQueryNode node = new ExpressionQueryNode(FACTORY, CONTEXT, CONFIG);
        node.initialize(null);

        QueryResult r1 = mock(QueryResult.class);
        when(r1.dataSource()).thenReturn(ID1);
        QueryNode n1 = mock(QueryNode.class);
        QueryNodeConfig c1 = mock(QueryNodeConfig.class);
        when(c1.getId()).thenReturn("a");
        when(n1.config()).thenReturn(c1);
        when(r1.source()).thenReturn(n1);
        when(r1.timeSeries()).thenReturn(Lists.newArrayList(M1));

        QueryResult r2 = mock(QueryResult.class);
        when(r2.dataSource()).thenReturn(ID2);
        QueryNodeConfig c2 = mock(QueryNodeConfig.class);
        when(c2.getId()).thenReturn("b");
        QueryNode n2 = mock(QueryNode.class);
        when(n2.config()).thenReturn(c2);
        when(r2.source()).thenReturn(n2);
        when(r2.timeSeries()).thenReturn(Lists.newArrayList(M2));

        // Offers the first required result
        node.onNext(r1);
        assertSame(r1, node.results.get(ID1));
        verify(UPSTREAM, never()).onNext(any(QueryResult.class));

        // Offers the second required result (now have all results)
        node.onNext(r2);
        assertSame(r2, node.results.get(ID2));
        verify(UPSTREAM, times(1)).onNext(any(QueryResult.class));

        assertEquals(1, node.result.timeSeries().size());
        assertTrue((node.result.timeSeries().get(0) instanceof ExpressionTimeSeries));
        ExpressionTimeSeries ts = (ExpressionTimeSeries) node.result.timeSeries().get(0);
        assertEquals(2, ts.sourceMap.size());
        assertTrue((ts.sourceMap.containsKey("m1")));
        assertTrue((ts.sourceMap.containsKey("m2")));


    }
}
