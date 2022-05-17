package org.quiltmc.javacodegen.expression;

public interface Expression {
	default boolean isConstant(){
		return false;
	}

	default Object evaluateConstant(){
		throw new IllegalStateException("Not a constant");
	}

	void javaLike(StringBuilder builder);


	int LAMBDA_PRECEDENCE = 0;
	int ASSIGNMENT_PRECEDENCE = 1;
	int CONDITIONAL_PRECEDENCE = 2;
	int CONDITIONAL_OR_PRECEDENCE = 3;
	int CONDITIONAL_AND_PRECEDENCE = 4;
	int INCLUSIVE_OR_PRECEDENCE = 5;
	int EXCLUSIVE_OR_PRECEDENCE = 6;
	int AND_PRECEDENCE = 7;
	int EQUALITY_PRECEDENCE = 8;
	int RELATIONAL_PRECEDENCE = 9;
	int INSTANCEOF_PRECEDENCE = 10;
	int SHIFT_PRECEDENCE = 11;
	int ADDITIVE_PRECEDENCE = 12;
	int MULTIPLICATIVE_PRECEDENCE = 13;
	int UNARY_PRECEDENCE = 14;
	int PRIMARY_PRECEDENCE = 15; // also includes pre and post increment
}
