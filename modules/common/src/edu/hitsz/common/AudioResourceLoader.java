package edu.hitsz.common;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AudioResourceLoader {

    private static final String[] CLASSPATH_ROOTS = {
            "assets/audio"
    };
    private static final String[][] FILESYSTEM_ROOTS = {
            {"src", "assets", "audio"}
    };

    private AudioResourceLoader() {
    }

    public static boolean resourceExists(String fileName) {
        if (loadFromClasspath(fileName) != null) {
            return true;
        }
        if (Files.exists(Paths.get(fileName))) {
            return true;
        }
        for (String[] root : FILESYSTEM_ROOTS) {
            if (Files.exists(resolveRoot(root).resolve(fileName))) {
                return true;
            }
        }
        return false;
    }

    public static AudioInputStream openAudioStream(String fileName) {
        InputStream classpathStream = loadFromClasspath(fileName);
        if (classpathStream != null) {
            try {
                return AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(classpathStream));
            } catch (UnsupportedAudioFileException | IOException e) {
                throw new IllegalStateException("Failed to read classpath audio resource: " + fileName, e);
            }
        }

        Path directPath = Paths.get(fileName);
        if (Files.exists(directPath)) {
            return openPath(directPath);
        }

        for (String[] root : FILESYSTEM_ROOTS) {
            Path path = resolveRoot(root).resolve(fileName);
            if (Files.exists(path)) {
                return openPath(path);
            }
        }

        throw new IllegalStateException(
                "Audio resource not found: " + fileName
                        + ". Expected classpath resource under assets/audio or filesystem path under src/assets/audio"
        );
    }

    private static AudioInputStream openPath(Path path) {
        try {
            return AudioSystem.getAudioInputStream(path.toFile());
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new IllegalStateException("Failed to read audio file: " + path.toAbsolutePath(), e);
        }
    }

    private static InputStream loadFromClasspath(String fileName) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        for (String root : CLASSPATH_ROOTS) {
            String resourcePath = root + "/" + fileName;
            InputStream stream = tryOpen(contextClassLoader, resourcePath);
            if (stream != null) {
                return stream;
            }
            stream = tryOpen(AudioResourceLoader.class.getClassLoader(), resourcePath);
            if (stream != null) {
                return stream;
            }
        }
        return null;
    }

    private static InputStream tryOpen(ClassLoader classLoader, String resourcePath) {
        if (classLoader == null) {
            return null;
        }
        return classLoader.getResourceAsStream(resourcePath);
    }

    private static Path resolveRoot(String[] root) {
        Path path = Paths.get(root[0]);
        for (int i = 1; i < root.length; i++) {
            path = path.resolve(root[i]);
        }
        return path;
    }
}
