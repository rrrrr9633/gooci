package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.ProfileService;
import com.gooci.console.web.dto.ProfileView;
import com.gooci.console.web.dto.UpdateMailCommand;
import com.gooci.console.web.dto.UpdateNameCommand;
import com.gooci.console.web.dto.UpdatePasswordCommand;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName ProfileController
 * @Description 工作台个人信息
 * @author GooCi
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/console/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ApiResult<ProfileView> load() {
        return ApiResult.ok(profileService.loadProfile());
    }

    @PostMapping("/name")
    public ApiResult<ProfileView> updateName(@Valid @RequestBody UpdateNameCommand command) {
        return ApiResult.ok(profileService.updateName(command));
    }

    @PostMapping("/mail")
    public ApiResult<ProfileView> updateMail(@Valid @RequestBody UpdateMailCommand command) {
        return ApiResult.ok(profileService.updateMail(command));
    }

    @PostMapping("/password")
    public ApiResult<Void> updatePassword(@Valid @RequestBody UpdatePasswordCommand command) {
        profileService.updatePassword(command);
        return ApiResult.ok(null);
    }
}
