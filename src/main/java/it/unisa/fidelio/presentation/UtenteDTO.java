package it.unisa.fidelio.presentation;  //DA CAMBIARE IN it.unisa.fidelio.presentation.dto

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtenteDTO {
    private Integer id;
    private String username;
    private String email;
    private String dtype;
    private String bio;
    private String immagineProfilo;
    private String livelloAccesso;
}
