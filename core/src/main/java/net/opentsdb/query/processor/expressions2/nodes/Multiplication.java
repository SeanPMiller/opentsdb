package net.opentsdb.query.processor.expressions2.nodes;

public class Multiplication extends ArithmeticBinaryOperator {
    public Multiplication(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super("*", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterMultiplication(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leaveMultiplication(this);
    }
}
