ALTER TABLE tenants
    ADD COLUMN slug VARCHAR(100);

CREATE UNIQUE INDEX uk_tenants_slug
    ON tenants (slug);

ALTER TABLE app_users
    ADD COLUMN password_hash VARCHAR(255);

CREATE INDEX idx_app_users_tenant_email
    ON app_users (tenant_id, email);