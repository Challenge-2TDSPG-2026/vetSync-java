

CREATE TABLE TB_PLANO_TRATAMENTO (
                                     id_plano        NUMBER(10)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     id_pet          NUMBER(10)    NOT NULL,
                                     id_veterinario  NUMBER(10)    NOT NULL,
                                     nr_pontos_bonus NUMBER(10)    DEFAULT 0 NOT NULL,
                                     ds_status       VARCHAR2(20)  DEFAULT 'EM_ANDAMENTO' NOT NULL,
                                     dt_criacao      DATE          DEFAULT CURRENT_DATE NOT NULL,
                                     CONSTRAINT fk_plano_pet FOREIGN KEY (id_pet) REFERENCES TB_PET(id_pet),
                                     CONSTRAINT fk_plano_veterinario FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario),
                                     CONSTRAINT ck_plano_status CHECK (ds_status IN ('EM_ANDAMENTO', 'CONCLUIDO', 'QUEBRADO'))
);

CREATE TABLE TB_PLANO_ITEM (
                               id_item        NUMBER(10)   GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               id_plano       NUMBER(10)   NOT NULL,
                               nr_ordem       NUMBER(3)    NOT NULL,
                               id_tipo_evento NUMBER(10)   NOT NULL,
                               id_evento      NUMBER(10),
                               ds_status      VARCHAR2(20) DEFAULT 'PENDENTE' NOT NULL,
                               CONSTRAINT fk_item_plano FOREIGN KEY (id_plano) REFERENCES TB_PLANO_TRATAMENTO(id_plano),
                               CONSTRAINT fk_item_tipo_evento FOREIGN KEY (id_tipo_evento) REFERENCES TB_TIPO_EVENTO(id_tipo_evento),
                               CONSTRAINT fk_item_evento FOREIGN KEY (id_evento) REFERENCES TB_EVENTO_SAUDE(id_evento),
                               CONSTRAINT uk_item_plano_ordem UNIQUE (id_plano, nr_ordem),
                               CONSTRAINT uk_item_evento UNIQUE (id_evento),
                               CONSTRAINT ck_item_status CHECK (ds_status IN ('PENDENTE', 'AGENDADO', 'CONCLUIDO', 'QUEBRADO'))
);


ALTER TABLE TB_LANCAMENTO_PONTOS MODIFY (id_evento NULL);

ALTER TABLE TB_LANCAMENTO_PONTOS ADD id_plano_tratamento NUMBER(10);

ALTER TABLE TB_LANCAMENTO_PONTOS ADD CONSTRAINT fk_lancamento_plano
    FOREIGN KEY (id_plano_tratamento) REFERENCES TB_PLANO_TRATAMENTO(id_plano);

ALTER TABLE TB_LANCAMENTO_PONTOS ADD CONSTRAINT uk_lancamento_plano UNIQUE (id_plano_tratamento);

ALTER TABLE TB_LANCAMENTO_PONTOS ADD CONSTRAINT ck_lancamento_origem CHECK (
    (id_evento IS NOT NULL AND id_plano_tratamento IS NULL)
        OR
    (id_evento IS NULL AND id_plano_tratamento IS NOT NULL)
    );