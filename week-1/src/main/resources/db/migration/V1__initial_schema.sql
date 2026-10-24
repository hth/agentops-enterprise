CREATE TABLE tenants (
     id UUID PRIMARY KEY,
     name VARCHAR(200) NOT NULL,
     status VARCHAR(30) NOT NULL,
     created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    email VARCHAR(320) NOT NULL,
    role VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    UNIQUE (tenant_id, email)
);

CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    user_id UUID NOT NULL REFERENCES app_users(id),
    title VARCHAR(300),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_conversations_tenant
    ON conversations (tenant_id, created_at DESC);