CREATE TABLE TB_DISPONIBILIDADE (
                                    id_disponibilidade NUMBER(10)  GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    id_veterinario     NUMBER(10)  NOT NULL,
                                    nr_dia_semana      NUMBER(1)   NOT NULL,
                                    hr_inicio          VARCHAR2(5) NOT NULL,
                                    hr_fim             VARCHAR2(5) NOT NULL,
                                    CONSTRAINT fk_disp_vet FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario),
                                    CONSTRAINT ck_disp_dia CHECK (nr_dia_semana BETWEEN 1 AND 7)
);

CREATE TABLE TB_BLOQUEIO_AGENDA (
                                    id_bloqueio     NUMBER(10) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    id_veterinario  NUMBER(10) NOT NULL,
                                    dt_inicio       DATE       NOT NULL,
                                    dt_fim          DATE       NOT NULL,
                                    ds_motivo       VARCHAR2(200),
                                    CONSTRAINT fk_bloq_vet FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario)
);