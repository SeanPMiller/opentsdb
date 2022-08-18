package net.opentsdb.query.processor.expressions2.nodes;

import net.opentsdb.query.processor.expressions2.ExpressionException;
import net.opentsdb.query.processor.expressions2.types.FunctionType;
import net.opentsdb.query.processor.expressions2.types.TupleType;
import net.opentsdb.query.processor.expressions2.types.TypeLiteral;

public class TernaryOperator extends ExpressionOperator {

    protected ExpressionNode condition;
    protected ExpressionNode trueCase;
    protected ExpressionNode falseCase;

    public TernaryOperator(final ExpressionNode conditionOperand, final ExpressionNode trueCaseOperand, final ExpressionNode falseCaseOperand) {
        super("cond ? expr1 : expr2", new TupleType(conditionOperand.getType(), trueCaseOperand.getType(), falseCaseOperand.getType()));

        conditionOperand.setParent(this);
        trueCaseOperand.setParent(this);
        falseCaseOperand.setParent(this);

        condition = conditionOperand;
        trueCase = trueCaseOperand;
        falseCase = falseCaseOperand;
    }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.enterTernary(this);
        condition.accept(visitor);
        visitor.leaveTernary(this);
    }

    @Override
    public int getArity() {
        return 3;
    }

    private static FunctionType[] SIGNATURES = {
            new FunctionType(new TupleType(TypeLiteral.BOOLEAN, TypeLiteral.NUMERIC, TypeLiteral.NUMERIC), TypeLiteral.NUMERIC),
    };

    @Override
    public FunctionType[] getTypeSignatures() {
        return SIGNATURES;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public void setCondition(ExpressionNode condition) {
        if (!this.condition.getType().equals(condition.getType())) {
            throw new ExpressionException("bad type for condition operand in TernaryOperator");
        }
        this.condition = condition;
    }

    public ExpressionNode getTrueCase() {
        return trueCase;
    }

    public void setTrueCase(ExpressionNode trueCase) {
        if (!this.trueCase.getType().equals(trueCase.getType())) {
            throw new ExpressionException("bad type for trueCase operand in TernaryOperator");
        }
        this.trueCase = trueCase;
    }

    public ExpressionNode getFalseCase() {
        return falseCase;
    }

    public void setFalseCase(ExpressionNode falseCase) {
        if (!this.falseCase.getType().equals(falseCase.getType())) {
            throw new ExpressionException("bad type for falseCase operand in TernaryOperator");
        }
        this.falseCase = falseCase;
    }

    @Override
    public void replaceChild(ExpressionNode replaceThis, ExpressionNode withThis) {
        if (replaceThis == condition) {
            setCondition(withThis);
            return ;
        }

        if (replaceThis == trueCase) {
            setTrueCase(withThis);
            return ;
        }

        if (replaceThis == falseCase) {
            setFalseCase(withThis);
            return ;
        }

        throw new ExpressionException("asked to replace child that does not exist in TernaryOperator");
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other instanceof TernaryOperator) {
            final TernaryOperator that = (TernaryOperator) other;
            return this.condition.equals(that.condition) &&
                    this.trueCase.equals(that.trueCase) &&
                    this.falseCase.equals(that.falseCase) &&
                    super.equals(that);
        }

        return false;
    }
}
