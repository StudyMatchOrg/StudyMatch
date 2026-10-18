package pt.upt.studymatch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.dto.ProfileResponse;
import pt.upt.studymatch.service.ProfileService;

@RestController
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/api/students/{studentId}/profile")
    public ProfileResponse profileOf(@PathVariable Long studentId) {
        return profileService.profileOf(studentId);
    }
}
