package net.opentsdb.query.processor.expressions2.eval;

import com.google.common.math.DoubleMath;
import net.opentsdb.pools.PooledObject;
import net.opentsdb.query.processor.expressions2.ExpressionException;
import net.opentsdb.query.processor.expressions2.nodes.Double;
import net.opentsdb.query.processor.expressions2.nodes.ExpressionNode;

public class DoubleValue extends NumericValue {
    double underlying;

    public DoubleValue(final ExpressionFactory factory, final double value) {
        super(factory);
        this.underlying = value;
    }

    public double getValue() {
        return underlying;
    }

    public boolean isExactLong() {
        return DoubleMath.isMathematicalInteger(underlying);
    }

    @Override
    public void close() {
    }

    @Override
    public ExpressionNode asNode() {
        return new Double(underlying);
    }

    @Override
    public ExpressionValue makeCopy() {
        return new DoubleValue(getFactory(), underlying);
    }

    @Override
    public ExpressionValue negate() {
        underlying = -underlying;
        return this;
    }

    @Override
    public ExpressionValue add(final ExpressionValue value) {
        return value.add(this);
    }

    @Override
    public ExpressionValue add(final LongValue value) {
        this.underlying += (double) value.getValue();
        return this;
    }

    @Override
    public ExpressionValue add(final DoubleValue value) {
        this.underlying += value.underlying;
        return this;
    }

    @Override
    public ExpressionValue add(final LongArrayValue values) {
        // Defer to LongArrayValue implementation.
        return values.add(this);
    }

    @Override
    public ExpressionValue add(final DoubleArrayValue values) {
        // Defer to DoubleArrayValue implementation.
        return values.add(this);
    }

    @Override
    public ExpressionValue subtract(final ExpressionValue value) {
        return value.negate().add(this);
    }

    @Override
    public ExpressionValue subtract(final LongValue value) {
        this.underlying -= (double) value.getValue();
        return this;
    }

    @Override
    public ExpressionValue subtract(final DoubleValue value) {
        this.underlying -= value.underlying;
        return this;
    }

    @Override
    public ExpressionValue subtract(final LongArrayValue values) {
        final PooledObject newUnderlying = getFactory().makeDoubleArray(values.underlying.length);
        final DoubleArrayValue newValue = new DoubleArrayValue(getFactory(), newUnderlying);
        for (int i = 0; i < values.underlying.length; ++i) {
            newValue.underlying[i] = this.underlying - (double) values.underlying[i];
        }
        values.close();
        return newValue;
    }

