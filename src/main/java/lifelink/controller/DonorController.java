package lifelink.controller;

import jakarta.servlet.http.HttpSession;
import lifelink.model.*;
import lifelink.repository.*;
import lifelink.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Controller
public class DonorController {
    private final DonorRepository donors; private final UserRepository users; private final EligibilityRepository eligibility;
    private final AppointmentRepository appointments; private final DonationRecordRepository donations; private final BloodRequestRepository requests;
    private final EligibilityService eligibilityService;
    public DonorController(DonorRepository donors, UserRepository users, EligibilityRepository eligibility, AppointmentRepository appointments,
                           DonationRecordRepository donations, BloodRequestRepository requests, EligibilityService eligibilityService) {
        this.donors=donors; this.users=users; this.eligibility=eligibility; this.appointments=appointments; this.donations=donations; this.requests=requests; this.eligibilityService=eligibilityService;
    }
    private boolean ok(HttpSession s){ return "DONOR".equalsIgnoreCase(String.valueOf(s.getAttribute("role"))) && s.getAttribute("userId")!=null; }
    private Donor donor(HttpSession s){ return donors.findByUser_Username(String.valueOf(s.getAttribute("username"))).orElse(null); }

    @GetMapping("/donor/dashboard") public String dashboard(Model m,HttpSession s){ if(!ok(s))return "redirect:/login"; Donor d=donor(s); if(d==null)return "redirect:/donor/complete-profile";
        var e=eligibility.findFirstByDonor_IdOrderBySubmittedAtDesc(d.getId()).orElse(null);
        var ap=appointments.findByDonor_IdOrderByAppointmentAtDesc(d.getId()).stream().filter(a->a.getAppointmentAt()!=null&&a.getAppointmentAt().isAfter(LocalDateTime.now())&&!("CANCELLED".equalsIgnoreCase(a.getStatus())||"DENIED".equalsIgnoreCase(a.getStatus()))).findFirst().orElse(null);
        m.addAttribute("moduleTitle","Donor Dashboard");m.addAttribute("username",s.getAttribute("displayName"));m.addAttribute("adminView",false);m.addAttribute("bloodType",d.getBloodType()==null?"Not set":d.getBloodType());m.addAttribute("eligibility",e==null?"Not assessed":e.getResultStatus());m.addAttribute("score",e==null?null:e.getReadinessScore());m.addAttribute("appointment",ap==null?"No appointment":ap.getAppointmentAt().toString().replace('T',' '));m.addAttribute("appointmentRecord",ap);
        List<DonationRecord> completed=donations.findByDonor_IdOrderByDonationDateDesc(d.getId()).stream().filter(x->"COMPLETED".equals(x.getStatus())).toList();
        LocalDate nextDate=completed.stream().map(DonationRecord::getDonationDate).filter(Objects::nonNull).max(LocalDate::compareTo).map(x->x.plusDays(90)).orElse(null);
        List<DonationRecord> history=donations.findByDonor_IdOrderByDonationDateDesc(d.getId());
        List<BloodRequest> donorRequests=requests.findByRequester_IdOrderByCreatedAtDesc((Long)s.getAttribute("userId"));
        m.addAttribute("nextEligibleDate",nextDate);m.addAttribute("totalDonations",completed.size());m.addAttribute("history",history.stream().limit(4).toList());m.addAttribute("requests",donorRequests.stream().limit(4).toList());
        return "donor-dashboard"; }
    @GetMapping("/donor/complete-profile") public String completeProfile(Model m,HttpSession s){
        if(!ok(s))return "redirect:/login";
        if(donor(s)!=null)return "redirect:/donor/dashboard";
        User user=users.findById((Long)s.getAttribute("userId")).orElse(null);
        if(user==null)return "redirect:/login";
        m.addAttribute("username",user.getUsername());
        m.addAttribute("email",user.getEmail());
        return "complete-profile";
    }
    @PostMapping("/donor/complete-profile") @Transactional
    public String saveCompletedProfile(@RequestParam int age,@RequestParam String email,@RequestParam String bloodType,
            @RequestParam String phone,@RequestParam String gender,@RequestParam String address,HttpSession s){
        if(!ok(s))return "redirect:/login";
        if(donor(s)!=null)return "redirect:/donor/dashboard";
        User user=users.findById((Long)s.getAttribute("userId")).orElse(null);
        if(user==null)return "redirect:/login";
        String cleanEmail=email.trim().toLowerCase(Locale.ROOT),cleanPhone=phone.trim(),cleanAddress=address.trim();
        if(age<1||age>120||!cleanEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")||cleanPhone.length()<7
                ||cleanPhone.length()>30||cleanAddress.isBlank()||!List.of("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(bloodType)
                ||!List.of("Female","Male").contains(gender))return "redirect:/donor/complete-profile?error=validation";
        if(!Objects.equals(user.getEmail(),cleanEmail)&&users.existsByEmail(cleanEmail))return "redirect:/donor/complete-profile?error=email";
        user.setEmail(cleanEmail);users.save(user);
        Donor profile=new Donor();profile.setUser(user);profile.setFullName(((user.getFirstName()==null?"":user.getFirstName())+" "+(user.getLastName()==null?"":user.getLastName())).trim());
        profile.setAge(age);profile.setEmail(cleanEmail);profile.setBloodType(bloodType);profile.setContactNumber(cleanPhone);profile.setGender(gender);profile.setAddress(cleanAddress);
        donors.save(profile);
        return "redirect:/donor/dashboard";
    }
    @PostMapping("/logout") public String logout(HttpSession s){s.invalidate();return "redirect:/login";}

    @GetMapping("/donor/information") public String information(Model m,HttpSession s){if(!ok(s))return "redirect:/login";Donor d=donor(s);if(d==null)return "redirect:/donor/complete-profile";m.addAttribute("donor",d);m.addAttribute("username",s.getAttribute("displayName"));m.addAttribute("adminView",false);m.addAttribute("moduleTitle","Donor Information");return "donor-module";}
    @PostMapping("/donor/profile/update") public String updateProfile(@RequestParam String firstName,@RequestParam String lastName,@RequestParam String username,@RequestParam String phone,HttpSession s){if(!ok(s))return "redirect:/login";Donor d=donor(s);User u=users.findById((Long)s.getAttribute("userId")).orElseThrow();if(!u.getUsername().equals(username)&&users.existsByUsername(username))return "redirect:/donor/information?error=username";u.setFirstName(firstName.trim());u.setLastName(lastName.trim());u.setUsername(username.trim());users.save(u);d.setFullName(firstName.trim()+" "+lastName.trim());d.setContactNumber(phone.trim());donors.save(d);s.setAttribute("username",u.getUsername());s.setAttribute("displayName",d.getFullName());return "redirect:/donor/information?saved";}
    @GetMapping("/donor/profile") public String profile(Model m,HttpSession s){return information(m,s);}

    @GetMapping("/donor/eligibility") public String eligibilityForm(Model m,HttpSession s){if(!ok(s))return "redirect:/login";Donor d=donor(s);if(d==null)return "redirect:/donor/complete-profile";m.addAttribute("username",s.getAttribute("displayName"));m.addAttribute("adminView",false);m.addAttribute("moduleTitle","Eligibility / Status");m.addAttribute("latestAssessment",eligibility.findFirstByDonor_IdOrderBySubmittedAtDesc(d.getId()).orElse(null));m.addAttribute("latestDonation",donations.findByDonor_IdOrderByDonationDateDesc(d.getId()).stream().filter(x->"COMPLETED".equals(x.getStatus())).findFirst().orElse(null));return "assessment";}
    @PostMapping("/donor/eligibility") public String assess(@RequestParam int age,@RequestParam double weightKg,@RequestParam(defaultValue="false") boolean feelingWell,@RequestParam(defaultValue="false") boolean donatedBefore,@RequestParam(required=false) LocalDate lastDonationDate,
       @RequestParam(defaultValue="false") boolean currentIllness,@RequestParam(defaultValue="false") boolean takingMedication,@RequestParam(defaultValue="false") boolean recentSurgery,@RequestParam(defaultValue="false") boolean recentTattooOrPiercing,@RequestParam(defaultValue="false") boolean medicalCondition,@RequestParam(defaultValue="false") boolean previouslyDeferred,@RequestParam(defaultValue="false") boolean recentVaccination,@RequestParam(defaultValue="false") boolean recentDentalProcedure,@RequestParam(defaultValue="false") boolean recentRiskTravel,@RequestParam(defaultValue="false") boolean priorTransfusionOrTransplant,@RequestParam(defaultValue="false") boolean pregnantOrRecentlyGaveBirth,HttpSession s){if(!ok(s))return "redirect:/login";Donor profile=donor(s);if(profile==null)return "redirect:/donor/complete-profile";if(age<1||age>120||!Double.isFinite(weightKg)||weightKg<=0||weightKg>500||(lastDonationDate!=null&&lastDonationDate.isAfter(LocalDate.now())))return "redirect:/donor/eligibility?error=validation";EligibilityAssessment a=new EligibilityAssessment();a.setDonor(profile);a.setAge(age);a.setWeightKg(weightKg);a.setFeelingWell(feelingWell);a.setDonatedBefore(donatedBefore);a.setLastDonationDate(lastDonationDate);a.setCurrentIllness(currentIllness);a.setTakingMedication(takingMedication);a.setRecentSurgery(recentSurgery);a.setRecentTattooOrPiercing(recentTattooOrPiercing);a.setMedicalCondition(medicalCondition);a.setPreviouslyDeferred(previouslyDeferred);a.setRecentVaccination(recentVaccination);a.setRecentDentalProcedure(recentDentalProcedure);a.setRecentRiskTravel(recentRiskTravel);a.setPriorTransfusionOrTransplant(priorTransfusionOrTransplant);a.setPregnantOrRecentlyGaveBirth(pregnantOrRecentlyGaveBirth);eligibilityService.evaluate(a, profile.getGender());return "redirect:/donor/eligibility?submitted";}

    private static final String[][] HOSPITALS={{"Bulacan Medical Center","Malolos","Government"},{"Malolos Provincial Hospital","Malolos","Government"},{"La Consolacion University General Hospital","Plaridel","Private"},{"Meycauayan Doctors Hospital and Medical Center","Meycauayan","Private"},{"Marymount Hospital, Inc.","Meycauayan","Private"},{"St. Paul Hospital Bulacan, Inc.","Bocaue","Private"},{"Mt. Carmel Medical Center","Bocaue","Private"},{"DOH - Joni Villanueva General Hospital","Bocaue","Government"},{"Grace Medical Center","San Jose del Monte","Private"},{"Healthway QualiMed Hospital San Jose Del Monte","San Jose del Monte","Private"},{"Ospital Ng San Jose Del Monte","San Jose del Monte","Government"},{"Dr. Cabuco Hospital","Santa Maria","Private"},{"Mendoza General Hospital","Santa Maria","Private"},{"Sta Maria Hospital","Santa Maria","Private"},{"Pandi District Hospital","Pandi","Government"},{"San Miguel District Hospital","San Miguel","Government"},{"Gregorio del Pilar District Hospital","Bulakan","Government"}};
    @GetMapping({"/donor/schedule","/donor/appointments"}) public String schedule(Model m,HttpSession s){if(!ok(s))return "redirect:/login";Donor d=donor(s);if(d==null)return "redirect:/donor/complete-profile";m.addAttribute("username",s.getAttribute("displayName"));m.addAttribute("adminView",false);m.addAttribute("moduleTitle","Schedule Donation");m.addAttribute("hospitals",HOSPITALS);m.addAttribute("appointments",appointments.findByDonor_IdOrderByAppointmentAtDesc(d.getId()));return "donor-module";}
    @PostMapping("/donor/schedule") public String saveAppointment(@RequestParam String hospitalName,@RequestParam LocalDateTime appointmentAt,HttpSession s){if(!ok(s))return "redirect:/login";Donor d=donor(s);if(d==null)return "redirect:/donor/complete-profile";if(appointmentAt.isBefore(LocalDateTime.now()))return "redirect:/donor/schedule?error=date";LocalDate earliest=donations.findByDonor_IdOrderByDonationDateDesc(d.getId()).stream().filter(x->"COMPLETED".equals(x.getStatus())&&x.getDonationDate()!=null).map(x->x.getDonationDate().plusDays(90)).max(LocalDate::compareTo).orElse(null);if(earliest!=null&&appointmentAt.toLocalDate().isBefore(earliest))return "redirect:/donor/schedule?error=interval";String[] h=Arrays.stream(HOSPITALS).filter(x->x[0].equals(hospitalName)).findFirst().orElse(null);if(h==null)return "redirect:/donor/schedule?error=hospital";Appointment a=new Appointment();a.setDonor(d);a.setHospitalName(h[0]);a.setCity(h[1]);a.setHospitalType(h[2]);a.setAppointmentAt(appointmentAt);a.setStatus("PENDING");appointments.save(a);return "redirect:/donor/appointments?saved";}
    @GetMapping("/donor/history") public String history(Model m,HttpSession s){if(!ok(s))return "redirect:/login";Donor d=donor(s);if(d==null)return "redirect:/donor/complete-profile";m.addAttribute("moduleTitle","Donation History");m.addAttribute("adminView",false);m.addAttribute("username",s.getAttribute("displayName"));m.addAttribute("history",donations.findByDonor_IdOrderByDonationDateDesc(d.getId()));return "donor-module";}

    @GetMapping("/donor/blood-requests") public String bloodRequests(Model m,HttpSession s){if(!ok(s))return "redirect:/login";m.addAttribute("moduleTitle","Blood Requests");m.addAttribute("adminView",false);m.addAttribute("username",s.getAttribute("displayName"));m.addAttribute("requests",requests.findByRequester_IdOrderByCreatedAtDesc((Long)s.getAttribute("userId")));return "donor-module";}
    @PostMapping("/donor/blood-requests") public String createRequest(@RequestParam String requesterName,@RequestParam String requesterPhone,@RequestParam String requesterEmail,@RequestParam String relationshipToPatient,@RequestParam String patientName,@RequestParam(required=false) Integer patientAge,@RequestParam(required=false) String patientBloodType,@RequestParam(defaultValue="false") boolean patientAdmitted,@RequestParam(required=false) String hospitalName,@RequestParam(required=false) String hospitalLocation,@RequestParam(required=false) String wardNumber,@RequestParam String requestedBloodType,@RequestParam int bagsNeeded,@RequestParam(required=false) LocalDateTime neededAt,@RequestParam String reason,@RequestParam String hospitalConfirmation,@RequestParam String transfusionStatus,@RequestParam String specificBloodTypeRequired,@RequestParam String directedDonorsRequired,@RequestParam(defaultValue="false") boolean hasDeadline,@RequestParam(required=false) LocalDateTime deadlineAt,@RequestParam(defaultValue="false") boolean informationConfirmed,@RequestParam(defaultValue="false") boolean dataUseAuthorized,@RequestParam(required=false) MultipartFile supportingDocument,HttpSession s){if(!ok(s))return "redirect:/login";if(!dataUseAuthorized||!informationConfirmed||bagsNeeded<1)return "redirect:/donor/blood-requests?error=required";BloodRequest b=new BloodRequest();b.setRequester(users.findById((Long)s.getAttribute("userId")).orElseThrow());b.setRequesterName(requesterName);b.setRequesterPhone(requesterPhone);b.setRequesterEmail(requesterEmail);b.setRelationshipToPatient(relationshipToPatient);b.setPatientName(patientName);b.setPatientAge(patientAge);b.setPatientBloodType(patientBloodType);b.setPatientAdmitted(patientAdmitted);b.setHospitalName(hospitalName);b.setHospitalLocation(hospitalLocation);b.setWardNumber(wardNumber);b.setRequestedBloodType(requestedBloodType);b.setBagsNeeded(bagsNeeded);b.setNeededAt(neededAt);b.setUrgency(neededAt==null?"NON-URGENT":neededAt.isBefore(LocalDateTime.now().plusHours(24))?"URGENT":"SCHEDULED");b.setReason(reason);b.setHospitalConfirmation(hospitalConfirmation);b.setTransfusionStatus(transfusionStatus);b.setSpecificBloodTypeRequired(specificBloodTypeRequired);b.setDirectedDonorsRequired(directedDonorsRequired);b.setHasDeadline(hasDeadline);b.setDeadlineAt(deadlineAt);b.setInformationConfirmed(true);b.setDataUseAuthorized(true);if("No".equalsIgnoreCase(hospitalConfirmation)||"Pending".equalsIgnoreCase(hospitalConfirmation))b.setStatus("PENDING");if(supportingDocument!=null&&!supportingDocument.isEmpty()){try{b.setSupportingDocument(supportingDocument.getBytes());b.setSupportingDocumentName(supportingDocument.getOriginalFilename());b.setSupportingDocumentProvided(true);}catch(Exception ignored){}}requests.save(b);return "redirect:/donor/blood-requests?submitted";}
}
