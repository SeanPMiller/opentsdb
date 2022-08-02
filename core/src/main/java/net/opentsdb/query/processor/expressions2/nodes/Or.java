package net.opentsdb.query.processor.expressions2.nodes;

public class Or extends LogicalBinaryOperator{
    public Or(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super("||", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterOr(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leaveOr(this);
    }
}
