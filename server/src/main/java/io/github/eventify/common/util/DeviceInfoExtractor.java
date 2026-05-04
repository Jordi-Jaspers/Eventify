package io.github.eventify.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for extracting device information from HTTP requests.
 */
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public final class DeviceInfoExtractor {

    private static final String UNKNOWN_DEVICE = "Unknown device";
    private static final String UNKNOWN_BROWSER = "Unknown";
    private static final String UNKNOWN_OS = "Unknown OS";
    private static final String ANDROID = "Android";
    private static final String WINDOWS = "Windows";
    private static final String LINUX = "Linux";
    private static final String IPV4_LOOPBACK = "127.0.0.1";
    private static final String IPV6_LOOPBACK_COMPRESSED = "::1";
    private static final String IPV6_LOOPBACK_FULL = "0:0:0:0:0:0:0:1";

    private DeviceInfoExtractor() {
        // utility class
    }

    /**
     * Extracts a human-readable device label from a User-Agent string.
     *
     * @param userAgent the raw User-Agent header value
     * @return a label like "Chrome on macOS", or "Unknown device" for null/garbage UA
     */
    public static String extractDeviceInfo(final String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UNKNOWN_DEVICE;
        }

        final String browser = detectBrowser(userAgent);
        final String os = detectOs(userAgent);
        final boolean bothUnknown = UNKNOWN_BROWSER.equals(browser) && UNKNOWN_OS.equals(os);
        return bothUnknown ? UNKNOWN_DEVICE : browser + " on " + os;
    }

    /**
     * Extracts the IP address from the request, preferring X-Forwarded-For.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    public static String extractIpAddress(final HttpServletRequest request) {
        final String remoteAddr = request.getRemoteAddr();
        final String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return normalizeLoopback(forwarded.split(",")[0].trim());
        }
        return normalizeLoopback(remoteAddr);
    }

    /**
     * Normalizes IPv6 loopback addresses (::1 and 0:0:0:0:0:0:0:1) to the canonical IPv4 form 127.0.0.1.
     */
    private static String normalizeLoopback(final String ip) {
        if (IPV6_LOOPBACK_COMPRESSED.equals(ip) || IPV6_LOOPBACK_FULL.equals(ip)) {
            return IPV4_LOOPBACK;
        }
        return ip;
    }

    /**
     * Extracts the raw User-Agent header value.
     *
     * @param request the HTTP request
     * @return the User-Agent string, or empty string if null
     */
    public static String extractUserAgent(final HttpServletRequest request) {
        final String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "";
    }

    private static String detectBrowser(final String userAgent) {
        final String browser;
        if (userAgent.contains("Edg/")) {
            browser = "Edge";
        } else if (userAgent.contains("Chrome/")) {
            browser = "Chrome";
        } else if (userAgent.contains("Firefox/")) {
            browser = "Firefox";
        } else if (userAgent.contains("Safari/")) {
            browser = "Safari";
        } else {
            browser = UNKNOWN_BROWSER;
        }
        return browser;
    }

    private static String detectOs(final String userAgent) {
        final String os;
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            os = "iOS";
        } else if (userAgent.contains(ANDROID)) {
            os = ANDROID;
        } else if (userAgent.contains(WINDOWS)) {
            os = WINDOWS;
        } else if (userAgent.contains("Mac OS X")) {
            os = "macOS";
        } else if (userAgent.contains(LINUX)) {
            os = LINUX;
        } else {
            os = UNKNOWN_OS;
        }
        return os;
    }
}
