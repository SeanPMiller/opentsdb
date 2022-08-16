package net.opentsdb.query.processor.expressions2;

import java.io.IOException;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import net.opentsdb.core.TSDB;
import net.opentsdb.data.TimeSeries;
import net.opentsdb.data.TimeSeriesDataType;
import net.opentsdb.data.TimeSeriesValue;
import net.opentsdb.data.TimeStamp;
import net.opentsdb.data.types.numeric.NumericArrayType;
import net.opentsdb.query.QueryResult;
import net.opentsdb.query.processor.expressions2.ExpressionConfig;
import net.opentsdb.query.processor.expressions.ExpressionResult;
import net.opentsdb.query.processor.expressions2.eval.DoubleArrayValue;
import net.opentsdb.query.processor.expressions2.eval.ExpressionValue;
import net.opentsdb.query.processor.expressions2.eval.LongArrayValue;

public class ExpressionNumericArrayIterator
        extends BaseExpressionNumericIterator<NumericArrayType>
        implements NumericArrayType {

    protected long[] long_values;
    protected double[] double_values;

    protected ExpressionValue value;
    protected QueryResult result;

    public ExpressionNumericArrayIterator(final TSDB tsdb,
                                          final ExpressionConfig config, final QueryResult result,
                                          final Map<String, TimeSeries> sources) {
        super(tsdb, config, sources);

        // We can immediately evaluate because we have arrays.
        this.value = evaluator.evaluate(expression);
        this.result = result;
    }

    @Override
    public TimeSeriesValue<? extends TimeSeriesDataType> next() {
        // TODO

        if (value instanceof DoubleArrayValue) {
            DoubleArrayValue doubleArray = (DoubleArrayValue) value;
            double_values = new double[doubleArray.getLength()];
            for (int i = 0; i < double_values.length; i++) {
                double_values[i] = doubleArray.getValueAt(i);
            }
        } else if (value instanceof LongArrayValue) {
            LongArrayValue longArray = (LongArrayValue) value;
            long_values = new long[longArray.getLength()];
            for (int i = 0; i < long_values.length; i++) {
                long_values[i] = longArray.getValueAt(i);
            }
        }
        // TODO: result is not array

        // The expression has been evaluated
        hasMoreValues = false;

        return this;
    }

    @Override
    public TimeStamp timestamp() {
        return nextTimestamp;
    }

    @Override
    public NumericArrayType value() {
        return this;
    }

    @Override
    public TypeToken<NumericArrayType> type() {
        return NumericArrayType.TYPE;
    }

    @Override
    public int offset() {
        return 0;
    }

    @Override
    public int end() {
        // array length
        return long_values != null ? long_values.length : double_values.length;
    }

    @Override
    public boolean isInteger() {
        return long_values != null;
    }

    @Override
    public long[] longArray() {
        return long_values;
    }

    @Override
    public double[] doubleArray() {
        return double_values;
    }

    @Override
    public void close() throws IOException {
        // TODO
    }
}
