package lifelink.controller;

import lifelink.model.Donor;
import lifelink.model.User;
import lifelink.repository.UserRepository;
import lifelink.service.DonorRepository;
import lifelink.service.PasswordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {
    private final UserRepository users;
    private final DonorRepository donors;
    private final PasswordService passwords;
    private final String adminCode;

    public RegistrationController(UserRepository users, DonorRepository donors, PasswordService passwords,
                                  @Value("${lifelink.registration.admin-code:}") String adminCode) {
        this.users = users; this.donors = donors; this.passwords = passwords;
        this.adminCode = adminCode;
    }

    @GetMapping("/register") public String showRegisterPage(Model model) {
        model.addAttribute("adminRegistrationEnabled", !adminCode.isBlank());
        return "register";
    }

    @PostMapping("/register") @Transactional
    public String registerUser(@RequestParam String firstName, @RequestParam String lastName,
            @RequestParam String username, @RequestParam String accountType,
            @RequestParam(required = false) String bloodType, @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender, @RequestParam(required = false) String email,
            @RequestParam(required = false) String address, @RequestParam(required = false) Integer age,
            @RequestParam String password, @RequestParam String confirmPassword,
            @RequestParam(required = false) String adminCode, Model model) {
        firstName = firstName.trim(); lastName = lastName.trim(); username = username.trim();
        email = email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
        address = address == null ? "" : address.trim();
        if (users.existsByUsername(username)) return fail(model, "That username is already taken.");
        if (username.length() < 3 || !username.matches("[A-Za-z0-9_.-]+")) return fail(model, "Use at least 3 letters, numbers, dots, dashes, or underscores in your username.");
        if (!java.util.List.of("DONOR", "ADMIN").contains(accountType)) return fail(model, "Choose a valid account type.");
        if ("ADMIN".equals(accountType) && !matchesAdminCode(adminCode)) return fail(model, "Admin registration is not enabled or the validation code is not valid.");
        if (!password.equals(confirmPassword)) return fail(model, "Passwords do not match.");
        if (password.length() < 8) return fail(model, "Password must contain at least 8 characters.");
        if (firstName.isBlank() || lastName.isBlank()) return fail(model, "Complete all required fields.");
        if ("DONOR".equals(accountType) && (phone == null || phone.isBlank() || email.isBlank() || age == null || address.isBlank())) return fail(model, "Complete all required donor fields.");
        if ("DONOR".equals(accountType) && (age < 1 || age > 120)) return fail(model, "Enter an age between 1 and 120.");
        if ("DONOR".equals(accountType) && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return fail(model, "Enter a valid email address.");
        if (!email.isBlank() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return fail(model, "Enter a valid email address.");
        if (!email.isBlank() && users.existsByEmail(email)) return fail(model, "That email address is already registered.");
        if ("DONOR".equals(accountType) && !phone.trim().matches("[0-9+() .-]{7,30}")) return fail(model, "Enter a valid contact number.");
        if ("DONOR".equals(accountType) && (bloodType == null || !java.util.List.of("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(bloodType))) return fail(model, "Choose a valid blood type.");
        if ("DONOR".equals(accountType) && (gender == null || !java.util.List.of("Female", "Male").contains(gender))) return fail(model, "Choose Female or Male for gender.");

        User user = new User(); user.setUsername(username); user.setEmail(email.isBlank() ? null : email); user.setPassword(passwords.hash(password));
        user.setFirstName(firstName); user.setLastName(lastName); user.setRole(accountType);
        users.save(user);
        if ("DONOR".equals(accountType)) {
            Donor donor = new Donor(); donor.setUser(user); donor.setFullName(firstName + " " + lastName);
            donor.setAge(age); donor.setEmail(email); donor.setAddress(address); donor.setBloodType(bloodType);
            donor.setContactNumber(phone.trim()); donor.setGender(gender); donors.save(donor);
        }
        return "redirect:/login?registered";
    }

    private String fail(Model model, String message) {
        model.addAttribute("error", message);
        model.addAttribute("adminRegistrationEnabled", !adminCode.isBlank());
        return "register";
    }

    private boolean matchesAdminCode(String providedCode) {
        if (adminCode.isBlank() || providedCode == null) return false;
        return java.security.MessageDigest.isEqual(
                adminCode.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                providedCode.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
