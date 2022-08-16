package net.opentsdb.query.processor.expressions2.eval;

import java.util.HashMap;
import java.util.Map;

import net.opentsdb.query.processor.expressions2.ExpressionException;
import net.opentsdb.query.processor.expressions2.nodes.*;
import net.opentsdb.query.processor.expressions2.nodes.Double;
import net.opentsdb.query.processor.expressions2.nodes.Long;

public class Evaluator extends DefaultExpressionVisitor {
    static final class TerminalState {
        private final ExpressionValue value;
        private int usesRemaining;

        TerminalState(final ExpressionValue v, final int uses) {
            value = v;
            usesRemaining = uses;
        }

        ExpressionValue use() {
            --usesRemaining;

            if (usesRemaining > 0) {
                return value.makeCopy();
            } else if (0 == usesRemaining) {
                return value;
            } else {
                throw new ExpressionException("tried to use terminal too many times in Evaluator");
            }
        }
    }

    private final ExpressionFactory factory;
    private final EvaluationContext context;
    private final Map<String, TerminalState> terminals;

    /**
     * TODO
     *
     * @param factory
     * @param context
     */
    public Evaluator(final ExpressionFactory factory,
                     final EvaluationContext context) {
        this.factory = factory;
        this.context = context;

        terminals = new HashMap<>();
    }

    void reset() {
        terminals.clear();
    }

    /**
     * Evaluate the given parsed expression using the configured context.
     *
     * @return An AutoCloseable object with backing storage that may still be
     * held by an object pool.
     */
    public ExpressionValue evaluate(final ExpressionNode parseTree) {
        reset();

        parseTree.accept(this);

        if (context.stackSize() != 1) {
            throw new ExpressionException("postcondition violation in Evaluator");
        }

        return context.pop();
    }

    @Override
    public void leaveAddition(final Addition a) {
        // TODO: handle infectiousNan
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.add(rhs));
    }

    @Override
    public void leaveSubtraction(Subtraction s) {
        // TODO: handle infectiousNan
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.subtract(rhs));
    }

    @Override
    public void leaveMultiplication(Multiplication s) {
        // TODO: handle infectiousNan
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.multiply(rhs));
    }

    @Override
    public void leaveDivision(Division s) {
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.divide(rhs));
    }

    @Override
    public void leaveModulo(Modulo s) {
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.mod(rhs));
    }

    @Override
    public void leavePower(Power s) {
        // TODO: handle infectiousNan
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.power(rhs));
    }

    @Override
    public void leaveEqual(Equal s) {
        // TODO: handle infectiousNan
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.isEqual(rhs));
    }

    @Override
    public void leaveGte(Gte s) {
        // TODO: handle infectiousNan
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.isGte(rhs));
    }

    @Override
    public void leaveGt(Gt s) {
        // TODO: handle infectiousNan
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.isGt(rhs));
    }

    @Override
    public void leaveLte(Lte s) {
        // TODO: handle infectiousNan
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.isLte(rhs));
    }

    @Override
    public void leaveLt(Lt s) {
        // TODO: handle infectiousNan
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.isLt(rhs));
    }

    @Override
    public void leaveNotEq(NotEq s) {
        // TODO: handle infectiousNan
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        context.push(lhs.isEqual(rhs).complement());
    }

    @Override
    public void leaveTernary(TernaryOperator s) {
        final ExpressionValue falseCase = context.pop();
        final ExpressionValue trueCase = context.pop();
        final ExpressionValue condition = context.pop();

        if (condition.equals(BooleanConstantValue.TRUE)) {
            context.push(trueCase);
        } else {
            context.push(falseCase);
        }
    }

    @Override
    public void leaveBool(final Bool b) {
        context.push(Bool.TRUE == b ?
                BooleanConstantValue.TRUE :
                BooleanConstantValue.FALSE);
    }

    @Override
    public void leaveAnd(final And s) {
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        if (lhs.equals(BooleanConstantValue.TRUE) && rhs.equals(BooleanConstantValue.TRUE)) {
            context.push(BooleanConstantValue.TRUE);
        } else {
            context.push(BooleanConstantValue.FALSE);
        }
    }

    @Override
    public void leaveOr(final Or s) {
        final ExpressionValue rhs = context.pop();
        final ExpressionValue lhs = context.pop();
        if (lhs.equals(BooleanConstantValue.TRUE) || rhs.equals(BooleanConstantValue.TRUE)) {
            context.push(BooleanConstantValue.TRUE);
        } else {
            context.push(BooleanConstantValue.FALSE);
        }
    }

    @Override
    public void leaveLogicalNegation(final LogicalNegation n) {
        context.push(context.pop().complement()); // bools are immutable
    }

    ExpressionValue getTerminalValue(final Metric m) {
        TerminalState s = terminals.get(m.getName());
        if (null == s) {
            ExpressionValue v = context.lookup(m.getName());
            if (factory.getOptions().getAllowMetricReuse()) {
                v = v.makeCopy();
            }

            s = new TerminalState(v, m.getUses());
            terminals.put(m.getName(), s);
        }
        return s.use();
    }

    @Override
    public void leaveMetric(final Metric m) {
        final ExpressionValue v = getTerminalValue(m);
        context.push(v);
    }

    @Override
    public void leaveDouble(final Double d) {
        context.push(factory.makeValueFrom(d.getValue()));
    }

    @Override
    public void leaveLong(final Long l) {
        context.push(factory.makeValueFrom(l.getValue()));
    }

    @Override
    public void leaveNumericNegation(final NumericNegation n) {
        context.peek().negate();
    }
}
