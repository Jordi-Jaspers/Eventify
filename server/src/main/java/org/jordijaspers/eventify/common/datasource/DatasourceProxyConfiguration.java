package org.jordijaspers.eventify.common.datasource;

import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import com.zaxxer.hikari.HikariDataSource;

import static net.ttddyy.dsproxy.support.ProxyDataSourceBuilder.create;

/**
 * Builds a ProxyDataSource with a SLF4JQueryLoggingListener.
 */
@Slf4j
@Configuration
public class DatasourceProxyConfiguration implements BeanPostProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.CloseResource")
    public Object postProcessAfterInitialization(@NonNull final Object bean, @NonNull final String beanName) throws BeansException {
        if (bean instanceof DataSource && !(bean instanceof ProxyDataSource)) {
            final HikariDataSource originalDatasource = (HikariDataSource) bean;
            final SLF4JQueryLoggingListener loggingListener = new SLF4JQueryLoggingListener();
            log.info("Enabling datasource proxy to log queries for datasource named '{}'", originalDatasource.getPoolName());

            final PrettyQueryEntryCreator prettyQueryEntryCreator = new PrettyQueryEntryCreator();
            prettyQueryEntryCreator.setMultiline(true);
            loggingListener.setQueryLogEntryCreator(prettyQueryEntryCreator);

            return create(originalDatasource)
                .name("Datasource Query log")
                .listener(loggingListener)
                .build();
        }
        return bean;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object postProcessBeforeInitialization(@NonNull final Object bean, @NonNull final String beanName) throws BeansException {
        return bean;
    }
}
