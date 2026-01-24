package com.cleanroommc.ksmlc;

import chaos.unity.nenggao.AbstractSpan;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public final class Utils {

  public static String synthesizeModuleMemberName(String moduleName, String memberName) {
    return String.format("%s_%s", moduleName, memberName);
  }

  public static AbstractSpan spanFromCtx(ParserRuleContext ctx) {
    return AbstractSpan.multipleLine(ctx.start.getLine(), ctx.start.getCharPositionInLine(),
        ctx.stop.getLine(), ctx.stop.getCharPositionInLine() + ctx.stop.getText().length());
  }

  public static AbstractSpan spanFromCtxs(ParserRuleContext ctx1, ParserRuleContext ctx2) {
    return AbstractSpan.multipleLine(ctx1.start.getLine(), ctx1.start.getCharPositionInLine(),
        ctx2.stop.getLine(), ctx2.stop.getCharPositionInLine() + ctx2.stop.getText().length());
  }

  public static AbstractSpan spanFromNode(TerminalNode token) {
    return spanFromToken(token.getSymbol());
  }

  public static AbstractSpan spanFromToken(Token token) {
    return AbstractSpan.singleLine(token.getLine(), token.getCharPositionInLine(),
        token.getCharPositionInLine() + token.getText().length());
  }
}
