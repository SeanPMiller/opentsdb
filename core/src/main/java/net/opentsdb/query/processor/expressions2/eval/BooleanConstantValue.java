package net.opentsdb.query.processor.expressions2.eval;

import jdk.internal.icu.util.CodePointMap;
import net.opentsdb.query.processor.expressions2.ExpressionException;
import net.opentsdb.query.processor.expressions2.nodes.Bool;
import net.opentsdb.query.processor.expressions2.nodes.ExpressionNode;

public class BooleanConstantValue extends ExpressionValue {
    private final boolean underlying;

    private BooleanConstantValue(final ExpressionFactory factory, final boolean value) {
        super(factory);
        this.underlying = value;
    }

    public static final BooleanConstantValue TRUE = new BooleanConstantValue(null, true);
    public static final BooleanConstantValue FALSE = new BooleanConstantValue(null, false);

    @Override
    public void close() {
    }

    @Override
    public ExpressionNode asNode() {
        return TRUE == this ? Bool.TRUE : Bool.FALSE;
    }

    @Override
    public ExpressionValue makeCopy() {
        return this; // no such thing -- immutable object
    }

    @Override
    public ExpressionValue negate() {
        throw new ExpressionException("illegal call of negate() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue complement() {
        return TRUE == this ? FALSE : TRUE;
    }

    @Override
    public ExpressionValue add(final ExpressionValue value) {
        throw new ExpressionException("illegal call of add() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue add(final LongValue value) {
        throw new ExpressionException("illegal call of add() on LongValue");
    }

    @Override
    public ExpressionValue add(final DoubleValue value) {
        throw new ExpressionException("illegal call of add() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue add(final LongArrayValue values) {
        throw new ExpressionException("illegal call of add() on LongValue");
    }

    @Override
    public ExpressionValue add(final DoubleArrayValue values) {
        throw new ExpressionException("illegal call of add() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue subtract(final ExpressionValue value) {
        throw new ExpressionException("illegal call of subtract() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue subtract(final LongValue value) {
        throw new ExpressionException("illegal call of subtract() on LongValue");
    }

    @Override
    public ExpressionValue subtract(final DoubleValue value) {
        throw new ExpressionException("illegal call of subtract() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue subtract(final LongArrayValue values) {
        throw new ExpressionException("illegal call of subtract() on LongValue");
    }

    @Override
    public ExpressionValue subtract(final DoubleArrayValue values) {
        throw new ExpressionException("illegal call of subtract() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue multiply(ExpressionValue value) {
        throw new ExpressionException("illegal call of multiply() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue multiply(LongValue value) {
        throw new ExpressionException("illegal call of multiply() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue multiply(DoubleValue value) {
        throw new ExpressionException("illegal call of multiply() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue multiply(LongArrayValue values) {
        throw new ExpressionException("illegal call of multiply() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue multiply(DoubleArrayValue values) {
        throw new ExpressionException("illegal call of multiply() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue divide(ExpressionValue value) {
        throw new ExpressionException("illegal call of divide() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue divide(LongValue value) {
        throw new ExpressionException("illegal call of divide() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue divide(DoubleValue value) {
        throw new ExpressionException("illegal call of divide() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue divide(LongArrayValue values) {
        throw new ExpressionException("illegal call of divide() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue divide(DoubleArrayValue values) {
        throw new ExpressionException("illegal call of divide() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue mod(ExpressionValue value) {
        throw new ExpressionException("illegal call of modulo() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue mod(LongValue value) {
        throw new ExpressionException("illegal call of modulo() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue mod(DoubleValue value) {
        throw new ExpressionException("illegal call of modulo() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue mod(LongArrayValue values) {
        throw new ExpressionException("illegal call of modulo() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue mod(DoubleArrayValue values) {
        throw new ExpressionException("illegal call of modulo() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue power(ExpressionValue value) {
        throw new ExpressionException("illegal call of power() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue power(LongValue value) {
        throw new ExpressionException("illegal call of power() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue power(DoubleValue value) {
        throw new ExpressionException("illegal call of power() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue power(LongArrayValue values) {
        throw new ExpressionException("illegal call of power() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue power(DoubleArrayValue values) {
        throw new ExpressionException("illegal call of power() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isEqual(ExpressionValue value) {
        return value.isEqual(this);
    }

    @Override
    public ExpressionValue isEqual(DoubleValue value) {
        throw new ExpressionException("illegal call of isEqual() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isEqual(LongValue value) {
        throw new ExpressionException("illegal call of isEqual() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isEqual(DoubleArrayValue values) {
        throw new ExpressionException("illegal call of isEqual() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isEqual(LongArrayValue values) {
        throw new ExpressionException("illegal call of isEqual() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isEqual(BooleanConstantValue value) {
        return this.underlying == value.underlying ? BooleanConstantValue.TRUE : BooleanConstantValue.FALSE;
    }

    @Override
    public ExpressionValue isGt(ExpressionValue value) {
        throw new ExpressionException("illegal call of isGt() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isGt(DoubleValue value) {
        throw new ExpressionException("illegal call of isGt() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isGt(LongValue value) {
        throw new ExpressionException("illegal call of isGt() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isGt(DoubleArrayValue values) {
        throw new ExpressionException("illegal call of isGt() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isGt(LongArrayValue values) {
        throw new ExpressionException("illegal call of isGt() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isGte(ExpressionValue value) {
        throw new ExpressionException("illegal call of isGte() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isGte(DoubleValue value) {
        throw new ExpressionException("illegal call of isGte() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isGte(LongValue value) {
        throw new ExpressionException("illegal call of isGte() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isGte(DoubleArrayValue values) {
        throw new ExpressionException("illegal call of isGte() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isGte(LongArrayValue values) {
        throw new ExpressionException("illegal call of isGte() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isLt(ExpressionValue value) {
        throw new ExpressionException("illegal call of isLt() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isLt(DoubleValue value) {
        throw new ExpressionException("illegal call of isLt() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isLt(LongValue value) {
        throw new ExpressionException("illegal call of isLt() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isLt(DoubleArrayValue values) {
        throw new ExpressionException("illegal call of isLt() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isLt(LongArrayValue values) {
        throw new ExpressionException("illegal call of isLt() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isLte(ExpressionValue value) {
        throw new ExpressionException("illegal call of isLte() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isLte(DoubleValue value) {
        throw new ExpressionException("illegal call of isLte() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isLte(LongValue value) {
        throw new ExpressionException("illegal call of isLte() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isLte(DoubleArrayValue values) {
        throw new ExpressionException("illegal call of isLte() on BooleanConstantValue");
    }

    @Override
    public ExpressionValue isLte(LongArrayValue values) {
        throw new ExpressionException("illegal call of isLte() on BooleanConstantValue");
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
            final BooleanConstantValue that = (BooleanConstantValue) other;
            return this.underlying == that.underlying;
        }

        return false;
    }

    @Override
    public String toString() {
        return "BooleanConstantValue{" + underlying + "}";
    }
}
