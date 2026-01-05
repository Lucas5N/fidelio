package it.unisa.fidelio.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ThreadRepository extends JpaRepository<Thread, Integer> {

    /**
     * Trova tutti i thread appartenenti a una specifica community.
     * Li ordina per data di creazione decrescente (dal più recente al più vecchio).
     *
     * @param communityId L'ID della community
     * @return Lista di Thread
     */
    List<Thread> findByCommunityIdOrderByDataCreazioneDesc(Integer communityId);

    /**
     * Trova tutti i thread scritti da un determinato autore.
     * Utile per la pagina "I miei post" nel profilo utente.
     *
     * @param autoreId L'ID dell'utente
     * @return Lista di Thread
     */
    List<Thread> findByAutoreId(Integer autoreId);
}