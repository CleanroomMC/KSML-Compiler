# KSML Compiler

A compiler for the KSML language (also known as Kirino Shader Metal Language).

## How to use it?

`KSMLCompiler` takes 3 arguments: the GLSL shader file, multiple KSML files to be assembled into the 
shader, and a series of features to toggle features (currently unused).

After invoking `KSMLCompiler#compile()`, you will get the assembled GLSL shader without any traces 
of KSML-specific syntax as a string, if none of the errors is encountered. Otherwise, the compiler
will crash.

```java
public class Main {

  public static void main(String[] args) throws IOException, URISyntaxException {
    KSMLCompiler compiler = new KSMLCompiler(SourceFile.fromResource("main.glsl"),
        new SourceFile[]{SourceFile.fromResource("math.ksml")}, null);

    System.out.println(compiler.compile());
  }
}
```

## Overview

WIP

## Goals

See [here](https://github.com/CleanroomMC/Kirino-Engine/blob/main/docs/ksml/ksml_spec_v1.md) for the 
spec. Please note that the compiler behavior may not match the spec exactly as it is still in development.

## License

This project is licensed under [MIT License](./LICENSE).
