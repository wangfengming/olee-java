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
        Map<TokenType, TokenTypeOps> expectOperand = new HashMap<>();
        expectOperand.put(TokenType.literal, new TokenTypeOps(StateType.expectBinOp, Parser::tokenLiteral));
        expectOperand.put(TokenType.identifier, new TokenTypeOps(StateType.expectBinOp, Parser::tokenIdentifier));
        expectOperand.put(TokenType.unaryOp, new TokenTypeOps(Parser::tokenUnaryOp));
        expectOperand.put(TokenType.openParen, new TokenTypeOps(StateType.subExp));
        expectOperand.put(TokenType.openCurly, new TokenTypeOps(StateType.expectObjKey, Parser::tokenObjStart));
        expectOperand.put(TokenType.openBracket, new TokenTypeOps(StateType.arrayVal, Parser::tokenArrayStart));
        expectOperand.put(TokenType.def, new TokenTypeOps(StateType.def));
        this.states.put(StateType.expectOperand, new State(expectOperand));

        Map<TokenType, TokenTypeOps> expectBinOp = new HashMap<>();
        expectBinOp.put(TokenType.binaryOp, new TokenTypeOps(StateType.expectOperand, Parser::tokenBinaryOp));
        expectBinOp.put(TokenType.openBracket, new TokenTypeOps(StateType.computedMember, Parser::tokenComputedMember));
        expectBinOp.put(TokenType.optionalBracket, new TokenTypeOps(StateType.computedMember, Parser::tokenComputedMember));
        expectBinOp.put(TokenType.dot, new TokenTypeOps(StateType.member, Parser::tokenMember));
        expectBinOp.put(TokenType.optionalDot, new TokenTypeOps(StateType.member, Parser::tokenMember));
        expectBinOp.put(TokenType.openParen, new TokenTypeOps(StateType.argVal, Parser::tokenFunctionCall));
        expectBinOp.put(TokenType.optionalParen, new TokenTypeOps(StateType.argVal, Parser::tokenFunctionCall));
        expectBinOp.put(TokenType.pipe, new TokenTypeOps(StateType.expectTransform));
        expectBinOp.put(TokenType.question, new TokenTypeOps(StateType.ternaryMid, Parser::tokenTernaryStart));
        this.states.put(StateType.expectBinOp, new State(expectBinOp, true));

        Map<TokenType, TokenTypeOps> member = new HashMap<>();
        member.put(TokenType.identifier, new TokenTypeOps(StateType.expectBinOp, Parser::tokenMemberProperty));
        this.states.put(StateType.member, new State(member, true));

        Map<TokenType, TokenTypeOps> expectObjKey = new HashMap<>();
        expectObjKey.put(TokenType.identifier, new TokenTypeOps(StateType.expectKeyValSep, Parser::tokenObjKey));
        expectObjKey.put(TokenType.literal, new TokenTypeOps(StateType.expectKeyValSep, Parser::tokenObjKey));
        expectObjKey.put(TokenType.openBracket, new TokenTypeOps(StateType.objKey));
        expectObjKey.put(TokenType.closeCurly, new TokenTypeOps(StateType.expectBinOp));
        this.states.put(StateType.expectObjKey, new State(expectObjKey));

        Map<TokenType, TokenTypeOps> expectKeyValSep = new HashMap<>();
        expectKeyValSep.put(TokenType.colon, new TokenTypeOps(StateType.objVal));
        this.states.put(StateType.expectKeyValSep, new State(expectKeyValSep));

        Map<TokenType, TokenTypeOps> def = new HashMap<>();
        def.put(TokenType.identifier, new TokenTypeOps(StateType.defAssign, Parser::tokenDefName));
        this.states.put(StateType.def, new State(def));

        Map<TokenType, TokenTypeOps> defAssign = new HashMap<>();
        defAssign.put(TokenType.assign, new TokenTypeOps(StateType.defVal));
        this.states.put(StateType.defAssign, new State(defAssign));

        Map<TokenType, TokenTypeOps> expectTransform = new HashMap<>();
        expectTransform.put(TokenType.identifier, new TokenTypeOps(StateType.postTransform, Parser::tokenTransform));
        expectTransform.put(TokenType.openParen, new TokenTypeOps(StateType.exprTransform, Parser::tokenTransform));
        this.states.put(StateType.expectTransform, new State(expectTransform));

        Map<TokenType, TokenTypeOps> postTransform = new HashMap<>(expectBinOp);
        postTransform.put(TokenType.openParen, new TokenTypeOps(StateType.argVal));
        postTransform.remove(TokenType.optionalParen);
        this.states.put(StateType.postTransform, new State(postTransform, true));
    }

    private void initSubTreeState() {
        Map<TokenType, StateType> computedMember = new HashMap<>();
        computedMember.put(TokenType.closeBracket, StateType.expectBinOp);
        this.states.put(StateType.computedMember, new State(Parser::astComputedMemberProperty, computedMember));

        Map<TokenType, StateType> subExp = new HashMap<>();
        subExp.put(TokenType.closeParen, StateType.expectBinOp);
        this.states.put(StateType.subExp, new State(Parser::astSubExp, subExp));

        Map<TokenType, StateType> objKey = new HashMap<>();
        objKey.put(TokenType.closeBracket, StateType.expectKeyValSep);
        this.states.put(StateType.objKey, new State(Parser::astObjKey, objKey));

        Map<TokenType, StateType> objVal = new HashMap<>();
        objVal.put(TokenType.comma, StateType.expectObjKey);
        objVal.put(TokenType.closeCurly, StateType.expectBinOp);
        this.states.put(StateType.objVal, new State(Parser::astObjVal, objVal));

        Map<TokenType, StateType> arrayVal = new HashMap<>();
        arrayVal.put(TokenType.comma, StateType.arrayVal);
        arrayVal.put(TokenType.closeBracket, StateType.expectBinOp);
        this.states.put(StateType.arrayVal, new State(Parser::astArrayVal, arrayVal));

        Map<TokenType, StateType> defVal = new HashMap<>();
        defVal.put(TokenType.semi, StateType.expectOperand);
        this.states.put(StateType.defVal, new State(Parser::astDefVal, defVal));

        Map<TokenType, StateType> exprTransform = new HashMap<>();
        exprTransform.put(TokenType.closeParen, StateType.postTransform);
        this.states.put(StateType.exprTransform, new State(Parser::astExprTransform, exprTransform));

        Map<TokenType, StateType> argVal = new HashMap<>();
        argVal.put(TokenType.comma, StateType.argVal);
        argVal.put(TokenType.closeParen, StateType.expectBinOp);
        this.states.put(StateType.argVal, new State(Parser::astArgVal, argVal));

        Map<TokenType, StateType> ternaryMid = new HashMap<>();
        ternaryMid.put(TokenType.colon, StateType.ternaryEnd);
        this.states.put(StateType.ternaryMid, new State(Parser::astTernaryMid, ternaryMid));

        this.states.put(StateType.ternaryEnd, new State(Parser::astTernaryEnd, true));
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
