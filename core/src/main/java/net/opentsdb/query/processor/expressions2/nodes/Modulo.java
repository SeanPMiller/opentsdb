package net.opentsdb.query.processor.expressions2.nodes;

public class Modulo extends ArithmeticBinaryOperator {
    public Modulo(final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super("%", leftOperand, rightOperand);
    }

    @Override
    public void accept(final ExpressionVisitor visitor) {
        visitor.enterModulo(this);
        lhs.accept(visitor);
        rhs.accept(visitor);
        visitor.leaveModulo(this);
    }
}
