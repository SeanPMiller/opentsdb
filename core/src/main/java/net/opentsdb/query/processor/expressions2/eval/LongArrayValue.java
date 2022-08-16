package net.opentsdb.query.processor.expressions2.eval;

import com.google.common.primitives.Longs;

import java.util.Arrays;

import net.opentsdb.pools.PooledObject;
import net.opentsdb.query.processor.expressions2.ExpressionException;
import net.opentsdb.query.processor.expressions2.nodes.ExpressionNode;

public class LongArrayValue extends NumericValue {
    private final PooledObject pooledObject;
    long[] underlying;

    /**
     * Construct directly from a raw array.
     *
     * @param factory
     * @param values
     */
    public LongArrayValue(final ExpressionFactory factory, final long[] values) {
        super(factory);
        pooledObject = null;
        underlying = values;
    }

    /**
     * Construct from a pooled array.
     *
     * @param factory
     * @param arrObj
     */
    public LongArrayValue(final ExpressionFactory factory, final PooledObject arrObj) {
        super(factory);
        pooledObject = arrObj;
        underlying = (long[]) arrObj.object();
    }

    /**
     * @return True only if this value still has backing storage.
     */
    public boolean isLive() {
        return null != underlying;
    }

    public int getLength() {
        return underlying.length;
    }

    public long getValueAt(int idx) {
        return underlying[idx];
    }

    @Override
    public void close() {
        underlying = null;
        if (null != pooledObject) {
            pooledObject.release();
        }
    }

    @Override
    public ExpressionNode asNode() {
        throw new UnsupportedOperationException("cannot transform LongArrayValue into ExpressionNode subclass");
    }

    @Override
    public ExpressionValue makeCopy() {
        final PooledObject newUnderlying = getFactory().copyLongArray(underlying);
        return new LongArrayValue(getFactory(), newUnderlying);
    }

    @Override
    public ExpressionValue negate() {
        for (int i = 0; i < underlying.length; ++i) {
            underlying[i] = -underlying[i];
        }
        return this;
    }

    @Override
    public ExpressionValue add(final ExpressionValue value) {
        return value.add(this);
    }

    @Override
    public ExpressionValue add(final LongValue value) {
        for (int i = 0; i < underlying.length; ++i) {
            underlying[i] += value.getValue();
        }
        return this;
    }

    @Override
    public ExpressionValue add(final DoubleValue value) {
        // Can we possibly avoid converting this array?
        if (value.isExactLong()) {
            // Yes, the addend is exactly representable as a long.
            final long addend = (long) value.getValue();
            for (int i = 0; i < underlying.length; ++i) {
                underlying[i] += addend;
            }
            return this;
        } else {
            // No, we need to create a double array.
            final double addend = value.getValue();
            final PooledObject newUnderlying = getFactory().makeDoubleArray(
                    underlying.length);
            final DoubleArrayValue newValue = new DoubleArrayValue(getFactory(),
                    newUnderlying);
            for (int i = 0; i < underlying.length; ++i) {
                newValue.underlying[i] = (double) underlying[i] + addend;
            }
            this.close();
            return newValue;
        }
    }

    @Override
    public ExpressionValue add(final LongArrayValue values) {
        for (int i = 0; i < underlying.length; ++i) {
            underlying[i] += values.getValueAt(i);
        }
        values.close();
        return this;
    }

    @Override
    public ExpressionValue add(final DoubleArrayValue values) {
        // We do not want to convert this array. Addition is commutative, so we
        // will defer to the double array implementation.
        return values.add(this);
    }

    @Override
    public ExpressionValue subtract(final ExpressionValue value) {
        return value.negate().add(this);
    }

    @Override
    public ExpressionValue subtract(final LongValue value) {
        for (int i = 0; i < underlying.length; ++i) {
            underlying[i] -= value.getValue();
        }
        return this;
    }

