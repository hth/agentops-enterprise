package com.agentops.config;

import com.agentops.common.RoleEnum;
import com.agentops.common.TenantStatus;
import com.agentops.common.UserStatus;
import com.agentops.domain.identity.AppUser;
import com.agentops.domain.tenant.Tenant;
import com.agentops.repository.AppUserRepository;
import com.agentops.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

@Configuration
public class DataSeederConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSeederConfig.class);

    // Fixed UUIDs for reproducible seed data
    private static final UUID PLATFORM_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ACME_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID GLOBEX_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private static final UUID PLATFORM_ADMIN_1_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID PLATFORM_ADMIN_2_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");

    private static final UUID ACME_ADMIN_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID ACME_EMPLOYEE_1_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID ACME_EMPLOYEE_2_ID = UUID.fromString("20000000-0000-0000-0000-000000000003");

    private static final UUID GLOBEX_ADMIN_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID GLOBEX_EMPLOYEE_1_ID = UUID.fromString("30000000-0000-0000-0000-000000000002");
    private static final UUID GLOBEX_EMPLOYEE_2_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");

    private static final String SEED_PASSWORD = "password";

    @Bean
    CommandLineRunner seedData(TenantRepository tenantRepository, AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (tenantRepository.count() == 0) {
                log.info("Seeding initial tenants...");
                Tenant platformTenant = new Tenant(PLATFORM_TENANT_ID, "Platform", "platform", TenantStatus.ACTIVE.name());
                Tenant acmeTenant = new Tenant(ACME_TENANT_ID, "Acme Corp", "acme-corp", TenantStatus.ACTIVE.name());
                Tenant globexTenant = new Tenant(GLOBEX_TENANT_ID, "Globex Inc", "globex-inc", TenantStatus.ACTIVE.name());

                tenantRepository.saveAllAndFlush(List.of(platformTenant, acmeTenant, globexTenant));
                log.info("Seeded 3 tenants: Platform, Acme Corp, Globex Inc");
            }

            if (appUserRepository.count() == 0) {
                log.info("Seeding initial users...");
                String encodedPassword = passwordEncoder.encode(SEED_PASSWORD);

                // --- Platform Users (PLATFORM_ADMIN role) ---
                AppUser platformAdmin1 = new AppUser(
                    PLATFORM_ADMIN_1_ID, PLATFORM_TENANT_ID,
                    "admin@agentops.com",
                    encodedPassword,
                    RoleEnum.PLATFORM_ADMIN.name(),
                    UserStatus.ACTIVE.name()
                );
                AppUser platformAdmin2 = new AppUser(
                    PLATFORM_ADMIN_2_ID, PLATFORM_TENANT_ID,
                    "superadmin@agentops.com",
                    encodedPassword,
                    RoleEnum.PLATFORM_ADMIN.name(),
                    UserStatus.ACTIVE.name()
                );

                // --- Acme Corp Users ---
                AppUser acmeAdmin = new AppUser(
                    ACME_ADMIN_ID, ACME_TENANT_ID,
                    "admin@acme.com",
                    encodedPassword,
                    RoleEnum.TENANT_ADMIN.name(),
                    UserStatus.ACTIVE.name()
                );
                AppUser acmeEmployee1 = new AppUser(
                    ACME_EMPLOYEE_1_ID, ACME_TENANT_ID,
                    "alice@acme.com",
                    encodedPassword,
                    RoleEnum.EMPLOYEE.name(),
                    UserStatus.ACTIVE.name()
                );
                AppUser acmeEmployee2 = new AppUser(
                    ACME_EMPLOYEE_2_ID, ACME_TENANT_ID,
                    "bob@acme.com",
                    encodedPassword,
                    RoleEnum.EMPLOYEE.name(),
                    UserStatus.ACTIVE.name()
                );

                // --- Globex Inc Users ---
                AppUser globexAdmin = new AppUser(
                    GLOBEX_ADMIN_ID, GLOBEX_TENANT_ID,
                    "admin@globex.com",
                    encodedPassword,
                    RoleEnum.TENANT_ADMIN.name(),
                    UserStatus.ACTIVE.name()
                );
                AppUser globexEmployee1 = new AppUser(
                    GLOBEX_EMPLOYEE_1_ID, GLOBEX_TENANT_ID,
                    "carol@globex.com",
                    encodedPassword,
                    RoleEnum.EMPLOYEE.name(),
                    UserStatus.ACTIVE.name()
                );
                AppUser globexEmployee2 = new AppUser(
                    GLOBEX_EMPLOYEE_2_ID, GLOBEX_TENANT_ID,
                    "dave@globex.com",
                    encodedPassword,
                    RoleEnum.EMPLOYEE.name(),
                    UserStatus.ACTIVE.name()
                );

                appUserRepository.saveAllAndFlush(List.of(
                    platformAdmin1, platformAdmin2,
                    acmeAdmin, acmeEmployee1, acmeEmployee2,
                    globexAdmin, globexEmployee1, globexEmployee2
                ));
                log.info("Seeded 8 users across 3 tenants (password: {})", SEED_PASSWORD);
            }
            log.info("Data seeding completed.");
        };
    }
}
