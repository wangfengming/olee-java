package com.meituan.olee.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.meituan.olee.grammar.TokenType;

public class States {
    Map<StateType, State> states;

    public States() {
        this.states = new HashMap<>();
        this.initTokenState();
        this.initSubTreeState();
        this.states.put(StateType.complete, new State(true));
    }

    private void initTokenState() {
        State expectOperand = new State(new HashMap<TokenType, TokenTypeOps>() {{
            put(TokenType.literal, new TokenTypeOps(StateType.expectBinOp, Parser::tokenLiteral));
            put(TokenType.identifier, new TokenTypeOps(StateType.expectBinOp, Parser::tokenIdentifier));
            put(TokenType.unaryOp, new TokenTypeOps(Parser::tokenUnaryOp));
            put(TokenType.openParen, new TokenTypeOps(StateType.subExp));
            put(TokenType.openCurly, new TokenTypeOps(StateType.expectObjKey, Parser::tokenObjStart));
            put(TokenType.openBracket, new TokenTypeOps(StateType.arrayVal, Parser::tokenArrayStart));
            put(TokenType.def, new TokenTypeOps(StateType.def));
        }});
        this.states.put(StateType.expectOperand, expectOperand);

        State expectBinOp = new State(new HashMap<TokenType, TokenTypeOps>() {{
            put(TokenType.binaryOp, new TokenTypeOps(StateType.expectOperand, Parser::tokenBinaryOp));
            put(TokenType.openBracket, new TokenTypeOps(StateType.computedMember, Parser::tokenComputedMember));
            put(TokenType.optionalBracket, new TokenTypeOps(StateType.computedMember, Parser::tokenComputedMember));
            put(TokenType.dot, new TokenTypeOps(StateType.member, Parser::tokenMember));
            put(TokenType.optionalDot, new TokenTypeOps(StateType.member, Parser::tokenMember));
            put(TokenType.openParen, new TokenTypeOps(StateType.argVal, Parser::tokenFunctionCall));
            put(TokenType.optionalParen, new TokenTypeOps(StateType.argVal, Parser::tokenFunctionCall));
            put(TokenType.pipe, new TokenTypeOps(StateType.expectTransform));
            put(TokenType.question, new TokenTypeOps(StateType.ternaryMid, Parser::tokenTernaryStart));
        }}, true);
        this.states.put(StateType.expectBinOp, expectBinOp);

        State member = new State(new HashMap<TokenType, TokenTypeOps>() {{
            put(TokenType.identifier, new TokenTypeOps(StateType.expectBinOp, Parser::tokenMemberProperty));
        }}, true);
        this.states.put(StateType.member, member);

        State expectObjKey = new State(new HashMap<TokenType, TokenTypeOps>() {{
            put(TokenType.identifier, new TokenTypeOps(StateType.expectKeyValSep, Parser::tokenObjKey));
            put(TokenType.literal, new TokenTypeOps(StateType.expectKeyValSep, Parser::tokenObjKey));
            put(TokenType.openBracket, new TokenTypeOps(StateType.objKey));
            put(TokenType.closeCurly, new TokenTypeOps(StateType.expectBinOp));
        }});
        this.states.put(StateType.expectObjKey, expectObjKey);

        State expectKeyValSep = new State(new HashMap<TokenType, TokenTypeOps>() {{
            put(TokenType.colon, new TokenTypeOps(StateType.objVal));
        }});
        this.states.put(StateType.expectKeyValSep, expectKeyValSep);

        State def = new State(new HashMap<TokenType, TokenTypeOps>() {{
            put(TokenType.identifier, new TokenTypeOps(StateType.defAssign, Parser::tokenDefName));
        }});
        this.states.put(StateType.def, def);

        State defAssign = new State(new HashMap<TokenType, TokenTypeOps>() {{
            put(TokenType.assign, new TokenTypeOps(StateType.defVal));
        }});
        this.states.put(StateType.defAssign, defAssign);

        State expectTransform = new State(new HashMap<TokenType, TokenTypeOps>() {{
            put(TokenType.identifier, new TokenTypeOps(StateType.postTransform, Parser::tokenTransform));
            put(TokenType.openParen, new TokenTypeOps(StateType.exprTransform, Parser::tokenTransform));
        }});
        this.states.put(StateType.expectTransform, expectTransform);

        State postTransform = new State(new HashMap<TokenType, TokenTypeOps>(expectBinOp.tokenTypes) {{
            put(TokenType.openParen, new TokenTypeOps(StateType.argVal));
            remove(TokenType.optionalParen);
        }}, true);
        this.states.put(StateType.postTransform, postTransform);
    }

