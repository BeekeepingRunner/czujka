package pl.czujka.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BoundingBoxTest {

    @Test
    void parsesCoordinatesInMinLonMinLatMaxLonMaxLatOrder() {
        assertThat(BoundingBox.parse("14.07,49.0,24.15,54.84"))
                .isEqualTo(new BoundingBox(14.07, 49.0, 24.15, 54.84));
    }

    @Test
    void ignoresWhitespaceAroundNumbers() {
        assertThat(BoundingBox.parse(" 14.07 , 49.0 ,24.15, 54.84 "))
                .isEqualTo(new BoundingBox(14.07, 49.0, 24.15, 54.84));
    }

    @Test
    void acceptsNegativeCoordinates() {
        assertThat(BoundingBox.parse("-10.5,-20,-5,-1.25"))
                .isEqualTo(new BoundingBox(-10.5, -20, -5, -1.25));
    }

    @Test
    void acceptsTheWholeWorld() {
        assertThat(BoundingBox.parse("-180,-90,180,90"))
                .isEqualTo(new BoundingBox(-180, -90, 180, 90));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1,2,3", "1,2,3,4,5", "14,49,24,54,", ",14,49,24,54"})
    void rejectsWrongNumberOfValues(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> BoundingBox.parse(value))
                .withMessage("bbox must have the form minLon,minLat,maxLon,maxLat");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a,b,c,d", ",,,", "14,49,24,", "14,49,24,54km", "14,49,24,0x10"})
    void rejectsValuesThatAreNotNumbers(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> BoundingBox.parse(value))
                .withMessage("bbox must contain four numbers");
    }

    @ParameterizedTest
    @ValueSource(strings = {"NaN,49,24,54", "14,NaN,24,54", "14,49,Infinity,54", "-Infinity,49,24,54"})
    void rejectsNonFiniteNumbers(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> BoundingBox.parse(value))
                .withMessage("bbox must contain finite numbers");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            min longitude greater than max | 24,49,14,54
            min latitude greater than max  | 14,54,24,49
            zero width                     | 14,49,14,54
            zero height                    | 14,49,24,49
            """)
    void rejectsEmptyOrInvertedBoxes(String description, String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> BoundingBox.parse(value))
                .withMessage("bbox min values must be lower than max values");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-180.1,49,24,54", "14,49,180.1,54", "14,-90.1,24,54", "14,49,24,90.1"})
    void rejectsCoordinatesOutsideWgs84Range(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> BoundingBox.parse(value))
                .withMessage("bbox is outside valid WGS 84 coordinates");
    }
}
