package com.gestion.criteria;

import com.gestion.entities.Recommandation;

/**
 * Critères de recherche et tri pour les recommandations.
 * API: GET /api/recommandations/user/:userId?contexte=couple (top 5 par Score DESC)
 */
public class RecommandationCriteria {
    private Long userId;
    private String contexte;  // couple, amis, famille pour filtrer bundles
    private Double scoreMinimum; // > 0.5 pour push
    private Recommandation.AlgorithmeReco algorithme;
    private Boolean validesSeulement; // non expirées, non utilisées
    private Integer limite;   // ex: 5 pour top 5
    private String sortBy;    // score, dateGeneration, dateExpiration
    private String sortOrder; // ASC, DESC

    public RecommandationCriteria() {
        this.scoreMinimum = 0.5;
        this.limite = 10;
        this.sortBy = "score";
        this.sortOrder = "DESC";
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getContexte() { return contexte; }
    public void setContexte(String contexte) { this.contexte = contexte; }
    public Double getScoreMinimum() { return scoreMinimum; }
    public void setScoreMinimum(Double scoreMinimum) { this.scoreMinimum = scoreMinimum; }
    public Recommandation.AlgorithmeReco getAlgorithme() { return algorithme; }
    public void setAlgorithme(Recommandation.AlgorithmeReco algorithme) { this.algorithme = algorithme; }
    public Boolean getValidesSeulement() { return validesSeulement; }
    public void setValidesSeulement(Boolean validesSeulement) { this.validesSeulement = validesSeulement; }
    public Integer getLimite() { return limite; }
    public void setLimite(Integer limite) { this.limite = limite; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy != null ? sortBy : "score"; }
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = "DESC".equalsIgnoreCase(sortOrder != null ? sortOrder : "") ? "DESC" : "ASC"; }
}
