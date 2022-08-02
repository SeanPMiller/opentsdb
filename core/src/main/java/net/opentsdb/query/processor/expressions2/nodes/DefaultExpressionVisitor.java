package net.opentsdb.query.processor.expressions2.nodes;

public class DefaultExpressionVisitor implements ExpressionVisitor {
    @Override
    public void enterAddition(final Addition a) {
    }

    @Override
    public void leaveAddition(final Addition a) {
    }

    @Override
    public void enterSubtraction(final Subtraction s) {
    }

    @Override
    public void leaveSubtraction(final Subtraction s) {
    }

    public void enterDivision(final Division s) {
    }

    public void leaveDivision(final Division s) {
    }

    public void enterMultiplication(final Multiplication s) {
    }

    public void leaveMultiplication(final Multiplication s) {
    }

    public void enterModulo(final Modulo s) {
    }

    public void leaveModulo(final Modulo s) {
    }

    public void enterPower(final Power s) {
    }

    public void leavePower(final Power s) {
    }

    public void enterEqual(final Equal s) {
    }

    public void leaveEqual(final Equal s) {
    }

    public void enterGte(final Gte s) {
    }

    public void leaveGte(Gte s) {
    }

    public void enterGt(Gt s) {
    }

    public void leaveGt(Gt s) {
    }

    public void enterLte(Lte s) {
    }

    public void leaveLte(Lte s) {
    }

    public void enterLt(Lt s) {
    }

    public void leaveLt(Lt s) {
    }

    public void enterNotEq(NotEq s) {
    }

    public void leaveNotEq(NotEq s) {
    }

    public void enterTernary(final TernaryOperator s) {
    }

    public void leaveTernary(final TernaryOperator s) {
    }

    @Override
    public void enterBool(final Bool b) {
    }

    @Override
    public void leaveBool(final Bool b) {
    }

    @Override
    public void enterLogicalNegation(final LogicalNegation n) {
    }

    @Override
    public void leaveLogicalNegation(final LogicalNegation n) {
    }

    @Override
    public void enterMetric(final Metric m) {
    }

    @Override
    public void leaveMetric(final Metric m) {
    }

    @Override
    public void enterDouble(final Double d) {
    }

    @Override
    public void leaveDouble(final Double d) {
    }

    @Override
    public void enterLong(final Long l) {
    }

    @Override
    public void leaveLong(final Long l) {
    }

    @Override
    public void enterNumericNegation(final NumericNegation n) {
    }

    @Override
    public void leaveNumericNegation(final NumericNegation n) {
    }

    @Override
    public void enterAnd(final And l) {
    }

    @Override
    public void leaveAnd(final And l) {
    }

    @Override
    public void enterOr(final Or l) {
    }

    @Override
    public void leaveOr(final Or l) {
    }
}
