package com.gestion.interfaces;

import com.gestion.criteria.RecommandationCriteria;
import com.gestion.entities.Recommandation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface de service pour la gestion des recommandations IA
 * Définit les opérations CRUD avancées et algorithmes de recommandation
 */
public interface RecommandationService {
    
    // Opérations CRUD de base
    Recommandation create(Recommandation recommandation);
    Optional<Recommandation> findById(Long id);
    List<Recommandation> findAll();
    List<Recommandation> findAll(String sortBy, String sortOrder);
    List<Recommandation> findByUserId(Long userId);
    /** Top N recommandations par utilisateur (ex: top 5 par Score DESC). API: GET /api/recommandations/user/:userId?contexte=couple */
    List<Recommandation> findTopByUserId(Long userId, String contexte, int limite);
    Recommandation update(Recommandation recommandation);
    boolean delete(Long id);

    // SCRUD : Recherche et tri avancés
    List<Recommandation> search(RecommandationCriteria criteria);
    
    // Opérations de recherche et filtrage avancées
    List<Recommandation> findByScoreMinimum(double scoreMin);
    List<Recommandation> findByAlgorithme(Recommandation.AlgorithmeReco algorithme);
    List<Recommandation> findByDateGenerationBetween(LocalDateTime debut, LocalDateTime fin);
    List<Recommandation> findRecommandationsValides();
    List<Recommandation> findRecommandationsExpirees();
    List<Recommandation> findRecommandationsNonUtilisees();
    List<Recommandation> findRecommandationsPrioritaires();
    
    // Génération de recommandations IA
    List<Recommandation> genererRecommandationsCollaboratives(Long userId, int limite);
    List<Recommandation> genererRecommandationsContentBased(Long userId, int limite);
    List<Recommandation> genererRecommandationsNLP(Long userId, String description, int limite);
    List<Recommandation> genererRecommandationsHybrides(Long userId, int limite);
    List<Recommandation> genererRecommandationsTensorFlow(Long userId, int limite);
    List<Recommandation> genererRecommandationsParClustering(Long userId, int limite);
    
    // Algorithmes de recommandation avancés
    double calculerScoreCollaboratif(Long userId, Long evenementId);
    double calculerScoreContentBased(Long userId, Long evenementId);
    double calculerScoreNLP(Long userId, String description, Long evenementId);
    double calculerScoreHybride(Long userId, Long evenementId);
    List<Long> trouverUtilisateursSimilaires(Long userId, int limite);
    List<Long> trouverEvenementsSimilaires(Long evenementId, int limite);
    
    // Analyse NLP et traitement du langage naturel
    String analyserContexteSocial(String description);
    String extrairePreferences(String texte);
    List<String> extraireMotsCles(String description);
    String genererRaisonPersonnalisee(Long userId, Long evenementId, double score);
    boolean detecterEmotion(String texte);
    
    // Machine Learning et TensorFlow
    void entrainerModeleTensorFlow();
    double predireConversion(Long userId, Long evenementId);
    double predireChurnAbonnement(Long userId);
    void mettreAJourModeleML();
    List<Recommandation> optimiserHyperparametres();
    
    // Web scraping et données externes
    List<Recommandation> scraperPrixEquipements(String query);
    void mettreAJourPrixEnTempsReel();
    String scraperDisponibilites(String urlEquipement);
    boolean validerSourceScraped(String source);
    List<String> getSourcesScrapingActives();
    
    // Personnalisation et bundles
    Recommandation creerBundlePersonnalise(Long userId, Long evenementId, String contexte);
    List<Recommandation> genererBundlesContextuels(Long userId);
    void mettreAJourEquipementsRecommandes(Long id, String contexte);
    List<Recommandation> findRecommandationsAvecBundle(String typeBundle);
    
    // A/B Testing et optimisation
    void creerTestAB(String nomTest, List<Recommandation> groupeA, List<Recommandation> groupeB);
    double mesurerConversionTestAB(String nomTest);
    List<Recommandation> getGroupeTestAB(String nomTest, char groupe);
    void conclureTestAB(String nomTest);
    
    // Analytics et performance
    double calculerTauxClicRecommandations(Long userId);
    double calculerTauxConversionRecommandations();
    List<Recommandation> getTopRecommandationsPerformantes(int limite);
    double calculerScoreMoyenAlgorithme(Recommandation.AlgorithmeReco algorithme);
    List<Recommandation> findRecommandationsLowPerformance();
    
    // Validation et nettoyage
    boolean validerRecommandation(Recommandation recommandation);
    void nettoyerRecommandationsExpirees();
    void supprimerRecommandationsObsolètes();
    boolean verifierCoherenceDonnees();
    
    // Notifications et workflows
    void envoyerNotificationPush(Recommandation recommandation);
    void envoyerRecommandationEmail(Long userId, List<Recommandation> recommandations);
    void programmerNotificationDifferée(Recommandation recommandation, LocalDateTime dateEnvoi);
    void suivreInteractionRecommandation(Long id, String typeInteraction);
    
    // Intégration avec d'autres modules
    List<Recommandation> synchroniserAvecEvenements();
    List<Recommandation> synchroniserAvecParticipations(Long userId);
    boolean integrerAvecTransport(Long recommandationId);
    List<Recommandation> getRecommandationsCrossModule(Long userId);
    
    // Export et API
    String exporterRecommandationsJSON(Long userId);
    List<Recommandation> importerRecommandationsAPI(String urlAPI);
    boolean synchroniserAvecAPIExterne(String endpoint);
    List<Recommandation> getRecommandationsParRegion(String region);

    void marquerCommeUtilisee(Long id);
}
