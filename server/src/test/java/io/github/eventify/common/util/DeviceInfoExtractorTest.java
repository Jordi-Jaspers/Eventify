package io.github.eventify.common.util;

import io.github.eventify.support.UnitTest;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - DeviceInfoExtractor")
public class DeviceInfoExtractorTest extends UnitTest {

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("Should extract Chrome on macOS from Chrome macOS user agent")
    public void extractsChromeOnMacOs() {
        // Given: A Chrome on macOS user agent string
        final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

        // When: Extracting device info
        final String deviceInfo = DeviceInfoExtractor.extractDeviceInfo(userAgent);

        // Then: The label should mention Chrome and Mac
        assertThat(deviceInfo, is(notNullValue()));
        assertThat(deviceInfo, containsStringIgnoringCase("Chrome"));
        assertThat(
            deviceInfo,
            anyOf(
                containsStringIgnoringCase("Mac"),
                containsStringIgnoringCase("macOS")
            )
        );
    }

    @Test
    @DisplayName("Should extract Safari on iOS from iOS Safari user agent")
    public void extractsSafariOnIos() {
        // Given: An iOS Safari user agent string
        final String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) "
            + "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";

        // When: Extracting device info
        final String deviceInfo = DeviceInfoExtractor.extractDeviceInfo(userAgent);

        // Then: The label should mention Safari and iOS or iPhone
        assertThat(deviceInfo, is(notNullValue()));
        assertThat(deviceInfo, containsStringIgnoringCase("Safari"));
        assertThat(
            deviceInfo,
            anyOf(
                containsStringIgnoringCase("iOS"),
                containsStringIgnoringCase("iPhone")
            )
        );
    }

    @Test
    @DisplayName("Should extract Firefox on Linux from Firefox Linux user agent")
    public void extractsFirefoxOnLinux() {
        // Given: A Firefox on Linux user agent string
        final String userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:120.0) Gecko/20100101 Firefox/120.0";

        // When: Extracting device info
        final String deviceInfo = DeviceInfoExtractor.extractDeviceInfo(userAgent);

        // Then: The label should mention Firefox and Linux
        assertThat(deviceInfo, is(notNullValue()));
        assertThat(deviceInfo, containsStringIgnoringCase("Firefox"));
        assertThat(deviceInfo, containsStringIgnoringCase("Linux"));
    }

    @Test
    @DisplayName("Should return a non-null fallback for unknown or garbage user agent")
    public void handlesUnknownUserAgentGracefully() {
        // Given: A garbage user agent string
        final String userAgent = "xyz-garbage-agent/0.0.0";

        // When: Extracting device info
        final String deviceInfo = DeviceInfoExtractor.extractDeviceInfo(userAgent);

        // Then: Should return a non-null, non-empty fallback (e.g. "Unknown device")
        assertThat(deviceInfo, is(notNullValue()));
        assertThat(deviceInfo, is(not(emptyString())));
    }

    @Test
    @DisplayName("Should return a non-null fallback when user agent is null")
    public void handlesNullUserAgentHeader() {
        // Given: A null user agent
        final String userAgent = null;

        // When: Extracting device info
        final String deviceInfo = DeviceInfoExtractor.extractDeviceInfo(userAgent);

        // Then: Should return a non-null fallback without throwing NPE
        assertThat(deviceInfo, is(notNullValue()));
        assertThat(deviceInfo, is(not(emptyString())));
    }

    @Test
    @DisplayName("Should return first IP from X-Forwarded-For header when present")
    public void extractsIpFromXForwardedForHeader() {
        // Given: A request with X-Forwarded-For containing multiple IPs
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4, 5.6.7.8");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // When: Extracting the IP address
        final String ipAddress = DeviceInfoExtractor.extractIpAddress(request);

        // Then: Should return the first IP from X-Forwarded-For
        assertThat(ipAddress, is(equalTo("1.2.3.4")));
    }

    @Test
    @DisplayName("Should return remoteAddr when X-Forwarded-For header is absent")
    public void extractsIpFromRemoteAddrWhenXForwardedForMissing() {
        // Given: A request without X-Forwarded-For header
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.5");

        // When: Extracting the IP address
        final String ipAddress = DeviceInfoExtractor.extractIpAddress(request);

        // Then: Should return the remote address
        assertThat(ipAddress, is(equalTo("10.0.0.5")));
    }

    @Test
    @DisplayName("Should normalize full IPv6 localhost 0:0:0:0:0:0:0:1 to 127.0.0.1")
    public void normalizesFullIpv6LocalhostToIpv4() {
        // Given: A request with full IPv6 localhost as remoteAddr
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");

        // When: Extracting the IP address
        final String ipAddress = DeviceInfoExtractor.extractIpAddress(request);

        // Then: Should return normalized IPv4 localhost
        assertThat(ipAddress, is(equalTo("127.0.0.1")));
    }

    @Test
    @DisplayName("Should normalize compressed IPv6 localhost ::1 to 127.0.0.1")
    public void normalizesCompressedIpv6LocalhostToIpv4() {
        // Given: A request with compressed IPv6 localhost as remoteAddr
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("::1");

        // When: Extracting the IP address
        final String ipAddress = DeviceInfoExtractor.extractIpAddress(request);

        // Then: Should return normalized IPv4 localhost
        assertThat(ipAddress, is(equalTo("127.0.0.1")));
    }

    @Test
    @DisplayName("Should normalize IPv6 localhost in X-Forwarded-For header to 127.0.0.1")
    public void normalizesIpv6LocalhostInXForwardedForHeader() {
        // Given: A request with IPv6 localhost as first IP in X-Forwarded-For
        when(request.getHeader("X-Forwarded-For")).thenReturn("::1, 8.8.8.8");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        // When: Extracting the IP address
        final String ipAddress = DeviceInfoExtractor.extractIpAddress(request);

        // Then: Should return normalized IPv4 localhost (not the proxy IP)
        assertThat(ipAddress, is(equalTo("127.0.0.1")));
    }

    @Test
    @DisplayName("Should pass through real IPv4 addresses unchanged")
    public void passesThroughRealIpv4AddressUnchanged() {
        // Given: A request with a real IPv4 address
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("203.0.113.42");

        // When: Extracting the IP address
        final String ipAddress = DeviceInfoExtractor.extractIpAddress(request);

        // Then: Should return the IPv4 address unchanged
        assertThat(ipAddress, is(equalTo("203.0.113.42")));
    }

    @Test
    @DisplayName("Should pass through non-localhost IPv6 addresses unchanged")
    public void passesThroughNonLocalhostIpv6AddressUnchanged() {
        // Given: A request with a real (non-localhost) IPv6 address
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("2001:db8::1");

        // When: Extracting the IP address
        final String ipAddress = DeviceInfoExtractor.extractIpAddress(request);

        // Then: Should return the IPv6 address unchanged
        assertThat(ipAddress, is(equalTo("2001:db8::1")));
    }
}
