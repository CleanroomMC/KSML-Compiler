package com.cleanroommc.ksmlc;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        KSMLCompiler compiler = new KSMLCompiler(null, new SourceFile[]{SourceFile.fromResource("math.ksml")}, null);

        compiler.compile();
    }
}
