package com.cleanroommc.ksmlc;

import com.cleanroommc.ksmlc.glsl.grammar.KSMLParser;
import com.cleanroommc.ksmlc.glsl.grammar.KSMLParserBaseListener;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.Interval;

public class KSMLRewriteListener extends KSMLParserBaseListener {
    private final TokenStreamRewriter rewriter;
    private final KSMLFileContext.Builder builder;
    
    public KSMLRewriteListener(final TokenStreamRewriter rewriter, final KSMLFileContext.Builder builder) {
        this.rewriter = rewriter;
        this.builder = builder;
    }

    @Override
    public void enterModuleMeta(KSMLParser.ModuleMetaContext ctx) {
        builder.setModule(ctx.IDENTIFIER().getText());
    }

    @Override
    public void enterGlVersionMeta(KSMLParser.GlVersionMetaContext ctx) {
        builder.setGlVersion(ctx.VERSION_NUMBER().getText());
    }
}