    private void initSubTreeState() {
        State computedMember = new State(Parser::astComputedMemberProperty, new HashMap<TokenType, StateType>() {{
            put(TokenType.closeBracket, StateType.expectBinOp);
        }});
        this.states.put(StateType.computedMember, computedMember);

        State subExp = new State(Parser::astSubExp, new HashMap<TokenType, StateType>() {{
            put(TokenType.closeParen, StateType.expectBinOp);
        }});
        this.states.put(StateType.subExp, subExp);

        State objKey = new State(Parser::astObjKey, new HashMap<TokenType, StateType>() {{
            put(TokenType.closeBracket, StateType.expectKeyValSep);
        }});
        this.states.put(StateType.objKey, objKey);

        State objVal = new State(Parser::astObjVal, new HashMap<TokenType, StateType>() {{
            put(TokenType.comma, StateType.expectObjKey);
            put(TokenType.closeCurly, StateType.expectBinOp);
        }});
        this.states.put(StateType.objVal, objVal);

        State arrayVal = new State(Parser::astArrayVal, new HashMap<TokenType, StateType>() {{
            put(TokenType.comma, StateType.arrayVal);
            put(TokenType.closeBracket, StateType.expectBinOp);
        }});
        this.states.put(StateType.arrayVal, arrayVal);

        State defVal = new State(Parser::astDefVal, new HashMap<TokenType, StateType>() {{
            put(TokenType.semi, StateType.expectOperand);
        }});
        this.states.put(StateType.defVal, defVal);

        State exprTransform = new State(Parser::astExprTransform, new HashMap<TokenType, StateType>() {{
            put(TokenType.closeParen, StateType.postTransform);
        }});
        this.states.put(StateType.exprTransform, exprTransform);

        State argVal = new State(Parser::astArgVal, new HashMap<TokenType, StateType>() {{
            put(TokenType.comma, StateType.argVal);
            put(TokenType.closeParen, StateType.expectBinOp);
        }});
        this.states.put(StateType.argVal, argVal);

        State ternaryMid = new State(Parser::astTernaryMid, new HashMap<TokenType, StateType>() {{
            put(TokenType.colon, StateType.ternaryEnd);
        }});
        this.states.put(StateType.ternaryMid, ternaryMid);

        State astTernaryEnd = new State(Parser::astTernaryEnd, true);
        this.states.put(StateType.ternaryEnd, astTernaryEnd);
    }

    State getState(StateType state) {
        return this.states.get(state);
    }

    static class State {
        Map<TokenType, TokenTypeOps> tokenTypes;
        boolean completable;
        Consumer<Parser> subHandler;
        Map<TokenType, StateType> endStates;

        State(boolean completable) {
            this.completable = completable;
        }

        State(Map<TokenType, TokenTypeOps> tokenTypes) {
            this.tokenTypes = tokenTypes;
        }

        State(Map<TokenType, TokenTypeOps> tokenTypes, boolean completable) {
            this.tokenTypes = tokenTypes;
            this.completable = completable;
        }

        State(Consumer<Parser> subHandler, boolean completable) {
            this.subHandler = subHandler;
            this.completable = completable;
        }

        State(Consumer<Parser> subHandler, Map<TokenType, StateType> endStates) {
            this.subHandler = subHandler;
            this.endStates = endStates;
        }

    }

    static class TokenTypeOps {
        StateType toState;
        Consumer<Parser> handler;

        TokenTypeOps(StateType toState) {
            this.toState = toState;
        }

        TokenTypeOps(Consumer<Parser> handler) {
            this.handler = handler;
        }

        TokenTypeOps(StateType toState, Consumer<Parser> handler) {
            this.toState = toState;
            this.handler = handler;
        }
    }
}
