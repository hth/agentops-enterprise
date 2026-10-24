package com.agentops.week1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@Transactional
class Week1ApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("mydatabase")
            .withUsername("my_db_user")
            .withPassword("my_secure_password");

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testLoginPageIsAvailable() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void testUnauthenticatedUserRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/me").header("Accept", "text/html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }

    @Test
    void testFormLoginSuccess() throws Exception {
        mockMvc.perform(formLogin().user("admin@agentops.com").password("password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/api/me"));
    }

    @Test
    void testFormLoginFailure() throws Exception {
        mockMvc.perform(formLogin().user("admin@agentops.com").password("wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?error"));
    }

    @Test
    void testPlatformAdminAccess() throws Exception {
        mockMvc.perform(get("/me").with(httpBasic("admin@agentops.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@agentops.com"))
                .andExpect(jsonPath("$.role").value("PLATFORM_ADMIN"));

        mockMvc.perform(get("/api/platform").with(httpBasic("admin@agentops.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted to PLATFORM_ADMIN"))
                .andExpect(jsonPath("$.user").value("admin@agentops.com"));

        mockMvc.perform(get("/api/admin-or-tenant").with(httpBasic("admin@agentops.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted to PLATFORM_ADMIN or TENANT_ADMIN"));

        mockMvc.perform(get("/api/tenant").with(httpBasic("admin@agentops.com", "password")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employee").with(httpBasic("admin@agentops.com", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testTenantAdminAccess() throws Exception {
        mockMvc.perform(get("/me").with(httpBasic("admin@acme.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@acme.com"))
                .andExpect(jsonPath("$.role").value("TENANT_ADMIN"));

        mockMvc.perform(get("/api/tenant").with(httpBasic("admin@acme.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted to TENANT_ADMIN"))
                .andExpect(jsonPath("$.user").value("admin@acme.com"));

        mockMvc.perform(get("/api/admin-or-tenant").with(httpBasic("admin@acme.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted to PLATFORM_ADMIN or TENANT_ADMIN"));

        mockMvc.perform(get("/api/platform").with(httpBasic("admin@acme.com", "password")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employee").with(httpBasic("admin@acme.com", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthenticatedApiMeReturns401() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testEmployeeAccess() throws Exception {
        mockMvc.perform(get("/me").with(httpBasic("alice@acme.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@acme.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));

        mockMvc.perform(get("/api/employee").with(httpBasic("alice@acme.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted to EMPLOYEE"))
                .andExpect(jsonPath("$.user").value("alice@acme.com"));

        mockMvc.perform(get("/api/platform").with(httpBasic("alice@acme.com", "password")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tenant").with(httpBasic("alice@acme.com", "password")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin-or-tenant").with(httpBasic("alice@acme.com", "password")))
                .andExpect(status().isForbidden());
    }
}
