package com.skyblockexp.lifesteal.seasons.compatibility;

import org.junit.jupiter.api.Test;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ServerEnvironmentIsolatedUnitTest {

    @Test
    void isolatedLoaderCoversNonFoliaFallbackBrandPath() throws Exception {
        Map<String, byte[]> compiled = compileBukkitStubs();
        byte[] serverEnvironmentBytes = readServerEnvironmentBytes();

        ClassLoader isolated = new ClassLoader(null) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if ("com.skyblockexp.lifesteal.seasons.compatibility.ServerEnvironment".equals(name)) {
                    return defineClass(name, serverEnvironmentBytes, 0, serverEnvironmentBytes.length);
                }
                byte[] bytes = compiled.get(name);
                if (bytes != null) {
                    return defineClass(name, bytes, 0, bytes.length);
                }
                throw new ClassNotFoundException(name);
            }
        };

        Class<?> clazz = Class.forName("com.skyblockexp.lifesteal.seasons.compatibility.ServerEnvironment", true, isolated);
        Method brand = clazz.getMethod("brand");
        Method folia = clazz.getMethod("isFolia");
        Method async = clazz.getMethod("hasAsyncScheduler");
        Method global = clazz.getMethod("hasGlobalRegionScheduler");

        assertEquals("FakeServer", brand.invoke(null));
        assertFalse((Boolean) folia.invoke(null));
        assertFalse((Boolean) async.invoke(null));
        assertFalse((Boolean) global.invoke(null));
    }

    private static byte[] readServerEnvironmentBytes() throws IOException {
        String resource = "/" + ServerEnvironment.class.getName().replace('.', '/') + ".class";
        try (var in = ServerEnvironment.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Missing class resource: " + resource);
            }
            return in.readAllBytes();
        }
    }

    private static Map<String, byte[]> compileBukkitStubs() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("System Java compiler not available");
        }

        Map<String, String> sources = Map.of(
                "org.bukkit.Server", "package org.bukkit; public interface Server { String getName(); }",
                "org.bukkit.FakeServer", "package org.bukkit; public class FakeServer implements Server { public String getName(){ return \"FakeServer\"; } }",
                "org.bukkit.Bukkit", "package org.bukkit; public final class Bukkit { private static final Server SERVER = new FakeServer(); public static Server getServer(){ return SERVER; } }"
        );

        StandardJavaFileManager standard = compiler.getStandardFileManager(null, null, null);
        MemoryFileManager fileManager = new MemoryFileManager(standard);
        List<JavaFileObject> units = sources.entrySet().stream()
                .map(e -> new SourceObject(e.getKey(), e.getValue()))
                .map(JavaFileObject.class::cast)
                .toList();

        Boolean ok = compiler.getTask(null, fileManager, null, null, null, units).call();
        if (!Boolean.TRUE.equals(ok)) {
            throw new IllegalStateException("Failed to compile Bukkit stubs");
        }
        return fileManager.compiled;
    }

    private static final class SourceObject extends SimpleJavaFileObject {
        private final String source;

        private SourceObject(String className, String source) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }

    private static class BytecodeObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();

        private BytecodeObject(String className) {
            super(URI.create("bytes:///" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
        }

        @Override
        public OutputStream openOutputStream() {
            return output;
        }

        private byte[] bytes() {
            return output.toByteArray();
        }
    }

    private static final class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private final Map<String, byte[]> compiled = new HashMap<>();

        private MemoryFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                   JavaFileObject.Kind kind, FileObject sibling) {
            return new BytecodeObject(className) {
                @Override
                public OutputStream openOutputStream() {
                    return new ByteArrayOutputStream() {
                        @Override
                        public void close() throws IOException {
                            super.close();
                            compiled.put(className, toByteArray());
                        }
                    };
                }
            };
        }
    }
}
