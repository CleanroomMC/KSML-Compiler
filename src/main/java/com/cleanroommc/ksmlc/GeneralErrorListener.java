package com.cleanroommc.ksmlc;

import chaos.unity.nenggao.FileReportBuilder;
import chaos.unity.nenggao.Span;
import com.diogonunes.jcolor.Attribute;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class GeneralErrorListener extends BaseErrorListener {
  private final FileReportBuilder fileReportBuilder;

  public GeneralErrorListener(SourceFile srcFile) {
    this.fileReportBuilder = srcFile.reportBuilder();
  }

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                          int line, int charPositionInLine, String msg, RecognitionException e) {
    final var offendingToken = (CommonToken) offendingSymbol;
    final var span = Span.singleLine(line, charPositionInLine, charPositionInLine + offendingToken.getText().length());
    fileReportBuilder.error(span, "Syntax error: %s", msg)
            .label(span, "Error occurs here").color(Attribute.RED_TEXT()).build()
            .build();
  }
}
