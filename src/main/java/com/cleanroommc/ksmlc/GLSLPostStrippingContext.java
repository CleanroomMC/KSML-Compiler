package com.cleanroommc.ksmlc;

public class GLSLPostStrippingContext {

  public final String version;
  public final String versionProfile;

  private GLSLPostStrippingContext(final String version, final String versionProfile) {
    this.version = version;
    this.versionProfile = versionProfile;
  }

  public static class Builder {

    private String version;
    private String versionProfile;

    public void setVersion(final String version) {
      this.version = version;
    }

    public void setVersionProfile(final String versionProfile) {
      this.versionProfile = versionProfile;
    }

    public GLSLPostStrippingContext build() {
      return new GLSLPostStrippingContext(version, versionProfile);
    }
  }
}
