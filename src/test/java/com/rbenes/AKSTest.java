package com.rbenes;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

public class AKSTest {
    
    // All the used inputs have been manually checked using
    // https://www.numberempire.com/primenumbers.php
    @ParameterizedTest
    @CsvSource(textBlock = """
            5645641, false
            5645657, true
            5799555, false
            15619, true
            5221652, false
            1234187, true
            9584, false
            211, true
            7, true
            9785, false
            65132114, false
            9788677, true
            23311, true
            54881, true
            21448, false
            2147483647, true
            """)
    void testAKS(Long input, boolean expectedPrimality) {

        AKS aks = new AKS();

        assertThat(aks.checkIsPrime(input)).isEqualTo(expectedPrimality);
    }
}
