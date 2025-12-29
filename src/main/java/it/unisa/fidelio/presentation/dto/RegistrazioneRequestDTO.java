package it.unisa.fidelio.presentation.dto;

import lombok.Data;

@Data
public class RegistrazioneRequestDTO {
    private String username;
    private String email;
    private String password;
    private String bio;
}