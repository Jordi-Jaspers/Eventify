package io.github.eventify.common.util;

import io.github.eventify.common.exception.CouldNotLoadResourceException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/** Utility class for loading resources from the classpath. */
@Slf4j
@UtilityClass
public class ResourceLoaderUtil {

    private static final String RESOURCES_PREFIX = "resources/";
    private static final ResourceLoader DEFAULT_LOADER = new DefaultResourceLoader();

    /**
     * Loads a classpath resource as a {@link Resource} using the default resource loader.
     *
     * @param path the resource path (with or without "resources" prefix)
     * @return the Spring {@link Resource}
     */
    public static Resource getResource(final String path) {
        return getResource(path, DEFAULT_LOADER);
    }

    /**
     * Loads a classpath resource as a {@link Resource} using the provided resource loader.
     *
     * @param path   the resource path (with or without "resources" prefix)
     * @param loader the resource loader to use
     * @return the Spring {@link Resource}
     */
    public static Resource getResource(final String path, final ResourceLoader loader) {
        final String classPath = toClassPath(path);
        log.debug("Loading resource from classpath: {}", classPath);
        return loader.getResource(classPath);
    }

    /**
     * Loads a classpath resource as an {@link InputStream} using the default resource loader.
     *
     * @param path the resource path (with or without "resources" prefix)
     * @return the resource input stream
     * @throws CouldNotLoadResourceException if the resource cannot be opened
     */
    public static InputStream getResourceFile(final String path) {
        return getResourceFile(path, DEFAULT_LOADER);
    }

    /**
     * Loads a classpath resource as an {@link InputStream} using the provided resource loader.
     *
     * @param path   the resource path (with or without "resources" prefix)
     * @param loader the resource loader to use
     * @return the resource input stream
     * @throws CouldNotLoadResourceException if the resource cannot be opened
     */
    public static InputStream getResourceFile(final String path, final ResourceLoader loader) {
        try {
            return getResource(path, loader).getInputStream();
        } catch (final IOException e) {
            throw new CouldNotLoadResourceException("Could not load resource: " + path, e);
        }
    }

    /**
     * Loads a classpath resource as a UTF-8 string using the default resource loader.
     *
     * @param path the resource path (with or without "resources" prefix)
     * @return the resource content as a string
     * @throws CouldNotLoadResourceException if the resource cannot be read
     */
    public static String getResourceAsString(final String path) {
        return getResourceAsString(path, DEFAULT_LOADER);
    }

    /**
     * Loads a classpath resource as a UTF-8 string using the provided resource loader.
     *
     * @param path   the resource path (with or without "resources" prefix)
     * @param loader the resource loader to use
     * @return the resource content as a string
     * @throws CouldNotLoadResourceException if the resource cannot be read
     */
    public static String getResourceAsString(final String path, final ResourceLoader loader) {
        try (InputStream inputStream = getResourceFile(path, loader)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new CouldNotLoadResourceException("Could not read resource as string: " + path, e);
        }
    }

    private static String toClassPath(final String path) {
        final String normalized = path.startsWith(RESOURCES_PREFIX)
            ? path.substring(RESOURCES_PREFIX.length())
            : path;
        return "classpath:" + normalized;
    }
}
