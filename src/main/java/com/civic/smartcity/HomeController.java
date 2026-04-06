package com.civic.smartcity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/dashboard.html";
    }

    @GetMapping("/submit")
    public String submit() {
        return "redirect:/submit.html";
    }

    @GetMapping("/mygrievances")
    public String mygrievances() {
        return "redirect:/mygrievances.html";
    }
}
