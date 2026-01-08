package it.unisa.fidelio.presentation;

import lombok.Data;

@Data
public class RegistrazioneRequestDTO {
    private String username;
    private String email;
    private String password;
    private String confermaPassword;

    private String nome;
    private String cognome;

    // NUOVO CAMPO
    private String viaEnumCivico;

    private String dtype;
    private String testataGiornalistica;
    private String casaProduzione;
    private String creditReference;
    private String immagineBase64;
}