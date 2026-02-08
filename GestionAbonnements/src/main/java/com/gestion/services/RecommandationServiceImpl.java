package com.gestion.services;

import com.gestion.criteria.RecommandationCriteria;
import com.gestion.entities.Recommandation;
import com.gestion.interfaces.RecommandationService;
import com.gestion.tools.MyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implémentation du service de gestion des recommandations IA
 * Utilise une approche Stream pour le traitement des données
 */
public class RecommandationServiceImpl implements RecommandationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommandationServiceImpl.class);
    private final MyConnection dbConnection;

    public RecommandationServiceImpl() {
        this.dbConnection = MyConnection.getInstance();
    }

    @Override
    public Recommandation create(Recommandation recommandation) {
        if (!validerRecommandation(recommandation)) {
            throw new IllegalArgumentException("Recommandation invalide");
        }

        String sql = "INSERT INTO recommandations (user_id, evenement_suggere_id, score, raison, " +
                    "algorithme_used, equipement_bundle, source_scraped, date_generation, date_expiration, est_utilisee) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, recommandation.getUserId());
            pstmt.setLong(2, recommandation.getEvenementSuggereId());
            pstmt.setDouble(3, recommandation.getScore());
            pstmt.setString(4, recommandation.getRaison());
            pstmt.setString(5, recommandation.getAlgorithmeUsed().name());
            pstmt.setString(6, convertEquipementBundleToString(recommandation.getEquipementBundle()));
            pstmt.setString(7, recommandation.getSourceScraped());
            pstmt.setTimestamp(8, Timestamp.valueOf(recommandation.getDateGeneration()));
            pstmt.setTimestamp(9, Timestamp.valueOf(recommandation.getDateExpiration()));
            pstmt.setBoolean(10, recommandation.isEstUtilisee());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Création de recommandation échouée, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    recommandation.setId(generatedKeys.getLong(1));
                    logger.info("Recommandation créée avec succès: ID {}", recommandation.getId());
                    
                    // Envoyer notification si score élevé
                    if (recommandation.estPrioritaire()) {
                        envoyerNotificationPush(recommandation);
                    }
                    
                    return recommandation;
                } else {
                    throw new SQLException("Création de recommandation échouée, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de la recommandation: {}", e.getMessage());
            throw new RuntimeException("Impossible de créer la recommandation", e);
        }
    }

    @Override
    public Optional<Recommandation> findById(Long id) {
        String sql = "SELECT * FROM recommandations WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRecommandation(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de recommandation par ID {}: {}", id, e.getMessage());
        }
        
        return Optional.empty();
    }

    @Override
    public List<Recommandation> findAll() {
        return findAll("dateGeneration", "DESC");
    }

    @Override
    public List<Recommandation> findAll(String sortBy, String sortOrder) {
        List<Recommandation> list = new ArrayList<>();
        String sql = "SELECT * FROM recommandations";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToRecommandation(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de toutes les recommandations: {}", e.getMessage());
            return list;
        }
        return sortRecommandations(list, sortBy != null ? sortBy : "dateGeneration", sortOrder != null ? sortOrder : "DESC");
    }

    @Override
    public List<Recommandation> findTopByUserId(Long userId, String contexte, int limite) {
        List<Recommandation> list = findByUserId(userId).stream()
                .filter(r -> r.getScore() >= 0.5)
                .filter(r -> r.getDateExpiration() == null || r.getDateExpiration().isAfter(LocalDateTime.now()))
                .filter(r -> !r.isEstUtilisee())
                .filter(r -> contexte == null || contexte.isBlank() || (r.getEquipementBundle() != null && r.getEquipementBundle().keySet().stream()
                        .anyMatch(k -> k.toLowerCase().contains(contexte.toLowerCase()))))
                .sorted(Comparator.comparingDouble(Recommandation::getScore).reversed())
                .limit(limite > 0 ? limite : 5)
                .toList();
        return list;
    }

    @Override
    public List<Recommandation> search(RecommandationCriteria criteria) {
        if (criteria == null) return findAll();
        List<Recommandation> list = criteria.getUserId() != null ? findByUserId(criteria.getUserId()) : findAll();
        Stream<Recommandation> stream = list.stream();
        if (criteria.getScoreMinimum() != null) {
            stream = stream.filter(r -> r.getScore() >= criteria.getScoreMinimum());
        }
        if (criteria.getAlgorithme() != null) {
            stream = stream.filter(r -> r.getAlgorithmeUsed() == criteria.getAlgorithme());
        }
        if (Boolean.TRUE.equals(criteria.getValidesSeulement())) {
            stream = stream.filter(Recommandation::estValide);
        }
        if (criteria.getContexte() != null && !criteria.getContexte().isBlank()) {
            String ctx = criteria.getContexte().toLowerCase();
            stream = stream.filter(r -> r.getEquipementBundle() != null && r.getEquipementBundle().keySet().stream()
                    .anyMatch(k -> k.toLowerCase().contains(ctx)));
        }
        list = stream.toList();
        list = sortRecommandations(list, criteria.getSortBy(), criteria.getSortOrder());
        if (criteria.getLimite() != null && criteria.getLimite() > 0) {
            list = list.stream().limit(criteria.getLimite()).toList();
        }
        return list;
    }

    private List<Recommandation> sortRecommandations(List<Recommandation> list, String sortBy, String sortOrder) {
        Comparator<Recommandation> cmp = switch (sortBy != null ? sortBy.toLowerCase() : "score") {
            case "dategeneration" -> Comparator.comparing(Recommandation::getDateGeneration, Comparator.nullsLast(Comparator.naturalOrder()));
            case "dateexpiration" -> Comparator.comparing(Recommandation::getDateExpiration, Comparator.nullsLast(Comparator.naturalOrder()));
            case "algorithme" -> Comparator.comparing(r -> r.getAlgorithmeUsed().name());
            default -> Comparator.comparingDouble(Recommandation::getScore);
        };
        if ("DESC".equalsIgnoreCase(sortOrder)) cmp = cmp.reversed();
        return list.stream().sorted(cmp).toList();
    }

    @Override
    public List<Recommandation> findByUserId(Long userId) {
        String sql = "SELECT * FROM recommandations WHERE user_id = ? ORDER BY score DESC, date_generation DESC";
        List<Recommandation> recommandations = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    recommandations.add(mapResultSetToRecommandation(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de recommandations pour l'utilisateur {}: {}", userId, e.getMessage());
        }
        
        return recommandations;
    }

    @Override
    public List<Recommandation> findByScoreMinimum(double scoreMin) {
        return findAll().stream()
                .filter(r -> r.getScore() >= scoreMin)
                .collect(Collectors.toList());
    }

    @Override
    public List<Recommandation> findRecommandationsValides() {
        return findAll().stream()
                .filter(Recommandation::estValide)
                .collect(Collectors.toList());
    }

    @Override
    public List<Recommandation> findRecommandationsPrioritaires() {
        return findAll().stream()
                .filter(Recommandation::estPrioritaire)
                .collect(Collectors.toList());
    }

    @Override
    public List<Recommandation> genererRecommandationsCollaboratives(Long userId, int limite) {
        logger.info("Génération de recommandations collaboratives pour l'utilisateur {}", userId);
        
        List<Recommandation> recommandations = new ArrayList<>();
        
        try {
            // Trouver des utilisateurs similaires
            List<Long> utilisateursSimilaires = trouverUtilisateursSimilaires(userId, 10);
            
            // Pour chaque utilisateur similaire, trouver leurs participations
            for (Long similarUser : utilisateursSimilaires) {
                List<Long> evenementsSimilaires = getEvenementsUtilisateur(similarUser);
                
                for (Long evenementId : evenementsSimilaires) {
                    double score = calculerScoreCollaboratif(userId, evenementId);
                    
                    if (score > 0.5) { // Seuil minimum
                        String raison = genererRaisonCollaborative(userId, evenementId, score);
                        Recommandation reco = new Recommandation(
                            userId, evenementId, score, raison, 
                            Recommandation.AlgorithmeReco.COLLABORATIVE
                        );
                        recommandations.add(reco);
                    }
                }
            }
            
            // Limiter et trier par score
            return recommandations.stream()
                    .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                    .limit(limite)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de recommandations collaboratives", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Recommandation> genererRecommandationsNLP(Long userId, String description, int limite) {
        logger.info("Génération de recommandations NLP pour l'utilisateur {} avec description: {}", userId, description);
        
        List<Recommandation> recommandations = new ArrayList<>();
        
        try {
            // Analyse NLP de la description
            String contexteSocial = analyserContexteSocial(description);
            List<String> motsCles = extraireMotsCles(description);
            boolean emotionPositive = detecterEmotion(description);
            
            // Trouver des événements correspondants
            List<Long> evenementsCandidats = trouverEvenementsParMotsCles(motsCles);
            
            for (Long evenementId : evenementsCandidats) {
                double score = calculerScoreNLP(userId, description, evenementId);
                
                if (score > 0.4) { // Seuil plus bas pour NLP
                    String raison = genererRaisonNLP(contexteSocial, motsCles, score);
                    Recommandation reco = new Recommandation(
                        userId, evenementId, score, raison, 
                        Recommandation.AlgorithmeReco.NLP
                    );
                    recommandations.add(reco);
                }
            }
            
            return recommandations.stream()
                    .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                    .limit(limite)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de recommandations NLP", e);
            return new ArrayList<>();
        }
    }

    @Override
    public String analyserContexteSocial(String description) {
        if (description == null) return "SOLO";
        
        String lowerDesc = description.toLowerCase();
        
        if (lowerDesc.contains("copine") || lowerDesc.contains("couple") || 
            lowerDesc.contains("romantique") || lowerDesc.contains("amoureux")) {
            return "COUPLE";
        } else if (lowerDesc.contains("amis") || lowerDesc.contains("groupe") || 
                   lowerDesc.contains("équipe") || lowerDesc.contains("bande")) {
            return "AMIS";
        } else if (lowerDesc.contains("famille") || lowerDesc.contains("enfants") || 
                   lowerDesc.contains("parents")) {
            return "FAMILLE";
        } else if (lowerDesc.contains("travail") || lowerDesc.contains("professionnel") || 
                   lowerDesc.contains("collègues")) {
            return "PROFESSIONNEL";
        } else {
            return "SOLO";
        }
    }

    @Override
    public List<String> extraireMotsCles(String description) {
        if (description == null) return new ArrayList<>();
        
        // Mots-clés prédéfinis pour les événements de loisirs
        String[] motsClesPotentiels = {
            "camping", "randonnée", "nature", "montagne", "mer", "plage",
            "barbecue", "musique", "festival", "sport", "aventure",
            "détente", "spa", "bien-être", "culture", "visite",
            "gastronomie", "vin", "dégustation", "jeux", "animation"
        };
        
        String lowerDesc = description.toLowerCase();
        List<String> motsClesTrouves = new ArrayList<>();
        
        for (String mot : motsClesPotentiels) {
            if (lowerDesc.contains(mot)) {
                motsClesTrouves.add(mot);
            }
        }
        
        return motsClesTrouves;
    }

    @Override
    public boolean detecterEmotion(String texte) {
        if (texte == null) return false;
        
        String lowerText = texte.toLowerCase();
        
        // Mots positifs
        String[] motsPositifs = {
            "heureux", "content", "excellent", "super", "génial", "fantastique",
            "amusant", "excitant", "passionnant", "magnifique", "wonderful"
        };
        
        // Mots négatifs
        String[] motsNegatifs = {
            "triste", "déçu", "mauvais", "horrible", "terrible",
            "ennuyeux", "frustrant", "stressant", "difficile", "problème"
        };
        
        int scorePositif = 0;
        int scoreNegatif = 0;
        
        for (String mot : motsPositifs) {
            if (lowerText.contains(mot)) scorePositif++;
        }
        
        for (String mot : motsNegatifs) {
            if (lowerText.contains(mot)) scoreNegatif++;
        }
        
        return scorePositif > scoreNegatif;
    }

    @Override
    public double calculerScoreCollaboratif(Long userId, Long evenementId) {
        // Implémentation simplifiée du collaborative filtering
        try {
            // Similarité cosinus basée sur les participations passées
            List<Long> evenementsUser = getEvenementsUtilisateur(userId);
            List<Long> evenementsSimilaires = getEvenementsSimilaires(evenementId);
            
            // Calculer l'intersection
            int intersection = 0;
            for (Long evenement : evenementsSimilaires) {
                if (evenementsUser.contains(evenement)) {
                    intersection++;
                }
            }
            
            // Score basé sur la similarité
            if (evenementsSimilaires.isEmpty()) return 0.0;
            
            double similarite = (double) intersection / evenementsSimilaires.size();
            
            // Ajuster selon la popularité de l'événement
            double popularite = getPopulariteEvenement(evenementId);
            
            return Math.min(1.0, similarite * 0.7 + popularite * 0.3);
            
        } catch (Exception e) {
            logger.error("Erreur lors du calcul du score collaboratif", e);
            return 0.0;
        }
    }

    @Override
    public double calculerScoreNLP(Long userId, String description, Long evenementId) {
        try {
            // Score basé sur l'analyse NLP
            String contexte = analyserContexteSocial(description);
            List<String> motsCles = extraireMotsCles(description);
            
            double scoreContexte = getScoreContexteEvenement(evenementId, contexte);
            double scoreMotsCles = getScoreMotsClesEvenement(evenementId, motsCles);
            double scorePopularite = getPopulariteEvenement(evenementId);
            
            // Pondération: 40% contexte, 40% mots-clés, 20% popularité
            return Math.min(1.0, scoreContexte * 0.4 + scoreMotsCles * 0.4 + scorePopularite * 0.2);
            
        } catch (Exception e) {
            logger.error("Erreur lors du calcul du score NLP", e);
            return 0.0;
        }
    }

    @Override
    public List<Long> trouverUtilisateursSimilaires(Long userId, int limite) {
        // Implémentation simplifiée
        List<Long> utilisateursSimilaires = new ArrayList<>();
        
        try {
            // Trouver des utilisateurs avec des participations similaires
            List<Long> evenementsUser = getEvenementsUtilisateur(userId);
            
            String sql = "SELECT DISTINCT user_id FROM participations WHERE evenement_id IN " +
                        "(SELECT evenement_id FROM participations WHERE user_id = ?) AND user_id != ? " +
                        "LIMIT ?";
            
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setLong(1, userId);
                pstmt.setLong(2, userId);
                pstmt.setInt(3, limite);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        utilisateursSimilaires.add(rs.getLong("user_id"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche d'utilisateurs similaires", e);
        }
        
        return utilisateursSimilaires;
    }

    @Override
    public boolean validerRecommandation(Recommandation recommandation) {
        if (recommandation == null) return false;
        if (recommandation.getUserId() == null || recommandation.getUserId() <= 0) return false;
        if (recommandation.getEvenementSuggereId() == null || recommandation.getEvenementSuggereId() <= 0) return false;
        if (recommandation.getScore() < 0 || recommandation.getScore() > 1) return false;
        if (recommandation.getAlgorithmeUsed() == null) return false;
        if (recommandation.getRaison() == null || recommandation.getRaison().trim().isEmpty()) return false;
        
        return true;
    }

    @Override
    public void envoyerNotificationPush(Recommandation recommandation) {
        logger.info("Notification push envoyée pour la recommandation {} à l'utilisateur {}", 
                   recommandation.getId(), recommandation.getUserId());
        // TODO: Implémenter l'envoi réel de notifications push
    }

    // Méthodes utilitaires privées
    private Recommandation mapResultSetToRecommandation(ResultSet rs) throws SQLException {
        Recommandation recommandation = new Recommandation();
        recommandation.setId(rs.getLong("id"));
        recommandation.setUserId(rs.getLong("user_id"));
        recommandation.setEvenementSuggereId(rs.getLong("evenement_suggere_id"));
        recommandation.setScore(rs.getDouble("score"));
        recommandation.setRaison(rs.getString("raison"));
        recommandation.setAlgorithmeUsed(Recommandation.AlgorithmeReco.valueOf(rs.getString("algorithme_used")));
        recommandation.setEquipementBundle(parseEquipementBundleFromString(rs.getString("equipement_bundle")));
        recommandation.setSourceScraped(rs.getString("source_scraped"));
        recommandation.setDateGeneration(rs.getTimestamp("date_generation").toLocalDateTime());
        recommandation.setDateExpiration(rs.getTimestamp("date_expiration").toLocalDateTime());
        recommandation.setEstUtilisee(rs.getBoolean("est_utilisee"));
        return recommandation;
    }

    private String convertEquipementBundleToString(java.util.Map<String, Object> bundle) {
        if (bundle == null) return "{}";
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(bundle);
        } catch (Exception e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> parseEquipementBundleFromString(String bundleStr) {
        if (bundleStr == null || bundleStr.trim().isEmpty()) return new java.util.HashMap<>();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(bundleStr, java.util.Map.class);
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }

    private List<Long> getEvenementsUtilisateur(Long userId) {
        List<Long> evenements = new ArrayList<>();
        String sql = "SELECT DISTINCT evenement_id FROM participations WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    evenements.add(rs.getLong("evenement_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des événements de l'utilisateur {}", userId, e);
        }
        
        return evenements;
    }

    private List<Long> getEvenementsSimilaires(Long evenementId) {
        // Implémentation simplifiée - retourne des événements de la même catégorie
        List<Long> similaires = new ArrayList<>();
        String sql = "SELECT id FROM evenements WHERE categorie = " +
                    "(SELECT categorie FROM evenements WHERE id = ?) AND id != ? LIMIT 10";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, evenementId);
            pstmt.setLong(2, evenementId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    similaires.add(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche d'événements similaires", e);
        }
        
        return similaires;
    }

    private double getPopulariteEvenement(Long evenementId) {
        String sql = "SELECT COUNT(*) as participations FROM participations WHERE evenement_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, evenementId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int participations = rs.getInt("participations");
                    // Normaliser entre 0 et 1 (max 100 participations)
                    return Math.min(1.0, participations / 100.0);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du calcul de popularité", e);
        }
        
        return 0.0;
    }

    private double getScoreContexteEvenement(Long evenementId, String contexte) {
        // Score basé sur la pertinence du contexte pour l'événement
        return switch (contexte) {
            case "COUPLE" -> 0.8; // Les événements sont généralement bons pour les couples
            case "AMIS" -> 0.9; // Très pertinent pour les groupes d'amis
            case "FAMILLE" -> 0.7;
            case "PROFESSIONNEL" -> 0.6;
            default -> 0.5;
        };
    }

    private double getScoreMotsClesEvenement(Long evenementId, List<String> motsCles) {
        if (motsCles.isEmpty()) return 0.3;
        
        // Récupérer la description de l'événement et calculer la correspondance
        String sql = "SELECT description, titre FROM evenements WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, evenementId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String texte = (rs.getString("titre") + " " + rs.getString("description")).toLowerCase();
                    long correspondances = motsCles.stream()
                            .mapToLong(mot -> texte.contains(mot.toLowerCase()) ? 1 : 0)
                            .sum();
                    
                    return Math.min(1.0, (double) correspondances / motsCles.size());
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du calcul de score mots-clés", e);
        }
        
        return 0.0;
    }

    private List<Long> trouverEvenementsParMotsCles(List<String> motsCles) {
        List<Long> evenements = new ArrayList<>();
        
        if (motsCles.isEmpty()) {
            // Retourner les événements les plus populaires
            String sql = "SELECT id FROM evenements ORDER BY popularite DESC LIMIT 20";
            
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    evenements.add(rs.getLong("id"));
                }
            } catch (SQLException e) {
                logger.error("Erreur lors de la recherche d'événements populaires", e);
            }
        } else {
            // Rechercher par mots-clés
            String sql = "SELECT DISTINCT id FROM evenements WHERE " +
                        motsCles.stream()
                                .map(mot -> "LOWER(description) LIKE '%" + mot.toLowerCase() + "%'")
                                .collect(Collectors.joining(" OR ")) +
                        " LIMIT 50";
            
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    evenements.add(rs.getLong("id"));
                }
            } catch (SQLException e) {
                logger.error("Erreur lors de la recherche d'événements par mots-clés", e);
            }
        }
        
        return evenements;
    }

    private String genererRaisonCollaborative(Long userId, Long evenementId, double score) {
        return String.format("Recommandé basé sur vos préférences et celles d'utilisateurs similaires " +
                          "(score de confiance: %.1f%%)", score * 100);
    }

    private String genererRaisonNLP(String contexte, List<String> motsCles, double score) {
        return String.format("Parfait pour %s avec %s " +
                          "(score IA: %.1f%%)", 
                          contexte.toLowerCase(), 
                          motsCles.stream().collect(Collectors.joining(", ")),
                          score * 100);
    }

    @Override
    public Recommandation update(Recommandation recommandation) {
        if (recommandation == null || recommandation.getId() == null || !validerRecommandation(recommandation)) {
            throw new IllegalArgumentException("Recommandation invalide pour mise à jour");
        }
        String sql = "UPDATE recommandations SET user_id = ?, evenement_suggere_id = ?, score = ?, raison = ?, " +
                "algorithme_used = ?, equipement_bundle = ?, source_scraped = ?, date_expiration = ?, est_utilisee = ? WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, recommandation.getUserId());
            pstmt.setLong(2, recommandation.getEvenementSuggereId());
            pstmt.setDouble(3, recommandation.getScore());
            pstmt.setString(4, recommandation.getRaison());
            pstmt.setString(5, recommandation.getAlgorithmeUsed().name());
            pstmt.setString(6, convertEquipementBundleToString(recommandation.getEquipementBundle()));
            pstmt.setString(7, recommandation.getSourceScraped());
            pstmt.setTimestamp(8, recommandation.getDateExpiration() != null ? Timestamp.valueOf(recommandation.getDateExpiration()) : null);
            pstmt.setBoolean(9, recommandation.isEstUtilisee());
            pstmt.setLong(10, recommandation.getId());
            if (pstmt.executeUpdate() > 0) {
                logger.info("Recommandation mise à jour: ID {}", recommandation.getId());
                return recommandation;
            }
        } catch (SQLException e) {
            logger.error("Erreur mise à jour recommandation {}: {}", recommandation.getId(), e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour la recommandation", e);
        }
        return null;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        String sql = "DELETE FROM recommandations WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            boolean deleted = pstmt.executeUpdate() > 0;
            if (deleted) logger.info("Recommandation supprimée (obsolète): ID {}", id);
            return deleted;
        } catch (SQLException e) {
            logger.error("Erreur suppression recommandation {}: {}", id, e.getMessage());
            return false;
        }
    }

    // Implémentations par défaut des autres méthodes
    @Override public List<Recommandation> findByAlgorithme(Recommandation.AlgorithmeReco algorithme) { return new ArrayList<>(); }
    @Override public List<Recommandation> findByDateGenerationBetween(LocalDateTime debut, LocalDateTime fin) { return new ArrayList<>(); }
    @Override public List<Recommandation> findRecommandationsExpirees() { return new ArrayList<>(); }
    @Override public List<Recommandation> findRecommandationsNonUtilisees() { return new ArrayList<>(); }
    @Override public List<Recommandation> genererRecommandationsContentBased(Long userId, int limite) { return new ArrayList<>(); }
    @Override public List<Recommandation> genererRecommandationsHybrides(Long userId, int limite) { return new ArrayList<>(); }
    @Override public List<Recommandation> genererRecommandationsTensorFlow(Long userId, int limite) { return new ArrayList<>(); }
    @Override public List<Recommandation> genererRecommandationsParClustering(Long userId, int limite) { return new ArrayList<>(); }
    @Override public double calculerScoreContentBased(Long userId, Long evenementId) { return 0.0; }
    @Override public double calculerScoreHybride(Long userId, Long evenementId) { return 0.0; }
    @Override public List<Long> trouverEvenementsSimilaires(Long evenementId, int limite) { return new ArrayList<>(); }
    @Override public String extrairePreferences(String texte) { return ""; }
    @Override public String genererRaisonPersonnalisee(Long userId, Long evenementId, double score) { return ""; }
    @Override public void entrainerModeleTensorFlow() {}
    @Override public double predireConversion(Long userId, Long evenementId) { return 0.0; }
    @Override public double predireChurnAbonnement(Long userId) { return 0.0; }
    @Override public void mettreAJourModeleML() {}
    @Override public List<Recommandation> optimiserHyperparametres() { return new ArrayList<>(); }
    @Override public List<Recommandation> scraperPrixEquipements(String query) { return new ArrayList<>(); }
    @Override public void mettreAJourPrixEnTempsReel() {}
    @Override public String scraperDisponibilites(String urlEquipement) { return ""; }
    @Override public boolean validerSourceScraped(String source) { return false; }
    @Override public List<String> getSourcesScrapingActives() { return new ArrayList<>(); }
    @Override public Recommandation creerBundlePersonnalise(Long userId, Long evenementId, String contexte) { return null; }
    @Override public List<Recommandation> genererBundlesContextuels(Long userId) { return new ArrayList<>(); }
    @Override public void mettreAJourEquipementsRecommandes(Long id, String contexte) {}
    @Override public List<Recommandation> findRecommandationsAvecBundle(String typeBundle) { return new ArrayList<>(); }
    @Override public void creerTestAB(String nomTest, List<Recommandation> groupeA, List<Recommandation> groupeB) {}
    @Override public double mesurerConversionTestAB(String nomTest) { return 0.0; }
    @Override public List<Recommandation> getGroupeTestAB(String nomTest, char groupe) { return new ArrayList<>(); }
    @Override public void conclureTestAB(String nomTest) {}
    @Override public double calculerTauxClicRecommandations(Long userId) { return 0.0; }
    @Override public double calculerTauxConversionRecommandations() { return 0.0; }
    @Override public List<Recommandation> getTopRecommandationsPerformantes(int limite) { return new ArrayList<>(); }
    @Override public double calculerScoreMoyenAlgorithme(Recommandation.AlgorithmeReco algorithme) { return 0.0; }
    @Override public List<Recommandation> findRecommandationsLowPerformance() { return new ArrayList<>(); }
    @Override public void nettoyerRecommandationsExpirees() {}
    @Override public void supprimerRecommandationsObsolètes() {}
    @Override public boolean verifierCoherenceDonnees() { return false; }
    @Override public void envoyerRecommandationEmail(Long userId, List<Recommandation> recommandations) {}
    @Override public void programmerNotificationDifferée(Recommandation recommandation, LocalDateTime dateEnvoi) {}
    @Override public void suivreInteractionRecommandation(Long id, String typeInteraction) {}
    @Override public List<Recommandation> synchroniserAvecEvenements() { return new ArrayList<>(); }
    @Override public List<Recommandation> synchroniserAvecParticipations(Long userId) { return new ArrayList<>(); }
    @Override public boolean integrerAvecTransport(Long recommandationId) { return false; }
    @Override public List<Recommandation> getRecommandationsCrossModule(Long userId) { return new ArrayList<>(); }
    @Override public String exporterRecommandationsJSON(Long userId) { return ""; }
    @Override public List<Recommandation> importerRecommandationsAPI(String urlAPI) { return new ArrayList<>(); }
    @Override public boolean synchroniserAvecAPIExterne(String endpoint) { return false; }
    @Override public List<Recommandation> getRecommandationsParRegion(String region) { return new ArrayList<>(); }

    @Override
    public void marquerCommeUtilisee(Long id) {

    }
}
