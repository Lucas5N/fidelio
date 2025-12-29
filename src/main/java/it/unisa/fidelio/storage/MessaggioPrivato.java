package it.unisa.fidelio.storage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "messaggio_privato")
public class MessaggioPrivato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "testo", nullable = false)
    private String testo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Convert(disableConversion = true)
    @Column(name = "data_invio")
    private Instant dataInvio;

    @ColumnDefault("0")
    @Column(name = "letto")
    private Boolean letto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "mittente_id", nullable = false)
    private Utente mittente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Utente destinatario;

}