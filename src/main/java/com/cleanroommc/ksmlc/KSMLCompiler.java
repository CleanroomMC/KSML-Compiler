package com.cleanroommc.ksmlc;

import com.cleanroommc.ksmlc.glsl.grammar.KSMLLexer;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

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

    public void compile() {
        for (SourceFile ksmlSource : ksmlSources) {
            KSMLFileContext.Builder builder = new KSMLFileContext.Builder();

            compileSource(ksmlSource,
                    KSMLLexer::new,
                    KSMLParser::new,
                    KSMLParser::ksmlTranslationUnit,
                    rewriter -> new KSMLRewriteListener(rewriter, builder));
            
            KSMLFileContext ctx = builder.build();
            
            System.out.println(ctx);
        }
    }

    private <LE extends Lexer, PA extends Parser, LI extends ParseTreeListener> String compileSource(
            SourceFile source,
            Function<CharStream, LE> lexerCtor,
            Function<TokenStream, PA> parserCtor,
            Function<PA, ParseTree> topRuleApplier,
            Function<TokenStreamRewriter, LI> listenerCtor
    ) {
        CharStream sourceStream = new ANTLRInputStream(source.source().toCharArray(), source.source().length());
        LE lexer = lexerCtor.apply(sourceStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PA parser = parserCtor.apply(tokenStream);
        ParseTree parseTree = topRuleApplier.apply(parser);
        TokenStreamRewriter rewriter = new TokenStreamRewriter(tokenStream);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listenerCtor.apply(rewriter), parseTree);
        return rewriter.getText();
    }
}
