package net.opentsdb.query.processor.expressions2.eval;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import net.opentsdb.pools.ArrayObjectPool;
import net.opentsdb.pools.DoubleArrayPool;
import net.opentsdb.pools.LongArrayPool;
import net.opentsdb.pools.PooledObject;
import net.opentsdb.query.processor.expressions2.ExpressionException;

/**
 * This class provides information that evaluation operations may need to do
 * their work.
 */
public class EvaluationContext {
    public static class Builder {
        private Map<String, ExpressionValue> metrics;

        public Builder() {
            metrics = new HashMap<>();
        }

        public Builder define(final String metricName, final ExpressionValue value) {
            metrics.put(metricName, value);
            return this;
        }

        public EvaluationContext build() {
            return new EvaluationContext(this);
        }
    }

    private final Map<String, ExpressionValue> metrics;
    private final Deque<ExpressionValue> stack;

    private EvaluationContext(final Builder builder) {
        this.metrics = Collections.unmodifiableMap(builder.metrics);
        stack = new ArrayDeque<>();
    }

    public void push(final ExpressionValue value) {
        stack.addFirst(value);
    }

    public ExpressionValue pop() {
        final ExpressionValue result = stack.pollFirst();
        if (null == result) {
            throw new ExpressionException("tried to pop from empty stack in EvaluationContext");
        }
        return result;
    }

    public ExpressionValue peek() {
        final ExpressionValue result = stack.peekFirst();
        if (null == result) {
            throw new ExpressionException("tried to peek at top of empty stack in EvaluationContext");
        }
        return result;
    }

    public int stackSize() {
        return stack.size();
    }

    /**
     * Whether you copy the returned value is up to you.
     */
    public ExpressionValue lookup(final String metricName) {
        final ExpressionValue result = metrics.get(metricName);
        if (null == result) {
            throw new ExpressionException("tried to look up an undefined metric name in EvaluationContext: " + metricName);
        }
        return result;
    }
}
