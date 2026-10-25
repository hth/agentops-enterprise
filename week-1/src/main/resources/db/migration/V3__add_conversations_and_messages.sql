-- Support foreign keys that enforce tenant ownership.

ALTER TABLE app_users
    ADD CONSTRAINT uk_app_users_id_tenant
        UNIQUE (id, tenant_id);

CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    title VARCHAR(300),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_conversations_tenant
       FOREIGN KEY (tenant_id)
           REFERENCES tenants (id),

    CONSTRAINT fk_conversations_user_tenant
       FOREIGN KEY (user_id, tenant_id)
           REFERENCES app_users (id, tenant_id),

    CONSTRAINT uk_conversations_id_tenant
       UNIQUE (id, tenant_id)
);

CREATE INDEX idx_conversations_tenant_created
    ON conversations (tenant_id, created_at DESC);

CREATE INDEX idx_conversations_user_created
    ON conversations (tenant_id, user_id, created_at DESC);

CREATE TABLE messages (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    conversation_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_messages_conversation_tenant
      FOREIGN KEY (conversation_id, tenant_id)
          REFERENCES conversations (id, tenant_id),

    CONSTRAINT chk_messages_role
      CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM'))
);

CREATE INDEX idx_messages_conversation_created
    ON messages (tenant_id, conversation_id, created_at, id);