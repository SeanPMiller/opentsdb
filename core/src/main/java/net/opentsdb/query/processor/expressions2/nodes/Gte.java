package net.opentsdb.query.processor.expressions2.nodes;

public class Gte extends OrderingBinaryOperator{
    public Gte(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super(">=", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterGte(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leaveGte(this);
    }
}
