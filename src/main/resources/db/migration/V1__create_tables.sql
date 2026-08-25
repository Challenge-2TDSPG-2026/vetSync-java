-- V1: schema completo do dominio VetSync (tutor, pet, especie, raca,
-- clinica, veterinario, tipo de evento, evento de saude, medicamento,
-- prescricao e log de erros).
--
-- Diferenca em relacao ao script original: TB_TUTOR ganhou ds_senha, e
-- TB_VETERINARIO ganhou ds_email e ds_senha, para que os dois perfis
-- consigam ter login proprio no sistema.

CREATE TABLE TB_LOG_ERROS (
                              id_log         NUMBER(10)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              nm_procedure   VARCHAR2(100),
                              nm_usuario     VARCHAR2(100) DEFAULT CURRENT_USER,
                              dt_ocorrencia  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
                              nr_codigo_erro NUMBER(10),
                              ds_mensagem    VARCHAR2(500)
);

CREATE TABLE TB_TUTOR (
                          id_tutor    NUMBER(10)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          nm_tutor    VARCHAR2(100) NOT NULL,
                          ds_email    VARCHAR2(150) NOT NULL UNIQUE,
                          nr_telefone VARCHAR2(20),
                          ds_cpf      CHAR(11)      NOT NULL UNIQUE,
                          ds_senha    VARCHAR2(255) NOT NULL,
                          dt_cadastro DATE          DEFAULT CURRENT_DATE NOT NULL
);

CREATE TABLE TB_ESPECIE (
                            id_especie NUMBER(5)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            nm_especie VARCHAR2(50) NOT NULL UNIQUE
);

CREATE TABLE TB_RACA (
                         id_raca    NUMBER(5)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         nm_raca    VARCHAR2(80) NOT NULL,
                         id_especie NUMBER(5)    NOT NULL,
                         CONSTRAINT fk_raca_especie FOREIGN KEY (id_especie) REFERENCES TB_ESPECIE(id_especie)
);

CREATE TABLE TB_PET (
                        id_pet        NUMBER(10)   GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        nm_pet        VARCHAR2(80) NOT NULL,
                        dt_nascimento DATE         NOT NULL,
                        ds_sexo       CHAR(1)      CHECK (ds_sexo IN ('M','F')),
                        nr_peso_kg    NUMBER(5,2),
                        id_tutor      NUMBER(10)   NOT NULL,
                        id_raca       NUMBER(5)    NOT NULL,
                        CONSTRAINT fk_pet_tutor FOREIGN KEY (id_tutor) REFERENCES TB_TUTOR(id_tutor),
                        CONSTRAINT fk_pet_raca  FOREIGN KEY (id_raca)  REFERENCES TB_RACA(id_raca)
);

CREATE TABLE TB_CLINICA (
                            id_clinica NUMBER(10)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            nm_clinica VARCHAR2(150) NOT NULL,
                            ds_cnpj    CHAR(14)      NOT NULL UNIQUE,
                            ds_cidade  VARCHAR2(80),
                            ds_uf      CHAR(2)
);

CREATE TABLE TB_VETERINARIO (
                                id_veterinario NUMBER(10)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                nm_veterinario VARCHAR2(100) NOT NULL,
                                nr_crmv        VARCHAR2(20)  NOT NULL UNIQUE,
                                ds_email       VARCHAR2(150) NOT NULL UNIQUE,
                                ds_senha       VARCHAR2(255) NOT NULL,
                                id_clinica     NUMBER(10)    NOT NULL,
                                CONSTRAINT fk_vet_clinica FOREIGN KEY (id_clinica) REFERENCES TB_CLINICA(id_clinica)
);

CREATE TABLE TB_TIPO_EVENTO (
                                id_tipo_evento NUMBER(5)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                nm_tipo_evento VARCHAR2(80) NOT NULL,
                                ds_categoria   VARCHAR2(30) CHECK (ds_categoria IN ('PREVENTIVO','TERAPEUTICO','BEM_ESTAR','EMERGENCIA'))
);

CREATE TABLE TB_EVENTO_SAUDE (
                                 id_evento      NUMBER(10)   GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 id_pet         NUMBER(10)   NOT NULL,
                                 id_tipo_evento NUMBER(5)    NOT NULL,
                                 id_veterinario NUMBER(10),
                                 dt_evento      DATE         NOT NULL,
                                 ds_observacao  VARCHAR2(500),
                                 vl_custo       NUMBER(10,2) DEFAULT 0,
                                 CONSTRAINT fk_ev_pet  FOREIGN KEY (id_pet)         REFERENCES TB_PET(id_pet),
                                 CONSTRAINT fk_ev_tipo FOREIGN KEY (id_tipo_evento) REFERENCES TB_TIPO_EVENTO(id_tipo_evento),
                                 CONSTRAINT fk_ev_vet  FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario)
);

CREATE TABLE TB_MEDICAMENTO (
                                id_medicamento NUMBER(10)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                nm_medicamento VARCHAR2(100) NOT NULL,
                                ds_principio   VARCHAR2(100),
                                vl_preco_ref   NUMBER(10,2)
);

CREATE TABLE TB_PRESCRICAO (
                               id_prescricao  NUMBER(10)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               id_evento      NUMBER(10)    NOT NULL,
                               id_medicamento NUMBER(10)    NOT NULL,
                               ds_posologia   VARCHAR2(200) NOT NULL,
                               dt_inicio      DATE          NOT NULL,
                               dt_fim         DATE,
                               qt_doses_dia   NUMBER(3),
                               CONSTRAINT fk_presc_evento FOREIGN KEY (id_evento)      REFERENCES TB_EVENTO_SAUDE(id_evento),
                               CONSTRAINT fk_presc_med    FOREIGN KEY (id_medicamento) REFERENCES TB_MEDICAMENTO(id_medicamento)
);