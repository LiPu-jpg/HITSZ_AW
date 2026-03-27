package edu.hitsz.common;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ImageResourceLoader {

    private static final String IMAGE_ROOT = "images";

    private ImageResourceLoader() {
    }

    public static BufferedImage load(String fileName) {
        BufferedImage imageFromClasspath = loadFromClasspath(fileName);
        if (imageFromClasspath != null) {
            return imageFromClasspath;
        }

        Path fallbackPath = Paths.get("src", IMAGE_ROOT, fileName);
        if (Files.exists(fallbackPath)) {
            try (InputStream inputStream = Files.newInputStream(fallbackPath)) {
                return ImageIO.read(inputStream);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read image file: " + fallbackPath.toAbsolutePath(), e);
            }
        }

        throw new IllegalStateException(
                "Image resource not found: " + fileName
                        + ". Expected classpath resource /" + IMAGE_ROOT + "/" + fileName
                        + " or filesystem path " + fallbackPath.toAbsolutePath()
        );
    }

    private static BufferedImage loadFromClasspath(String fileName) {
        String resourcePath = IMAGE_ROOT + "/" + fileName;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            BufferedImage image = tryRead(contextClassLoader, resourcePath);
            if (image != null) {
                return image;
            }
        }
        return tryRead(ImageResourceLoader.class.getClassLoader(), resourcePath);
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
