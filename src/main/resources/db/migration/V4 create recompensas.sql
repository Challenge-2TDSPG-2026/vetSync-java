

ALTER TABLE TB_TIPO_EVENTO ADD nr_pontos NUMBER(5) DEFAULT 0 NOT NULL;

CREATE TABLE TB_RECOMPENSA (
                               id_recompensa   NUMBER(10)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               nm_recompensa   VARCHAR2(150) NOT NULL,
                               ds_descricao    VARCHAR2(500),
                               nr_custo_pontos NUMBER(10)    NOT NULL,
                               ds_tipo         VARCHAR2(20)  NOT NULL,
                               fl_ativo        NUMBER(1)     DEFAULT 1 NOT NULL,
                               CONSTRAINT ck_recompensa_tipo CHECK (ds_tipo IN ('PRODUTO','CUPOM_DESCONTO')),
                               CONSTRAINT ck_recompensa_custo CHECK (nr_custo_pontos > 0)
);

CREATE TABLE TB_RESGATE (
                            id_resgate                NUMBER(10)  GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            id_tutor                  NUMBER(10)  NOT NULL,
                            id_recompensa              NUMBER(10)  NOT NULL,
                            dt_resgate                 TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
                            id_veterinario_validador   NUMBER(10),
                            ds_status                  VARCHAR2(20) DEFAULT 'PENDENTE' NOT NULL,
                            CONSTRAINT fk_resgate_tutor FOREIGN KEY (id_tutor) REFERENCES TB_TUTOR(id_tutor),
                            CONSTRAINT fk_resgate_recompensa FOREIGN KEY (id_recompensa) REFERENCES TB_RECOMPENSA(id_recompensa),
                            CONSTRAINT fk_resgate_vet FOREIGN KEY (id_veterinario_validador) REFERENCES TB_VETERINARIO(id_veterinario),
                            CONSTRAINT ck_resgate_status CHECK (ds_status IN ('PENDENTE','VALIDADO','NEGADO'))
);