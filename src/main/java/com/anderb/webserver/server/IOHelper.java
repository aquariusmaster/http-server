package com.anderb.webserver.server;

import lombok.SneakyThrows;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class IOHelper {

    @SneakyThrows
    public static void writeResourceToStream(String resourcePath, OutputStream out) {
        Objects.requireNonNull(resourcePath);
        Objects.requireNonNull(out);
        try (FileInputStream fis = new FileInputStream(resourcePath)) {
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