    @Override
    public ExpressionValue subtract(final DoubleValue value) {
        // Can we possibly avoid converting this array?
        if (value.isExactLong()) {
            // Yes, the subtrahend is exactly representable as a long.
            final long subtrahend = (long) value.getValue();
            for (int i = 0; i < underlying.length; ++i) {
                underlying[i] -= subtrahend;
            }
            return this;
        } else {
            // No, we need to create a double array.
            final double subtrahend = value.getValue();
            final PooledObject newUnderlying = getFactory().makeDoubleArray(
                    underlying.length);
            final DoubleArrayValue newValue = new DoubleArrayValue(getFactory(),
                    newUnderlying);
            for (int i = 0; i < underlying.length; ++i) {
                newValue.underlying[i] = (double) underlying[i] - subtrahend;
            }
            this.close();
            return newValue;
        }
    }

    @Override
    public ExpressionValue subtract(final LongArrayValue values) {
        for (int i = 0; i < underlying.length; ++i) {
            underlying[i] -= values.getValueAt(i);
        }
        values.close();
        return this;
    }

    @Override
    public ExpressionValue subtract(final DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = (double) this.underlying[i] - values.underlying[i];
        }
        this.close();
        return values;
    }

    @Override
    public ExpressionValue multiply(final ExpressionValue value) {
        return value.multiply(this);
    }

    @Override
    public ExpressionValue multiply(final LongValue value) {
        for (int i = 0; i < underlying.length; ++i) {
            underlying[i] *= value.getValue();
        }
        return this;
    }

    @Override
    public ExpressionValue multiply(final DoubleValue value) {
        // Can we possibly avoid converting this array?
        if (value.isExactLong()) {
            // Yes, the subtrahend is exactly representable as a long.
            final long subtrahend = (long) value.getValue();
            for (int i = 0; i < underlying.length; ++i) {
                underlying[i] *= subtrahend;
            }
            return this;
        } else {
            // No, we need to create a double array.
            final double subtrahend = value.getValue();
            final PooledObject newUnderlying = getFactory().makeDoubleArray(
                    underlying.length);
            final DoubleArrayValue newValue = new DoubleArrayValue(getFactory(),
                    newUnderlying);
            for (int i = 0; i < underlying.length; ++i) {
                newValue.underlying[i] = (double) underlying[i] * subtrahend;
            }
            this.close();
            return newValue;
        }
    }

    @Override
    public ExpressionValue multiply(final LongArrayValue values) {
        for (int i = 0; i < underlying.length; ++i) {
            underlying[i] *= values.getValueAt(i);
        }
        values.close();
        return this;
    }

    @Override
    public ExpressionValue multiply(final DoubleArrayValue values) {
        // Defer to DoubleArrayValue
        return values.multiply(this);
    }

    @Override
    public ExpressionValue divide(final ExpressionValue value) {
        if (value instanceof LongValue) {
            return this.divide((LongValue) value);
        } else if (value instanceof LongArrayValue) {
            return this.divide((LongArrayValue) value);
        } else if (value instanceof DoubleValue) {
            return this.divide((DoubleValue) value);
        } else if (value instanceof DoubleArrayValue) {
            return this.divide((DoubleArrayValue) value);
        }

        throw new ExpressionException("unsupported expression operation: LongArrayValue.divide(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue divide(final LongValue value) {
        if (value.getValue() == 0) {
            // If divisor is 0, convert long array to double array of NaN values
            if (getFactory().getOptions().getInfectiousNaN()) {
                double[] results = new double[underlying.length];
                for (int i = 0; i < underlying.length; ++i) {
                    results[i] = NaN;
                }
                return new DoubleArrayValue(getFactory(), results);
            } else {
                for (int i = 0; i < underlying.length; ++i) {
                    underlying[i] = 0;
                }
                return this;
            }
        } else {
            if (getFactory().getOptions().getForceFloatingPointDivision()) {
                double[] results = new double[underlying.length];
                for (int i = 0; i < underlying.length; ++i) {
                    results[i] = (double) underlying[i] / value.getValue();
                }
                return new DoubleArrayValue(getFactory(), results);
            } else {
                for (int i = 0; i < underlying.length; ++i) {
                    underlying[i] /= value.getValue();
                }
                return this;
            }
        }
    }

    @Override
    public ExpressionValue divide(final DoubleValue value) {
        // Can we possibly avoid converting this array?
        if (value.isExactLong()) {
            // Yes, the subtrahend is exactly representable as a long.
            return this.divide(new LongValue(getFactory(), (long) value.getValue()));
        } else {
            // No, we need to create a double array.
            final double subtrahend = value.getValue();
            final PooledObject newUnderlying = getFactory().makeDoubleArray(
                    underlying.length);
            final DoubleArrayValue newValue = new DoubleArrayValue(getFactory(),
                    newUnderlying);

            if (subtrahend == 0) {
                double val = getFactory().getOptions().getInfectiousNaN() ? NaN : 0;
                for (int i = 0; i < underlying.length; ++i) {
                    newValue.underlying[i] = val;
                }
            } else {
                for (int i = 0; i < underlying.length; ++i) {
                    newValue.underlying[i] = underlying[i] / subtrahend;
                }
            }

            this.close();
            return newValue;
        }
    }

    @Override
    public ExpressionValue divide(final LongArrayValue values) {
        if (getFactory().getOptions().getForceFloatingPointDivision()) {
            return this.divide(new DoubleArrayValue(getFactory(), values.underlying));
        }
        for (int i = 0; i < underlying.length; ++i) {
            if (values.getValueAt(i) == 0 && getFactory().getOptions().getInfectiousNaN()) {
                return this.divide(new DoubleArrayValue(getFactory(), values.underlying));
            }
            underlying[i] = values.getValueAt(i) == 0 ? 0 : underlying[i] / values.getValueAt(i);
        }
        values.close();
        return this;
    }

    @Override
    public ExpressionValue divide(final DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = values.underlying[i] == 0 ?
                    (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                    this.underlying[i] / values.underlying[i];
        }
        this.close();
        return values;
    }

    @Override
    public ExpressionValue mod(final ExpressionValue value) {
        if (value instanceof LongValue) {
            return this.mod((LongValue) value);
        } else if (value instanceof LongArrayValue) {
            return this.mod((LongArrayValue) value);
        } else if (value instanceof DoubleValue) {
            return this.mod((DoubleValue) value);
        } else if (value instanceof DoubleArrayValue) {
            return this.mod((DoubleArrayValue) value);
        }

        throw new ExpressionException("unsupported expression operation: LongArrayValue.mod(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue mod(final LongValue value) {
        if (value.getValue() == 0) {
            // If divisor is 0, convert long array to double array of NaN values
            if (getFactory().getOptions().getInfectiousNaN()) {
                double[] results = new double[underlying.length];
                for (int i = 0; i < underlying.length; ++i) {
                    results[i] = NaN;
                }
                return new DoubleArrayValue(getFactory(), results);
            } else {
                for (int i = 0; i < underlying.length; ++i) {
                    underlying[i] = 0;
                }
                return this;
            }
        } else {
            for (int i = 0; i < underlying.length; ++i) {
                underlying[i] %= value.getValue();
            }
            return this;
        }
    }

    @Override
    public ExpressionValue mod(final DoubleValue value) {
        // Can we possibly avoid converting this array?
        if (value.isExactLong()) {
            // Yes, the subtrahend is exactly representable as a long.
            return this.mod(new LongValue(getFactory(), (long) value.getValue()));
        } else {
            // No, we need to create a double array.
            final double subtrahend = value.getValue();
            final PooledObject newUnderlying = getFactory().makeDoubleArray(
                    underlying.length);
            final DoubleArrayValue newValue = new DoubleArrayValue(getFactory(),
                    newUnderlying);

            if (subtrahend == 0) {
                double val = getFactory().getOptions().getInfectiousNaN() ? NaN : 0;
                for (int i = 0; i < underlying.length; ++i) {
                    newValue.underlying[i] = val;
                }
            } else {
                for (int i = 0; i < underlying.length; ++i) {
                    newValue.underlying[i] = underlying[i] % subtrahend;
                }
            }

            this.close();
            return newValue;
        }
    }

    @Override
    public ExpressionValue mod(final LongArrayValue values) {
        for (int i = 0; i < underlying.length; ++i) {
            if (values.getValueAt(i) == 0 && getFactory().getOptions().getInfectiousNaN()) {
                return this.mod(new DoubleArrayValue(getFactory(), values.underlying));
            }
            underlying[i] = values.getValueAt(i) == 0 ? 0 : underlying[i] % values.getValueAt(i);
        }
        values.close();
        return this;
    }

    @Override
    public ExpressionValue mod(final DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = values.underlying[i] == 0 ?
                    (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                    this.underlying[i] % values.underlying[i];
        }
        this.close();
        return values;
    }

    @Override
    public ExpressionValue power(final ExpressionValue value) {
        if (value instanceof LongValue) {
            return this.power((LongValue) value);
        } else if (value instanceof LongArrayValue) {
            return this.power((LongArrayValue) value);
        } else if (value instanceof DoubleValue) {
            return this.power((DoubleValue) value);
        } else if (value instanceof DoubleArrayValue) {
            return this.power((DoubleArrayValue) value);
        }

        throw new ExpressionException("unsupported expression operation: LongArrayValue.power(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue power(final LongValue value) {
        for (int i = 0; i < underlying.length; ++i) {
            underlying[i] = (long) Math.pow(underlying[i], value.getValue());
        }
        return this;
    }

    @Override
    public ExpressionValue power(final DoubleValue value) {
        // Can we possibly avoid converting this array?
        if (value.isExactLong()) {
            // Yes, the subtrahend is exactly representable as a long.
            final long subtrahend = (long) value.getValue();
            for (int i = 0; i < underlying.length; ++i) {
                underlying[i] = (long) Math.pow(underlying[i], subtrahend);
            }
            return this;
        } else {
            // No, we need to create a double array.
            final double subtrahend = value.getValue();
            final PooledObject newUnderlying = getFactory().makeDoubleArray(
                    underlying.length);
            final DoubleArrayValue newValue = new DoubleArrayValue(getFactory(),
                    newUnderlying);
            for (int i = 0; i < underlying.length; ++i) {
                newValue.underlying[i] = Math.pow((double) underlying[i], subtrahend);
            }
            this.close();
            return newValue;
        }
    }

    @Override
    public ExpressionValue power(final LongArrayValue values) {
        for (int i = 0; i < underlying.length; ++i) {
            underlying[i] = (long) Math.pow(underlying[i], values.getValueAt(i));
        }
        values.close();
        return this;
    }

    @Override
    public ExpressionValue power(final DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = Math.pow((double) this.underlying[i], values.underlying[i]);
        }
        this.close();
        return values;
    }

    @Override
    public ExpressionValue isEqual(ExpressionValue value) {
        return value.isEqual(this);
    }

    @Override
    public ExpressionValue isEqual(DoubleValue value) {
        for (long num : this.underlying) {
            if (num != value.underlying) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isEqual(LongValue value) {
        for (long num : this.underlying) {
            if (num != value.underlying) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isEqual(DoubleArrayValue values) {
        for (int i = 0; i < this.underlying.length; i++) {
            if (this.underlying[i] != values.underlying[i]) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isEqual(LongArrayValue values) {
        for (int i = 0; i < this.underlying.length; i++) {
            if (this.underlying[i] != values.underlying[i]) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isEqual(BooleanConstantValue value) {
        throw new ExpressionException("illegal call of isEqual() on LongArrayValue");
    }

    @Override
    public ExpressionValue isGt(ExpressionValue value) {
        if (value instanceof LongValue) {
            return this.isGt((LongValue) value);
        } else if (value instanceof LongArrayValue) {
            return this.isGt((LongArrayValue) value);
        } else if (value instanceof DoubleValue) {
            return this.isGt((DoubleValue) value);
        } else if (value instanceof DoubleArrayValue) {
            return this.isGt((DoubleArrayValue) value);
        }

        throw new ExpressionException("unsupported expression operation: LongArrayValue.isGt(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue isGt(DoubleValue value) {
        for (long num : this.underlying) {
            if (num <= value.underlying) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isGt(LongValue value) {
        for (long num : this.underlying) {
            if (num <= value.underlying) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isGt(DoubleArrayValue values) {
        for (int i = 0; i < this.underlying.length; i++) {
            if (this.underlying[i] <= values.underlying[i]) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isGt(LongArrayValue values) {
        for (int i = 0; i < this.underlying.length; i++) {
            if (this.underlying[i] <= values.underlying[i]) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isGte(ExpressionValue value) {
        if (value instanceof LongValue) {
            return this.isGte((LongValue) value);
        } else if (value instanceof LongArrayValue) {
            return this.isGte((LongArrayValue) value);
        } else if (value instanceof DoubleValue) {
            return this.isGte((DoubleValue) value);
        } else if (value instanceof DoubleArrayValue) {
            return this.isGte((DoubleArrayValue) value);
        }

        throw new ExpressionException("unsupported expression operation: LongArrayValue.isGte(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue isGte(DoubleValue value) {
        for (long num : this.underlying) {
            if (num < value.underlying) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isGte(LongValue value) {
        for (long num : this.underlying) {
            if (num < value.underlying) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isGte(DoubleArrayValue values) {
        for (int i = 0; i < this.underlying.length; i++) {
            if (this.underlying[i] < values.underlying[i]) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isGte(LongArrayValue values) {
        for (int i = 0; i < this.underlying.length; i++) {
            if (this.underlying[i] < values.underlying[i]) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isLt(ExpressionValue value) {
        if (value instanceof LongValue) {
            return this.isLt((LongValue) value);
        } else if (value instanceof LongArrayValue) {
            return this.isLt((LongArrayValue) value);
        } else if (value instanceof DoubleValue) {
            return this.isLt((DoubleValue) value);
        } else if (value instanceof DoubleArrayValue) {
            return this.isLt((DoubleArrayValue) value);
        }

        throw new ExpressionException("unsupported expression operation: LongArrayValue.isLt(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue isLt(DoubleValue value) {
        for (long num : this.underlying) {
            if (num >= value.underlying) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isLt(LongValue value) {
        for (long num : this.underlying) {
            if (num >= value.underlying) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isLt(DoubleArrayValue values) {
        for (int i = 0; i < this.underlying.length; i++) {
            if (this.underlying[i] >= values.underlying[i]) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isLt(LongArrayValue values) {
        for (int i = 0; i < this.underlying.length; i++) {
            if (this.underlying[i] >= values.underlying[i]) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isLte(ExpressionValue value) {
        if (value instanceof LongValue) {
            return this.isLte((LongValue) value);
        } else if (value instanceof LongArrayValue) {
            return this.isLte((LongArrayValue) value);
        } else if (value instanceof DoubleValue) {
            return this.isLte((DoubleValue) value);
        } else if (value instanceof DoubleArrayValue) {
            return this.isLte((DoubleArrayValue) value);
        }

        throw new ExpressionException("unsupported expression operation: LongArrayValue.isLte(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue isLte(DoubleValue value) {
        for (long num : this.underlying) {
            if (num > value.underlying) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isLte(LongValue value) {
        for (long num : this.underlying) {
            if (num > value.underlying) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isLte(DoubleArrayValue values) {
        for (int i = 0; i < this.underlying.length; i++) {
            if (this.underlying[i] > values.underlying[i]) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isLte(LongArrayValue values) {
        for (int i = 0; i < this.underlying.length; i++) {
            if (this.underlying[i] > values.underlying[i]) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (this.getClass() == other.getClass()) {
            final LongArrayValue that = (LongArrayValue) other;
            return Arrays.equals(this.underlying, that.underlying);
        }

        return false;
    }

    @Override
    public String toString() {
        return "LongArrayValue{" + Longs.join(",", underlying) + "}";
    }
}
