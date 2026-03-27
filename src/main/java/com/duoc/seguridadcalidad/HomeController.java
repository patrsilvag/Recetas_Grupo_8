package com.duoc.seguridadcalidad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.RequestParam; 

@Controller 
public class HomeController { 

     @Value("${backend.url}")
    private String backendUrl;

    @GetMapping("/login")
    public String login(Model model) {
    // Pasamos la URL del backend a la vista para que el JS la use
    model.addAttribute("backendUrl", backendUrl);
    return "login";
}

    @GetMapping("/")
    public String root(Model model) {
        model.addAttribute("name", "Seguridad y Calidad en el Desarrollo");
        return "recetas"; // Esta será la página que TODOS pueden ver
    }

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("name", "Seguridad y Calidad en el Desarrollo");
        return "home"; // Esta será la página PRIVADA (Dashboard de usuario)
    }

        @GetMapping("/buscar")
        public String buscar(
                @RequestParam(name = "nombre", required = false) String nombre,
                Model model) {

            // Enviamos el nombre buscado de vuelta a la vista
            model.addAttribute("terminoBusqueda", nombre);

            return "buscar";
        }

        @GetMapping("/detalle")
        public String detalle(@RequestParam(name = "id", required = false) String id, Model model) {
            // Esto debe devolver el nombre exacto de tu archivo HTML sin la extensión
            return "detalle";
        }
}

