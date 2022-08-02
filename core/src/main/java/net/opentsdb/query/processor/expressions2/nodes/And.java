package net.opentsdb.query.processor.expressions2.nodes;

public class And extends LogicalBinaryOperator{
    public And(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super("&&", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterAnd(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leaveAnd(this);
    }
}
