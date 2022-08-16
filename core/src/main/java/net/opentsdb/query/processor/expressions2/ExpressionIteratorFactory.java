package net.opentsdb.query.processor.expressions2;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import com.stumbleupon.async.Deferred;
import net.opentsdb.core.TSDB;
import net.opentsdb.data.TimeSeries;
import net.opentsdb.data.TimeSeriesDataType;
import net.opentsdb.data.TypedTimeSeriesIterator;
import net.opentsdb.data.types.numeric.NumericArrayType;
import net.opentsdb.data.types.numeric.NumericSummaryType;
import net.opentsdb.data.types.numeric.NumericType;
import net.opentsdb.query.*;
import net.opentsdb.query.plan.DefaultQueryPlanner;
import net.opentsdb.query.plan.QueryPlanner;
import net.opentsdb.query.processor.BaseQueryNodeFactory;
import net.opentsdb.query.processor.expressions.ExpressionFactory;
import net.opentsdb.query.processor.expressions.ExpressionParseNode;
import net.opentsdb.query.processor.expressions.TernaryParseNode;
import net.opentsdb.query.processor.expressions2.ExpressionConfig;
import net.opentsdb.query.processor.expressions2.nodes.ExpressionNode;

public class ExpressionIteratorFactory extends BaseQueryNodeFactory<ExpressionConfig, ExpressionQueryNode> {

    // This type should be unique among other plugins
    public static final String TYPE = "Expression2";

    public ExpressionIteratorFactory() {
        super();
        // register iterator factory in a map
        registerIteratorFactory(NumericArrayType.TYPE,
                new NumericArrayIteratorFactory());
        // TODO: support for NumericSummaryType (and maybe NumericType)
    }

    @Override
    public <T extends TimeSeriesDataType> TypedTimeSeriesIterator newTypedIterator(TypeToken<T> type, ExpressionQueryNode node, QueryResult result, Map<String, TimeSeries> sources) {
        if (type == NumericType.TYPE || type == NumericSummaryType.TYPE) {
            // not support numeric summary or numeric type now
            throw new IllegalArgumentException("Type cannot be numeric or numeric summary.");
        }
        return super.newTypedIterator(type, node, result, sources);
    }

    @Override
    public Deferred<Object> initialize(TSDB tsdb, String id) {
        this.id = Strings.isNullOrEmpty(id) ? TYPE : id;
        return Deferred.fromResult(null);
    }

    @Override
    public ExpressionQueryNode newNode(QueryPipelineContext context, ExpressionConfig config) {
        return new ExpressionQueryNode(this, context, config);
    }

    @Override
    public void setupGraph(QueryPipelineContext context, ExpressionConfig config, QueryPlanner planner) {
        ExpressionConfig.Builder builder = config.toBuilder()
                .addResultId(new DefaultQueryResultId(config.getId(), config.getId()));


        final Map<String, QueryNodeConfig> node_map = Maps.newHashMap();
        for (final QueryNodeConfig node : planner.configGraph().successors(config)) {
            node_map.put(node.getId(), node);
        }

        for (String source : builder.getSourcesAlias()) {
            String ds = validate(source, builder, planner, node_map);
            if (ds == null) {
                throw new RuntimeException("Failed to find a data source for the "
                        + " for expression node:\n"
                        + config.getId() + ((DefaultQueryPlanner) planner).printConfigGraph());
            }
        }

        // replace the current config node with the configured node
        planner.replace(config, builder.build());
    }

    /**
     * Helper that finds the metric for a variable in an expression.
     * Add the downstream result to the expression config
     *
     * @param builder  The non-null builder.
     * @param plan     The query plan.
     * @param node_map Map of nodes of current config's successors.
     * @return The data source as a string if found, null if there was an error.
     */
    static String validate(final String source,
                           final ExpressionConfig.Builder builder,
                           final QueryPlanner plan,
                           final Map<String, QueryNodeConfig> node_map) {
        for (final QueryNodeConfig node : node_map.values()) {
            for (final QueryResultId src : (List<QueryResultId>) node.resultIds()) {
                final String metric = plan.getMetricForDataSource(node, src.dataSource());

                // If source equals to full name of metric: sys.foo
                // or the alias of metric: m1, then we add the result source
                if (source.equals(metric) || source.equals(src.dataSource())) {
                    builder.addNeededResult(src)
                            .addSource(node.getId());
                    return src.dataSource();
                }
            }
        }

        // Required result resource not found
        return null;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public ExpressionConfig parseConfig(ObjectMapper mapper, TSDB tsdb, JsonNode node) {
        return null;
    }

    class NumericArrayIteratorFactory implements QueryIteratorFactory<ExpressionQueryNode, NumericArrayType> {
        @Override
        public Collection<TypeToken<? extends TimeSeriesDataType>> types() {
            return NumericArrayType.SINGLE_LIST;
        }

        @Override
        public TypedTimeSeriesIterator<NumericArrayType> newIterator(
                ExpressionQueryNode node,
                QueryResult result,
                Collection<TimeSeries> sources,
                TypeToken<? extends TimeSeriesDataType> type) {
            throw new UnsupportedOperationException("Expression iterators must have a map.");
        }

        @Override
        public TypedTimeSeriesIterator<NumericArrayType> newIterator(
                ExpressionQueryNode node,
                QueryResult result,
                Map<String, TimeSeries> sources,
                TypeToken<? extends TimeSeriesDataType> type) {
            return new ExpressionNumericArrayIterator(node.pipelineContext().tsdb(), node.config(), result, sources);
        }
    }
}
