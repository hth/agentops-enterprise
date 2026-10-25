package com.agentops.api;

import com.agentops.config.OidcContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final OidcContext oidcContext;

    public AuthController(OidcContext oidcContext) {
        this.oidcContext = oidcContext;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Map<String, Object> response = new HashMap<>();
        response.put("id", oidcContext.getCurrentUserId());
        response.put("email", oidcContext.getCurrentUserEmail());
        response.put("roles", oidcContext.getCurrentUserRoles());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/platform")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, String>> platformAdminEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Access granted to PLATFORM_ADMIN");
        response.put("user", oidcContext.getCurrentUserEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tenant")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Map<String, String>> tenantAdminEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Access granted to TENANT_ADMIN");
        response.put("user", oidcContext.getCurrentUserEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, String>> employeeEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Access granted to EMPLOYEE");
        response.put("user", oidcContext.getCurrentUserEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-or-tenant")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<Map<String, String>> adminOrTenantEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Access granted to PLATFORM_ADMIN or TENANT_ADMIN");
        response.put("user", oidcContext.getCurrentUserEmail());
        response.put("roles", oidcContext.getCurrentUserRoles().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/claims")
    public ResponseEntity<Map<String, Object>> getClaims() {
        return ResponseEntity.ok(oidcContext.getAllClaims());
    }
}
