package com.cleanroommc.ksmlc;

import chaos.unity.nenggao.AbstractSpan;
import chaos.unity.nenggao.FileReportBuilder;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser.ExportMetaContext;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser.FunctionCallContext;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser.FunctionDeclContext;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser.Function_definitionContext;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser.GlRequiresMetaContext;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser.GlVersionMetaContext;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser.MemberAccessContext;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser.ModuleMetaContext;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser.RequiresMetaContext;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParserBaseListener;
import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.Interval;

public class KSMLRewriteListener extends KSMLParserBaseListener {

  private final TokenStreamRewriter rewriter;
  private final SourceFile ksmlSource;
  private final FileReportBuilder fileReportBuilder;
  private final KSMLFileContext.Builder builder;
  // @module context
  private String moduleName;
  // @gl_require context
  private String requireVersion;
  // @export context
  private ExportMetaContext exportMetaCtx;

  public KSMLRewriteListener(final TokenStreamRewriter rewriter,
      final SourceFile ksmlSource,
      final KSMLFileContext.Builder builder) {
    this.rewriter = rewriter;
    this.ksmlSource = ksmlSource;
    this.fileReportBuilder = ksmlSource.reportBuilder();
    this.builder = builder;
  }

  @Override
  public void enterModuleMeta(ModuleMetaContext ctx) {
    builder.setModule(moduleName = ctx.IDENTIFIER().getText());
    rewriter.replace(ctx.start, ctx.stop, "");
  }

  @Override
  public void enterRequiresMeta(RequiresMetaContext ctx) {
    builder.addRequiredModule(ctx.IDENTIFIER().getText());
    rewriter.replace(ctx.start, ctx.stop, "");
  }

  @Override
  public void enterGlVersionMeta(GlVersionMetaContext ctx) {
    builder.setGlVersion(ctx.VERSION_NUMBER().getText());
    rewriter.replace(ctx.start, ctx.stop, "");
  }

  @Override
  public void enterExportMeta(ExportMetaContext ctx) {
    if (exportMetaCtx != null) {
      fileReportBuilder.error(Utils.spanFromCtx(ctx),
              "Cannot export multiple times for declaration")
          .label(Utils.spanFromCtx(ctx), "Error occurs here").color(Attribute.RED_TEXT()).build()
          .build();
    }

    exportMetaCtx = ctx;
    rewriter.replace(ctx.start, ctx.stop, "");
  }

  @Override
  public void enterGlRequiresMeta(GlRequiresMetaContext ctx) {
    if (requireVersion != null) {
      fileReportBuilder.error(Utils.spanFromCtx(ctx),
              "Cannot require version multiple times for declaration")
          .label(Utils.spanFromCtx(ctx), "Error occurs here").color(Attribute.RED_TEXT()).build()
          .build();
    }

    requireVersion = ctx.VERSION_NUMBER().getText();
  }

  @Override
  public void enterFunction_definition(Function_definitionContext ctx) {
    var functionNameIdentifier = ctx.function_prototype().IDENTIFIER();
    var functionName = functionNameIdentifier.getText();

    if (!builder.addMemberName(functionName, functionName)) {
      fileReportBuilder.error(Utils.spanFromCtx(ctx.function_prototype()),
              "Function with same globalVisibleName already exists in module")
          .label(Utils.spanFromCtx(ctx), "Error occurs here").color(Attribute.RED_TEXT()).build()
          .build();
    }

    if (exportMetaCtx != null) {
      var globalVisibleName =
          exportMetaCtx.IDENTIFIER() != null ? exportMetaCtx.IDENTIFIER().getText() : null;

      builder.addExportable(globalVisibleName,
          functionName,
          requireVersion,
          KSMLFileContext.ExportTargetType.Function);

      if (globalVisibleName != null) {
        if (globalVisibleName.equals(functionName)) {
          fileReportBuilder.warning(Utils.spanFromCtxs(exportMetaCtx, ctx.function_prototype()),
                  Ansi.colorize(
                      "Exporting symbol with same globalVisibleName as function globalVisibleName is redundant",
                      Attribute.YELLOW_TEXT()))
              .label(Utils.spanFromNode(exportMetaCtx.IDENTIFIER()),
                  Ansi.colorize("Warning occurs here",
                      Attribute.YELLOW_TEXT()))
              .color(Attribute.YELLOW_TEXT())
              .hint("Consider remove this unnecessary export globalVisibleName").build()
              .label(Utils.spanFromNode(functionNameIdentifier),
                  Ansi.colorize("Symbol already got same globalVisibleName",
                      Attribute.YELLOW_TEXT()))
              .color(Attribute.YELLOW_TEXT()).build()
              .build();
        } else if (!builder.addMemberName(globalVisibleName, functionName)) {
          fileReportBuilder.error(Utils.spanFromCtx(exportMetaCtx),
                  Ansi.colorize("Exporting symbol with same globalVisibleName already exists in module",
                      Attribute.RED_TEXT()))
              .label(Utils.spanFromCtx(exportMetaCtx),
                  Ansi.colorize("Error occurs here", Attribute.RED_TEXT()))
              .color(Attribute.RED_TEXT()).build()
              .build();
        }
      }

      // Reset declaration state
      exportMetaCtx = null;
      requireVersion = null;
    }

    rewriter.replace(functionNameIdentifier.getSymbol(), functionNameIdentifier.getSymbol(),
        Utils.synthesizeModuleMemberName(moduleName, functionName));
    builder.addDeclaration(rewriter.getText(
        new Interval(ctx.function_prototype().start.getTokenIndex(),
            ctx.function_prototype().stop.getTokenIndex())));
    builder.addModuleMemberReference(moduleName, functionName, Utils.spanFromCtx(ctx));
  }

  @Override
  public void enterFunctionDecl(FunctionDeclContext ctx) {
    AbstractSpan span = Utils.spanFromCtx(ctx);

    fileReportBuilder.error(span, "Function prototype is not allowed in KSML")
        .label(span, "Error occurs here").color(Attribute.RED_TEXT()).build()
        .build();
  }

  @Override
  public void enterFunctionCall(FunctionCallContext ctx) {
    if (ctx.postfix_expression() instanceof MemberAccessContext maCtx) {
      var moduleName = rewriter.getText(
          Interval.of(maCtx.postfix_expression().start.getTokenIndex(),
              maCtx.postfix_expression().stop.getTokenIndex()));
      var functionName = maCtx.field_selection().variable_identifier().IDENTIFIER().getText();

      // ChAoS: We assume the target reference is present here, but later actually checked in KSMLChecker
      builder.addModuleMemberReference(moduleName, functionName, Utils.spanFromCtx(ctx));
      rewriter.replace(maCtx.start, maCtx.stop,
          Utils.synthesizeModuleMemberName(moduleName, functionName));
    }
  }
}
