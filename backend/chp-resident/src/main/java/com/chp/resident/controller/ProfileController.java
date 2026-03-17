package com.chp.resident.controller;

import com.chp.common.result.Result;
import com.chp.resident.service.ResidentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/resident/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ResidentProfileService profileService;

    /** GET /api/resident/profile */
    @GetMapping
    public Result<Map<String, Object>> getProfile() {
        return Result.success(profileService.getProfile());
    }

    /** PUT /api/resident/profile/password */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        profileService.changePassword(body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }

    /** PUT /api/resident/profile/phone */
    @PutMapping("/phone")
    public Result<Void> changePhone(@RequestBody Map<String, String> body) {
        profileService.changePhone(body.get("password"), body.get("newPhone"));
        return Result.success();
    }

    /** GET /api/resident/profile/emergency-contact */
    @GetMapping("/emergency-contact")
    public Result<Map<String, Object>> getEmergencyContact() {
        return Result.success(profileService.getEmergencyContact());
    }

    /** PUT /api/resident/profile/emergency-contact */
    @PutMapping("/emergency-contact")
    public Result<Void> updateEmergencyContact(@RequestBody Map<String, String> body) {
        profileService.updateEmergencyContact(body.get("name"), body.get("relation"), body.get("phone"));
        return Result.success();
    }
}
