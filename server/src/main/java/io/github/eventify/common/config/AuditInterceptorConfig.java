package io.github.eventify.common.config;

import io.github.eventify.api.admin.audit.model.interceptor.AdminAuditInterceptor;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static io.github.eventify.api.Paths.ADMIN_PATH;
import static io.github.eventify.api.Paths.WILDCARD_PART;

/**
 * Registers the admin audit interceptor for all admin endpoints.
 */
@Configuration
@RequiredArgsConstructor
public class AuditInterceptorConfig implements WebMvcConfigurer {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuditInterceptor(eventPublisher))
            .addPathPatterns(ADMIN_PATH + WILDCARD_PART);
    }
}
