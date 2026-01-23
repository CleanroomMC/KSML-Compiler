package com.cleanroommc.ksmlc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public record SourceFile(String fileName, String source) {
    public static SourceFile fromResource(String filePath) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (var stream = classLoader.getResourceAsStream(filePath)) {
            assert stream != null;
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = stream.read(buffer)) != -1; )
                result.write(buffer, 0, length);
            String source = result.toString(Charset.defaultCharset());
            return new SourceFile(filePath, source);
        }
    }
}
