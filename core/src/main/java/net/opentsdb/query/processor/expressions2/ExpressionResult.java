package net.opentsdb.query.processor.expressions2;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import net.opentsdb.data.TimeSeries;
import net.opentsdb.data.TimeSeriesId;
import net.opentsdb.data.TimeSpecification;
import net.opentsdb.query.QueryNode;
import net.opentsdb.query.QueryResult;
import net.opentsdb.query.QueryResultId;
import net.opentsdb.query.joins.JoinConfig;
import net.opentsdb.rollup.RollupConfig;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The result of a {@link ExpressionQueryNode}.
 *
 * @since 3.0
 */
public class ExpressionResult implements QueryResult {
    /**
     * The parent node.
     */
    protected final ExpressionQueryNode node;

    /**
     * The list of joined time series.
     */
    protected List<TimeSeries> time_series;

    /**
     * The first non-null result we encounter so we can pass through result fields
     * like timespec, etc.
     */
    protected QueryResult non_null_result;

    /**
     * Package private ctor.
     *
     * @param node A non-null parent node.
     */
    ExpressionResult(final ExpressionQueryNode node) {
        this.node = node;
        this.time_series = new ArrayList<>();
    }

    /**
     * Package private method called by the node when it has seen all the
     * results it needs for the expression.
     * It will join a map of
     */
    void join() {
        // Build a map for expression time series with the needed time series for evaluation
        final ImmutableMap.Builder<String, TimeSeries> builder = ImmutableMap.builder();

        for (Map.Entry<QueryResultId, QueryResult> entry : node.results().entrySet()) {
            for (TimeSeries ts : entry.getValue().timeSeries()) {
                builder.put(entry.getKey().dataSource(), ts);
                break;
            }
        }
        time_series.add(new ExpressionTimeSeries(node, this, builder.build()));
    }

    @Override
    public TimeSpecification timeSpecification() {
        if (non_null_result == null) {
            non_null_result = node.results().values().iterator().next();
        }
        return non_null_result.timeSpecification();
    }

    @Override
    public List<TimeSeries> timeSeries() {
        return time_series;
    }

    @Override
    public String error() {
        // TODO - implement
        return null;
    }

    @Override
    public Throwable exception() {
        // TODO - implement
        return null;
    }

    @Override
    public long sequenceId() {
        return 0;
    }

    @Override
    public QueryNode source() {
        return node;
    }

    @Override
    public QueryResultId dataSource() {
        return node.config().resultIds().get(0);
    }

    @Override
    public TypeToken<? extends TimeSeriesId> idType() {
        if (non_null_result == null) {
            non_null_result = node.results().values().iterator().next();
        }
        return non_null_result.idType();
    }

    @Override
    public ChronoUnit resolution() {
        if (non_null_result == null) {
            non_null_result = node.results().values().iterator().next();
        }
        return non_null_result.resolution();
    }

    @Override
    public RollupConfig rollupConfig() {
        if (non_null_result == null) {
            non_null_result = node.results().values().iterator().next();
        }
        return non_null_result.rollupConfig();
    }

    @Override
    public void close() {
        for (final Map.Entry<QueryResultId, QueryResult> entry : node.results().entrySet()) {
            entry.getValue().close();
        }
    }

    @Override
    public boolean processInParallel() {
        return false;
    }

}