package net.opentsdb.query.processor.expressions2;

import com.google.common.collect.Lists;
import net.opentsdb.data.*;
import net.opentsdb.data.types.numeric.NumericArrayTimeSeries;
import net.opentsdb.data.types.numeric.NumericArrayType;
import net.opentsdb.data.types.numeric.NumericType;
import net.opentsdb.query.DefaultQueryResultId;
import net.opentsdb.query.QueryFillPolicy;
import net.opentsdb.query.QueryResult;
import net.opentsdb.query.QueryResultId;
import net.opentsdb.query.interpolation.types.numeric.NumericInterpolatorConfig;
import net.opentsdb.query.joins.JoinConfig;
import net.opentsdb.query.joins.Joiner;
import net.opentsdb.query.pojo.FillPolicy;
import net.opentsdb.query.processor.expressions2.ExpressionConfig;
import net.opentsdb.query.processor.expressions.ExpressionParseNode;
import net.opentsdb.query.processor.expressions2.ExpressionResult;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestExpressionResult extends BaseNumericTest {

    private ExpressionQueryNode node;
    private ExpressionConfig config;

    @Before
    public void before() throws Exception {
        node = mock(ExpressionQueryNode.class);

        ExpressionIteratorFactory factory = new ExpressionIteratorFactory();
        when(node.factory()).thenReturn(factory);

        config = (ExpressionConfig) ExpressionConfig.newBuilder()
                .setExpression("m1 + m2")
                .setJoinConfig(JOIN_CONFIG)
                .addInterpolatorConfig(NUMERIC_CONFIG)
                .setId("e1")
                .setId("expression")
                .build();


        when(node.config()).thenReturn(config);
    }

    @Test
    public void ctor() throws Exception {
        ExpressionResult result = new ExpressionResult(node);
    }


    @Test
    public void testJoin() {
        ExpressionResult result = new ExpressionResult(node);
        assertEquals(0, result.time_series.size());

        ExpressionResult result1 = mock(ExpressionResult.class);
        ExpressionResult result2 = mock(ExpressionResult.class);

        TimeSeriesId M1_ID = BaseTimeSeriesStringId.newBuilder()
                .setMetric("m1")
                .build();
        TimeSeriesId M2_ID = BaseTimeSeriesStringId.newBuilder()
                .setMetric("m2")
                .build();

        TimeSeries m1_ts = new NumericArrayTimeSeries(M1_ID,
                new SecondTimeStamp(60));
        ((NumericArrayTimeSeries) m1_ts).add(1);
        ((NumericArrayTimeSeries) m1_ts).add(5);
        ((NumericArrayTimeSeries) m1_ts).add(2);

        TimeSeries m2_ts = new NumericArrayTimeSeries(M2_ID,
                new SecondTimeStamp(60));
        ((NumericArrayTimeSeries) m2_ts).add(4);
        ((NumericArrayTimeSeries) m2_ts).add(10);
        ((NumericArrayTimeSeries) m2_ts).add(8);

        List<TimeSeries> m1_list = Lists.newArrayList(m1_ts);
        List<TimeSeries> m2_list = Lists.newArrayList(m2_ts);
        when(result1.timeSeries()).thenReturn(m1_list);
        when(result2.timeSeries()).thenReturn(m2_list);

        Map<QueryResultId, QueryResult> results = new HashMap() {{
            put(new DefaultQueryResultId("m1", "m1"), result1);
            put(new DefaultQueryResultId("m2", "m2"), result2);
        }};
        when(node.results()).thenReturn(results);

        result.join();
        assertEquals(1, result.time_series.size());

        when(node.pipelineContext()).thenReturn(CONTEXT);
        ExpressionTimeSeries ts = (ExpressionTimeSeries) result.time_series.get(0);

        // Test iterator to evaluate the expression
        Optional<TypedTimeSeriesIterator<? extends TimeSeriesDataType>> optional = ts.iterator(NumericArrayType.TYPE);
        assertTrue(optional.isPresent());
        TypedTimeSeriesIterator<? extends TimeSeriesDataType> iterator = optional.get();
        assertTrue(iterator.hasNext());

        TimeSeriesValue<NumericArrayType> value = (TimeSeriesValue<NumericArrayType>) iterator.next();
        assertArrayEquals(new long[]{5, 15, 10},
                value.value().longArray());

    }
}
