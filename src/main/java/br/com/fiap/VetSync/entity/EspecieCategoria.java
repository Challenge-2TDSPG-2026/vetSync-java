package br.com.fiap.VetSync.entity;

import com.fasterxml.jackson.annotation.JsonCreator;


public enum EspecieCategoria {
    CAO("Cão"),
    GATO("Gato"),
    AVE("Ave"),
    OUTRO(null);

    private final String nomeOficial;

    EspecieCategoria(String nomeOficial) {
        this.nomeOficial = nomeOficial;
    }

    public String getNomeOficial() {
        return nomeOficial;
    }


    @JsonCreator
    public static EspecieCategoria fromString(String valor) {
        if (valor == null) return null;
        String chave = valor.trim().toLowerCase()
                .replace("ã", "a").replace("õ", "o");
        return switch (chave) {
            case "cao", "cachorro" -> CAO;
            case "gato" -> GATO;
            case "ave", "avenida", "passaro" -> AVE;
            default -> OUTRO;
        };
    }
}