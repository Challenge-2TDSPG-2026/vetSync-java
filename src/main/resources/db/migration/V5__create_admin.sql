CREATE TABLE TB_ADMIN (
                          id_admin    NUMBER(10)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          nm_admin    VARCHAR2(100) NOT NULL,
                          ds_email    VARCHAR2(150) NOT NULL UNIQUE,
                          ds_senha    VARCHAR2(255) NOT NULL,
                          dt_cadastro DATE          DEFAULT CURRENT_DATE NOT NULL
);