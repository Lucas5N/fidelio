package it.unisa.fidelio.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Integer> {

    // Cerca community per nome
    List<Community> findByNomeContainingIgnoreCase(String nome);

    // Community più popolari (ordinate per numero membri)
    List<Community> findAllByOrderByNumMembriDesc();
}