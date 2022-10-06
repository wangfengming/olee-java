package com.meituan.olee.parser;

public enum StateType {
    expectOperand,
    expectBinOp,
    expectObjKey,
    expectKeyValSep,
    computedMember,
    member,
    def,
    defAssign,
    defVal,
    expectTransform,
    postTransform,
    exprTransform,
    subExp,
    argVal,
    objKey,
    objVal,
    arrayVal,
    ternaryMid,
    ternaryEnd,
    complete,
}
