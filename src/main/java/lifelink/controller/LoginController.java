package lifelink.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import lifelink.model.User;
import lifelink.repository.UserRepository;
import lifelink.service.PasswordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    private final UserRepository users;
    private final PasswordService passwords;
    public LoginController(UserRepository users, PasswordService passwords) { this.users = users; this.passwords = passwords; }

    @GetMapping("/login") public String showLoginPage(@RequestParam(required = false) String registered, Model model) {
        if (registered != null) model.addAttribute("registered", true);
        return "login";
    }

    @PostMapping("/login") public String loginUser(@RequestParam String username, @RequestParam String password,
            Model model, HttpSession session, HttpServletRequest request) {
        String loginName = username.trim();
        User user = users.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase(loginName, loginName).orElse(null);
        if (user == null || !("ADMIN".equalsIgnoreCase(user.getRole()) || "DONOR".equalsIgnoreCase(user.getRole()))
                || !passwords.matches(password, user.getPassword())) {
            model.addAttribute("error", "Invalid username/email or password. Check the credentials you used when registering.");
            model.addAttribute("submittedUsername", loginName);
            return "login";
        }
        if (passwords.needsUpgrade(user.getPassword())) { user.setPassword(passwords.hash(password)); users.save(user); }
        request.changeSessionId();
        session.setAttribute("userId", user.getId()); session.setAttribute("username", user.getUsername());
        session.setAttribute("displayName", ((user.getFirstName() == null ? "" : user.getFirstName()) + " " + (user.getLastName() == null ? "" : user.getLastName())).trim().isEmpty() ? user.getUsername() : (user.getFirstName() + " " + user.getLastName()).trim());
        session.setAttribute("role", user.getRole());
        return "ADMIN".equalsIgnoreCase(user.getRole()) ? "redirect:/admin/dashboard" : "redirect:/donor/dashboard";
    }
}
