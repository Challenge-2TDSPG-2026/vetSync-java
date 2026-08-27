-- V2: adiciona controle de estado ao evento de saude, permitindo o
-- fluxo solicitado -> confirmado -> concluido/cancelado entre tutor
-- e veterinario.

ALTER TABLE TB_EVENTO_SAUDE ADD ds_status VARCHAR2(20) DEFAULT 'SOLICITADO' NOT NULL;
ALTER TABLE TB_EVENTO_SAUDE ADD ds_motivo_cancelamento VARCHAR2(300);

ALTER TABLE TB_EVENTO_SAUDE ADD CONSTRAINT ck_evento_status
    CHECK (ds_status IN ('SOLICITADO','CONFIRMADO','CONCLUIDO','CANCELADO'));