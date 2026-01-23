package com.cleanroommc.ksmlc;

public record KSMLFileContext(String moduleName, String glVersion) {
    public static class Builder {
        private String moduleName;
        private String glVersion;
        
        public void setModule(final String moduleName) {
            this.moduleName = moduleName;
        }
        
        public void setGlVersion(final String glVersion) {
            this.glVersion = glVersion;
        }
        
        public KSMLFileContext build() {
            return new KSMLFileContext(moduleName, glVersion);
        }
    }
}
