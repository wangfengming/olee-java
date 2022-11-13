package com.meituan.olee.parser;

import com.meituan.olee.grammar.TokenType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class States {
    Map<StateType, State> states;

    public States() {
        this.states = new HashMap<>();
        this.initTokenState();
        this.initSubTreeState();
        this.states.put(StateType.complete, new State().completable());
    }

    private void initTokenState() {
        State expectOperand = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.literal, new TokenTypeOps()
                    .toState(StateType.expectBinOp)
                    .handler(Parser::tokenLiteral));
                put(TokenType.identifier, new TokenTypeOps()
                    .toState(StateType.expectBinOp)
                    .handler(Parser::tokenIdentifier));
                put(TokenType.unaryOp, new TokenTypeOps()
                    .handler(Parser::tokenUnaryOp));
                put(TokenType.openParen, new TokenTypeOps()
                    .toState(StateType.subExp));
                put(TokenType.openCurly, new TokenTypeOps()
                    .toState(StateType.expectObjKey)
                    .handler(Parser::tokenObjStart));
                put(TokenType.openBracket, new TokenTypeOps()
                    .toState(StateType.arrayVal)
                    .handler(Parser::tokenArrayStart));
                put(TokenType.def, new TokenTypeOps()
                    .toState(StateType.def));
                put(TokenType.fn, new TokenTypeOps()
                    .toState(StateType.fn)
                    .handler(Parser::tokenFn));
                put(TokenType.spread, new TokenTypeOps()
                    .toState(StateType.spread));
            }});
        this.states.put(StateType.expectOperand, expectOperand);

        State expectBinOp = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.binaryOp, new TokenTypeOps()
                    .toState(StateType.expectOperand)
                    .handler(Parser::tokenBinaryOp));
                put(TokenType.openBracket, new TokenTypeOps()
                    .toState(StateType.computedMember)
                    .handler(Parser::tokenComputedMember));
                put(TokenType.optionalBracket, new TokenTypeOps()
                    .toState(StateType.computedMember)
                    .handler(Parser::tokenComputedMember));
                put(TokenType.dot, new TokenTypeOps()
                    .toState(StateType.member)
                    .handler(Parser::tokenMember));
                put(TokenType.optionalDot, new TokenTypeOps()
                    .toState(StateType.member)
                    .handler(Parser::tokenMember));
                put(TokenType.openParen, new TokenTypeOps()
                    .toState(StateType.argVal)
                    .handler(Parser::tokenFunctionCall));
                put(TokenType.optionalParen, new TokenTypeOps()
                    .toState(StateType.argVal)
                    .handler(Parser::tokenFunctionCall));
                put(TokenType.pipe, new TokenTypeOps()
                    .toState(StateType.expectTransform));
                put(TokenType.question, new TokenTypeOps()
                    .toState(StateType.ternaryMid)
                    .handler(Parser::tokenTernaryStart));
            }})
            .completable();
        this.states.put(StateType.expectBinOp, expectBinOp);

        State member = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.identifier, new TokenTypeOps()
                    .toState(StateType.expectBinOp)
                    .handler(Parser::tokenMemberProperty));
            }})
            .completable();
        this.states.put(StateType.member, member);

        State expectObjKey = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.identifier, new TokenTypeOps()
                    .toState(StateType.expectKeyValSep)
                    .handler(Parser::tokenObjKey));
                put(TokenType.literal, new TokenTypeOps()
                    .toState(StateType.expectKeyValSep)
                    .handler(Parser::tokenObjKey));
                put(TokenType.openBracket, new TokenTypeOps()
                    .toState(StateType.objKey));
                put(TokenType.closeCurly, new TokenTypeOps()
                    .toState(StateType.expectBinOp));
                put(TokenType.spread, new TokenTypeOps()
                    .toState(StateType.objSpreadVal));
            }});
        this.states.put(StateType.expectObjKey, expectObjKey);

        State expectKeyValSep = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.colon, new TokenTypeOps()
                    .toState(StateType.objVal));
            }});
        this.states.put(StateType.expectKeyValSep, expectKeyValSep);

        State def = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.identifier, new TokenTypeOps()
                    .toState(StateType.defAssign)
                    .handler(Parser::tokenDefName));
            }});
        this.states.put(StateType.def, def);

        State defAssign = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.assign, new TokenTypeOps()
                    .toState(StateType.defVal));
            }});
        this.states.put(StateType.defAssign, defAssign);

        State expectTransform = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.identifier, new TokenTypeOps()
                    .toState(StateType.postTransform)
                    .handler(Parser::tokenTransform));
                put(TokenType.openParen, new TokenTypeOps()
                    .toState(StateType.exprTransform)
                    .handler(Parser::tokenTransform));
            }});
        this.states.put(StateType.expectTransform, expectTransform);

        State postTransform = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>(expectBinOp.tokenTypes) {{
                put(TokenType.openParen, new TokenTypeOps()
                    .toState(StateType.argVal));
                remove(TokenType.optionalParen);
            }})
            .completable();
        this.states.put(StateType.postTransform, postTransform);

        State fn = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.openParen, new TokenTypeOps()
                    .toState(StateType.fnArg));
            }});
        this.states.put(StateType.fn, fn);

        State fnArg = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.identifier, new TokenTypeOps()
                    .toState(StateType.fnPostArg)
                    .handler(Parser::tokenFnArg));
                put(TokenType.closeParen, new TokenTypeOps()
                    .toState(StateType.fnArrow));
            }});
        this.states.put(StateType.fnArg, fnArg);

        State fnPostArg = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.comma, new TokenTypeOps()
                    .toState(StateType.fnArg));
                put(TokenType.closeParen, new TokenTypeOps()
                    .toState(StateType.fnArrow));
            }});
        this.states.put(StateType.fnPostArg, fnPostArg);

        State fnArrow = new State()
            .tokenTypes(new HashMap<TokenType, TokenTypeOps>() {{
                put(TokenType.arrow, new TokenTypeOps()
                    .toState(StateType.fnExpr));
            }});
        this.states.put(StateType.fnArrow, fnArrow);
    }

    private void initSubTreeState() {
        State computedMember = new State()
            .subHandler(Parser::astComputedMemberProperty)
            .endStates(new HashMap<TokenType, StateType>() {{
                put(TokenType.closeBracket, StateType.expectBinOp);
            }})
            .required();
        this.states.put(StateType.computedMember, computedMember);

        State subExp = new State()
            .subHandler(Parser::astSubExp)
            .endStates(new HashMap<TokenType, StateType>() {{
                put(TokenType.closeParen, StateType.expectBinOp);
            }})
            .required();
        this.states.put(StateType.subExp, subExp);

        State objKey = new State()
            .subHandler(Parser::astObjKey)
            .endStates(new HashMap<TokenType, StateType>() {{
                put(TokenType.closeBracket, StateType.expectKeyValSep);
            }})
            .required();
        this.states.put(StateType.objKey, objKey);

        State objVal = new State()
            .subHandler(Parser::astObjVal)
            .endStates(new HashMap<TokenType, StateType>() {{
                put(TokenType.comma, StateType.expectObjKey);
                put(TokenType.closeCurly, StateType.expectBinOp);
            }})
            .required();
        this.states.put(StateType.objVal, objVal);

        State objSpreadVal = new State()
            .subHandler(Parser::astObjSpreadVal)
            .endStates(new HashMap<TokenType, StateType>() {{
                put(TokenType.comma, StateType.expectObjKey);
                put(TokenType.closeCurly, StateType.expectBinOp);
            }})
            .required();
        this.states.put(StateType.objSpreadVal, objSpreadVal);

        State spread = new State()
            .subHandler(Parser::astSpread)
            .completable()
            .required();
        this.states.put(StateType.spread, spread);

        State arrayVal = new State()
            .subHandler(Parser::astArrayVal)
            .endStates(new HashMap<TokenType, StateType>() {{
                put(TokenType.comma, StateType.arrayVal);
                put(TokenType.closeBracket, StateType.expectBinOp);
            }});
        this.states.put(StateType.arrayVal, arrayVal);

        State defVal = new State()
            .subHandler(Parser::astDefVal)
            .endStates(new HashMap<TokenType, StateType>() {{
                put(TokenType.semi, StateType.expectOperand);
            }})
            .required();
        this.states.put(StateType.defVal, defVal);

        State exprTransform = new State()
            .subHandler(Parser::astExprTransform)
            .endStates(new HashMap<TokenType, StateType>() {{
                put(TokenType.closeParen, StateType.postTransform);
            }})
            .required();
        this.states.put(StateType.exprTransform, exprTransform);

        State argVal = new State()
            .subHandler(Parser::astArgVal)
            .endStates(new HashMap<TokenType, StateType>() {{
                put(TokenType.comma, StateType.argVal);
                put(TokenType.closeParen, StateType.expectBinOp);
            }});
        this.states.put(StateType.argVal, argVal);

        State ternaryMid = new State()
            .subHandler(Parser::astTernaryMid)
            .endStates(new HashMap<TokenType, StateType>() {{
                put(TokenType.colon, StateType.ternaryEnd);
            }})
            .required();
        this.states.put(StateType.ternaryMid, ternaryMid);

        State ternaryEnd = new State()
            .subHandler(Parser::astTernaryEnd)
            .completable()
            .required();
        this.states.put(StateType.ternaryEnd, ternaryEnd);

        State fnExpr = new State()
            .subHandler(Parser::astFnExpr)
            .completable()
            .required();
        this.states.put(StateType.fnExpr, fnExpr);
    }

    State getState(StateType state) {
        return this.states.get(state);
    }

    static class State {
        Map<TokenType, TokenTypeOps> tokenTypes;
        Consumer<Parser> subHandler;
        Map<TokenType, StateType> endStates;
        boolean completable;
        boolean required;

        State tokenTypes(Map<TokenType, TokenTypeOps> tokenTypes) {
            this.tokenTypes = tokenTypes;
            return this;
        }

        State subHandler(Consumer<Parser> subHandler) {
            this.subHandler = subHandler;
            return this;
        }

        State endStates(Map<TokenType, StateType> endStates) {
            this.endStates = endStates;
            return this;
        }

        State completable() {
            this.completable = true;
            return this;
        }

        State required() {
            this.required = true;
            return this;
        }
    }

    static class TokenTypeOps {
        StateType toState;
        Consumer<Parser> handler;

        TokenTypeOps toState(StateType toState) {
            this.toState = toState;
            return this;
        }

        TokenTypeOps handler(Consumer<Parser> handler) {
            this.handler = handler;
            return this;
        }
    }
}
