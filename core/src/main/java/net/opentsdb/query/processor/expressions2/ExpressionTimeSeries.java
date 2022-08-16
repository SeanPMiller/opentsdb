package net.opentsdb.query.processor.expressions2;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import net.opentsdb.data.TimeSeries;
import net.opentsdb.data.TimeSeriesDataType;
import net.opentsdb.data.TimeSeriesId;
import net.opentsdb.data.TypedTimeSeriesIterator;
import net.opentsdb.data.types.numeric.NumericArrayType;
import net.opentsdb.query.QueryResult;
import net.opentsdb.query.processor.expressions.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A container class for computing operation on one or more
 * time series in an expression node graph.
 *
 * @since 3.0
 */
public class ExpressionTimeSeries implements TimeSeries {
    ExpressionQueryNode node;
    Map<String, TimeSeries> sourceMap;
    /**
     * The query result this series belongs to.
     */
    protected final QueryResult result;

    /**
     * The set of types in this series.
     */
    protected final Collection<TypeToken<? extends TimeSeriesDataType>> types;

    /**
     * The new Id of the time series.
     */
    protected final TimeSeriesId id;

    /**
     * Package private ctor. Constructs the new ID from the joiner belonging
     * to the node.
     *
     * @param node      The non-null parent node.
     * @param result    The non-null result this series belongs to.
     * @param sourceMap The optional left hand time series.
     */
    ExpressionTimeSeries(final ExpressionQueryNode node,
                         final QueryResult result,
                         final Map<String, TimeSeries> sourceMap) {
        this.node = node;
        this.result = result;
        this.sourceMap = sourceMap;
        this.types = Lists.newArrayList(NumericArrayType.TYPE);
        // TODO: id
        this.id = null;
    }

    @Override
    public TimeSeriesId id() {
        return id;
    }

    @Override
    public Optional<TypedTimeSeriesIterator<? extends TimeSeriesDataType>> iterator(
            final TypeToken<? extends TimeSeriesDataType> type) {
        if (!types.contains(type)) {
            return Optional.empty();
        }

        final TypedTimeSeriesIterator iterator = ((ExpressionIteratorFactory) node.factory())
                .newTypedIterator(type, node, result, sourceMap);

        if (iterator == null) {
            return Optional.empty();
        }
        return Optional.of(iterator);
    }

    @Override
    public Collection<TypedTimeSeriesIterator<? extends TimeSeriesDataType>> iterators() {

        final List<TypedTimeSeriesIterator<? extends TimeSeriesDataType>> iterators =
                Lists.newArrayListWithExpectedSize(types.size());
        for (final TypeToken<? extends TimeSeriesDataType> type : types) {
            iterators.add(((ExpressionIteratorFactory) node.factory()).newTypedIterator(
                    type, node, result, sourceMap));

        }

        return iterators;
    }

    @Override
    public Collection<TypeToken<? extends TimeSeriesDataType>> types() {
        return types;
    }

    @Override
    public void close() {
        for (TimeSeries ts : sourceMap.values()) {
            ts.close();
        }
    }
}