package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.CloudLaunchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cloud-launch")
public class CloudLaunchController {
    private final CloudLaunchService launches;

    public CloudLaunchController(CloudLaunchService launches) {
        this.launches = launches;
    }

    @PostMapping("/issue")
    public ResponseEntity<ApiResult<CloudLaunchService.Launch>> issue(@Valid @RequestBody LaunchRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ApiResult.ok(launches.issue(request.appCode())));
    }

    @PostMapping("/redeem")
    public ResponseEntity<ApiResult<CloudLaunchService.Identity>> redeem(@Valid @RequestBody RedeemRequest request,
            @RequestHeader(name = "X-Gooci-Deployment-Credential", required = false) String credential) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ApiResult.ok(launches.redeem(request.deploymentId(), credential, request.code())));
    }

    public record LaunchRequest(@NotBlank @Size(max = 50) String appCode) {}
    public record RedeemRequest(@NotBlank @Size(max = 36) String deploymentId,
            @NotBlank @Size(max = 43) String code) {}
}
