package net.opentsdb.query.processor.expressions2.nodes;

public class Equal extends EquivalenceBinaryOperator{
    public Equal(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super("==", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterEqual(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leaveEqual(this);
    }
}
