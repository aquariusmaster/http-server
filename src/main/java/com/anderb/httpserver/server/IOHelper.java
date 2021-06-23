package com.anderb.httpserver.server;

import lombok.SneakyThrows;

import java.io.*;
import java.util.Objects;

public class IOHelper {

    private final static String STATIC_PATH = "static/";

    @SneakyThrows
    public static void writeStaticFileToStream(String resourcePath, OutputStream out) {
        Objects.requireNonNull(resourcePath);
        Objects.requireNonNull(out);

        try (InputStream fis = IOHelper.class.getClassLoader().getResourceAsStream(STATIC_PATH + resourcePath)) {
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
