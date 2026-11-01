package lifelink.controller;

import jakarta.servlet.http.HttpSession;
import lifelink.model.*;
import lifelink.repository.*;
import lifelink.service.DonorRepository;
import lifelink.service.PasswordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Controller
public class AdminController {
    private final DonorRepository donors; private final UserRepository users; private final DonationRecordRepository donations;
    private final AppointmentRepository appointments; private final BloodRequestRepository requests; private final BloodInventoryRepository inventory;
    private final EligibilityRepository eligibility; private final PasswordService passwords;
    public AdminController(DonorRepository d,UserRepository u,DonationRecordRepository dr,AppointmentRepository a,BloodRequestRepository br,BloodInventoryRepository bi,EligibilityRepository e,PasswordService p){donors=d;users=u;donations=dr;appointments=a;requests=br;inventory=bi;eligibility=e;passwords=p;}
    private boolean isAdmin(HttpSession s){return "ADMIN".equalsIgnoreCase(String.valueOf(s.getAttribute("role")));}
    private String gate(HttpSession s){return isAdmin(s)?null:"redirect:/login";}
    private void layout(Model m,HttpSession s,String title){m.addAttribute("moduleTitle",title);m.addAttribute("username",s.getAttribute("displayName"));m.addAttribute("adminView",true);}

    @GetMapping("/admin/dashboard") public String dashboard(Model m,HttpSession s){if(!isAdmin(s))return "redirect:/login";layout(m,s,"Admin Dashboard");m.addAttribute("totalDonors",donors.findByArchivedFalseOrderByFullNameAsc().size());m.addAttribute("totalDonations",donations.countByStatus("COMPLETED"));m.addAttribute("appointments",appointments.countByStatusInAndAppointmentAtAfter(List.of("PENDING","APPROVED"),LocalDateTime.now()));m.addAttribute("bloodRequests",requests.countByStatusIn(List.of("PENDING","COMPLETED")));m.addAttribute("inventory",inventory.findAll());m.addAttribute("lowStock",inventory.findAll().stream().filter(i->i.getBags()<=10).toList());m.addAttribute("recentRequests",requests.findAllByOrderByCreatedAtDesc().stream().limit(5).toList());m.addAttribute("upcomingAppointments",appointments.findByAppointmentAtAfterOrderByAppointmentAtAsc(LocalDateTime.now()).stream().limit(5).toList());m.addAttribute("monthlyDonations",donations.findAllByOrderByDonationDateDesc().stream().filter(d->d.getDonationDate()!=null&&d.getDonationDate().getMonth()==LocalDate.now().getMonth()&&d.getDonationDate().getYear()==LocalDate.now().getYear()&&"COMPLETED".equals(d.getStatus())).toList());return "admin-dashboard";}

    @GetMapping("/admin/donors") public String donorList(@RequestParam(required=false,defaultValue="") String q,Model m,HttpSession s){if(!isAdmin(s))return "redirect:/login";layout(m,s,"Donor Information");m.addAttribute("donors",q.isBlank()?donors.findByArchivedFalseOrderByFullNameAsc():donors.searchActive(q));m.addAttribute("query",q);m.addAttribute("archivedDonors",donors.findAll().stream().filter(Donor::isArchived).toList());return "donors";}
    @PostMapping("/admin/donors") @Transactional
    public String addDonor(@RequestParam String firstName, @RequestParam String lastName,
                           @RequestParam String username, @RequestParam String email, @RequestParam int age,
                           @RequestParam String bloodType, @RequestParam String phone,
                           @RequestParam String gender, @RequestParam String address,
                           @RequestParam String password, HttpSession s) {
        if (!isAdmin(s)) return "redirect:/login";

        String cleanFirstName = firstName.trim();
        String cleanLastName = lastName.trim();
        String cleanUsername = username.trim();
        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);
        String cleanPhone = phone.trim();
        String cleanAddress = address.trim();
        Set<String> bloodTypes = Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        boolean validEmail = cleanEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
        boolean validPhone = cleanPhone.matches("[0-9+() .-]{7,20}");

        if (cleanFirstName.isBlank() || cleanLastName.isBlank() || cleanUsername.isBlank() || age < 1 || age > 120
                || cleanAddress.isBlank() || !validEmail || !validPhone
                || !bloodTypes.contains(bloodType) || !Set.of("Female", "Male").contains(gender)
                || password.length() < 8) {
            return "redirect:/admin/donors?error=validation";
        }
        if (users.existsByUsername(cleanUsername)) return "redirect:/admin/donors?error=username";
        if (users.existsByEmail(cleanEmail)) return "redirect:/admin/donors?error=email";

