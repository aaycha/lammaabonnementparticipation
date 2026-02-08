/*package com.gestion.interfaces;

import com.gestion.entities.ProgrammeRecommender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface de service pour la gestion des programmes
 * Utilise Stream pour le traitement des données
 */
/*public interface ProgrammeService {
    
    // Opérations CRUD de base
    ProgrammeRecommender create(ProgrammeRecommender programme);
    Optional<ProgrammeRecommender> findById(Long id);
    List<ProgrammeRecommender> findAll();
    List<ProgrammeRecommender> findAll(String sortBy, String sortOrder);
    ProgrammeRecommender update(ProgrammeRecommender programme);
    boolean delete(Long id);
    
    // Recherche par critères
    List<ProgrammeRecommender> findByEventId(Long eventId);
    List<ProgrammeRecommender> findByTitre(String titre);
    List<ProgrammeRecommender> findProgrammesEnCours();
    List<ProgrammeRecommender> findProgrammesTermines();
    List<ProgrammeRecommender> findProgrammesAVenir();
    
    // Recherche temporelle
    List<ProgrammeRecommender> findByDateBetween(LocalDateTime debut, LocalDateTime fin);
    List<ProgrammeRecommender> findByDateDebutAfter(LocalDateTime date);
    List<ProgrammeRecommender> findByDateFinBefore(LocalDateTime date);
    
    // Validation
    boolean validerProgramme(ProgrammeRecommender programme);
    boolean peutEtreSupprime(Long id);
}*/


package com.gestion.interfaces;

import com.gestion.entities.ProgrammeRecommender;

import java.util.List;
import java.util.Optional;

public interface ProgrammeService {

    ProgrammeRecommender create(ProgrammeRecommender reco);

    Optional<ProgrammeRecommender> findById(Long id);

    List<ProgrammeRecommender> findAll();

    List<ProgrammeRecommender> findByUser(Long userId);

    List<ProgrammeRecommender> topRecommandations(Long userId, int limite);

    ProgrammeRecommender update(ProgrammeRecommender reco);

    boolean delete(Long id);

    void marquerCommeUtilisee(Long id);
}

