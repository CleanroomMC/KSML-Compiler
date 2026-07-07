package com.cleanroommc.ksmlc;

import chaos.unity.nenggao.FileReportBuilder;
import com.cleanroommc.ksmlc.glsl.grammar.GLSLLexer;
import com.cleanroommc.ksmlc.glsl.grammar.GLSLParser;
import com.cleanroommc.ksmlc.glsl.grammar.GLSLParserBaseListener;
import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import java.util.List;
import java.util.Optional;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.Interval;

public class GLSLRewriteListener extends GLSLParserBaseListener implements RewriterListener {

  private final TokenStreamRewriter rewriter;
  private final SourceFile glslSource;
  private final FileReportBuilder fileReportBuilder;
  private final List<KSMLFileContext> contexts;
  private final GLSLPostStrippingContext.Builder builder;

  public GLSLRewriteListener(
      final TokenStreamRewriter rewriter,
      final SourceFile glslSource,
      final List<KSMLFileContext> contexts,
      final GLSLPostStrippingContext.Builder builder
  ) {
    this.rewriter = rewriter;
    this.glslSource = glslSource;
    this.fileReportBuilder = glslSource.reportBuilder();
    this.contexts = contexts;
    this.builder = builder;
  }

  @Override
  public void enterVersion_directive(GLSLParser.Version_directiveContext ctx) {
    builder.setVersion(ctx.number().getText());

    if (ctx.profile() != null) {
      builder.setVersionProfile(ctx.profile().PROFILE().getText());
    }

    deleteNewlines(ctx);
    deleteCtx(ctx);
  }

  @Override
  public void enterImportMeta(GLSLParser.ImportMetaContext ctx) {
    deleteNewlines(ctx);
    deleteCtx(ctx);
  }

  @Override
  public void enterFunctionCall(GLSLParser.FunctionCallContext ctx) {
    if (ctx.postfix_expression() instanceof GLSLParser.MemberAccessContext maCtx) {
      var moduleName = rewriter.getText(
          Interval.of(maCtx.postfix_expression().start.getTokenIndex(),
              maCtx.postfix_expression().stop.getTokenIndex()));
      var functionName = maCtx.field_selection().variable_identifier().IDENTIFIER().getText();

      var candidateSymbol = contexts.stream()
          .filter(fileCtx -> fileCtx.moduleName.equals(moduleName))
          .findFirst().flatMap(fileCtx -> Optional.ofNullable(fileCtx.exported.get(functionName)));

      if (candidateSymbol.isPresent()
          && candidateSymbol.get().targetType() == KSMLFileContext.ExportTargetType.Function) {
        rewriter.replace(maCtx.start, maCtx.stop,
            Utils.synthesizeModuleMemberName(moduleName, candidateSymbol.get().ksmlVisibleName()));
      } else {
        fileReportBuilder.error(Utils.spanFromCtx(ctx),
                Ansi.colorize("Unresolved function call \"%s\" from module \"%s\"",
                    Attribute.RED_TEXT()), functionName, moduleName)
            .label(Utils.spanFromCtx(ctx), Ansi.colorize("Error occurs here", Attribute.RED_TEXT()))
            .color(Attribute.RED_TEXT()).build()
            .build();
      }
    }
  }

  @Override
  public TokenStreamRewriter getRewriter() {
    return rewriter;
  }

  @Override
  public int getNewLineType() {
    return GLSLLexer.WHITE_SPACE;
  }
}
