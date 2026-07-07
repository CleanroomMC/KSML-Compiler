package com.cleanroommc.ksmlc;

import com.cleanroommc.ksmlc.glsl.grammar.GLSLLexer;
import com.cleanroommc.ksmlc.glsl.grammar.GLSLParser;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLLexer;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class KSMLCompiler {

  private final SourceFile glslSource;
  private final SourceFile[] ksmlSources;
  private final String[] features;

  public KSMLCompiler(SourceFile glslSource, SourceFile[] ksmlSources, String[] features) {
    this.glslSource = glslSource;
    this.ksmlSources = ksmlSources;
    this.features = features;
  }

  public String compile() {
    var contexts = new ArrayList<KSMLFileContext>(ksmlSources.length);

    for (SourceFile ksmlSource : ksmlSources) {
      KSMLFileContext.Builder builder = new KSMLFileContext.Builder(ksmlSource);

      var stripped = compileSource(ksmlSource,
              KSMLLexer::new,
              KSMLParser::new,
              KSMLParser::ksmlTranslationUnit,
              GeneralErrorListener::new,
              rewriter -> new KSMLRewriteListener(rewriter, ksmlSource, builder));

      System.out.println("KEK");
      System.out.println(stripped);

      KSMLFileContext ctx = builder.build(stripped);
      contexts.add(ctx);
    }

    new KSMLChecker(contexts).check();

    for (var context : contexts) {
      context.fileReportBuilder.print(System.out);
    }

    if (contexts.stream().anyMatch(ctx -> ctx.fileReportBuilder.containsError())) {
      System.exit(-1);
    }

    var postStrippingContextBuilder = new GLSLPostStrippingContext.Builder();
    var strippedGLSL = compileSource(glslSource, GLSLLexer::new, GLSLParser::new,
            GLSLParser::translation_unit,
            GeneralErrorListener::new,
            rewriter -> new GLSLRewriteListener(rewriter, glslSource, contexts,
                    postStrippingContextBuilder));
    var postStrippingContext = postStrippingContextBuilder.build();

    glslSource.reportBuilder().print(System.out);
    if (glslSource.reportBuilder().containsError()) {
      System.exit(-1);
    }

    var builder = new StringBuilder();
    assembleGLSLVersion(postStrippingContext, builder);
    assembleKSMLModuleHeaders(contexts, builder);
    assembleKSMLModuleBodies(contexts, builder);
    assembleGLSLBody(builder);
    builder.append(strippedGLSL);

    return builder.toString().replace(System.lineSeparator().repeat(2), "");
  }

  private void assembleGLSLVersion(final GLSLPostStrippingContext ctx,
                                   final StringBuilder builder) {
    builder.append("/* GLSL SHADER PRE-DEFINITIONS */\n");

    if (ctx.version != null) {
      builder.append(String.format("#version %s %s\n", ctx.version,
              ctx.versionProfile != null ? ctx.versionProfile : ""));
    }
  }

  // Responsible for assembling KSML module's functions as function prototypes first so circular
  // dependencies can be properly resolved on assembled GLSL shader code.
  private void assembleKSMLModuleHeaders(final List<KSMLFileContext> contexts,
                                         final StringBuilder builder) {
    for (var ctx : contexts) {
      builder.append(String.format("/* MODULE \"%s\" PROTOTYPE DEFINITIONS */\n", ctx.moduleName));
      builder.append(String.format("/* MODULE GL_VERSION: %s */\n", ctx.glVersion));
      builder.append("/* AUTO-GENERATED PROTOTYPES */\n");
      for (var declaration : ctx.declarationPrototypes) {
        builder.append(declaration).append(";\n");
      }
    }
  }

  private void assembleKSMLModuleBodies(final List<KSMLFileContext> contexts,
                                        final StringBuilder builder) {
    for (var ctx : contexts) {
      builder.append(String.format("/* MODULE \"%s\" DEFINITIONS */\n", ctx.moduleName));
      builder.append(String.format("#line 1 \"%s\"\n", ctx.sourceFile.fileName()));
      builder.append(ctx.strippedSource);
    }
  }

  private void assembleGLSLBody(final StringBuilder builder) {
    builder.append("/* GLSL SHADER DEFINITION */\n");
    builder.append(String.format("#line 1 \"%s\"\n", glslSource.fileName()));
  }

  private <LE extends Lexer, PA extends Parser, EL extends BaseErrorListener, LI extends ParseTreeListener> String compileSource(
          SourceFile source,
          Function<CharStream, LE> lexerCtor,
          Function<TokenStream, PA> parserCtor,
          Function<PA, ParseTree> topRuleApplier,
          Function<SourceFile, EL> errorListener,
          Function<TokenStreamRewriter, LI> listenerCtor
  ) {
    CharStream sourceStream = new ANTLRInputStream(source.source().toCharArray(),
            source.source().length());
    LE lexer = lexerCtor.apply(sourceStream);
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    PA parser = parserCtor.apply(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener.apply(source));
    ParseTree parseTree = topRuleApplier.apply(parser);
    TokenStreamRewriter rewriter = new TokenStreamRewriter(tokenStream);
    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(listenerCtor.apply(rewriter), parseTree);
    return rewriter.getText();
  }
}
