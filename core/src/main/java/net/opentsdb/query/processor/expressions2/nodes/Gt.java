package net.opentsdb.query.processor.expressions2.nodes;

public class Gt extends OrderingBinaryOperator{
    public Gt(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super(">", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterGt(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leaveGt(this);
    }
}
