package basecamp.taxonomien.springwebserver;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Steuert die About Seite
 * Kann um dynamischen Content erweitert werden
 */
@Controller
public class AboutController {

    @GetMapping("/about")
    public String home(Model model) {
        return "about";
    }

}

