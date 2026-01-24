package com.cleanroommc.ksmlc;

import com.diogonunes.jcolor.Attribute;
import java.util.List;

public class KSMLChecker {

  private final List<KSMLFileContext> fileContexts;

  KSMLChecker(final List<KSMLFileContext> fileContexts) {
    this.fileContexts = fileContexts;
  }

  public void check() {
    checkReferences();
  }

  private void checkReferences() {
    ctxLoop:
    for (var ctx : fileContexts) {
      for (var ref : ctx.moduleMemberReferences) {
        var candidateCtx = fileContexts.stream()
            .filter(targetModule -> targetModule.moduleName.equals(ref.module())).findFirst();

        if (candidateCtx.isEmpty()) {
          ctx.fileReportBuilder.error(ref.span(), "Unresolved module %s", ref.module())
              .label(ref.span(), "Error occurs here").color(Attribute.RED_TEXT()).build()
              .build();
          continue ctxLoop;
        }

        var targetCtx = candidateCtx.get();
        var candidateMember = targetCtx.memberNames.get(ref.memberName());

        if (candidateMember == null) {
          ctx.fileReportBuilder.error(ref.span(), "Unresolved member %s in module %s",
                  ref.memberName(), ref.module())
              .label(ref.span(), "Error occurs here").color(Attribute.RED_TEXT()).build()
              .build();
        }
      }
    }
  }
}
