package net.opentsdb.query.processor.expressions2.nodes;

public class NotEq extends EquivalenceBinaryOperator{
    public NotEq(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super("!=", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterNotEq(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leaveNotEq(this);
    }
}
