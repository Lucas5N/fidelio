package it.unisa.fidelio.presentation;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
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
}