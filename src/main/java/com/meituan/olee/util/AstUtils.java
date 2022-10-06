package com.meituan.olee.util;

import com.meituan.olee.ast.AstNode;
import com.meituan.olee.ast.BinaryNode;
import com.meituan.olee.ast.MemberNode;
import com.meituan.olee.ast.UnaryNode;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.grammar.Grammar;
import com.meituan.olee.grammar.UnaryOpGrammar;

public class AstUtils {
    public static int getPriority(AstNode node, Grammar grammar) {
        if (node instanceof BinaryNode) {
            String operator = ((BinaryNode) node).operator;
            BinaryOpGrammar binaryOp = grammar.binaryOps.get(operator);
            return binaryOp.priority;
        } else if (node instanceof UnaryNode) {
            String operator = ((UnaryNode) node).operator;
            UnaryOpGrammar unaryOp = grammar.unaryOps.get(operator);
            return unaryOp.priority;
        } else if (node instanceof MemberNode) {
            return grammar.MEMBER_PRIORITY;
        }
        return Integer.MAX_VALUE;
    }

    public static boolean rtl(AstNode node, Grammar grammar) {
        if (node instanceof BinaryNode) {
            String operator = ((BinaryNode) node).operator;
            BinaryOpGrammar binaryOp = grammar.binaryOps.get(operator);
            return binaryOp.rtl;
        }
        return false;
    }
}
