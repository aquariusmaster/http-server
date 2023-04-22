package com.anderb.server;

import lombok.SneakyThrows;

import java.io.*;
import java.util.Objects;

public class IOHelper {

    @SneakyThrows
    public static void writeFileFromResource(String resourcePath, OutputStream out) {
        Objects.requireNonNull(resourcePath);
        Objects.requireNonNull(out);

        try (InputStream fis = IOHelper.class.getClassLoader().getResourceAsStream(resourcePath)) {
            int n;
            byte[] buffer = new byte[8192];
            while ((n = fis.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        } finally {
            closeQuietly(out);
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final IOException e) {
                //ignore
            }
        }
    }
}
