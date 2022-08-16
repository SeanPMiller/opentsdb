package net.opentsdb.query.processor.expressions2.eval;

import net.opentsdb.query.processor.expressions2.ExpressionException;

public abstract class NumericValue extends ExpressionValue {
    public static final double EPSILON = 1e-14;
    public final double NaN = Double.NaN;

    public NumericValue(final ExpressionFactory factory) {
        super(factory);
    }

    @Override
    public ExpressionValue complement() {
        throw new ExpressionException("illegal call of complement() on NumericValue");
    }
}
