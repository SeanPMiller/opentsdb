package net.opentsdb.query.processor.expressions2.nodes;

public class Power extends ArithmeticBinaryOperator {
    public Power(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super("^", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterPower(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leavePower(this);
    }
}
