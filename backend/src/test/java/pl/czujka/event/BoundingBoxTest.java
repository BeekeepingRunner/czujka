package pl.czujka.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BoundingBoxTest {

    @Test
    void parsesCommaSeparatedCoordinates() {
        assertThat(BoundingBox.parse("14.07, 49.0, 24.15, 54.84"))
                .isEqualTo(new BoundingBox(14.07, 49.0, 24.15, 54.84));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1,2,3", "a,b,c,d", "24,49,14,54", "14,49,200,54"})
    void rejectsInvalidInput(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> BoundingBox.parse(value));
    }
}
