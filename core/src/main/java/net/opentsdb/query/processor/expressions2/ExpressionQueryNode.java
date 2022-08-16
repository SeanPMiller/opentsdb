package net.opentsdb.query.processor.expressions2;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import net.opentsdb.data.TimeSeries;
import net.opentsdb.data.TypedTimeSeriesIterator;
import net.opentsdb.data.types.numeric.NumericArrayType;
import net.opentsdb.query.*;
import net.opentsdb.query.processor.expressions.BinaryExpressionNode;
import net.opentsdb.query.processor.expressions.BinaryExpressionNodeFactory;
import net.opentsdb.query.processor.expressions2.ExpressionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExpressionQueryNode extends AbstractQueryNode<ExpressionConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(
            ExpressionQueryNode.class);

    /**
     * The parsed config for this particular node.
     */
    protected final ExpressionConfig expression_config;

    /**
     * The map of result IDs to results.
     */
    protected final Map<QueryResultId, QueryResult> results;

    /**
     * The result of this query node
     */
    protected ExpressionResult result;

    /**
     * Flag populated when all the results have arrived.
     */
    protected final AtomicBoolean all_in;

    /**
     * Default c-tor.
     *
     * @param factory  The factory we came from.
     * @param context  The non-null context.
     * @param exprConf The non-null expression config.
     */
    public ExpressionQueryNode(final QueryNodeFactory factory,
                               final QueryPipelineContext context,
                               final ExpressionConfig exprConf) {
        super(factory, context);
        all_in = new AtomicBoolean();
        if (null == exprConf) {
            throw new IllegalArgumentException("Expression config cannot be null.");
        }
        this.expression_config = exprConf;
        this.result = new ExpressionResult(this);

        this.results = new HashMap<>();
        // Store ids of all needed results as key
        // We need all these results to evaluate this query
        for (QueryResultId resultId : exprConf.getResultsNeeded()) {
            // TODO: set a query result ID as key
            results.put(resultId, null);
        }
    }

    @Override
    public ExpressionConfig config() {
        return expression_config;
    }

    @Override
    public void close() {
        if (!results.isEmpty()) {
            for (final QueryResult result : results.values()) {
                if (result != null) {
                    result.close();
                }
            }
        }
    }

    @Override
    public void onNext(final QueryResult next) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Result: " + next.dataSource() + " (" + next.getClass() + ") "
                    + " Want<" + results.keySet() + ">");
        }

        // If there is an error or exception
        // Send a failed result to upstream and return
        if (!Strings.isNullOrEmpty(next.error()) || next.exception() != null) {
            sendUpstream(new FailedQueryResult(next));
            return;
        }

        synchronized (results) {
            if (!results.containsKey(next.dataSource())) {
                LOG.debug("Unmatched result: " + next.dataSource());
                return;
            }

            results.put(next.dataSource(), next);
        }

//        // TODO: decide whether or not the logic is needed here
//        // referring to onNext method in BinaryExpressionNode
//        if (resolveMetrics(next)) {
//            // resolving, don't progress yet.
//            return;
//        }
//
//        if (resolveJoinStrings(next)) {
//            // resolving, don't progress yet.
//            return;
//        }

        // see if all the results are in.
        boolean full_map = true;
        synchronized (results) {
            for (final Map.Entry<QueryResultId, QueryResult> entry : results.entrySet()) {
                if (entry.getValue() == null) {
                    full_map = false;
                    break;
                }
            }
        }

        if (!full_map) {
            LOG.trace("Not all results are in for: " + expression_config.getId());
            return;
        }

        if (all_in.compareAndSet(false, true)) {
            try {
                result.join();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Sending expression upstream: " + expression_config.getId());
                }

                // Send the result to upstream
                sendUpstream(result);
            } catch (Exception e) {
                e.printStackTrace();
                sendUpstream(e);
            }
        }
    }

    Map<QueryResultId, QueryResult> results() {
        return results;
    }

    class FailedQueryResult extends BaseWrappedQueryResult {

        public FailedQueryResult(final QueryResult result) {
            super(ExpressionQueryNode.this, result);
        }

    }
}
