package com.gestion.services;

import com.gestion.entities.Evenement;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EvenementService {

    // ====== FILTRES ======

    // Filtrer: événements futurs
    public List<Evenement> futurs(List<Evenement> events) {
        LocalDateTime now = LocalDateTime.now();
        return events.stream()
                .filter(e -> e.getDateDebut() != null && e.getDateDebut().isAfter(now))
                .toList();
    }

    // Filtrer: par type (SOIREE/RANDONNEE/CAMPING/SEJOUR)
    public List<Evenement> parType(List<Evenement> events, String type) {
        String t = safeUpper(type);
        return events.stream()
                .filter(e -> safeUpper(e.getType()).equals(t))
                .toList();
    }

    // Filtrer: par lieu (contient)
    public List<Evenement> parLieuContient(List<Evenement> events, String keyword) {
        String k = safeLower(keyword);
        return events.stream()
                .filter(e -> safeLower(e.getLieu()).contains(k))
                .toList();
    }

    // Filtrer: entre deux dates (sur dateDebut)
    public List<Evenement> entreDates(List<Evenement> events, LocalDateTime from, LocalDateTime to) {
        return events.stream()
                .filter(e -> e.getDateDebut() != null)
                .filter(e -> !e.getDateDebut().isBefore(from) && !e.getDateDebut().isAfter(to))
                .toList();
    }

    // ====== TRI ======

    // Trier par dateDebut asc
    public List<Evenement> trierParDateAsc(List<Evenement> events) {
        return events.stream()
                .sorted(Comparator.comparing(Evenement::getDateDebut,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    // Trier par dateDebut desc
    public List<Evenement> trierParDateDesc(List<Evenement> events) {
        return events.stream()
                .sorted(Comparator.comparing(Evenement::getDateDebut,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    // Trier par titre A-Z
    public List<Evenement> trierParTitre(List<Evenement> events) {
        return events.stream()
                .sorted(Comparator.comparing(e -> safeLower(e.getTitre())))
                .toList();
    }

    // ====== RECHERCHE ======

    // Recherche globale (titre OU description OU lieu) contient keyword
    public List<Evenement> rechercher(List<Evenement> events, String keyword) {
        String k = safeLower(keyword);
        return events.stream()
                .filter(e ->
                        safeLower(e.getTitre()).contains(k) ||
                                safeLower(e.getDescription()).contains(k) ||
                                safeLower(e.getLieu()).contains(k)
                )
                .toList();
    }

    // ====== Helpers ======
    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
    }

    private String safeUpper(String s) {
        return s == null ? "" : s.toUpperCase(Locale.ROOT).trim();
    }
}
