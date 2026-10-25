package com.agentops.controller;

import com.agentops.domain.identity.AppUser;
import com.agentops.dto.CurrentUserResponse;
import com.agentops.security.CurrentUser;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class MeController {

    @GetMapping("/")
    public void root(HttpServletResponse response) throws IOException {
        response.sendRedirect("/login-success");
    }

    @GetMapping("/login-success")
    public String loginSuccess(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        if (currentUser != null) {
            AppUser user = currentUser.getAppUser();
            model.addAttribute("email", user.getEmail());
            model.addAttribute("role", user.getRole());
            model.addAttribute("status", user.getStatus());
            model.addAttribute("tenantId", user.getTenantId());
        }
        return "login-success";
    }

    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal CurrentUser currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        AppUser user = currentUser.getAppUser();
        return ResponseEntity.ok(new CurrentUserResponse(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        ));
    }
}
