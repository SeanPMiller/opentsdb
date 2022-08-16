package net.opentsdb.query.processor.expressions2;


import java.util.*;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.reflect.TypeToken;

import net.opentsdb.core.Const;
import net.opentsdb.core.TSDB;
import net.opentsdb.query.BaseQueryNodeConfigWithInterpolators;
import net.opentsdb.query.QueryResultId;
import net.opentsdb.query.interpolation.QueryInterpolatorConfig;
import net.opentsdb.query.interpolation.QueryInterpolatorFactory;
import net.opentsdb.query.joins.JoinConfig;
import net.opentsdb.query.processor.expressions.ExpressionParseNode;
import net.opentsdb.query.processor.expressions2.eval.Simplifier;
import net.opentsdb.query.processor.expressions2.nodes.ExpressionNode;

/**
 * Represents a single arithmetic and/or logical expression involving
 * (for now) numeric time series.
 * <p>
 * TODO - overrides in hashes/equals/compareto
 *
 * @since 3.0
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = ExpressionConfig.Builder.class)
public class ExpressionConfig extends BaseQueryNodeConfigWithInterpolators<ExpressionConfig.Builder, ExpressionConfig> {

    /**
     * The original expression string.
     */
    private final String expression;

    /**
     * The non-null join config.
     */
    private final JoinConfig join_config;

    /**
     * An optional map of variable to interpolators to override the defaults.
     */
    private final Map<String, List<QueryInterpolatorConfig>> variable_interpolators;

    /**
     * Whether or not NaN is infectious.
     */
    private final boolean infectious_nan;

    /**
     * Whether or not floating point division is forced
     */
    private final boolean force_floating_point_division;

    /**
     * Whether or not metric is allowed to be reused
     */
    private final boolean allow_metric_reuse;

    /**
     * The resulting metric name.
     */
    private final String as;

    /**
     * Parsed tree of expression after simplication
     */
    private final ExpressionNode parseTree;

    /**
     * ids of results needed to perform this evaluation
     */
    private final List<QueryResultId> results_needed;

    /**
     * name or alias for metrics parsed from the expression
     * For example: m1 + m2 will generate m1, m2 as the needed source alias
     * And metric.foo + 1 will generated metric.foo
     */
    private final List<String> sources_alias;

    /**
     * Protected ctor.
     *
     * @param builder The non-null builder.
     */
    protected ExpressionConfig(final ExpressionConfig.Builder builder) {
        super(builder);
        if (Strings.isNullOrEmpty(builder.expression)) {
            throw new IllegalArgumentException("Expression cannot be null.");
        }
        if (builder.joinConfig == null) {
            throw new IllegalArgumentException("Join config cannot be null.");
        }
        if (interpolator_configs == null || interpolator_configs.isEmpty()) {
            throw new IllegalArgumentException("Must have at least default interpolator.");
        }

        // Analyze and simplify the expression to be evaluated.
        expression = builder.expression;
        final ExpressionParser parser = new ExpressionParser();
        ExpressionNode expressionTree = parser.parse(expression);
        final Simplifier simplifier = new Simplifier();
        parseTree = simplifier.simplify(expressionTree);


        if (null == builder.sources_alias) {
            sources_alias = Lists.newArrayListWithCapacity(parser.getTerminals().size());
            for (String source : parser.getTerminals().keySet()) {
                sources_alias.add(source);
            }
        } else {
            sources_alias = builder.sources_alias;
        }

        join_config = builder.joinConfig;
        variable_interpolators = builder.variable_interpolators;

        // Evaluation options
        infectious_nan = builder.infectiousNan;
        force_floating_point_division = builder.forceFloatingPointDivision;
        allow_metric_reuse = builder.allowMetricReuse;

        if (Strings.isNullOrEmpty(builder.as)) {
            as = getId();
        } else {
            as = builder.as;
        }

        results_needed = builder.results_needed;
    }

    /**
     * @return The needed result ids for this query to evaluate.
     */
    public List<QueryResultId> getResultsNeeded() {
        return results_needed;
    }

    /**
     * @return The simplified parsed expression tree.
     */
    public ExpressionNode getParseTree() {
        return parseTree;
    }

    /**
     * @return The raw expression string to be parsed.
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @return The join config.
     */
    public JoinConfig getJoin() {
        return join_config;
    }

    /**
     * @return A possibly null map of variable names to interpolators.
     */
    public Map<String, List<QueryInterpolatorConfig>> getVariableInterpolators() {
        return variable_interpolators;
    }

    /**
     * @return Whether or not nans are infectious.
     */
    public boolean isInfectious_nan() {
        return infectious_nan;
    }

    /**
     * @return Whether or not floating point division is forced.
     */
    public boolean isForce_floating_point_division() {
        return force_floating_point_division;
    }

    /**
     * @return Whether or not metrix reuse is allowed.
     */
    public boolean isAllow_metric_reuse() {
        return allow_metric_reuse;
    }

    /**
     * @return The new name for the metric.
     */
    public String getAs() {
        return as;
    }

    /**
     * Helper to pull out the proper config based on the optional variable
     * name.
     *
     * @param type     The non-null data type.
     * @param variable An optional variable name.
     * @return An interpolator or null if none is configured for the given type.
     */
    public QueryInterpolatorConfig interpolatorConfig(final TypeToken<?> type,
                                                      final String variable) {
        QueryInterpolatorConfig config = null;
        if (!Strings.isNullOrEmpty(variable) && variable_interpolators != null) {
            final List<QueryInterpolatorConfig> configs = variable_interpolators.get(variable);
            if (configs != null) {
                for (final QueryInterpolatorConfig cfg : configs) {
                    if (cfg.type() == type) {
                        config = cfg;
                        break;
                    }
                }
            }
        }

        if (config != null) {
            return config;
        }

        return interpolatorConfig(type);
    }

    @Override
    public boolean pushDown() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean joins() {
        return true;
    }

    @Override
    public ExpressionConfig.Builder toBuilder() {
        ExpressionConfig.Builder cloneBuilder = new ExpressionConfig.Builder();
        cloneBuilder(this, cloneBuilder);
        super.toBuilder(cloneBuilder);
        return cloneBuilder;
    }

    @Override
    public HashCode buildHashCode() {
        final List<HashCode> hashes = Lists.newArrayListWithExpectedSize(2);
        hashes.add(join_config.buildHashCode());
        hashes.add(Const.HASH_FUNCTION().newHasher()
                .putBoolean(infectious_nan)
                .putString(id == null ? "null" : id, Const.UTF8_CHARSET)
                .putString(expression, Const.UTF8_CHARSET)
                .putString(as == null ? "null" : as, Const.UTF8_CHARSET)
                .hash());
        if (variable_interpolators != null && !variable_interpolators.isEmpty()) {
            final Map<String, List<QueryInterpolatorConfig>> sorted =
                    new TreeMap<String, List<QueryInterpolatorConfig>>(variable_interpolators);
            for (final Entry<String, List<QueryInterpolatorConfig>> entry : sorted.entrySet()) {
                Collections.sort(entry.getValue());
                for (final QueryInterpolatorConfig cfg : entry.getValue()) {
                    hashes.add(cfg.buildHashCode());
                }
            }
        }
        if (interpolator_configs != null &&
                !interpolator_configs.isEmpty()) {
            final Map<String, QueryInterpolatorConfig> sorted =
                    new TreeMap<String, QueryInterpolatorConfig>();
            for (final Entry<TypeToken<?>, QueryInterpolatorConfig> entry :
                    interpolator_configs.entrySet()) {
                sorted.put(entry.getKey().toString(), entry.getValue());
            }
            for (final Entry<String, QueryInterpolatorConfig> entry : sorted.entrySet()) {
                hashes.add(entry.getValue().buildHashCode());
            }
        }
        return Hashing.combineOrdered(hashes);
    }

    @Override
    public int compareTo(final ExpressionConfig o) {
        if (o == null) {
            return 1;
        }
        if (o == this) {
            return 0;
        }

        return ComparisonChain.start()
                .compare(id, o.id)
                .compare(expression, o.expression)
                .compare(join_config, o.join_config)
                .compare(variable_interpolators, o.variable_interpolators, VARIABLE_INTERP_CMP)
                .compare(interpolator_configs, o.interpolator_configs, INTERPOLATOR_CMP)
                .compare(infectious_nan, o.infectious_nan)
                .compare(force_floating_point_division, o.force_floating_point_division)
                .compare(allow_metric_reuse, o.allow_metric_reuse)
                .compare(as, o.as)
                .result();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof ExpressionConfig)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        final ExpressionConfig other = (ExpressionConfig) o;
        return Objects.equals(id, other.id) &&
                Objects.equals(expression, other.expression) &&
                Objects.equals(join_config, other.join_config) &&
                Objects.equals(variable_interpolators, other.variable_interpolators) &&
                Objects.equals(interpolator_configs, other.interpolator_configs) &&
                Objects.equals(infectious_nan, other.infectious_nan) &&
                Objects.equals(as, other.as);
    }

    @Override
    public int hashCode() {
        return buildHashCode().asInt();
    }

    /**
     *
     * @param mapper mapper that helps parse the json tree
     * @param tsdb TSDB object that contains the necessary plugins
     * @param node JsonNode of the query
     * @return An ExpressionConfig object parsed from a json node
     */
    public static ExpressionConfig parse(final ObjectMapper mapper,
                                         final TSDB tsdb,
                                         final JsonNode node) {
        ExpressionConfig.Builder builder = new ExpressionConfig.Builder();
        JsonNode n = node.get("expression");
        if (n != null) {
            builder.setExpression(n.asText());
        }

        n = node.get("join");
        if (n != null) {
            try {
                builder.setJoinConfig(mapper.treeToValue(n, JoinConfig.class));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Unable to parse JoinConfig", e);
            }
        }

        n = node.get("infectiousNan");
        if (n != null) {
            builder.setInfectiousNan(n.asBoolean());
        }

        n = node.get("as");
        if (n != null) {
            builder.setAs(n.asText());
        }

        n = node.get("id");
        if (n != null) {
            builder.setId(n.asText());
        }

        n = node.get("variableInterpolators");
        if (n != null && !n.isNull()) {
            final Iterator<Entry<String, JsonNode>> iterator = n.fields();
            while (iterator.hasNext()) {
                final Entry<String, JsonNode> entry = iterator.next();
                for (final JsonNode config : entry.getValue()) {
                    JsonNode type_json = config.get("type");
                    final QueryInterpolatorFactory factory = tsdb.getRegistry().getPlugin(
                            QueryInterpolatorFactory.class,
                            type_json == null ? null : type_json.asText());
                    if (factory == null) {
                        throw new IllegalArgumentException("Unable to find an "
                                + "interpolator factory for: " +
                                type_json == null ? "default" :
                                type_json.asText());
                    }

                    final QueryInterpolatorConfig interpolator_config =
                            factory.parseConfig(mapper, tsdb, config);
                    builder.addVariableInterpolator(entry.getKey(), interpolator_config);
                }
            }
        }

        n = node.get("interpolatorConfigs");
        if (n != null && !n.isNull()) {
            for (final JsonNode config : n) {
                JsonNode type_json = config.get("type");
                final QueryInterpolatorFactory factory = tsdb.getRegistry().getPlugin(
                        QueryInterpolatorFactory.class,
                        type_json == null ? null : type_json.asText());
                if (factory == null) {
                    throw new IllegalArgumentException("Unable to find an "
                            + "interpolator factory for: " +
                            type_json == null ? "default" :
                            type_json.asText());
                }

                final QueryInterpolatorConfig interpolator_config =
                        factory.parseConfig(mapper, tsdb, config);
                builder.addInterpolatorConfig(interpolator_config);
            }
        }

        n = node.get("sources");
        if (n != null && !n.isNull()) {
            try {
                builder.setSources(mapper.treeToValue(n, List.class));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to parse json", e);
            }
        }

        return builder.build();
    }

    /**
     * Following the pattern of DefaultTimeSeriesDataSourceConfig
     */
    public static void cloneBuilder(
            final ExpressionConfig expressionConfig, final ExpressionConfig.Builder builder) {

        final JoinConfig join = expressionConfig.getJoin();

        builder.setId(expressionConfig.getId())
                .setAs(expressionConfig.getAs())
                .setType(expressionConfig.getType())
                .setSources(expressionConfig.getSources() == null ? null : Lists.newArrayList(expressionConfig.getSources()))
                .setInterpolatorConfigs(expressionConfig.getInterpolatorConfigs() == null ? null : Lists.newArrayList(expressionConfig.getInterpolatorConfigs()))
                .setVariableInterpolators(expressionConfig.getVariableInterpolators() == null ? null : Maps.newHashMap(expressionConfig.getVariableInterpolators()))
                .setOverrides(expressionConfig.getOverrides() == null ? null : Maps.newHashMap(expressionConfig.getOverrides()))
                .setExpression(expressionConfig.getExpression())
                .setJoinConfig(join.toBuilder().build())
                .setInfectiousNan(expressionConfig.isInfectious_nan())
                .setForceFloatingPointDivision(expressionConfig.isForce_floating_point_division())
                .setAllowMetricReuse(expressionConfig.isAllow_metric_reuse())
                .setSourcesAlias(expressionConfig.sources_alias);

    }

    public static ExpressionConfig.Builder newBuilder() {
        return new ExpressionConfig.Builder();
    }

    public static class Builder extends BaseQueryNodeConfigWithInterpolators.Builder<ExpressionConfig.Builder, ExpressionConfig> {
        @JsonProperty
        private String expression;
        @JsonProperty
        private JoinConfig joinConfig;
        @JsonProperty
        private Map<String, List<QueryInterpolatorConfig>> variable_interpolators;
        @JsonProperty
        private boolean infectiousNan;
        @JsonProperty
        private boolean forceFloatingPointDivision;
        @JsonProperty
        private boolean allowMetricReuse;
        @JsonProperty
        private String as;
        @JsonProperty
        private boolean substituteMissing;
        private List<QueryResultId> results_needed;
        private List<String> sources_alias;

        Builder() {
            setType(ExpressionIteratorFactory.TYPE);
        }

        public ExpressionConfig.Builder setExpression(final String expression) {
            this.expression = expression;
            return this;
        }

        public ExpressionConfig.Builder setJoinConfig(final JoinConfig join) {
            this.joinConfig = join;
            return this;
        }

        public ExpressionConfig.Builder setVariableInterpolators(
                final Map<String, List<QueryInterpolatorConfig>> variable_interpolators) {
            this.variable_interpolators = variable_interpolators;
            return this;
        }

        public ExpressionConfig.Builder addVariableInterpolator(final String variable,
                                                                final QueryInterpolatorConfig interpolator) {
            if (variable_interpolators == null) {
                variable_interpolators = Maps.newHashMap();
            }
            List<QueryInterpolatorConfig> configs = variable_interpolators.get(variable);
            if (configs == null) {
                configs = Lists.newArrayList();
                variable_interpolators.put(variable, configs);
            }
            configs.add(interpolator);
            return this;
        }

        public ExpressionConfig.Builder setInfectiousNan(final boolean infectious_nan) {
            this.infectiousNan = infectious_nan;
            return this;
        }

        public ExpressionConfig.Builder setForceFloatingPointDivision(final boolean forceFloatingPointDivision) {
            this.forceFloatingPointDivision = forceFloatingPointDivision;
            return this;
        }

        public ExpressionConfig.Builder setAllowMetricReuse(final boolean allowMetricReuse) {
            this.allowMetricReuse = allowMetricReuse;
            return this;
        }

        public ExpressionConfig.Builder setAs(final String as) {
            this.as = as;
            return this;
        }

        public ExpressionConfig.Builder addNeededResult(final QueryResultId resultId) {
            if (null == results_needed) {
                results_needed = Lists.newArrayListWithExpectedSize(1);
            }
            results_needed.add(resultId);
            return this;
        }

        public ExpressionConfig.Builder addSource(final String source) {
            if (sources == null) {
                sources = Lists.newArrayListWithExpectedSize(1);
            }
            if (!sources.contains(source)) {
                sources.add(source);
            }
            return this;
        }

        public ExpressionConfig.Builder setSourcesAlias(final List<String> alias) {
            sources_alias = alias;
            return this;
        }

        public List<String> getSourcesAlias() {
            return sources_alias;
        }

        @Override
        public ExpressionConfig build() {
            return new ExpressionConfig(this);
        }

        @Override
        public ExpressionConfig.Builder self() {
            return this;
        }
    }

    public static class InterpCmp
            implements Comparator<Map<String, List<QueryInterpolatorConfig>>> {

        @Override
        public int compare(final Map<String, List<QueryInterpolatorConfig>> a,
                           Map<String, List<QueryInterpolatorConfig>> b) {
            if (a == b || a == null && b == null) {
                return 0;
            }
            if (a == null && b != null) {
                return -1;
            }
            if (b == null && a != null) {
                return 1;
            }
            if (a.size() > b.size()) {
                return -1;
            }
            if (b.size() > a.size()) {
                return 1;
            }
            for (final Entry<String, List<QueryInterpolatorConfig>> entry : a.entrySet()) {
                final List<QueryInterpolatorConfig> b_value = b.get(entry.getKey());
                if (b_value == null && entry.getValue() != null) {
                    return 1;
                }
                if (entry.getValue().size() != b_value.size()) {
                    return entry.getValue().size() - b_value.size();
                }
                for (final QueryInterpolatorConfig cfg : entry.getValue()) {
                    if (!b_value.contains(cfg)) {
                        return -1;
                    }
                }
            }
            return 0;
        }

    }

    private static final ExpressionConfig.InterpCmp VARIABLE_INTERP_CMP = new ExpressionConfig.InterpCmp();
}
