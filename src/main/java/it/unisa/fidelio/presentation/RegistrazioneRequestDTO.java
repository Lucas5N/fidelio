package it.unisa.fidelio.presentation;

import lombok.Data;

@Data
public class RegistrazioneRequestDTO {
    private String username;
    private String email;
    private String password;
    private String bio;
}