package net.opentsdb.query.processor.expressions2;


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.opentsdb.pools.DefaultObjectPoolConfig;
import net.opentsdb.pools.DoubleArrayPool;
import net.opentsdb.pools.LongArrayPool;
import net.opentsdb.pools.MockArrayObjectPool;
import net.opentsdb.query.processor.expressions.BinaryExpressionNode;
import net.opentsdb.query.processor.expressions2.ExpressionConfig;
import net.opentsdb.query.processor.expressions.ExpressionParseNode;
import org.junit.Before;
import org.junit.BeforeClass;

import net.opentsdb.core.Registry;
import net.opentsdb.core.TSDB;
import net.opentsdb.data.BaseTimeSeriesStringId;
import net.opentsdb.data.TimeSeriesId;
import net.opentsdb.data.types.numeric.NumericType;
import net.opentsdb.query.QueryPipelineContext;
import net.opentsdb.query.QueryResult;
import net.opentsdb.query.QueryFillPolicy.FillWithRealPolicy;
import net.opentsdb.query.interpolation.QueryInterpolatorFactory;
import net.opentsdb.query.interpolation.DefaultInterpolatorFactory;
import net.opentsdb.query.interpolation.types.numeric.NumericInterpolatorConfig;
import net.opentsdb.query.joins.JoinConfig;
import net.opentsdb.query.joins.Joiner;
import net.opentsdb.query.joins.JoinConfig.JoinType;
import net.opentsdb.query.pojo.FillPolicy;
import net.opentsdb.query.processor.expressions.ExpressionParseNode.ExpressionOp;
import net.opentsdb.query.processor.expressions.ExpressionParseNode.OperandType;

public class BaseNumericTest extends ConfigBasedTest {

    protected static Joiner JOINER;
    protected static QueryResult RESULT;
    protected static TSDB TSDB;
    protected static QueryPipelineContext CONTEXT;
    protected static TimeSeriesId LEFT_ID;
    protected static TimeSeriesId RIGHT_ID;
    protected static MockArrayObjectPool longPool;
    protected static MockArrayObjectPool doublePool;


    protected ExpressionQueryNode node;
    protected ExpressionConfig config;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void beforeClass() throws Exception {
        JOINER = mock(Joiner.class);
        RESULT = mock(QueryResult.class);
        TSDB = mock(TSDB.class);

        longPool = new MockArrayObjectPool(DefaultObjectPoolConfig.newBuilder()
                .setAllocator(new LongArrayPool())
                .setInitialCount(4)
                .setArrayLength(3)
                .setId(LongArrayPool.TYPE)
                .build());

        doublePool = new MockArrayObjectPool(DefaultObjectPoolConfig.newBuilder()
                .setAllocator(new DoubleArrayPool())
                .setInitialCount(4)
                .setArrayLength(3)
                .setId(DoubleArrayPool.TYPE)
                .build());



        CONTEXT = mock(QueryPipelineContext.class);
        when(CONTEXT.tsdb()).thenReturn(TSDB);
        final Registry registry = mock(Registry.class);
        when(registry.getObjectPool(LongArrayPool.TYPE)).thenReturn(longPool);
        when(registry.getObjectPool(DoubleArrayPool.TYPE)).thenReturn(doublePool);
        when(TSDB.getRegistry()).thenReturn(registry);
        final QueryInterpolatorFactory interp_factory = new DefaultInterpolatorFactory();
        interp_factory.initialize(TSDB, null).join();
        when(registry.getPlugin(any(Class.class), anyString())).thenReturn(interp_factory);

        LEFT_ID = BaseTimeSeriesStringId.newBuilder()
                .setMetric("a")
                .build();
        RIGHT_ID = BaseTimeSeriesStringId.newBuilder()
                .setMetric("a")
                .build();
    }

    @Before
    public void before() throws Exception {
        node = mock(ExpressionQueryNode.class);

        config = ExpressionConfig.newBuilder()
                .setExpression("m1 + m2")
                .setJoinConfig(JOIN_CONFIG)
                .addInterpolatorConfig(NUMERIC_CONFIG)
                .setId("e1")
                .build();

//        expression_config = ExpressionParseNode.newBuilder()
//                .setLeft("a")
//                .setLeftType(OperandType.VARIABLE)
//                .setRight("b")
//                .setRightType(OperandType.VARIABLE)
//                .setExpressionOp(ExpressionOp.ADD)
//                .setExpressionConfig(config)
//                .setId("expression")
//                .build();

        when(node.pipelineContext()).thenReturn(CONTEXT);
        when(node.config()).thenReturn(config);
//        when(node.joiner()).thenReturn(JOINER);
    }
}
