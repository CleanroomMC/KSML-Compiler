package com.cleanroommc.ksmlc;

import chaos.unity.nenggao.FileReportBuilder;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLLexer;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser.*;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParserBaseListener;
import com.diogonunes.jcolor.Attribute;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.Interval;

import java.util.ArrayList;
import java.util.List;

public class KSMLRewriteListener extends KSMLParserBaseListener implements RewriterListener {

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
  // @gl_requires context
  private GlRequiresMetaContext glRequiresMetaCtx;
  // @feature context
  private List<FeatureMetaContext> featureMetaCtxs = new ArrayList<>();
  // @code context
  private CodeBlockContext codeBlockCtx;

  public KSMLRewriteListener(final TokenStreamRewriter rewriter,
                             final SourceFile ksmlSource,
                             final KSMLFileContext.Builder builder) {
    this.rewriter = rewriter;
    this.ksmlSource = ksmlSource;
    this.fileReportBuilder = ksmlSource.reportBuilder();
    this.builder = builder;
  }

  @Override
  public void enterKsmlTranslationUnit(KsmlTranslationUnitContext ctx) {
    deleteNewlines(ctx.moduleMeta());
    super.enterKsmlTranslationUnit(ctx);
  }

  @Override
  public void enterKsmlDeclaration(KsmlDeclarationContext ctx) {
    for (var metaCtx : ctx.declarationMeta()) {
      deleteNewlines(metaCtx);
    }
    deleteNewlines(ctx.codeBlock());
  }

  @Override
  public void enterModuleMeta(ModuleMetaContext ctx) {
    builder.setModule(moduleName = ctx.IDENTIFIER().getText());
    deleteNewlines(ctx);
    deleteCtx(ctx);
  }

  @Override
  public void enterRequiresMeta(RequiresMetaContext ctx) {
    builder.addRequiredModule(ctx.IDENTIFIER().getText());
    deleteNewlines(ctx);
    deleteCtx(ctx);
  }

  @Override
  public void enterGlVersionMeta(GlVersionMetaContext ctx) {
    var profile = ctx.glVersionIdent();
    var profileIndent = profile != null ? profile.getText() : null;

    builder.setGlVersion(ctx.VERSION_NUMBER().getText(), profileIndent);
    deleteNewlines(ctx);
    deleteCtx(ctx);
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
    deleteNewlines(ctx);
    deleteCtx(ctx);
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
    glRequiresMetaCtx = ctx;
    deleteNewlines(ctx);
    deleteCtx(ctx);
  }

  @Override
  public void enterFeatureMeta(FeatureMetaContext ctx) {
    featureMetaCtxs.add(ctx);
  }

  @Override
  public void exitCodeBlock(CodeBlockContext ctx) {
    rewriter.delete(ctx.AT().getSymbol(), ctx.TRIPLE_QUOTE(0).getSymbol());
    deleteNewlines(ctx.TRIPLE_QUOTE(0));
    rewriter.delete(ctx.TRIPLE_QUOTE(1).getSymbol());
    // Clear meta contexts
    exportMetaCtx = null;
    glRequiresMetaCtx = null;
    featureMetaCtxs.clear();
  }

  @Override
  public void enterFunction_definition(Function_definitionContext ctx) {
    var functionNameIdentifier = ctx.function_prototype().IDENTIFIER();
    var functionName = functionNameIdentifier.getText();

    if (!builder.addMemberName(functionName, functionName)) {
      fileReportBuilder.error(Utils.spanFromCtx(ctx.function_prototype()),
                      "Function with same name already exists in module")
              .label(Utils.spanFromCtx(ctx), "Error occurs here").color(Attribute.RED_TEXT()).build()
              .build();
    }

    if (exportMetaCtx != null) {
      builder.addExportable(functionName,
              functionName,
              requireVersion,
              KSMLFileContext.ExportTargetType.Function);
    }

    rewriter.replace(functionNameIdentifier.getSymbol(), functionNameIdentifier.getSymbol(),
            Utils.synthesizeModuleMemberName(moduleName, functionName));
    builder.addDeclarationPrototype(rewriter.getText(
            new Interval(ctx.function_prototype().start.getTokenIndex(),
                    ctx.function_prototype().stop.getTokenIndex())));
    builder.addModuleMemberReference(moduleName, functionName, Utils.spanFromCtx(ctx));
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

  @Override
  public TokenStreamRewriter getRewriter() {
    return rewriter;
  }

  @Override
  public int getNewLineType() {
    return KSMLLexer.NEW_LINES;
  }
}
