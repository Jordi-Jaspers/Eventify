package nl.vodafoneziggo.smc.cucc.common.util;

import nl.vodafoneziggo.smc.cucc.common.exception.CouldNotLoadResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility class for loading resources.
 */
@SuppressWarnings("MultipleStringLiterals")
public final class ResourceLoaderUtil {

    /**
     * The class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoaderUtil.class);

    /**
     * The constructor is private to prevent instantiation.
     */
    private ResourceLoaderUtil() {
        // Empty constructor.
    }

    /**
     * Get the file from the resource folder via a classloader.
     *
     * @param path the resource path.
     * @return the resource file.
     */
    public static File getResourceFile(final String path) {
        try {
            final Resource resource = getResource(path);
            final File file = resource.getFile();
            if (!file.exists()) {
                throw new CouldNotLoadResourceException("File does not exist: " + file.getAbsolutePath());
            }
            LOGGER.debug("Resource file: {}", file);
            return file;
        } catch (final IOException exception) {
            LOGGER.error("Error while retrieving the resource file.", exception);
            throw new CouldNotLoadResourceException(exception);
        }
    }

    /**
     * Get the resource as string.
     *
     * @param path the resource path.
     * @return the resource as string.
     */
    public static String getResourceAsString(final String path) {
        final Resource resource = getResource(path);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (final IOException exception) {
            LOGGER.error("Cannot load resource as string.", exception);
            throw new CouldNotLoadResourceException(exception);
        }
    }

    /**
     * Returns the resource on the classpath.
     *
     * @param path the resource path.
     * @return The resource on the classpath.
     */
    public static Resource getResource(final String path) {
        final String classPath = getClassPath(path);
        final ResourceLoader resourceLoader = new DefaultResourceLoader();
        final Resource resource = resourceLoader.getResource(classPath);

        if (!resource.exists()) {
            throw new CouldNotLoadResourceException("Resource does not exist: " + classPath);
        }
        return resource;
    }

    private static String getClassPath(final String path) {
        return path.contains("resources")
            ? path.split("resources")[1]
            : path;
    }
}
