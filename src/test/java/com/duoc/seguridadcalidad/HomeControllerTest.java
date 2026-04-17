package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

@SpringBootTest
class HomeControllerTest {

    @Autowired
    private HomeController homeController;

    @Test
    void testHomeRoute() {
        // Ejecutamos el método que carga el dashboard
        Model model = new ConcurrentModel();
        String viewName = homeController.home(model);

        // Verificamos que retorne la vista "home"
        assertEquals("home", viewName);
    }
}
