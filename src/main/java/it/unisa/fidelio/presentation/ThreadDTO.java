package it.unisa.fidelio.presentation;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadDTO {
    private Integer id;
    private String titolo;
    private String contenuto;
    private Instant dataCreazione;
    private Integer numRisposte;
    private String autoreUsername;
    private Integer communityId;
    private String communityNome;
}