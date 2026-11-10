package pl.czujka.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;

class DatabasePasswordCheckTest {

    private final DatabasePasswordCheck check = new DatabasePasswordCheck();

    @Test
    void acceptsConnectionDetailsWithAPassword() {
        var details = new Details("secret");

        assertThat(check.postProcessAfterInitialization(details, "jdbcConnectionDetails")).isSameAs(details);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsMissingPassword(String password) {
        assertThatIllegalStateException()
                .isThrownBy(() -> check.postProcessAfterInitialization(new Details(password), "jdbcConnectionDetails"))
                .withMessageContaining("DB_PASSWORD");
    }

    @Test
    void ignoresOtherBeans() {
        var bean = new Object();

        assertThat(check.postProcessAfterInitialization(bean, "other")).isSameAs(bean);
    }

    private record Details(String password) implements JdbcConnectionDetails {

        @Override
        public String getUsername() {
            return "czujka";
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getJdbcUrl() {
            return "jdbc:postgresql://localhost:5432/czujka";
        }
    }
}
