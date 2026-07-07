package com.cleanroommc.ksmlc;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

  public static void main(String[] args) throws IOException, URISyntaxException {
    KSMLCompiler compiler = new KSMLCompiler(SourceFile.fromResource("main.glsl"),
            new SourceFile[]{SourceFile.fromResource("math.ksml"),
                    SourceFile.fromResource("functional.ksml")}, null);

    System.out.println(compiler.compile());
  }
}
