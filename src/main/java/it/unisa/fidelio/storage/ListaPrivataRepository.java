package it.unisa.fidelio.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListaPrivataRepository extends JpaRepository<ListaPrivata, Integer> {

    List<ListaPrivata> findByProprietarioEmail(String email);


    // Trova una lista specifica per proprietario e nome (es. "Film Visti")
    Optional<ListaPrivata> findByProprietarioUsernameAndNome(String username, String nome);

    // Trova le 3 liste speciali in un colpo solo (utile per "i miei film")
    List<ListaPrivata> findByProprietarioUsernameAndNomeIn(
            String username,
            List<String> nomi    // es. List.of("Film Visti", "Da Vedere", "Preferiti")
    );

    // Verifica se una lista esiste già (per evitare duplicati alla creazione)
    boolean existsByProprietarioUsernameAndNome(String username, String nome);

    // Opzionale: tutte le liste pubbliche (per future funzionalità)
    List<ListaPrivata> findByPrivataFalse();
}