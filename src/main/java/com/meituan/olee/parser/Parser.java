package com.meituan.olee.parser;

import com.meituan.olee.ast.*;
import com.meituan.olee.exceptions.ParseException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.grammar.Grammar;
import com.meituan.olee.grammar.TokenType;
import com.meituan.olee.tokenizer.Token;
import com.meituan.olee.util.AstUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Parser {
    Grammar grammar;
    String exprStr;
    Map<TokenType, StateType> stopMap;

    StateType stateType;
    States.State state;
    States states;
    Token token;
    AstNode tree;
    AstNode cursor;
    Parser subParser;
    AstNode subTree;
    boolean parentStop;
    boolean isMaybeLambda;
    LinkedList<DefNode.Def> defs = new LinkedList<>();

    public Parser(Grammar grammar, States states) {
        this(grammar, states, "", new HashMap<>());
    }

    public Parser(Grammar grammar, States states, String exprStr, Map<TokenType, StateType> stopMap) {
        this.grammar = grammar;
        this.states = states;
        this.exprStr = exprStr;
        this.stopMap = stopMap;
        this.switchState(StateType.expectOperand);
    }

    public StateType addToken(Token token) throws ParseException {
        if (this.stateType == StateType.complete) {
            throw new ParseException("Cannot add a new token to a completed Parser");
        }
        String startExpr = this.exprStr;
        this.token = token;
        this.exprStr += token.raw;
        if (this.state.subHandler != null) {
            if (this.subParser == null) {
                this.startSubExp(startExpr);
            }
            StateType stopState = this.subParser.addToken(token);
            if (stopState != null) {
                this.endSubExp();
                if (this.parentStop) {
                    return stopState;
                }
                this.switchState(stopState);
            }
        } else if (this.state.tokenTypes != null
            && this.state.tokenTypes.get(token.type) != null) {
            States.TokenTypeOps tokenTypeOps = this.state.tokenTypes.get(token.type);
            if (tokenTypeOps.handler != null) {
                tokenTypeOps.handler.accept(this);
            }
            if (tokenTypeOps.toState != null) {
                this.switchState(tokenTypeOps.toState);
            }
        } else if (this.stopMap.get(token.type) != null) {
            return this.stopMap.get(token.type);
        } else {
            this.asserts(false);
        }
        return null;
    }

    public void addTokens(List<Token> tokens) throws ParseException {
        tokens.forEach(this::addToken);
    }

    public AstNode complete() throws ParseException {
        if (this.cursor != null && !this.state.completable) {
            throw new ParseException("Unexpected end of expression: " + this.exprStr);
        }
        if (this.subParser != null) {
            this.endSubExp();
        }
        this.switchState(StateType.complete);
        if (this.isMaybeLambda) {
            this.tree.isMaybeLambda = true;
        }
        return this.tree != null && !this.defs.isEmpty()
            ? new DefNode(this.defs, this.tree)
            : this.tree;
    }

    private void switchState(StateType stateType) {
        this.stateType = stateType;
        this.state = this.states.getState(this.stateType);
    }

    private void startSubExp(String exprStr) {
        Map<TokenType, StateType> endStates = this.state.endStates;
        if (endStates == null) {
            this.parentStop = true;
            endStates = this.stopMap;
        }
        this.subParser = new Parser(this.grammar, this.states, exprStr, endStates);
    }

    private void endSubExp() {
        AstNode ast = this.subParser.complete();
        if (ast != null && ast.isMaybeLambda) {
            this.isMaybeLambda = true;
        }
        this.subTree = ast;
        this.state.subHandler.accept(this);
        this.subParser = null;
    }

    private void placeAtCursor(AstNode node) {
        if (this.cursor == null) {
            this.tree = node;
        } else {
            if (this.cursor instanceof BinaryNode) {
                ((BinaryNode) this.cursor).right = node;
            } else if (this.cursor instanceof UnaryNode) {
                ((UnaryNode) this.cursor).right = node;
            } else if (this.cursor instanceof MemberNode) {
                ((MemberNode) this.cursor).right = node;
            }
            node.parent = this.cursor;
        }
        this.cursor = node;
    }

    private void placeBeforeCursor(AstNode node) {
        this.cursor = this.cursor.parent;
        this.placeAtCursor(node);
    }

    private void rotatePriority(int priority) {
        this.rotatePriority(priority, false);
    }

    private void rotatePriority(int priority, boolean rtl) {
        AstNode parent = this.cursor != null
            ? this.cursor.parent
            : null;
        while (parent != null) {
            int parentPriority = AstUtils.getPriority(parent, this.grammar);
            if (rtl ? (parentPriority <= priority) : (parentPriority < priority)) {
                break;
            }
            this.cursor = parent;
            parent = parent.parent;
        }
    }

    private AstNode maybeLambda(AstNode node) {
        return node.isMaybeLambda
            ? new FunctionNode(node)
            : node;
    }

    private void leftOptional(AstNode node) {
        if (this.cursor.optional || this.cursor.leftOptional) {
            node.leftOptional = true;
        }
    }

    private void asserts(Boolean condition) throws ParseException {
        if (!condition) {
            throw new ParseException("Token " + token.raw + " unexpected in expression: " + this.exprStr);
        }
    }

    public void tokenLiteral() {
        this.placeAtCursor(new LiteralNode(this.token.literal));
    }

    public void tokenIdentifier() {
        IdentifierNode node = new IdentifierNode(this.token.value);
        if (this.token.isArg) {
            node.isArg = true;
            node.argIndex = this.token.argIndex;
            this.isMaybeLambda = true;
        }
        this.placeAtCursor(node);
    }

    public void tokenUnaryOp() {
        this.placeAtCursor(new UnaryNode(this.token.value, null));
    }

    public void tokenBinaryOp() {
        BinaryOpGrammar binaryOp = this.grammar.binaryOps.get(this.token.value);
        this.rotatePriority(binaryOp.priority, binaryOp.rtl);
        this.placeBeforeCursor(new BinaryNode(token.value, this.cursor, null));
    }

    public void tokenMember() {
        this.rotatePriority(this.grammar.MEMBER_PRIORITY);
        MemberNode node = new MemberNode(this.cursor, null);
        node.optional = token.type == TokenType.optionalDot;
        this.leftOptional(node);
        this.placeBeforeCursor(node);
    }

    public void tokenMemberProperty() {
        this.placeAtCursor(new LiteralNode(token.value));
    }

    public void tokenComputedMember() {
        this.rotatePriority(this.grammar.MEMBER_PRIORITY);
        MemberNode node = new MemberNode(this.cursor, null).computed(true);
        node.optional = token.type == TokenType.optionalBracket;
        this.leftOptional(node);
        this.placeBeforeCursor(node);
    }

    public void astComputedMemberProperty() {
        this.asserts(this.subTree != null);
        ((MemberNode) this.cursor).right = this.subTree;
    }

    public void tokenDefName() {
        this.defs.add(new DefNode.Def(this.token.value, null));
    }

    public void astDefVal() {
        this.asserts(this.subTree != null);
        this.defs.getLast().value = this.maybeLambda(this.subTree);
    }

    public void tokenArrayStart() {
        this.placeAtCursor(new ArrayNode(new LinkedList<>()));
    }

    public void astArrayVal() {
        if (this.subTree != null) {
            ((ArrayNode) this.cursor).value.add(this.subTree);
        }
    }

    public void tokenObjStart() {
        this.placeAtCursor(new ObjectNode(new LinkedList<>()));
    }

    public void tokenObjKey() {
        String key = this.token.type == TokenType.literal
            ? this.token.literal == null ? "null" : this.token.literal.toString()
            : this.token.value;
        AstNode node = new LiteralNode(key);
        ObjectNode.Entry entry = new ObjectNode.Entry(node, null);
        ((ObjectNode) this.cursor).entries.add(entry);
    }

    public void astObjKey() {
        this.asserts(this.subTree != null);
        ObjectNode.Entry entry = new ObjectNode.Entry(this.subTree, null);
        ((ObjectNode) this.cursor).entries.add(entry);
    }

    public void astObjVal() {
        this.asserts(this.subTree != null);
        LinkedList<ObjectNode.Entry> entries = ((ObjectNode) this.cursor).entries;
        entries.getLast().value = this.subTree;
    }

    public void tokenTernaryStart() {
        this.tree = new ConditionNode(this.tree, null, null);
        this.cursor = this.tree;
    }

    public void astTernaryMid() {
        this.asserts(this.subTree != null);
        ((ConditionNode) this.cursor).consequent = this.subTree;
    }

    public void astTernaryEnd() {
        this.asserts(this.subTree != null);
        ((ConditionNode) this.cursor).alternate = this.subTree;
    }

    public void tokenTransform() {
        this.rotatePriority(this.grammar.PIPE_PRIORITY);
        AstNode func = token.type == TokenType.identifier
            ? new IdentifierNode(this.token.value)
            : null;
        FunctionCallNode node = new FunctionCallNode(func, new LinkedList<>()).isTransform(true);
        node.args.add(this.cursor);
        this.placeBeforeCursor(node);
    }

    public void astExprTransform() {
        this.asserts(this.subTree != null);
        this.isMaybeLambda = false;
        ((FunctionCallNode) this.cursor).func = this.maybeLambda(this.subTree);
    }

    public void tokenFunctionCall() {
        this.rotatePriority(this.grammar.FUNCTION_CALL_PRIORITY);
        AstNode node = new FunctionCallNode(this.cursor, new LinkedList<>());
        node.optional = this.token.type == TokenType.optionalParen;
        this.leftOptional(node);
        this.placeBeforeCursor(node);
    }

    public void astArgVal() {
        this.isMaybeLambda = false;
        if (this.subTree != null) {
            ((FunctionCallNode) this.cursor).args.add(this.maybeLambda(this.subTree));
        }
    }

    public void tokenFn() {
        this.placeAtCursor(new FunctionNode(new LinkedList<>()));
    }

    public void tokenFnArg() {
        ((FunctionNode) this.cursor).argNames.add(this.token.value);
    }

    public void astFnExpr() {
        this.asserts(this.subTree != null);
        ((FunctionNode) this.cursor).expr = this.subTree;
        this.isMaybeLambda = false;
    }

    public void astSubExp() {
        this.asserts(this.subTree != null);
        this.placeAtCursor(this.subTree);
    }

}
