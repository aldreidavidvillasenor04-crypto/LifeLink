package lifelink;

import lifelink.model.User;
import lifelink.model.Donor;
import lifelink.model.EligibilityAssessment;
import lifelink.model.DonationRecord;
import lifelink.model.Appointment;
import lifelink.model.BloodInventory;
import lifelink.repository.AppointmentRepository;
import lifelink.repository.BloodInventoryRepository;
import lifelink.repository.DonationRecordRepository;
import lifelink.repository.EligibilityRepository;
import lifelink.repository.UserRepository;
import lifelink.service.PasswordService;
import lifelink.service.DonorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "lifelink.registration.admin-code=")
@AutoConfigureMockMvc
class PublicPagesAndAuthenticationTests {

    private static MockHttpServletRequestBuilder postWithCsrf(String path, Object... pathVariables) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post(path, pathVariables)
                .with(csrf());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordService passwords;

    @Autowired
    private DonorRepository donors;

    @Autowired
    private EligibilityRepository eligibility;

    @Autowired
    private DonationRecordRepository donations;

    @Autowired
    private AppointmentRepository appointments;

    @Autowired
    private BloodInventoryRepository inventory;

    @Test
    void publicNavigationPagesRenderTheirOwnContent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your Blood Can")));

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("About LifeLink")))
                .andExpect(content().string(containsString("What guides the service")));

        mockMvc.perform(get("/features"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tools for the donation journey")))
                .andExpect(content().string(containsString("Donation history")));

        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contact LifeLink")))
                .andExpect(content().string(containsString("support@lifelink.com")));
    }

    @Test
    void stateChangingRequestsRequireCsrfAndThymeleafFormsIncludeTheToken() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/login").param("username", "csrf-test").param("password", "invalid"))
                .andExpect(status().isForbidden());
    }

    @Test
    void readinessRequiresTheDatabaseAndReturnsNoConnectionDetails() throws Exception {
        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"UP\"")));
    }

    @Test
    @Transactional
    void adminDashboardAndManagementPagesRenderTheSharedRoleAwareShell() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "ADMIN");
        session.setAttribute("displayName", "Dashboard QA");
        session.setAttribute("username", "dashboard-qa");

        for (String path : List.of("/admin/dashboard", "/admin/donors", "/admin/assessment", "/admin/appointments",
                "/admin/donations", "/admin/blood-requests", "/admin/reports", "/admin/profile")) {
            mockMvc.perform(get(path).session(session))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("LifeLink")))
                    .andExpect(content().string(containsString("Workspace")))
                    .andExpect(content().string(containsString("Dashboard QA")))
                    .andExpect(content().string(containsString("/admin/dashboard")));
        }

        mockMvc.perform(get("/admin/blood-requests").session(session))
                .andExpect(content().string(containsString("All requests")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Request blood"))));
    }

    @Test
    @Transactional
    void adminLoginUsesTheAdminDashboardAndCannotUseDonorRoutes() throws Exception {
        String username = "admin-qa-" + UUID.randomUUID().toString().substring(0, 10);
        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(username + "@example.test");
        admin.setPassword(passwords.hash("LifeLink-admin-test-2026"));
        admin.setFirstName("Admin");
        admin.setLastName("QA");
        admin.setRole("ADMIN");
        users.saveAndFlush(admin);

        MockHttpSession anonymousSession = new MockHttpSession();
        String anonymousSessionId = anonymousSession.getId();
        MvcResult login = mockMvc.perform(postWithCsrf("/login").session(anonymousSession).param("username", username)
                        .param("password", "LifeLink-admin-test-2026"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(request().sessionAttribute("role", "ADMIN"))
                .andReturn();
        MockHttpSession adminSession = (MockHttpSession) login.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotEquals(anonymousSessionId, adminSession.getId(), "Login must rotate the session id");
        mockMvc.perform(get("/admin/dashboard").session(adminSession)).andExpect(status().isOk());
        mockMvc.perform(get("/donor/dashboard").session(adminSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @Transactional
    void adminRegistrationIsClosedWhenNoSecretIsConfigured() throws Exception {
        String username = "admin-signup-" + UUID.randomUUID().toString().substring(0, 10);
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("<option value=\"ADMIN\">Administrator</option>"))));
        mockMvc.perform(postWithCsrf("/register").param("firstName", "Admin").param("lastName", "Attempt")
                        .param("username", username).param("accountType", "ADMIN")
                        .param("password", "LifeLink-test-2026").param("confirmPassword", "LifeLink-test-2026")
                        .param("adminCode", "incorrect-admin-code"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin registration is not enabled")));
        org.junit.jupiter.api.Assertions.assertFalse(users.existsByUsername(username));
    }

    @Test
    @Transactional
    void donorWithoutProfileIsRoutedToProfileCompletionAcrossModules() throws Exception {
        User user = new User();
        user.setUsername("legacy-empty-" + UUID.randomUUID().toString().substring(0, 8));
        user.setEmail(user.getUsername() + "@example.test");
        user.setPassword(passwords.hash("LifeLink-test-2026"));
        user.setRole("DONOR");
        users.saveAndFlush(user);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "DONOR");
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("displayName", user.getUsername());

        for (String path : List.of("/donor/dashboard", "/donor/information", "/donor/eligibility",
                "/donor/schedule", "/donor/appointments", "/donor/history")) {
            mockMvc.perform(get(path).session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/donor/complete-profile"));
        }
    }

    @Test
    @Transactional
    void newlyRegisteredDonorCanLogInWithTheRegisteredUsernameAndPassword() throws Exception {
        String username = "qa-" + UUID.randomUUID().toString().substring(0, 12);
        String password = "LifeLink-test-2026";

        mockMvc.perform(postWithCsrf("/register")
                        .param("firstName", "LifeLink")
                        .param("lastName", "Test")
                        .param("username", username)
                        .param("accountType", "DONOR")
                        .param("email", username + "@example.test")
                        .param("age", "28")
                        .param("address", "1 Test Street")
                        .param("bloodType", "O+")
                        .param("phone", "+63 900 000 0000")
                        .param("gender", "Female")
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        MvcResult login = mockMvc.perform(postWithCsrf("/login")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donor/dashboard"))
                .andExpect(request().sessionAttribute("role", "DONOR"))
                .andReturn();

        MockHttpSession donorSession = (MockHttpSession) login.getRequest().getSession(false);
        mockMvc.perform(get("/donor/dashboard").session(donorSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Donor Dashboard")))
                .andExpect(content().string(containsString("Your Blood Requests")))
                .andExpect(content().string(containsString("Recent Donations")))
                .andExpect(content().string(containsString("Schedule a Donation")));

        for (String path : List.of("/donor/information", "/donor/eligibility", "/donor/schedule",
                "/donor/appointments", "/donor/history", "/donor/blood-requests", "/donor/profile")) {
            mockMvc.perform(get(path).session(donorSession))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Workspace")))
                    .andExpect(content().string(containsString("LifeLink")))
                    .andExpect(content().string(containsString("/donor/dashboard")));
        }
        mockMvc.perform(get("/donor/blood-requests").session(donorSession))
                .andExpect(content().string(containsString("Request blood")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("All requests"))));

        mockMvc.perform(postWithCsrf("/login")
                        .param("username", username + "@example.test")
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donor/dashboard"));

        mockMvc.perform(postWithCsrf("/login").param("username", username).param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Invalid username/email or password")));
        mockMvc.perform(postWithCsrf("/logout").session(donorSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @Transactional
    void donorDashboardRendersPopulatedAssessmentAndDonationValues() throws Exception {
        String username = "qa-data-" + UUID.randomUUID().toString().substring(0, 10);
        String password = "LifeLink-test-2026";
        mockMvc.perform(postWithCsrf("/register").param("firstName", "Dashboard").param("lastName", "Data")
                        .param("username", username).param("accountType", "DONOR")
                        .param("email", username + "@example.test").param("age", "28")
                        .param("address", "1 Test Street").param("bloodType", "O+")
                        .param("phone", "+63 900 000 0000").param("gender", "Female")
                        .param("password", password).param("confirmPassword", password))
                .andExpect(status().is3xxRedirection());
        MvcResult login = mockMvc.perform(postWithCsrf("/login").param("username", username).param("password", password))
                .andExpect(status().is3xxRedirection()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        Donor donor = donors.findByUser_Username(username).orElseThrow();

        EligibilityAssessment assessment = new EligibilityAssessment();
        assessment.setDonor(donor);
        assessment.setAge(28);
        assessment.setWeightKg(60);
        assessment.setResultStatus("ELIGIBLE");
        assessment.setReadinessScore(85);
        eligibility.saveAndFlush(assessment);

        DonationRecord donation = new DonationRecord();
        donation.setDonor(donor);
        donation.setDonationDate(java.time.LocalDate.now().minusDays(100));
        donation.setBloodType("O+");
        donation.setBags(1);
        donation.setStatus("COMPLETED");
        donations.saveAndFlush(donation);

        mockMvc.perform(get("/donor/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Preliminary readiness score: 85%")))
                .andExpect(content().string(containsString("Next donation interval begins")))
                .andExpect(content().string(containsString("COMPLETED")));
    }

    @Test
    @Transactional
    void donorAssessmentAndAdminDonationUpdatesPersistUsingExistingRules() throws Exception {
        String username = "qa-modules-" + UUID.randomUUID().toString().substring(0, 10);
        String password = "LifeLink-test-2026";
        mockMvc.perform(postWithCsrf("/register").param("firstName", "Module").param("lastName", "Donor")
                        .param("username", username).param("accountType", "DONOR")
                        .param("email", username + "@example.test").param("age", "28")
                        .param("address", "1 Test Street").param("bloodType", "O+")
                        .param("phone", "+63 900 000 0000").param("gender", "Female")
                        .param("password", password).param("confirmPassword", password))
                .andExpect(status().is3xxRedirection());
        MvcResult login = mockMvc.perform(postWithCsrf("/login").param("username", username).param("password", password))
                .andExpect(status().is3xxRedirection()).andReturn();
        MockHttpSession donorSession = (MockHttpSession) login.getRequest().getSession(false);
        Donor donor = donors.findByUser_Username(username).orElseThrow();

        mockMvc.perform(postWithCsrf("/donor/eligibility").session(donorSession)
                        .param("age", "28").param("weightKg", "60")
                        .param("feelingWell", "true").param("donatedBefore", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donor/eligibility?submitted"));
        EligibilityAssessment assessment = eligibility.findFirstByDonor_IdOrderBySubmittedAtDesc(donor.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("ELIGIBLE", assessment.getResultStatus());

        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("role", "ADMIN");
        adminSession.setAttribute("displayName", "Module QA");
        adminSession.setAttribute("username", "module-admin-qa");
        mockMvc.perform(postWithCsrf("/admin/donations").session(adminSession)
                        .param("donorId", donor.getId().toString())
                        .param("donationDate", java.time.LocalDate.now().toString()).param("bags", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/donations?saved"));
        DonationRecord record = donations.findByDonor_IdOrderByDonationDateDesc(donor.getId()).get(0);
        mockMvc.perform(postWithCsrf("/admin/donations/{id}/status", record.getId()).session(adminSession)
                        .param("status", "COMPLETED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/donations?updated"));
        DonationRecord completed = donations.findByDonor_IdOrderByDonationDateDesc(donor.getId()).get(0);
        org.junit.jupiter.api.Assertions.assertEquals("COMPLETED", completed.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(completed.isInventoryApplied());
        mockMvc.perform(postWithCsrf("/admin/donations/{id}/status", record.getId()).session(adminSession)
                        .param("status", "CANCELLED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/donations?error=status"));
        org.junit.jupiter.api.Assertions.assertEquals("COMPLETED", donations.findById(record.getId()).orElseThrow().getStatus());
        mockMvc.perform(postWithCsrf("/admin/donations/{id}/status", record.getId()).session(adminSession)
                        .param("status", "UNRECOGNIZED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/donations?error=status"));
        org.junit.jupiter.api.Assertions.assertEquals("COMPLETED", donations.findById(record.getId()).orElseThrow().getStatus());
    }

    @Test
    @Transactional
    void donorDashboardRendersForExistingDatabaseDonors() throws Exception {
        String username = "qa-existing-donor-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.test");
        user.setPassword(passwords.hash("LifeLink-test-2026"));
        user.setFirstName("Existing");
        user.setLastName("Donor");
        user.setRole("DONOR");
        users.saveAndFlush(user);

        Donor donor = new Donor();
        donor.setUser(user);
        donor.setFullName("Existing Donor");
        donor.setEmail(user.getEmail());
        donor.setAge(30);
        donor.setBloodType("O+");
        donor.setContactNumber("+63 900 000 0000");
        donor.setGender("Female");
        donor.setAddress("1 Test Street");
        donors.saveAndFlush(donor);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "DONOR");
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", username);
        session.setAttribute("displayName", donor.getFullName());
        mockMvc.perform(get("/donor/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Existing Donor")));
    }

    @Test
    @Transactional
    void donorDashboardHandlesUpcomingAppointmentWithMissingStatus() throws Exception {
        String username = "qa-null-status-" + UUID.randomUUID().toString().substring(0, 10);
        String password = "LifeLink-test-2026";
        mockMvc.perform(postWithCsrf("/register").param("firstName", "Dashboard").param("lastName", "Status")
                        .param("username", username).param("accountType", "DONOR")
                        .param("email", username + "@example.test").param("age", "28")
                        .param("address", "1 Test Street").param("bloodType", "O+")
                        .param("phone", "+63 900 000 0000").param("gender", "Female")
                        .param("password", password).param("confirmPassword", password))
                .andExpect(status().is3xxRedirection());
        MvcResult login = mockMvc.perform(postWithCsrf("/login").param("username", username).param("password", password))
                .andExpect(status().is3xxRedirection()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        Donor donor = donors.findByUser_Username(username).orElseThrow();

        Appointment appointment = new Appointment();
        appointment.setDonor(donor);
        appointment.setAppointmentAt(java.time.LocalDateTime.now().plusDays(2));
        appointment.setStatus(null);
        appointments.saveAndFlush(appointment);

        mockMvc.perform(get("/donor/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Donor Dashboard")));
    }

    @Test
    @Transactional
    void adminReportsRenderDatabaseRecordsAcrossModules() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 10);
        User user = new User();
        user.setUsername("report-" + suffix);
        user.setEmail("report-" + suffix + "@example.test");
        user.setPassword(passwords.hash("LifeLink-test-2026"));
        user.setFirstName("Report");
        user.setLastName("Donor");
        user.setRole("DONOR");
        users.saveAndFlush(user);

        Donor donor = new Donor();
        donor.setUser(user);
        donor.setFullName("Report Donor");
        donor.setAge(34);
        donor.setEmail(user.getEmail());
        donor.setBloodType("AB+");
        donor.setContactNumber("+63 900 111 2222");
        donor.setGender("Female");
        donors.saveAndFlush(donor);

        DonationRecord donation = new DonationRecord();
        donation.setDonor(donor);
        donation.setDonationDate(java.time.LocalDate.of(2026, 2, 3));
        donation.setBloodType("AB+");
        donation.setBags(1);
        donation.setStatus("COMPLETED");
        donations.saveAndFlush(donation);

        EligibilityAssessment assessment = new EligibilityAssessment();
        assessment.setDonor(donor);
        assessment.setAge(34);
        assessment.setWeightKg(62);
        assessment.setResultStatus("NOT ELIGIBLE");
        assessment.setReadinessScore(40);
        assessment.setResultReasons("Recent illness");
        eligibility.saveAndFlush(assessment);

        Appointment appointment = new Appointment();
        appointment.setDonor(donor);
        appointment.setAppointmentAt(java.time.LocalDateTime.of(2026, 3, 4, 10, 30));
        appointment.setHospitalName("LifeLink Test Center");
        appointment.setCity("Manila");
        appointment.setStatus("PENDING");
        appointments.saveAndFlush(appointment);

        BloodInventory stock = new BloodInventory();
        stock.setBloodType("AB+");
        stock.setBags(4);
        inventory.saveAndFlush(stock);

        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("role", "ADMIN");
        adminSession.setAttribute("displayName", "Report QA");
        adminSession.setAttribute("username", "report-admin-qa");
        mockMvc.perform(get("/admin/reports").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Donor ID")))
                .andExpect(content().string(containsString("Report Donor")))
                .andExpect(content().string(containsString("Recent illness")))
                .andExpect(content().string(containsString("LifeLink Test Center")))
                .andExpect(content().string(containsString("Restock required")));

        MockHttpSession donorSession = new MockHttpSession();
        donorSession.setAttribute("role", "DONOR");
        donorSession.setAttribute("userId", user.getId());
        donorSession.setAttribute("username", user.getUsername());
        mockMvc.perform(get("/admin/reports").session(donorSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @Transactional
    void legacyDonorWithoutProfileCanLogInAndCompleteTheirProfile() throws Exception {
        String username = "legacy-" + UUID.randomUUID().toString().substring(0, 12);
        String email = username + "@example.test";
        String password = "LifeLink-test-2026";
        User legacyUser = new User();
        legacyUser.setUsername(username);
        legacyUser.setEmail(email);
        legacyUser.setPassword(passwords.hash(password));
        legacyUser.setFirstName("Legacy");
        legacyUser.setLastName("Donor");
        legacyUser.setRole("DONOR");
        users.saveAndFlush(legacyUser);

        MvcResult login = mockMvc.perform(postWithCsrf("/login").param("username", username).param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donor/dashboard"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(get("/donor/dashboard").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donor/complete-profile"));
        mockMvc.perform(get("/donor/complete-profile").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Complete your donor profile")));
        mockMvc.perform(postWithCsrf("/donor/complete-profile").session(session)
                        .param("age", "28").param("email", email).param("bloodType", "O+")
                        .param("phone", "+63 900 000 0000").param("gender", "Female")
                        .param("address", "1 Test Street"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donor/dashboard"));
        mockMvc.perform(get("/donor/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Donor Dashboard")));
    }
}
