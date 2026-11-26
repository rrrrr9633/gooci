package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.CloudDeploymentRegistry;
import com.gooci.console.service.OpsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops/deployments")
public class CloudDeploymentController {
    private final OpsService ops;
    private final CloudDeploymentRegistry registry;

    public CloudDeploymentController(OpsService ops, CloudDeploymentRegistry registry) {
        this.ops = ops;
        this.registry = registry;
    }

    @GetMapping("/availability")
    public ApiResult<Boolean> availability() {
        ops.session();
        return ApiResult.ok(registry.available());
    }

    @GetMapping("/{enterId}")
    public ApiResult<CloudDeploymentRegistry.Deployment> find(@PathVariable String enterId) {
        ops.session();
        return ApiResult.ok(registry.find(enterId));
    }

    @PutMapping("/{enterId}")
    public ApiResult<CloudDeploymentRegistry.Deployment> register(@PathVariable String enterId,
            @Valid @RequestBody Registration registration) {
        ops.session();
        return ApiResult.ok(registry.register(enterId, registration.publicOrigin()));
    }

    public record Registration(@NotBlank @Size(max = 255) String publicOrigin) {}
}
