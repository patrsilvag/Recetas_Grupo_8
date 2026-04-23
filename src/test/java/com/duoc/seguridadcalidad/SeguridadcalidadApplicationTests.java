package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SeguridadcalidadApplicationTests {

	@Test
	void contextLoads() {
	}

    @Test
    void mainMethodTest() {
        // Esto asegura que la clase principal se ejecute y sea contabilizada por JaCoCo
        SeguridadcalidadApplication.main(new String[] {});
    }
}
