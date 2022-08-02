package net.opentsdb.query.processor.expressions2.nodes;

public class Lte extends OrderingBinaryOperator{
    public Lte(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super("<=", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterLte(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leaveLte(this);
    }
}
