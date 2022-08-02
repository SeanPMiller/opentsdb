package net.opentsdb.query.processor.expressions2.nodes;

import net.opentsdb.query.processor.expressions2.types.FunctionType;
import net.opentsdb.query.processor.expressions2.types.TupleType;
import net.opentsdb.query.processor.expressions2.types.TypeLiteral;

public abstract class LogicalBinaryOperator extends BinaryOperator{
    public LogicalBinaryOperator(final String symbol, final ExpressionNode leftOperand, final ExpressionNode rightOperand) {
        super(symbol, leftOperand, rightOperand);
    }

    private static FunctionType[] SIGNATURES = {
            new FunctionType(new TupleType(TypeLiteral.BOOLEAN, TypeLiteral.BOOLEAN), TypeLiteral.BOOLEAN),
    };

    @Override
    public FunctionType[] getTypeSignatures() {
        return SIGNATURES;
    }
}
