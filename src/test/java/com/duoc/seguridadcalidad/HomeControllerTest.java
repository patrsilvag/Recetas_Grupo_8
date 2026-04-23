package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class HomeControllerTest {

    @Autowired
    private HomeController homeController;

    @Test
    void testLoginRoute() {
        Model model = new ConcurrentModel();
        String viewName = homeController.login(model);
        assertEquals("login", viewName);
    }

    @Test
    void testRootRoute() {
        String viewName = homeController.root();
        assertEquals("recetas", viewName);
    }

    @Test
    void testHomeRoute() {
        Model model = new ConcurrentModel();
        String viewName = homeController.home(model);
        assertEquals("home", viewName);
    }

    @Test
    void testBuscarRoute() {
        Model model = new ConcurrentModel();
        String viewName = homeController.buscar("pasta", model);
        assertEquals("buscar", viewName);
        assertEquals("pasta", model.getAttribute("terminoBusqueda"));
    }

    @Test
    void testDetalleRoute() {
        Model model = new ConcurrentModel();
        String viewName = homeController.detalle("1", model);
        assertEquals("detalle", viewName);
    }

    @Test
    void testRegistroRoute() {
        Model model = new ConcurrentModel();
        String viewName = homeController.registroPrivado(model);
        assertEquals("registro", viewName);
    }

    @Test
    void testCrearRecetaRoute() {
        Model model = new ConcurrentModel();
        String viewName = homeController.crearReceta(model);
        assertEquals("crear-receta", viewName);
    }

    @Test
    void testAdminUsuariosRoute() {
        Model model = new ConcurrentModel();
        String viewName = homeController.adminUsuarios(model);
        assertEquals("admin-usuarios", viewName);
    }
}