    @Override
    public ExpressionValue subtract(final DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = this.underlying - values.underlying[i];
        }
        return values;
    }

    @Override
    public ExpressionValue multiply(final ExpressionValue value) {
        return value.multiply(this);
    }

    @Override
    public ExpressionValue multiply(final LongValue value) {
        this.underlying *= (double) value.getValue();
        return this;
    }

    @Override
    public ExpressionValue multiply(final DoubleValue value) {
        this.underlying *= value.underlying;
        return this;
    }

    @Override
    public ExpressionValue multiply(final LongArrayValue values) {
        // Defer to LongArrayValue
        return values.multiply(this);
    }

    @Override
    public ExpressionValue multiply(final DoubleArrayValue values) {
        // Defer to DoubleArrayValue
        return values.multiply(this);
    }

    @Override
    public ExpressionValue divide(ExpressionValue value) {
        if (value instanceof LongValue) {
            return this.divide((LongValue) value);
        } else if (value instanceof LongArrayValue) {
            return this.divide((LongArrayValue) value);
        } else if (value instanceof DoubleValue) {
            return this.divide((DoubleValue) value);
        } else if (value instanceof DoubleArrayValue) {
            return this.divide((DoubleArrayValue) value);
        }

        throw new ExpressionException("unsupported expression operation: DoubleValue.divide(unknown ExpressionValue)");
    }

    // TODO: handle divide-by-zero exception
    @Override
    public ExpressionValue divide(LongValue value) {
        this.underlying = value.getValue() == 0 ?
                (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                this.underlying / value.getValue();
        return this;
    }

    @Override
    public ExpressionValue divide(DoubleValue value) {
        this.underlying = value.getValue() == 0 ?
                (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                this.underlying / value.getValue();
        return this;
    }

    @Override
    public ExpressionValue divide(LongArrayValue values) {
        final PooledObject newUnderlying = getFactory().makeDoubleArray(values.underlying.length);
        final DoubleArrayValue newValue = new DoubleArrayValue(getFactory(), newUnderlying);
        for (int i = 0; i < values.underlying.length; ++i) {
            newValue.underlying[i] = values.underlying[i] == 0 ?
                    (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                    this.underlying / values.underlying[i];
        }
        values.close();
        return newValue;
    }

    @Override
    public ExpressionValue divide(DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = values.underlying[i] == 0 ?
                    (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                    this.underlying / values.underlying[i];
        }
        return values;
    }

    @Override
    public ExpressionValue mod(ExpressionValue value) {
        if (value instanceof LongValue) {
            return this.mod((LongValue) value);
        } else if (value instanceof LongArrayValue) {
            return this.mod((LongArrayValue) value);
        } else if (value instanceof DoubleValue) {
            return this.mod((DoubleValue) value);
        } else if (value instanceof DoubleArrayValue) {
            return this.mod((DoubleArrayValue) value);
        }

        throw new ExpressionException("unsupported expression operation: DoubleValue.mod(unknown ExpressionValue)");
    }

    // TODO: handle divide-by-zero exception
    @Override
    public ExpressionValue mod(LongValue value) {
        this.underlying = value.getValue() == 0 ?
                (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                this.underlying % value.getValue();
        return this;
    }

    @Override
    public ExpressionValue mod(DoubleValue value) {
        this.underlying = value.getValue() == 0 ?
                (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                this.underlying % value.getValue();
        return this;
    }

    @Override
    public ExpressionValue mod(LongArrayValue values) {
        final PooledObject newUnderlying = getFactory().makeDoubleArray(values.underlying.length);
        final DoubleArrayValue newValue = new DoubleArrayValue(getFactory(), newUnderlying);
        for (int i = 0; i < values.underlying.length; ++i) {
            newValue.underlying[i] = values.underlying[i] == 0 ?
                    (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                    this.underlying % values.underlying[i];
        }
        values.close();
        return newValue;
    }

    @Override
    public ExpressionValue mod(DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = values.underlying[i] == 0 ?
                    (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                    this.underlying % values.underlying[i];
        }
        return values;
    }

    @Override
    public ExpressionValue power(ExpressionValue value) {
        if (value instanceof LongValue) {
            return this.power((LongValue) value);
        } else if (value instanceof LongArrayValue) {
            return this.power((LongArrayValue) value);
        } else if (value instanceof DoubleValue) {
            return this.power((DoubleValue) value);
        } else if (value instanceof DoubleArrayValue) {
            return this.power((DoubleArrayValue) value);
        }

        throw new ExpressionException("unsupported expression operation: DoubleValue.power(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue power(LongValue value) {
        this.underlying = Math.pow(this.underlying, (double) value.getValue());
        return this;
    }

    @Override
    public ExpressionValue power(DoubleValue value) {
        this.underlying = Math.pow(this.underlying, value.getValue());
        return this;
    }

    @Override
    public ExpressionValue power(LongArrayValue values) {
        final PooledObject newUnderlying = getFactory().makeDoubleArray(values.underlying.length);
        final DoubleArrayValue newValue = new DoubleArrayValue(getFactory(), newUnderlying);
        for (int i = 0; i < values.underlying.length; ++i) {
            newValue.underlying[i] = Math.pow(this.underlying, (double) values.underlying[i]);
        }
        values.close();
        return newValue;
    }

    @Override
    public ExpressionValue power(DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = Math.pow(this.underlying, values.underlying[i]);
        }
        return values;
    }

    @Override
    public ExpressionValue isEqual(ExpressionValue value) {
        return value.isEqual(this);
    }

    @Override
    public ExpressionValue isEqual(DoubleValue value) {
        return this.underlying == value.underlying ? BooleanConstantValue.TRUE : BooleanConstantValue.FALSE;
    }

    @Override
    public ExpressionValue isEqual(LongValue value) {
        return this.underlying == value.underlying ? BooleanConstantValue.TRUE : BooleanConstantValue.FALSE;
    }

    @Override
    public ExpressionValue isEqual(DoubleArrayValue values) {
        return values.isEqual(this);
    }

    @Override
    public ExpressionValue isEqual(LongArrayValue values) {
        return values.isEqual(this);
    }

    @Override
    public ExpressionValue isEqual(BooleanConstantValue value) {
        throw new ExpressionException("illegal call of isEqual() on DoubleValue");
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

        throw new ExpressionException("unsupported expression operation: DoubleValue.isGt(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue isGt(DoubleValue value) {
        return this.underlying > value.underlying ? BooleanConstantValue.TRUE : BooleanConstantValue.FALSE;
    }

    @Override
    public ExpressionValue isGt(LongValue value) {
        return this.underlying > value.underlying ? BooleanConstantValue.TRUE : BooleanConstantValue.FALSE;
    }

    @Override
    public ExpressionValue isGt(DoubleArrayValue values) {
        for (double num : values.underlying) {
            if (this.underlying <= num) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isGt(LongArrayValue values) {
        for (long num : values.underlying) {
            if (this.underlying <= num) {
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

        throw new ExpressionException("unsupported expression operation: DoubleValue.isGte(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue isGte(DoubleValue value) {
        return this.underlying >= value.underlying ? BooleanConstantValue.TRUE : BooleanConstantValue.FALSE;
    }

    @Override
    public ExpressionValue isGte(LongValue value) {
        return this.underlying >= value.underlying ? BooleanConstantValue.TRUE : BooleanConstantValue.FALSE;
    }

    @Override
    public ExpressionValue isGte(DoubleArrayValue values) {
        for (double num : values.underlying) {
            if (this.underlying < num) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isGte(LongArrayValue values) {
        for (long num : values.underlying) {
            if (this.underlying < num) {
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

        throw new ExpressionException("unsupported expression operation: DoubleValue.isLt(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue isLt(DoubleValue value) {
        return this.underlying < value.underlying ? BooleanConstantValue.TRUE : BooleanConstantValue.FALSE;
    }

    @Override
    public ExpressionValue isLt(LongValue value) {
        return this.underlying < value.underlying ? BooleanConstantValue.TRUE : BooleanConstantValue.FALSE;
    }

    @Override
    public ExpressionValue isLt(DoubleArrayValue values) {
        for (double num : values.underlying) {
            if (this.underlying >= num) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isLt(LongArrayValue values) {
        for (long num : values.underlying) {
            if (this.underlying >= num) {
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

        throw new ExpressionException("unsupported expression operation: DoubleValue.isLte(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue isLte(DoubleValue value) {
        return this.underlying <= value.underlying ? BooleanConstantValue.TRUE : BooleanConstantValue.FALSE;
    }

    @Override
    public ExpressionValue isLte(LongValue value) {
        return this.underlying <= value.underlying ? BooleanConstantValue.TRUE : BooleanConstantValue.FALSE;
    }

    @Override
    public ExpressionValue isLte(DoubleArrayValue values) {
        for (double num : values.underlying) {
            if (this.underlying > num) {
                return BooleanConstantValue.FALSE;
            }
        }
        return BooleanConstantValue.TRUE;
    }

    @Override
    public ExpressionValue isLte(LongArrayValue values) {
        for (long num : values.underlying) {
            if (this.underlying > num) {
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
            final DoubleValue that = (DoubleValue) other;
            return DoubleMath.fuzzyEquals(this.underlying, that.underlying, EPSILON);
        }

        return false;
    }

    @Override
    public String toString() {
        return "DoubleValue{" + underlying + "}";
    }
}
