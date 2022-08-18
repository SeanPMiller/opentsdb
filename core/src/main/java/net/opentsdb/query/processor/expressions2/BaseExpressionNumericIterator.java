package net.opentsdb.query.processor.expressions2;

import com.google.common.reflect.TypeToken;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.opentsdb.core.TSDB;
import net.opentsdb.data.*;
import net.opentsdb.data.types.numeric.NumericArrayType;
import net.opentsdb.data.types.numeric.NumericType;
import net.opentsdb.pools.ArrayObjectPool;
import net.opentsdb.pools.DoubleArrayPool;
import net.opentsdb.pools.LongArrayPool;
import net.opentsdb.query.QueryIterator;
import net.opentsdb.query.QueryNode;
import net.opentsdb.query.processor.expressions2.ExpressionConfig;
import net.opentsdb.query.processor.expressions2.eval.*;
import net.opentsdb.query.processor.expressions2.nodes.DefaultExpressionVisitor;
import net.opentsdb.query.processor.expressions2.nodes.ExpressionNode;
import net.opentsdb.query.processor.expressions2.nodes.Metric;

public abstract class BaseExpressionNumericIterator<T extends TimeSeriesDataType>
        implements QueryIterator, TimeSeriesValue<T> {
    static final class Metadata {
        Set<String> variablesUsed;

        public Metadata() {
            variablesUsed = new HashSet<>();
        }
    }

    static final class MetadataCollector extends DefaultExpressionVisitor {
        final Metadata meta;

        MetadataCollector(final Metadata meta) {
            this.meta = meta;
        }

        @Override
        public void leaveMetric(final Metric m) {
            meta.variablesUsed.add(m.getName());
        }
    }

    /**
     * TODO
     */
    protected TimeStamp nextTimestamp;

    /**
     * Whether this expression has been evaluated
     */
    protected boolean hasMoreValues;

    /**
     * This factory enables any evaluator to create new objects.
     */
    protected final ExpressionFactory factory;

    /**
     * Evaluator to evaluate an expression tree
     */
    protected final Evaluator evaluator;

    /**
     * This builder collects data as it arrives from other nodes.
     */
    protected final EvaluationContext.Builder contextBuilder;

    /**
     * This metadata object describes the expression.
     */
    protected final Metadata metadata;

    /**
     * This graph represents the expression.
     */
    protected final ExpressionNode expression;

    /**
     * C-tor.
     *
     * @param tsdb
     * @param config
     * @param sources
     */
    public BaseExpressionNumericIterator(final TSDB tsdb,
                                         final ExpressionConfig config,
                                         final Map<String, TimeSeries> sources) {
        // Initialize state objects.
        nextTimestamp = new MillisecondTimeStamp(0);
        nextTimestamp.setMax();
        hasMoreValues = true;

        // Build options object based on expression configuration.
        final EvaluationOptions options = new EvaluationOptions.Builder().
                setInfectiousNaN(config.isInfectious_nan()).
                setForceFloatingPointDivision(config.isForce_floating_point_division()). // TODO: make this configurable
                        setAllowMetricReuse(config.isAllow_metric_reuse()). // contexts will not be reusable
                        build();

        // Fetch the pools we need from TSDB.
        final ArrayObjectPool longPool = (ArrayObjectPool) tsdb.getRegistry().
                getObjectPool(LongArrayPool.TYPE);
        final ArrayObjectPool doublePool = (ArrayObjectPool) tsdb.getRegistry().
                getObjectPool(DoubleArrayPool.TYPE);

        // With the above, we can build our factory.
        factory = new ExpressionFactory(options, longPool, doublePool);

        // We will collect data here as it comes in.
        contextBuilder = new EvaluationContext.Builder();

        // Analyze the expression to be evaluated.
        expression = config.getParseTree();

        // We can now collect metadata from the expression.
        metadata = new Metadata();
//        final MetadataCollector collector = new MetadataCollector(metadata);
//        expression.accept(collector);

        // Get all metric values and define them in context
        for (String m : config.getSourcesAlias()) {
            if (!sources.containsKey(m)) continue; // TODO: does not have the metric
            TypedTimeSeriesIterator<?> iterator = sources.get(m).iterator(NumericArrayType.TYPE).get();
            TimeSeriesValue<NumericArrayType> value = (TimeSeriesValue<NumericArrayType>) iterator.next();
            nextTimestamp.update(value.timestamp());

            // Create expression value of matched type for evaluation
            if (value.value().isInteger()) {
                contextBuilder.define(
                        m, factory.makeValueFrom(Arrays.copyOfRange(value.value().longArray(), value.value().offset(), value.value().end())));
            } else {
                contextBuilder.define(
                        m, factory.makeValueFrom(Arrays.copyOfRange(value.value().doubleArray(), value.value().offset(), value.value().end())));
            }
        }
        evaluator = new Evaluator(factory, contextBuilder.build());

    }

    @Override
    public boolean hasNext() {
        return hasMoreValues;
    }

}
