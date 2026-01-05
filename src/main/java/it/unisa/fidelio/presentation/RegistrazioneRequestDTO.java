package it.unisa.fidelio.presentation;

import lombok.Data;

@Data
public class RegistrazioneRequestDTO {
    private String username;
    private String email;
    private String password;
    private String nome;
    private String cognome;
    private String dtype; // Cinefilo, Critico, Fedele
    private String testataGiornalistica;
    private String casaProduzione;
    private String creditReference;

    // Campo aggiunto per ricevere l'immagine dal frontend in formato Base64
    private String immagineBase64;
}