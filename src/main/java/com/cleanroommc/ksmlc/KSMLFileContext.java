package com.cleanroommc.ksmlc;

import chaos.unity.nenggao.AbstractSpan;
import chaos.unity.nenggao.FileReportBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KSMLFileContext {

  public final SourceFile sourceFile;
  public final FileReportBuilder fileReportBuilder;
  // Per file context
  public final String moduleName;
  public final String glVersion;
  public final String glProfile;
  public final List<String> requiredModules;
  // The members defined in this file, this includes function, struct, interface block etc,
  // the key represents the exposed globalVisibleName that other KSML files can access, and the value is the
  // originally defined globalVisibleName in the function prototype.
  public final HashMap<String, String> memberNames;
  public final HashMap<String, Exportable> exported;
  // Non-visible context
  public final List<String> declarationPrototypes;
  public final List<ModuleMemberReference> moduleMemberReferences;
  public final String strippedSource;

  private KSMLFileContext(
      final SourceFile sourceFile,
      final String moduleName,
      final String glVersion,
      final String glProfile,
      final List<String> requiredModules,
      final HashMap<String, String> memberNames,
      final HashMap<String, Exportable> exported,
      final List<String> declarationPrototypes,
      final List<ModuleMemberReference> moduleMemberReferences,
      final String strippedSource
  ) {
    this.sourceFile = sourceFile;
    this.fileReportBuilder = sourceFile.reportBuilder();
    this.moduleName = moduleName;
    this.glVersion = glVersion;
    this.glProfile = glProfile;
    this.requiredModules = requiredModules;
    this.memberNames = memberNames;
    this.exported = exported;
    this.declarationPrototypes = declarationPrototypes;
    this.moduleMemberReferences = moduleMemberReferences;
    this.strippedSource = strippedSource;
  }

  public static class Builder {

    private final SourceFile sourceFile;
    private String moduleName;
    private String glVersion;
    private String glProfile;
    private final List<String> requiredModules = new ArrayList<>();
    private final HashMap<String, String> memberNames = new HashMap<>();
    private final HashMap<String, Exportable> exported = new HashMap<>();
    private final List<String> declarationPrototypes = new ArrayList<>();
    private final List<ModuleMemberReference> moduleMemberReferences = new ArrayList<>();

    public Builder(final SourceFile sourceFile) {
      this.sourceFile = sourceFile;
    }

    public void setModule(final String moduleName) {
      this.moduleName = moduleName;
    }

    public void setGlVersion(final String glVersion, final String glProfile) {
      this.glVersion = glVersion;
      this.glProfile = glProfile;
    }

    public void addRequiredModule(final String moduleName) {
      requiredModules.add(moduleName);
    }

    public boolean addMemberName(final String memberName, final String declaredName) {
      if (!memberNames.containsKey(memberName)) {
        memberNames.put(memberName, declaredName);
        return true;
      }
      return false;
    }

    public void addExportable(
        final String globalVisibleName,
        final String ksmlVisibleName,
        final String version,
        final ExportTargetType targetType
    ) {
      exported.put(globalVisibleName,
          new Exportable(globalVisibleName, ksmlVisibleName, version, targetType));
    }

    public void addDeclarationPrototype(final String declaration) {
      declarationPrototypes.add(declaration);
    }

    public void addModuleMemberReference(final String module, final String memberName,
        final AbstractSpan span) {
      moduleMemberReferences.add(new ModuleMemberReference(module, memberName, span));
    }

    public KSMLFileContext build(String strippedSource) {
      return new KSMLFileContext(
          sourceFile,
          moduleName,
          glVersion,
          glProfile,
          requiredModules,
          memberNames,
          exported,
          declarationPrototypes,
          moduleMemberReferences,
          strippedSource
      );
    }
  }

  public record Exportable(String globalVisibleName,
                           String ksmlVisibleName,
                           String version,
                           ExportTargetType targetType) {

  }

  public enum ExportTargetType {
    Function
  }

  public record ModuleMemberReference(String module, String memberName, AbstractSpan span) {

  }
}
