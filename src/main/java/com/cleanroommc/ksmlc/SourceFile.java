package com.cleanroommc.ksmlc;

import chaos.unity.nenggao.CharacterSet;
import chaos.unity.nenggao.FileReportBuilder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

public record SourceFile(String fileName, String source, FileReportBuilder reportBuilder) {

  public static SourceFile fromResource(String filePath) throws IOException, URISyntaxException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    URL url = classLoader.getResource(filePath);

    if (url == null) {
      throw new IllegalArgumentException("File not found: " + filePath);
    }

    try (var stream = classLoader.getResourceAsStream(filePath)) {
      assert stream != null;
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      for (int length; (length = stream.read(buffer)) != -1; ) {
        result.write(buffer, 0, length);
      }
      String source = result.toString(Charset.defaultCharset());
      File file = new File(url.toURI());
      FileReportBuilder builder = FileReportBuilder.sourceFile(file)
          .characterSet(CharacterSet.ASCII);
      return new SourceFile(filePath, source, builder);
    }
  }
}
