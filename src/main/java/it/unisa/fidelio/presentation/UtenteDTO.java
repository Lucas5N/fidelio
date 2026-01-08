package it.unisa.fidelio.presentation;

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

    // Aggiungi questi due campi che mancavano ma che usi nel Service
    private String nome;
    private String cognome;

    private String dtype;
    private String bio;
    private String immagineProfilo;

    // Conviene usare la minuscola per convenzione Java, Lombok genererà setAmministratore e isAmministratore
    private boolean amministratore;
}