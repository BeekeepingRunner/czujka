package pl.czujka.common;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Fails startup with a clear message when no database password is configured,
 * before anything tries to connect. Runs on the connection details the DataSource
 * is built from, so a password supplied by Testcontainers also counts.
 */
@Component
class DatabasePasswordCheck implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof JdbcConnectionDetails details && !StringUtils.hasText(details.getPassword())) {
            throw new IllegalStateException("Database password is not set. Set the DB_PASSWORD environment variable.");
        }
        return bean;
    }
}