        User user = new User();
        user.setFirstName(cleanFirstName);
        user.setLastName(cleanLastName);
        user.setUsername(cleanUsername);
        user.setEmail(cleanEmail);
        user.setPassword(passwords.hash(password));
        user.setRole("DONOR");
        users.save(user);

        Donor donor = new Donor();
        donor.setUser(user);
        donor.setFullName(cleanFirstName + " " + cleanLastName);
        donor.setAge(age);
        donor.setEmail(cleanEmail);
        donor.setBloodType(bloodType);
        donor.setContactNumber(cleanPhone);
        donor.setGender(gender);
        donor.setAddress(cleanAddress);
        donors.save(donor);
        return "redirect:/admin/donors?saved";
    }
    @PostMapping("/admin/donors/{id}/edit") public String editDonor(@PathVariable Long id,@RequestParam String firstName,@RequestParam String lastName,@RequestParam String username,@RequestParam String bloodType,@RequestParam String phone,@RequestParam String gender,HttpSession s){if(!isAdmin(s))return "redirect:/login";Donor d=donors.findById(id).orElseThrow();User u=d.getUser();u.setFirstName(firstName.trim());u.setLastName(lastName.trim());u.setUsername(username.trim());users.save(u);d.setFullName(firstName.trim()+" "+lastName.trim());d.setBloodType(bloodType);d.setContactNumber(phone);d.setGender(gender);donors.save(d);return "redirect:/admin/donors?updated";}
    @PostMapping("/admin/donors/{id}/archive") public String archive(@PathVariable Long id,HttpSession s){if(!isAdmin(s))return "redirect:/login";Donor d=donors.findById(id).orElseThrow();d.setArchived(true);donors.save(d);return "redirect:/admin/donors?archived";}
    @PostMapping("/admin/donors/{id}/restore") public String restore(@PathVariable Long id,HttpSession s){if(!isAdmin(s))return "redirect:/login";Donor d=donors.findById(id).orElseThrow();d.setArchived(false);donors.save(d);return "redirect:/admin/donors?restored";}

    @GetMapping("/admin/assessment") public String assessments(Model m,HttpSession s){if(!isAdmin(s))return "redirect:/login";layout(m,s,"Eligibility Review");m.addAttribute("assessments",eligibility.findAllByOrderBySubmittedAtDesc());return "assessment";}
    @PostMapping("/admin/assessment/{id}/review") public String review(@PathVariable Long id,@RequestParam String status,HttpSession s){if(!isAdmin(s))return "redirect:/login";if(!List.of("ELIGIBLE","NOT ELIGIBLE","IN PROCESS").contains(status))return "redirect:/admin/assessment";EligibilityAssessment a=eligibility.findById(id).orElseThrow();a.setResultStatus(status);eligibility.save(a);return "redirect:/admin/assessment?updated";}

    @GetMapping("/admin/appointments") public String appointmentList(Model m,HttpSession s){if(!isAdmin(s))return "redirect:/login";layout(m,s,"Appointment Management");m.addAttribute("appointments",appointments.findAllByOrderByAppointmentAtDesc());return "donor-module";}
    @PostMapping("/admin/appointments/{id}/status") public String appointmentStatus(@PathVariable Long id,@RequestParam String status,HttpSession s){if(!isAdmin(s))return "redirect:/login";if(!List.of("PENDING","APPROVED","DENIED","CANCELLED").contains(status))return "redirect:/admin/appointments";Appointment a=appointments.findById(id).orElseThrow();a.setStatus(status);appointments.save(a);return "redirect:/admin/appointments?updated";}
    @PostMapping("/admin/appointments/{id}/reschedule") public String reschedule(@PathVariable Long id,@RequestParam LocalDateTime appointmentAt,HttpSession s){if(!isAdmin(s))return "redirect:/login";Appointment old=appointments.findById(id).orElseThrow();old.setStatus("CANCELLED");appointments.save(old);Appointment n=new Appointment();n.setDonor(old.getDonor());n.setHospitalName(old.getHospitalName());n.setCity(old.getCity());n.setHospitalType(old.getHospitalType());n.setAppointmentAt(appointmentAt);n.setPreviousAppointmentId(old.getId());n.setStatus("PENDING");appointments.save(n);return "redirect:/admin/appointments?rescheduled";}

    @GetMapping("/admin/donations") public String donationsPage(Model m,HttpSession s){if(!isAdmin(s))return "redirect:/login";layout(m,s,"Donation Management");m.addAttribute("donors",donors.findByArchivedFalseOrderByFullNameAsc());m.addAttribute("donations",donations.findAllByOrderByDonationDateDesc());m.addAttribute("appointments",appointments.findAllByOrderByAppointmentAtDesc());return "donation";}
    @PostMapping("/admin/donations") public String recordDonation(@RequestParam Long donorId,@RequestParam LocalDate donationDate,@RequestParam int bags,HttpSession s){if(!isAdmin(s))return "redirect:/login";if(bags<1)return "redirect:/admin/donations?error=bags";Donor d=donors.findById(donorId).orElseThrow();DonationRecord r=new DonationRecord();r.setDonor(d);r.setDonationDate(donationDate);r.setBloodType(d.getBloodType());r.setBags(bags);r.setStatus("PENDING");donations.save(r);return "redirect:/admin/donations?saved";}
    @PostMapping("/admin/donations/{id}/status") @Transactional public String donationStatus(@PathVariable Long id,@RequestParam String status,HttpSession s){if(!isAdmin(s))return "redirect:/login";if(!List.of("PENDING","COMPLETED","CANCELLED").contains(status))return "redirect:/admin/donations?error=status";DonationRecord r=donations.findById(id).orElseThrow();if(r.isInventoryApplied()&& !"COMPLETED".equals(status))return "redirect:/admin/donations?error=status";if("COMPLETED".equals(status)&&!r.isInventoryApplied()){BloodInventory b=inventory.findByBloodType(r.getBloodType()).orElseGet(()->{BloodInventory n=new BloodInventory();n.setBloodType(r.getBloodType());n.setBags(0);return n;});b.setBags(b.getBags()+r.getBags());inventory.save(b);r.setInventoryApplied(true);}r.setStatus(status);donations.save(r);return "redirect:/admin/donations?updated";}
    @PostMapping("/admin/inventory") public String inventory(@RequestParam String bloodType,@RequestParam int bags,HttpSession s){if(!isAdmin(s))return "redirect:/login";if(bags<0||!List.of("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(bloodType))return "redirect:/admin/dashboard?error=stock";BloodInventory b=inventory.findByBloodType(bloodType).orElseGet(()->{BloodInventory x=new BloodInventory();x.setBloodType(bloodType);return x;});b.setBags(bags);inventory.save(b);return "redirect:/admin/dashboard?stockUpdated";}

    @GetMapping("/admin/blood-requests") public String requestsPage(Model m,HttpSession s){if(!isAdmin(s))return "redirect:/login";layout(m,s,"Blood Requests");m.addAttribute("requests",requests.findAllByOrderByCreatedAtDesc());m.addAttribute("inventory",inventory.findAll());return "donor-module";}
    @PostMapping("/admin/blood-requests/{id}/status") @Transactional public String requestStatus(@PathVariable Long id,@RequestParam String status,@RequestParam(required=false) String adminNote,HttpSession s){if(!isAdmin(s))return "redirect:/login";if(!List.of("PENDING","DECLINED","NO AVAILABLE BLOOD","COMPLETED","RECEIVED").contains(status))return "redirect:/admin/blood-requests";BloodRequest r=requests.findById(id).orElseThrow();r.setAdminNote(adminNote);if("RECEIVED".equals(status)&&!r.isInventoryDeducted()){BloodInventory b=inventory.findByBloodType(r.getRequestedBloodType()).orElse(null);if(b==null||b.getBags()<r.getBagsNeeded()){r.setStatus("NO AVAILABLE BLOOD");requests.save(r);return "redirect:/admin/blood-requests?stock";}b.setBags(b.getBags()-r.getBagsNeeded());inventory.save(b);r.setInventoryDeducted(true);}r.setStatus(status);requests.save(r);return "redirect:/admin/blood-requests?updated";}
    @GetMapping("/admin/reports") public String reports(Model m,HttpSession s){if(!isAdmin(s))return "redirect:/login";layout(m,s,"Reports");m.addAttribute("donors",donors.findAll().stream().sorted(Comparator.comparing(Donor::getFullName,Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))).toList());m.addAttribute("donations",donations.findAllByOrderByDonationDateDesc());m.addAttribute("appointments",appointments.findAllByOrderByAppointmentAtDesc());m.addAttribute("assessments",eligibility.findAllByOrderBySubmittedAtDesc());m.addAttribute("inventory",inventory.findAll());return "reports";}
    @GetMapping("/admin/profile") public String profile(Model m,HttpSession s){if(!isAdmin(s))return "redirect:/login";layout(m,s,"Profile");return "donor-module";}
}
