package net.opentsdb.query.processor.expressions2;

import com.google.common.collect.ImmutableMap;
import net.opentsdb.data.SecondTimeStamp;
import net.opentsdb.data.TimeSeries;
import net.opentsdb.data.TimeSeriesValue;
import net.opentsdb.data.types.numeric.NumericArrayTimeSeries;
import net.opentsdb.data.types.numeric.NumericArrayType;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


public class TestExpressionNumericArrayIterator extends BaseNumericTest {
    private TimeSeries left;
    private TimeSeries right;

    @Test
    public void addition() throws Exception {
        left = new NumericArrayTimeSeries(LEFT_ID,
                new SecondTimeStamp(60));
        ((NumericArrayTimeSeries) left).add(1);
        ((NumericArrayTimeSeries) left).add(5);
        ((NumericArrayTimeSeries) left).add(2);

        right = new NumericArrayTimeSeries(RIGHT_ID,
                new SecondTimeStamp(60));
        ((NumericArrayTimeSeries) right).add(4);
        ((NumericArrayTimeSeries) right).add(10);
        ((NumericArrayTimeSeries) right).add(8);

        ExpressionNumericArrayIterator iterator =
                new ExpressionNumericArrayIterator(TSDB, node.config(), RESULT,
                        (Map) ImmutableMap.builder()
                                .put("m1", left)
                                .put("m2", right)
                                .build());
        assertTrue(iterator.hasNext());
        TimeSeriesValue<NumericArrayType> value =
                (TimeSeriesValue<NumericArrayType>) iterator.next();
        assertArrayEquals(new long[]{5, 15, 10},
                value.value().longArray());
        assertEquals(0, value.value().offset());
        assertEquals(3, value.value().end());
        assertFalse(iterator.hasNext());


    }


}
