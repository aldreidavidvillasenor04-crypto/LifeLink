
package lifelink.controller;

import lifelink.model.Donor;
import lifelink.model.User;
import lifelink.repository.UserRepository;
import lifelink.service.DonorRepository;
import lifelink.service.PasswordService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    private final UserRepository users;
    private final DonorRepository donors;
    private final PasswordService passwords;

    public RegistrationController(
            UserRepository users,
            DonorRepository donors,
            PasswordService passwords) {

        this.users = users;
        this.donors = donors;
        this.passwords = passwords;
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    @Transactional
    public String registerUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String username,
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Integer age,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        firstName = firstName.trim();
        lastName = lastName.trim();

        username = username.trim().replaceAll("\\s+", " ");

        email = email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);

        address = address == null ? "" : address.trim();

        // Validate username / normal full name
        if (username.length() < 3 || username.length() > 60 ||
                !username.matches("[\\p{L}\\p{M}][\\p{L}\\p{M} .'-]*")) {
            return fail(model, "Enter a valid name for your username.");
        }

        if (users.existsByUsername(username)) {
            return fail(model, "That username is already taken. Please use another name.");
        }

        // Validate first and last name
        if (firstName.isBlank() || lastName.isBlank()) {
            return fail(model, "Complete all required name fields.");
        }

        // Validate password
        if (!password.equals(confirmPassword)) {
            return fail(model, "Passwords do not match.");
        }

        if (password.length() < 8) {
            return fail(model, "Password must contain at least 8 characters.");
        }

        // Validate required donor information
        if (phone == null || phone.isBlank() ||
                email.isBlank() || age == null || address.isBlank()) {
            return fail(model, "Complete all required donor fields.");
        }

        if (age < 1 || age > 120) {
            return fail(model, "Enter an age between 1 and 120.");
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return fail(model, "Enter a valid email address.");
        }

        if (users.existsByEmail(email)) {
            return fail(model, "That email address is already registered.");
        }

        if (!phone.trim().matches("[0-9+() .-]{7,30}")) {
            return fail(model, "Enter a valid contact number.");
        }

        if (bloodType == null ||
                !java.util.List.of(
                        "A+", "A-", "B+", "B-",
                        "AB+", "AB-", "O+", "O-").contains(bloodType)) {
            return fail(model, "Choose a valid blood type.");
        }

        if (gender == null ||
                !java.util.List.of("Female", "Male").contains(gender)) {
            return fail(model, "Choose Female or Male for gender.");
        }

        // Create ordinary donor account
        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwords.hash(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole("DONOR");

        users.save(user);

        // Create donor profile
        Donor donor = new Donor();

        donor.setUser(user);
        donor.setFullName(firstName + " " + lastName);
        donor.setAge(age);
        donor.setEmail(email);
        donor.setAddress(address);
        donor.setBloodType(bloodType);
        donor.setContactNumber(phone.trim());
        donor.setGender(gender);

        donors.save(donor);

        return "redirect:/login?registered";
    }

    private String fail(Model model, String message) {
        model.addAttribute("error", message);
        return "register";
    }
}
