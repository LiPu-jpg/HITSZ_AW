package edu.hitsz.common;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ImageResourceLoader {

    private static final String[] CLASSPATH_ROOTS = {
            "videos/images",
            "images"
    };
    private static final String[][] FILESYSTEM_ROOTS = {
            {"src", "videos", "images"},
            {"src", "images"}
    };

    private ImageResourceLoader() {
    }

    public static BufferedImage load(String fileName) {
        BufferedImage imageFromClasspath = loadFromClasspath(fileName);
        if (imageFromClasspath != null) {
            return imageFromClasspath;
        }

        Path directPath = Paths.get(fileName);
        if (Files.exists(directPath)) {
            try (InputStream inputStream = Files.newInputStream(directPath)) {
                return ImageIO.read(inputStream);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read image file: " + directPath.toAbsolutePath(), e);
            }
        }

        for (String[] root : FILESYSTEM_ROOTS) {
            Path fallbackPath = Paths.get(root[0], java.util.Arrays.copyOfRange(root, 1, root.length)).resolve(fileName);
            if (Files.exists(fallbackPath)) {
                try (InputStream inputStream = Files.newInputStream(fallbackPath)) {
                    return ImageIO.read(inputStream);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read image file: " + fallbackPath.toAbsolutePath(), e);
                }
            }
        }

        throw new IllegalStateException(
                "Image resource not found: " + fileName
                        + ". Expected classpath resource under " + String.join(", ", CLASSPATH_ROOTS)
                        + " or filesystem path under src/videos/images or src/images"
        );
    }

    public static BufferedImage loadOrFallback(String preferredFileName, String fallbackFileName) {
        try {
            return load(preferredFileName);
        } catch (IllegalStateException ignored) {
            return load(fallbackFileName);
        }
    }

    private static BufferedImage loadFromClasspath(String fileName) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        for (String root : CLASSPATH_ROOTS) {
            String resourcePath = root + "/" + fileName;
            if (contextClassLoader != null) {
                BufferedImage image = tryRead(contextClassLoader, resourcePath);
                if (image != null) {
                    return image;
                }
            }
            BufferedImage image = tryRead(ImageResourceLoader.class.getClassLoader(), resourcePath);
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    private static BufferedImage tryRead(ClassLoader classLoader, String resourcePath) {
        if (classLoader == null) {
            return null;
        }
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return null;
            }
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read classpath image resource: " + resourcePath, e);
        }
    }
}
