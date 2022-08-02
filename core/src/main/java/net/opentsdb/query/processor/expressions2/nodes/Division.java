package net.opentsdb.query.processor.expressions2.nodes;

public class Division extends ArithmeticBinaryOperator {
    public Division(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super("/", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterDivision(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leaveDivision(this);
    }
}
