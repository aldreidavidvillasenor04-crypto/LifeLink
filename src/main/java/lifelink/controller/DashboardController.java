package lifelink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/features")
    public String features() {
        return "features";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
