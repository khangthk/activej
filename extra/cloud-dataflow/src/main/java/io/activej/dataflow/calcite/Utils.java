package io.activej.dataflow.calcite;

import io.activej.codegen.DefiningClassLoader;
import io.activej.common.exception.ToDoException;
import io.activej.dataflow.calcite.function.ProjectionFunction;
import io.activej.dataflow.calcite.where.Operand;
import io.activej.dataflow.calcite.where.OperandFieldAccess;
import io.activej.dataflow.calcite.where.OperandRecordField;
import io.activej.dataflow.calcite.where.OperandScalar;
import org.apache.calcite.rex.*;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class Utils {
	public static <T> T toJavaType(RexLiteral literal) {
		SqlTypeName typeName = literal.getTypeName();

		return (T) switch (requireNonNull(typeName.getFamily())) {
			case CHARACTER -> literal.getValueAs(String.class);
			case NUMERIC, INTEGER -> literal.getValueAs(Integer.class);
			default -> throw new ToDoException();
		};
	}

	public static Operand toOperand(RexNode conditionNode, DefiningClassLoader classLoader) {
		if (conditionNode instanceof RexDynamicParam dynamicParam) {
			return new OperandScalar(Value.unmaterializedValue(dynamicParam));
		} else if (conditionNode instanceof RexCall call) {
			switch (call.getKind()) {
				case CAST -> {
					return toOperand(call.getOperands().get(0), classLoader);
				}
				case OTHER_FUNCTION -> {
					SqlOperator operator = call.getOperator();
					if (operator instanceof ProjectionFunction projectionFunction) {
						List<Operand> operands = call.getOperands()
								.stream()
								.map(operand -> toOperand(operand, classLoader))
								.collect(Collectors.toList());

						return projectionFunction.toOperand(operands);
					}
				}
			}
		} else if (conditionNode instanceof RexLiteral literal) {
			Value value = Value.materializedValue(toJavaType(literal));
			return new OperandScalar(value);
		} else if (conditionNode instanceof RexInputRef inputRef) {
			Value value = Value.materializedValue(inputRef.getIndex());
			Operand indexOperand = new OperandScalar(value);
			return new OperandRecordField(indexOperand);
		} else if (conditionNode instanceof RexFieldAccess fieldAccess) {
			Operand objectOperand = toOperand(fieldAccess.getReferenceExpr(), classLoader);
			Value value = Value.materializedValue(fieldAccess.getField().getName());
			Operand fieldNameOperand = new OperandScalar(value);
			return new OperandFieldAccess(objectOperand, fieldNameOperand, classLoader);
		}
		throw new IllegalArgumentException("Unknown node: " + conditionNode);
	}

}
