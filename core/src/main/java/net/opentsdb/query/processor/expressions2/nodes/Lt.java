package net.opentsdb.query.processor.expressions2.nodes;

public class Lt extends OrderingBinaryOperator{
    public Lt(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super("<", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterLt(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leaveLt(this);
    }
}