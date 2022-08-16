package net.opentsdb.query.processor.expressions2.eval;

import net.opentsdb.query.processor.expressions2.ExpressionException;
import net.opentsdb.query.processor.expressions2.nodes.ExpressionNode;
import net.opentsdb.query.processor.expressions2.nodes.Long;

public class LongValue extends NumericValue {
    long underlying;

    public LongValue(final ExpressionFactory factory, final long value) {
        super(factory);
        this.underlying = value;
    }

    public long getValue() {
        return underlying;
    }

    @Override
    public void close() {
    }

    @Override
    public ExpressionNode asNode() {
        return new Long(underlying);
    }

    @Override
    public ExpressionValue makeCopy() {
        return new LongValue(getFactory(), underlying);
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
        this.underlying += value.underlying;
        return this;
    }

    @Override
    public ExpressionValue add(final DoubleValue value) {
        // Defer to DoubleValue implementation.
        return value.add(this);
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
        this.underlying -= value.underlying;
        return this;
    }

    @Override
    public ExpressionValue subtract(final DoubleValue value) {
        value.underlying = this.underlying - value.underlying;
        return value;
    }

    @Override
    public ExpressionValue subtract(final LongArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = this.underlying - values.underlying[i];
        }
        return values;
    }

    @Override
    public ExpressionValue subtract(final DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = (double) this.underlying - values.underlying[i];
        }
        return values;
    }

    @Override
    public ExpressionValue multiply(final ExpressionValue value) {
        return value.multiply(this);
    }

    @Override
    public ExpressionValue multiply(final LongValue value) {
        this.underlying *= value.underlying;
        return this;
    }

    @Override
    public ExpressionValue multiply(final DoubleValue value) {
        // Defer to DoubleValue
        return value.multiply(this);
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

        throw new ExpressionException("unsupported expression operation: LongValue.divide(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue divide(final LongValue value) {
        if (value.underlying == 0) {
            if (getFactory().getOptions().getInfectiousNaN()) {
                return new DoubleValue(getFactory(), NaN);
            } else {
                underlying = 0;
                return this;
            }
        }

        if (getFactory().getOptions().getForceFloatingPointDivision()) {
            return new DoubleValue(getFactory(), (double) this.underlying / value.underlying);
        }

        this.underlying /= value.underlying;
        return this;
    }

    @Override
    public ExpressionValue divide(final DoubleValue value) {
        value.underlying = value.underlying == 0 ?
                (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                this.underlying / value.underlying;
        return value;
    }

    @Override
    public ExpressionValue divide(final LongArrayValue values) {
        if (getFactory().getOptions().getForceFloatingPointDivision()) {
            double result[] = new double[values.underlying.length];
            for (int i = 0; i < values.underlying.length; ++i) {
                result[i] = values.underlying[i] == 0 ?
                        (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                        (double) this.underlying / values.underlying[i];
            }
            return new DoubleArrayValue(getFactory(), result);
        }

        for (int i = 0; i < values.underlying.length; ++i) {
            if (values.underlying[i] == 0) {
                return this.divide(new DoubleArrayValue(getFactory(), values.underlying));
            }
            values.underlying[i] = this.underlying / values.underlying[i];
        }
        return values;
    }

    @Override
    public ExpressionValue divide(final DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = values.underlying[i] == 0 ?
                    (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                    this.underlying / values.underlying[i];
        }
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

        throw new ExpressionException("unsupported expression operation: LongValue.mod(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue mod(final LongValue value) {
        if (value.underlying == 0) {
            if (getFactory().getOptions().getInfectiousNaN()) {
                return new DoubleValue(getFactory(), NaN);
            } else {
                underlying = 0;
                return this;
            }
        }

        this.underlying %= value.underlying;
        return this;
    }

    @Override
    public ExpressionValue mod(final DoubleValue value) {
        value.underlying = value.underlying == 0 ? (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                this.underlying % value.underlying;
        return value;
    }

    @Override
    public ExpressionValue mod(final LongArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            if (values.underlying[i] == 0) {
                return this.mod(new DoubleArrayValue(getFactory(), values.underlying));
            }
            values.underlying[i] = this.underlying % values.underlying[i];
        }
        return values;
    }

    @Override
    public ExpressionValue mod(final DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = values.underlying[i] == 0 ?
                    (getFactory().getOptions().getInfectiousNaN() ? NaN : 0) :
                    this.underlying % values.underlying[i];
        }
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

        throw new ExpressionException("unsupported expression operation: LongValue.power(unknown ExpressionValue)");
    }

    @Override
    public ExpressionValue power(final LongValue value) {
        this.underlying = (long) Math.pow(this.underlying, value.underlying);
        return this;
    }

    @Override
    public ExpressionValue power(final DoubleValue value) {
        value.underlying = Math.pow(this.underlying, value.underlying);
        return value;
    }

    @Override
    public ExpressionValue power(final LongArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = (long) Math.pow(this.underlying, values.underlying[i]);
        }
        return values;
    }

    @Override
    public ExpressionValue power(final DoubleArrayValue values) {
        for (int i = 0; i < values.underlying.length; ++i) {
            values.underlying[i] = Math.pow((double) this.underlying, values.underlying[i]);
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
        throw new ExpressionException("illegal call of isEqual() on LongValue");
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

        throw new ExpressionException("unsupported expression operation: LongValue.isGt(unknown ExpressionValue)");
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

        throw new ExpressionException("unsupported expression operation: LongValue.isGte(unknown ExpressionValue)");
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

        throw new ExpressionException("unsupported expression operation: LongValue.isLt(unknown ExpressionValue)");
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

        throw new ExpressionException("unsupported expression operation: LongValue.isLte(unknown ExpressionValue)");
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
            final LongValue that = (LongValue) other;
            return this.underlying == that.underlying;
        }

        return false;
    }

    @Override
    public String toString() {
        return "LongValue{" + underlying + "}";
    }
}
