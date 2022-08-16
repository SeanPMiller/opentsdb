package net.opentsdb.query.processor.expressions2.eval;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.opentsdb.core.Registry;
import net.opentsdb.core.TSDB;
import net.opentsdb.pools.DefaultObjectPoolConfig;
import net.opentsdb.pools.DoubleArrayPool;
import net.opentsdb.pools.LongArrayPool;
import net.opentsdb.pools.MockArrayObjectPool;
import org.junit.Before;
import org.junit.BeforeClass;

public class FactoryBasedTest {
    protected final int arrayLength;
    protected final TSDB tsdb;
    protected final MockArrayObjectPool longPool;
    protected final MockArrayObjectPool doublePool;
    protected final ExpressionFactory factory;

    protected static final int DEFAULT_ARRAY_LEN = 5;


    static EvaluationOptions DEFAULT_EVAL_OPTIONS = mock(EvaluationOptions.class);


    public FactoryBasedTest() {
        this(DEFAULT_ARRAY_LEN);
    }

    public FactoryBasedTest(final int arrayLength) {
        this(arrayLength, DEFAULT_EVAL_OPTIONS);
        when(DEFAULT_EVAL_OPTIONS.getAllowMetricReuse()).thenReturn(true);
        when(DEFAULT_EVAL_OPTIONS.getInfectiousNaN()).thenReturn(false);
        when(DEFAULT_EVAL_OPTIONS.getForceFloatingPointDivision()).thenReturn(false);
    }

    public FactoryBasedTest(final int arrayLength, final EvaluationOptions options) {
        this.arrayLength = arrayLength;

        longPool = new MockArrayObjectPool(DefaultObjectPoolConfig.newBuilder()
                .setAllocator(new LongArrayPool())
                .setInitialCount(4)
                .setArrayLength(arrayLength)
                .setId(LongArrayPool.TYPE)
                .build());

        doublePool = new MockArrayObjectPool(DefaultObjectPoolConfig.newBuilder()
                .setAllocator(new DoubleArrayPool())
                .setInitialCount(4)
                .setArrayLength(arrayLength)
                .setId(DoubleArrayPool.TYPE)
                .build());

        final Registry registry = mock(Registry.class);
        when(registry.getObjectPool(LongArrayPool.TYPE)).thenReturn(longPool);
        when(registry.getObjectPool(DoubleArrayPool.TYPE)).thenReturn(doublePool);

        tsdb = mock(TSDB.class);
        when(tsdb.getRegistry()).thenReturn(registry);

        factory = new ExpressionFactory(options, longPool, doublePool);
    }

    @Before
    public void beforeFactoryBasedTest() {
        longPool.resetCounters();
        doublePool.resetCounters();
    }
}
