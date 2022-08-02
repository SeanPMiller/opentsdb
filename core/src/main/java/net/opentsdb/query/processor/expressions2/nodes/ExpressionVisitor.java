package net.opentsdb.query.processor.expressions2.nodes;

public interface ExpressionVisitor {
    void enterAddition(Addition a);
    void leaveAddition(Addition a);

    void enterSubtraction(Subtraction s);
    void leaveSubtraction(Subtraction s);

    void enterDivision(Division s);
    void leaveDivision(Division s);

    void enterMultiplication(Multiplication s);
    void leaveMultiplication(Multiplication s);

    void enterModulo(Modulo s);
    void leaveModulo(Modulo s);

    void enterPower(Power s);
    void leavePower(Power s);

    void enterEqual(Equal s);
    void leaveEqual(Equal s);

    void enterGte(Gte s);
    void leaveGte(Gte s);

    void enterGt(Gt s);
    void leaveGt(Gt s);

    void enterLte(Lte s);
    void leaveLte(Lte s);

    void enterLt(Lt s);
    void leaveLt(Lt s);

    void enterNotEq(NotEq s);
    void leaveNotEq(NotEq s);

    void enterTernary(TernaryOperator s);
    void leaveTernary(TernaryOperator s);

    void enterBool(Bool b);
    void leaveBool(Bool b);

    void enterLogicalNegation(LogicalNegation n);
    void leaveLogicalNegation(LogicalNegation n);

    void enterMetric(Metric m);
    void leaveMetric(Metric m);

    void enterDouble(Double d);
    void leaveDouble(Double d);

    void enterLong(Long l);
    void leaveLong(Long l);

    void enterNumericNegation(NumericNegation n);
    void leaveNumericNegation(NumericNegation n);

    void enterAnd(And n);
    void leaveAnd(And n);

    void enterOr(Or n);
    void leaveOr(Or n);
}
