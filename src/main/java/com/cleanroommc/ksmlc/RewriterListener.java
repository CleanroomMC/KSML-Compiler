package com.cleanroommc.ksmlc;

import com.cleanroommc.ksmlc.glsl.grammar.KSMLLexer;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.TerminalNode;

public interface RewriterListener {
  TokenStreamRewriter getRewriter();

  int getNewLineType();

  default void deleteCtx(ParserRuleContext ctx) {
    getRewriter().delete(ctx.start, ctx.stop);
  }

  default void deleteNewlines(ParserRuleContext ctx) {
    deleteNewlines(ctx.start);
  }

  default void deleteNewlines(TerminalNode node) {
    deleteNewlines(node.getSymbol());
  }

  default void deleteNewlines(Token start) {
    var rewriter = getRewriter();
    var tokenIndex = start.getTokenIndex();
    var hiddenTokens = ((BufferedTokenStream) rewriter.getTokenStream()).getHiddenTokensToLeft(tokenIndex, Token.HIDDEN_CHANNEL);

    if (hiddenTokens != null) {
      for (var token : hiddenTokens) {
        if (token.getType() == getNewLineType()) {
          if (!token.getText().isEmpty()) {
            rewriter.delete(token);
          }
        }
      }
    }
  }
}
