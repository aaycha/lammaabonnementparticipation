package com.gestion;

import com.gestion.controllers.EvenementDAO;
import com.gestion.controllers.ProgrammeDAO;
import com.gestion.controllers.ProgrammeRecommenderController;
import com.gestion.criteria.ParticipationCriteria;
import com.gestion.entities.*;
import com.gestion.interfaces.AbonnementService;
import com.gestion.interfaces.ProgrammeService;
import com.gestion.interfaces.TicketService;
import com.gestion.services.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsoleTestMain {

    private static final Scanner sc = new Scanner(System.in);

    // ===== SERVICES =====
    private static final ParticipationServiceImpl participationService = new ParticipationServiceImpl();
    private static final ProgrammeRecommenderService programmeRecoService = new ProgrammeRecommenderService();
    private static final TicketService ticketService = new TicketServiceImpl();
    private static final AbonnementService abonnementService = new AbonnementServiceImpl();

    // ===== DAO JDBC =====
    private static final EvenementDAO eventDAO = new EvenementDAO();
    private static final ProgrammeDAO programmeDAO = new ProgrammeDAO();

    // ======================= MAIN =======================
    public static void main(String[] args) {
        while (true) {
            afficherMenu();
            System.out.print("👉 Votre choix : ");
            String choix = sc.nextLine();

            switch (choix) {
                case "1" -> afficherEvenements();
                case "2" -> afficherProgrammesEvent();
                case "3" -> gestionProgrammeRecommender();
                case "4" -> gestionParticipation(); // Bloc Participation
                case "5" -> testerTicket();
                case "6" -> testerAbonnement();
                case "0" -> {
                    System.out.println("\n👋 Fin du programme. Merci !");
                    return;
                }
                default -> System.out.println("❌ Choix invalide !");
            }
        }
    }

    // ======================= MENU =======================
    private static void afficherMenu() {
        System.out.println("\n======================================");
        System.out.println("     🎯 CONSOLE DE TEST - GESTION");
        System.out.println("======================================");
        System.out.println("1️⃣  Afficher les événements");
        System.out.println("2️⃣  Programmes d’un événement");
        System.out.println("3️⃣  Programmes recommandés (selon participation)");
        System.out.println("4️⃣  Gérer les participations");
        System.out.println("5️⃣  Tester un ticket");
        System.out.println("6️⃣  Tester un abonnement");
        System.out.println("0️⃣  Quitter");
        System.out.println("======================================");
    }

    // ======================= EVENEMENTS =======================
    private static void afficherEvenements() {
        System.out.println("\n📌 LISTE DES ÉVÉNEMENTS");
        try {
            List<Evenement> list = eventDAO.findAll();
            list.forEach(e -> System.out.println("• " + e));
        } catch (Exception e) {
            System.out.println("❌ Erreur événements : " + e.getMessage());
        }
    }

    // ======================= PROGRAMMES EVENT =======================
    private static void afficherProgrammesEvent() {
        try {
            System.out.print("\nID de l'événement : ");
            int eventId = Integer.parseInt(sc.nextLine());
            List<Programme> list = programmeDAO.findByEventId(eventId);
            System.out.println("\n📅 PROGRAMMES DE L'ÉVÉNEMENT");
            list.forEach(p -> System.out.println("• " + p));
        } catch (Exception e) {
            System.out.println("❌ Erreur programme événement : " + e.getMessage());
        }
    }


    // ======================= PARTICIPATION =======================
    private static void gestionParticipation() {
        int choix;
        do {
            System.out.println("\n=== MENU GESTION PARTICIPATION ===");
            System.out.println("1) Créer participation");
            System.out.println("2) Lister toutes les participations");
            System.out.println("3) Rechercher par utilisateur");
            System.out.println("4) Rechercher par événement");
            System.out.println("5) Mettre à jour participation");
            System.out.println("6) Supprimer participation");
            System.out.println("7) Lister participations confirmées");
            System.out.println("8) Lister participations en attente");
            System.out.println("9) Confirmer une participation");
            System.out.println("10) Annuler une participation");
            System.out.println("12) Supprimer participation");
            System.out.println("13) Confirmer une participation");
            System.out.println("15) Statistiques rapides");
            System.out.println("0) Retour au menu principal");
            System.out.print("Choix : ");

            String input = sc.nextLine().trim();

            try {
                choix = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Choix invalide, veuillez entrer un nombre.");
                choix = -1; // continue la boucle
                continue;
            }

            try {
                switch (choix) {
                    case 1 -> creerParticipation();
                    case 2 -> {
                        System.out.println("\nToutes les participations :");
                        participationService.findAll().forEach(System.out::println);
                    }
                    case 3 -> rechercherParUtilisateur();
                    case 4 -> rechercherParEvenement();
                    case 5 -> mettreAJourParticipation();
                    case 6 -> supprimerParticipation();
                    case 7 -> {
                        System.out.println("\nParticipations confirmées :");
                        participationService.findParticipationsConfirmees().forEach(System.out::println);
                    }
                    case 8 -> {
                        System.out.println("\nParticipations en attente :");
                        participationService.findParticipationsEnAttente().forEach(System.out::println);
                    }
                    case 9 -> confirmerParticipation();
                    case 10 -> annulerParticipation();
                    case 15 -> afficherStats();
                    case 0 -> System.out.println("Retour au menu principal...");
                    default -> System.out.println("Choix invalide ! Veuillez choisir un numéro entre 0 et 10.");
                }
            } catch (Exception e) {
                System.out.println("❌ Erreur : " + e.getMessage());
            }
        } while (choix != 0);
    }
    // Tri personnalisé
    private static void listerAvecTri() {
        System.out.print("Trier par (date/type/statut/nuits) [défaut: date] : ");
        String sortBy = sc.nextLine().trim();
        System.out.print("Ordre (ASC/DESC) [défaut: DESC] : ");
        String sortOrder = sc.nextLine().trim();

        List<Participation> result = participationService.findAll(sortBy, sortOrder);
        if (result.isEmpty()) {
            System.out.println("Aucune participation trouvée.");
        } else {
            result.forEach(System.out::println);
        }
    }

    // Recherche avancée (exemple simple)
    private static void rechercheAvancee() {
        System.out.print("Statut (EN_ATTENTE/CONFIRME/ANNULE/REFUSE - vide pour tous) : ");
        String statutStr = sc.nextLine().trim().toUpperCase();

        List<Participation> result;
        if (!statutStr.isEmpty()) {
            try {
                Participation.StatutParticipation statut = Participation.StatutParticipation.valueOf(statutStr);
                result = participationService.findByStatut(statut);
            } catch (IllegalArgumentException e) {
                System.out.println("Statut invalide.");
                return;
            }
        } else {
            result = participationService.findAll();
        }

        if (result.isEmpty()) {
            System.out.println("Aucun résultat.");
        } else {
            result.forEach(System.out::println);
        }
    }

    // Liste d'attente d'un événement
    private static void listerListeAttente() {
        try {
            System.out.print("ID de l'événement : ");
            Long eventId = Long.parseLong(sc.nextLine().trim());
            List<Participation> attente = participationService.findListeAttente(eventId);
            if (attente.isEmpty()) {
                System.out.println("Aucune personne en liste d'attente pour cet événement.");
            } else {
                System.out.println("Liste d'attente pour l'événement " + eventId + " :");
                attente.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    // Statistiques rapides
    private static void afficherStats() {
        System.out.println("\n=== Statistiques rapides ===");
        System.out.println("Total participations     : " + participationService.findAll().size());
        System.out.println("Confirmées               : " + participationService.findParticipationsConfirmees().size());
        System.out.println("En attente               : " + participationService.findParticipationsEnAttente().size());
        System.out.println("Avec hébergement         : " + participationService.findAvecHebergement().size());
    }

    // ────────────────────────────────────────────────
// 1. Créer participation
// ────────────────────────────────────────────────
    private static void creerParticipation() {
        try {
            System.out.print("User ID : ");
            String userInput = sc.nextLine().trim();
            if (userInput.isEmpty()) throw new IllegalArgumentException("User ID obligatoire");
            Long userId = Long.parseLong(userInput);
            if (userId <= 0) throw new IllegalArgumentException("User ID doit être positif");

            System.out.print("Événement ID : ");
            String eventInput = sc.nextLine().trim();
            if (eventInput.isEmpty()) throw new IllegalArgumentException("Événement ID obligatoire");
            Long eventId = Long.parseLong(eventInput);
            if (eventId <= 0) throw new IllegalArgumentException("Événement ID doit être positif");

            System.out.print("Type (SIMPLE, HEBERGEMENT, GROUPE) : ");
            String typeStr = sc.nextLine().trim().toUpperCase();
            if (typeStr.isEmpty()) throw new IllegalArgumentException("Type obligatoire");
            Participation.TypeParticipation type = Participation.TypeParticipation.valueOf(typeStr);

            System.out.print("Contexte (COUPLE, AMIS, FAMILLE, SOLO, PROFESSIONNEL) : ");
            String contexteStr = sc.nextLine().trim().toUpperCase();
            if (contexteStr.isEmpty()) throw new IllegalArgumentException("Contexte obligatoire");
            Participation.ContexteSocial contexte = Participation.ContexteSocial.valueOf(contexteStr);

            int nuits = 0;
            if (type == Participation.TypeParticipation.HEBERGEMENT) {
                System.out.print("Nombre de nuits (≥ 1) : ");
                String nuitsStr = sc.nextLine().trim();
                if (nuitsStr.isEmpty()) throw new IllegalArgumentException("Nombre de nuits obligatoire pour HEBERGEMENT");
                nuits = Integer.parseInt(nuitsStr);
                if (nuits < 1) throw new IllegalArgumentException("Au moins 1 nuit pour un hébergement");
            }

            Participation p = new Participation(userId, eventId, type, contexte);
            p.setHebergementNuits(nuits);
            p.setStatut(Participation.StatutParticipation.EN_ATTENTE);
            p.setDateInscription(LocalDateTime.now());

            Participation created = participationService.create(p);
            System.out.println("\n✅ Participation créée avec succès :");
            System.out.println(created);

        } catch (NumberFormatException e) {
            System.out.println("❌ Erreur : veuillez entrer un nombre valide");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur de saisie : " + e.getMessage());
            System.out.println("Veuillez recommencer.");
        } catch (Exception e) {
            System.out.println("❌ Erreur inattendue : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 3. Rechercher par utilisateur
// ────────────────────────────────────────────────
    private static void rechercherParUtilisateur() {
        try {
            System.out.print("User ID : ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) throw new IllegalArgumentException("User ID obligatoire");
            Long userId = Long.parseLong(input);
            if (userId <= 0) throw new IllegalArgumentException("User ID doit être positif");

            List<Participation> participations = participationService.findByUserId(userId);
            if (participations.isEmpty()) {
                System.out.println("Aucune participation trouvée pour l'utilisateur " + userId);
            } else {
                System.out.println("\nParticipations de l'utilisateur " + userId + " :");
                participations.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un nombre valide pour l'ID");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 4. Rechercher par événement
// ────────────────────────────────────────────────
    private static void rechercherParEvenement() {
        try {
            System.out.print("Événement ID : ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) throw new IllegalArgumentException("Événement ID obligatoire");
            Long eventId = Long.parseLong(input);
            if (eventId <= 0) throw new IllegalArgumentException("Événement ID doit être positif");

            List<Participation> participations = participationService.findByEvenementId(eventId);
            if (participations.isEmpty()) {
                System.out.println("Aucune participation trouvée pour l'événement " + eventId);
            } else {
                System.out.println("\nParticipations à l'événement " + eventId + " :");
                participations.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un nombre valide pour l'ID");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 5. Mettre à jour participation
// ────────────────────────────────────────────────
    private static void mettreAJourParticipation() {
        try {
            System.out.print("ID de la participation à modifier : ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) throw new IllegalArgumentException("ID obligatoire");
            Long id = Long.parseLong(input);
            if (id <= 0) throw new IllegalArgumentException("ID doit être positif");

            Optional<Participation> opt = participationService.findById(id);
            if (opt.isEmpty()) {
                System.out.println("❌ Participation introuvable avec l'ID " + id);
                return;
            }

            Participation p = opt.get();
            System.out.println("\nParticipation actuelle :");
            System.out.println(p);

            // Statut
            System.out.print("Nouveau statut (EN_ATTENTE, CONFIRME, ANNULE, REFUSE) - vide pour garder : ");
            String statutStr = sc.nextLine().trim().toUpperCase();
            if (!statutStr.isEmpty()) {
                try {
                    p.setStatut(Participation.StatutParticipation.valueOf(statutStr));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Statut invalide. Valeurs possibles : EN_ATTENTE, CONFIRME, ANNULE, REFUSE");
                }
            }

            // Hébergement (seulement si type HEBERGEMENT)
            if (p.getType() == Participation.TypeParticipation.HEBERGEMENT) {
                System.out.print("Nouveau nombre de nuits (vide pour garder) : ");
                String nuitsStr = sc.nextLine().trim();
                if (!nuitsStr.isEmpty()) {
                    int nuits = Integer.parseInt(nuitsStr);
                    if (nuits < 0) throw new IllegalArgumentException("Nombre de nuits ne peut pas être négatif");
                    p.setHebergementNuits(nuits);
                }
            }

            Participation updated = participationService.update(p);
            System.out.println("\n✅ Participation mise à jour avec succès :");
            System.out.println(updated);

        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un nombre valide");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur inattendue : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 6. Supprimer participation
// ────────────────────────────────────────────────
    private static void supprimerParticipation() {
        try {
            System.out.print("ID de la participation à supprimer : ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) throw new IllegalArgumentException("ID obligatoire");
            Long id = Long.parseLong(input);
            if (id <= 0) throw new IllegalArgumentException("ID doit être positif");

            if (participationService.delete(id)) {
                System.out.println("✅ Participation supprimée avec succès (ID " + id + ")");
            } else {
                System.out.println("❌ Échec de la suppression : participation introuvable ou ne peut être supprimée");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un nombre valide");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 9. Confirmer participation
// ────────────────────────────────────────────────
    private static void confirmerParticipation() {
        try {
            System.out.print("ID de la participation à confirmer : ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) throw new IllegalArgumentException("ID obligatoire");
            Long id = Long.parseLong(input);
            if (id <= 0) throw new IllegalArgumentException("ID doit être positif");

            Participation confirmed = participationService.confirmerParticipation(id);
            System.out.println("\n✅ Participation confirmée avec succès :");
            System.out.println(confirmed);
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un nombre valide");
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 10. Annuler participation
// ────────────────────────────────────────────────
    private static void annulerParticipation() {
        try {
            System.out.print("ID de la participation à annuler : ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) throw new IllegalArgumentException("ID obligatoire");
            Long id = Long.parseLong(input);
            if (id <= 0) throw new IllegalArgumentException("ID doit être positif");

            System.out.print("Raison de l'annulation : ");
            String raison = sc.nextLine().trim();
            if (raison.isEmpty()) throw new IllegalArgumentException("La raison est obligatoire");

            Participation cancelled = participationService.annulerParticipation(id, raison);
            System.out.println("\n✅ Participation annulée avec succès :");
            System.out.println(cancelled);
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un nombre valide");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur inattendue : " + e.getMessage());
        }
    }



    // ======================= TICKET =======================
    /*private static void testerTicket() {
        try {
            System.out.println("\n🎫 TEST TICKET");

            System.out.print("Entrez l'ID de la participation : ");
            Long participationId = Long.parseLong(sc.nextLine());

            System.out.print("Entrez votre ID utilisateur : ");
            Long userId = Long.parseLong(sc.nextLine());

            System.out.print("Type de ticket (TICKET/BADGE/PASS) : ");
            Ticket.TypeTicket type = Ticket.TypeTicket.valueOf(sc.nextLine().toUpperCase());

            System.out.print("Format du ticket (NUMERIQUE/PHYSIQUE/HYBRIDE) : ");
            Ticket.FormatTicket format = Ticket.FormatTicket.valueOf(sc.nextLine().toUpperCase());

            Ticket t = ticketService.creerTicketSelonChoix(
                    participationId,
                    userId,
                    type,
                    36.8, 10.2, "Tunis", format
            );

            System.out.println("\n✅ Ticket généré avec succès : " + t);

        } catch (Exception e) {
            System.out.println("❌ Erreur ticket : " + e.getMessage());
        }
    }*/

    // ======================= TICKET =======================
    private static void testerTicket() {
        boolean quitter = false;

        while (!quitter) {
            System.out.println("\n======================================");
            System.out.println("      🎫   GESTION DES TICKETS   🎫      ");
            System.out.println("======================================");
            System.out.println("1) Créer un nouveau ticket");
            System.out.println("2) Afficher un ticket par ID");
            System.out.println("3) Lister tous les tickets");
            System.out.println("4) Lister tous les tickets (avec tri)");
            System.out.println("5) Modifier un ticket");
            System.out.println("6) Supprimer un ticket");
            System.out.println("7) Rechercher tickets par participation");
            System.out.println("8) Rechercher tickets par utilisateur");
            System.out.println("9) Rechercher tickets par type");
            System.out.println("10) Rechercher tickets par statut");
            System.out.println("11) Rechercher tickets par format");
            System.out.println("12) Rechercher tickets près d'une position (coordonnées)");
            System.out.println("13) Rechercher tickets par lieu");
            System.out.println("14) Marquer un ticket comme utilisé");
            System.out.println("15) Annuler un ticket");
            System.out.println("16) Lister les tickets valides");
            System.out.println("17) Lister les tickets expirés");
            System.out.println("18) Valider un ticket par code unique");
            System.out.println("0) Retour au menu principal");
            System.out.println("======================================");
            System.out.print("👉 Votre choix : ");

            String choixStr = sc.nextLine().trim();
            int choix;

            try {
                choix = Integer.parseInt(choixStr);
            } catch (NumberFormatException e) {
                System.out.println("❌ Veuillez entrer un nombre valide.");
                continue;
            }

            System.out.println();

            try {
                switch (choix) {
                    case 1  -> creerTicketInteractif();
                    case 2  -> afficherTicketParId();
                    case 3  -> afficherTousTickets();
                    case 4  -> afficherTousTicketsAvecTri();
                    case 5  -> modifierTicketInteractif();
                    case 6  -> supprimerTicketInteractif();
                    case 7  -> listerParParticipation();
                    case 8  -> listerParUtilisateur();
                    case 9  -> listerParType();
                    case 10 -> listerParStatut();
                    case 11 -> listerParFormat();
                    case 12 -> listerParCoordonnees();
                    case 13 -> listerParLieu();
                    case 14 -> marquerUtilise();
                    case 15 -> annulerTicket();
                    case 16 -> listerTicketsValides();
                    case 17 -> listerTicketsExpires();
                    case 18 -> validerTicketParCode();
                    case 0  -> {
                        System.out.println("Retour au menu principal...");
                        quitter = true;
                    }
                    default -> System.out.println("Choix invalide. Essayez encore.");
                }
            } catch (Exception e) {
                System.out.println("❌ Erreur : " + e.getMessage());
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("invalide")) {
                    System.out.println("   → Vérifiez les valeurs saisies (ID positif, type/format correct, etc.)");
                }
            }

            System.out.println();
        }
    }

// ────────────────────────────────────────────────
// Méthodes auxiliaires (CRUD + filtres)
// ────────────────────────────────────────────────

    private static void creerTicketInteractif() {
        try {
            System.out.println("\n📝 Création d'un nouveau ticket");

            System.out.print("ID participation : ");
            Long participationId = Long.parseLong(sc.nextLine().trim());

            System.out.print("ID utilisateur : ");
            Long userId = Long.parseLong(sc.nextLine().trim());

            System.out.print("Type (TICKET / BADGE / PASS) : ");
            String typeStr = sc.nextLine().trim().toUpperCase();
            Ticket.TypeTicket type = Ticket.TypeTicket.valueOf(typeStr);

            System.out.print("Format (NUMERIQUE / PHYSIQUE / HYBRIDE) : ");
            String formatStr = sc.nextLine().trim().toUpperCase();
            Ticket.FormatTicket format = Ticket.FormatTicket.valueOf(formatStr);

            System.out.print("Lieu (ex: Tunis, Ariana...) : ");
            String lieu = sc.nextLine().trim();

            System.out.print("Latitude (optionnel, ex: 36.8) : ");
            String latStr = sc.nextLine().trim();
            Double latitude = latStr.isEmpty() ? null : Double.parseDouble(latStr);

            System.out.print("Longitude (optionnel, ex: 10.2) : ");
            String lonStr = sc.nextLine().trim();
            Double longitude = lonStr.isEmpty() ? null : Double.parseDouble(lonStr);

            Ticket ticket = ticketService.creerTicketSelonChoix(
                    participationId, userId, type, latitude, longitude, lieu, format
            );

            System.out.println("\n🎉 Ticket créé avec succès !");
            System.out.println(ticket);

        } catch (NumberFormatException e) {
            System.out.println("❌ Erreur : veuillez entrer des nombres valides pour les IDs, latitude et longitude.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur de saisie : " + e.getMessage());
            System.out.println("   → Vérifiez type (TICKET/BADGE/PASS) et format (NUMERIQUE/PHYSIQUE/HYBRIDE)");
        } catch (Exception e) {
            System.out.println("❌ Erreur inattendue : " + e.getMessage());
        }
    }

    private static void afficherTicketParId() {
        System.out.print("Entrez l'ID du ticket : ");
        try {
            Long id = Long.parseLong(sc.nextLine().trim());
            Ticket t = ticketService.getById(id);
            if (t == null) {
                System.out.println("❌ Aucun ticket trouvé avec l'ID " + id);
            } else {
                System.out.println("\nTicket trouvé :");
                System.out.println(t);
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide. Entrez un nombre.");
        }
    }

    private static void afficherTousTickets() {
        List<Ticket> tickets = ticketService.getAll();
        afficherListeTickets(tickets, "tous les tickets");
    }

    private static void afficherTousTicketsAvecTri() {
        System.out.print("Trier par (date_creation / statut / type / format / date_expiration) [défaut: date_creation] : ");
        String sortBy = sc.nextLine().trim();
        if (sortBy.isEmpty()) sortBy = "date_creation";

        System.out.print("Ordre (ASC / DESC) [défaut: DESC] : ");
        String sortOrder = sc.nextLine().trim().toUpperCase();
        if (sortOrder.isEmpty()) sortOrder = "DESC";

        List<Ticket> tickets = ticketService.getAll(sortBy, sortOrder);
        afficherListeTickets(tickets, "tous les tickets triés par " + sortBy + " (" + sortOrder + ")");
    }

    private static void modifierTicketInteractif() {
        System.out.print("ID du ticket à modifier : ");
        try {
            Long id = Long.parseLong(sc.nextLine().trim());
            Ticket ticket = ticketService.getById(id);

            if (ticket == null) {
                System.out.println("❌ Ticket " + id + " introuvable.");
                return;
            }

            System.out.println("\nTicket actuel :");
            System.out.println(ticket);

            System.out.print("Nouveau lieu (vide = garder) : ");
            String lieu = sc.nextLine().trim();
            if (!lieu.isEmpty()) ticket.setLieu(lieu);

            System.out.print("Nouveau statut (VALIDE/UTILISE/EXPIRE/ANNULE - vide = garder) : ");
            String statutStr = sc.nextLine().trim().toUpperCase();
            if (!statutStr.isEmpty()) {
                ticket.setStatut(Ticket.StatutTicket.valueOf(statutStr));
            }

            Ticket updated = ticketService.update(ticket);
            System.out.println("\nTicket mis à jour :");
            System.out.println(updated);

        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Statut invalide : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    private static void supprimerTicketInteractif() {
        System.out.print("ID du ticket à supprimer : ");
        try {
            Long id = Long.parseLong(sc.nextLine().trim());
            if (ticketService.delete(id)) {
                System.out.println("✅ Ticket " + id + " supprimé avec succès !");
            } else {
                System.out.println("❌ Impossible de supprimer (ticket introuvable ou déjà utilisé)");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide.");
        }
    }

    private static void listerParParticipation() {
        System.out.print("ID participation : ");
        try {
            Long id = Long.parseLong(sc.nextLine().trim());
            List<Ticket> tickets = ticketService.findByParticipationId(id);
            afficherListeTickets(tickets, "participation " + id);
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide.");
        }
    }

    private static void listerParUtilisateur() {
        System.out.print("ID utilisateur : ");
        try {
            Long id = Long.parseLong(sc.nextLine().trim());
            List<Ticket> tickets = ticketService.findByUserId(id);
            afficherListeTickets(tickets, "utilisateur " + id);
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide.");
        }
    }

    private static void listerParType() {
        System.out.print("Type (TICKET / BADGE / PASS) : ");
        try {
            String typeStr = sc.nextLine().trim().toUpperCase();
            Ticket.TypeTicket type = Ticket.TypeTicket.valueOf(typeStr);
            List<Ticket> tickets = ticketService.findByType(type);
            afficherListeTickets(tickets, "type " + type);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Type invalide. Valeurs possibles : TICKET, BADGE, PASS");
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    private static void listerParStatut() {
        System.out.print("Statut (VALIDE / UTILISE / EXPIRE / ANNULE) : ");
        try {
            String statutStr = sc.nextLine().trim().toUpperCase();
            Ticket.StatutTicket statut = Ticket.StatutTicket.valueOf(statutStr);
            List<Ticket> tickets = ticketService.findByStatut(statut);
            afficherListeTickets(tickets, "statut " + statut);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Statut invalide. Valeurs possibles : VALIDE, UTILISE, EXPIRE, ANNULE");
        }
    }

    private static void listerParFormat() {
        System.out.print("Format (NUMERIQUE / PHYSIQUE / HYBRIDE) : ");
        try {
            String formatStr = sc.nextLine().trim().toUpperCase();
            Ticket.FormatTicket format = Ticket.FormatTicket.valueOf(formatStr);
            List<Ticket> tickets = ticketService.findByFormat(format);
            afficherListeTickets(tickets, "format " + format);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Format invalide. Valeurs possibles : NUMERIQUE, PHYSIQUE, HYBRIDE");
        }
    }

    private static void listerParCoordonnees() {
        try {
            System.out.print("Latitude : ");
            Double latitude = Double.parseDouble(sc.nextLine().trim());

            System.out.print("Longitude : ");
            Double longitude = Double.parseDouble(sc.nextLine().trim());

            System.out.print("Rayon en km : ");
            Double rayon = Double.parseDouble(sc.nextLine().trim());

            List<Ticket> tickets = ticketService.findByCoordonnees(latitude, longitude, rayon);
            afficherListeTickets(tickets, "proximité " + rayon + "km autour de (" + latitude + ", " + longitude + ")");
        } catch (NumberFormatException e) {
            System.out.println("❌ Valeurs numériques invalides pour latitude, longitude ou rayon.");
        }
    }

    private static void listerParLieu() {
        System.out.print("Lieu (ex: Tunis, Ariana) : ");
        String lieu = sc.nextLine().trim();
        List<Ticket> tickets = ticketService.findByLieu(lieu);
        afficherListeTickets(tickets, "lieu contenant '" + lieu + "'");
    }

    private static void marquerUtilise() {
        System.out.print("ID ticket à marquer comme utilisé : ");
        try {
            Long id = Long.parseLong(sc.nextLine().trim());
            Ticket t = ticketService.marquerCommeUtilise(id);
            System.out.println("\nTicket marqué comme utilisé :");
            System.out.println(t);
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    private static void annulerTicket() {
        System.out.print("ID ticket à annuler : ");
        try {
            Long id = Long.parseLong(sc.nextLine().trim());
            Ticket t = ticketService.annulerTicket(id);
            System.out.println("\nTicket annulé :");
            System.out.println(t);
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    private static void listerTicketsValides() {
        List<Ticket> tickets = ticketService.findTicketsValides();
        afficherListeTickets(tickets, "tickets valides");
    }

    private static void listerTicketsExpires() {
        List<Ticket> tickets = ticketService.findTicketsExpires();
        afficherListeTickets(tickets, "tickets expirés");
    }

    private static void validerTicketParCode() {
        System.out.print("Code unique du ticket : ");
        String code = sc.nextLine().trim();
        boolean valide = ticketService.validerTicket(code);
        System.out.println("\nTicket avec code " + code + " : " + (valide ? "VALIDE ✅" : "INVALIDE ou inexistant ❌"));
    }

    private static void afficherListeTickets(List<Ticket> tickets, String titre) {
        if (tickets.isEmpty()) {
            System.out.println("Aucun ticket trouvé pour : " + titre);
        } else {
            System.out.println("\n" + titre + " (" + tickets.size() + " ticket(s)) :");
            tickets.forEach(System.out::println);
        }
    }



    // ======================= ABONNEMENT =======================
    private static void testerAbonnement() {
        boolean quitter = false;
        while (!quitter) {
            System.out.println("\n================ ABONNEMENT - MENU =================");
            System.out.println("1️⃣ Créer un abonnement");
            System.out.println("2️⃣ Afficher tous les abonnements");
            System.out.println("3️⃣ Mettre à jour un abonnement");
            System.out.println("4️⃣ Supprimer un abonnement");
            System.out.println("5️⃣ Rechercher un abonnement par ID");
            System.out.println("6️⃣ Filtrer par type");
            System.out.println("7️⃣ Filtrer par statut");
            System.out.println("8️⃣ Filtrer par utilisateur");
            System.out.println("9️⃣ Afficher abonnements proches de l'expiration");
            System.out.println("10️ Ajouter des points à un abonnement");
            System.out.println("11️ Utiliser des points d'un abonnement");
            System.out.println("12️ Afficher abonnements par date fin avant une date");
            System.out.println("13️ Afficher abonnements par date fin entre deux dates");
            System.out.println("14️ Afficher abonnements avec auto-renew");
            System.out.println("15️ Afficher abonnements avec points minimum");
            System.out.println("16️ Upgrade abonnement");
            System.out.println("17️ Downgrade abonnement");
            System.out.println("18️ Renouveler abonnement");
            System.out.println("19️ Suspendre abonnement");
            System.out.println("20️ Reactiver abonnement");
            System.out.println("21️ Afficher top utilisateurs par points");
            System.out.println("22️ Compter par statut");
            System.out.println("23️ Compter par type");
            System.out.println("24️ Calculer revenu total");
            System.out.println("25️ Calculer revenu par mois");
            System.out.println("26️ Afficher abonnements risque churn");
            System.out.println("27️ Calculer taux retention");
            System.out.println("28️ Afficher abonnements avec participations actives");
            System.out.println("29️ Afficher abonnements sans participation récente");
            System.out.println("30️ Vérifier si peut supprimer");
            System.out.println("31️ Toggle auto-renew");
            System.out.println("0️⃣ Retour au menu principal");
            System.out.print("👉 Votre choix : ");
            String choix = sc.nextLine();
            try {
                switch (choix) {
                    case "1" -> creerAbonnementAvecControles();
                    case "2" -> {
                        System.out.print("Trier par (date_debut, date_fin, prix, statut) : ");
                        String sortBy = sc.nextLine();
                        System.out.print("Ordre (ASC/DESC) : ");
                        String sortOrder = sc.nextLine();
                        abonnementService.findAll(sortBy, sortOrder).forEach(System.out::println);
                    }
                    case "3" -> mettreAJourAbonnementAvecControles();
                    case "4" -> {
                        System.out.print("ID à supprimer : ");
                        Long id = Long.parseLong(sc.nextLine());
                        if (abonnementService.delete(id)) {
                            System.out.println("✅ Supprimé !");
                        } else {
                            System.out.println("❌ Impossible de supprimer.");
                        }
                    }
                    case "5" -> {
                        System.out.print("ID : ");
                        Long id = Long.parseLong(sc.nextLine());
                        abonnementService.findById(id).ifPresentOrElse(System.out::println, () -> System.out.println("Introuvable"));
                    }
                    case "6" -> {
                        System.out.print("Type (MENSUEL/ANNUEL/PREMIUM) : ");
                        Abonnement.TypeAbonnement type = Abonnement.TypeAbonnement.valueOf(sc.nextLine().toUpperCase());
                        abonnementService.findByType(type).forEach(System.out::println);
                    }
                    case "7" -> {
                        System.out.print("Statut (ACTIF/EXPIRE/SUSPENDU/EN_ATTENTE) : ");
                        Abonnement.StatutAbonnement statut = Abonnement.StatutAbonnement.valueOf(sc.nextLine().toUpperCase());
                        abonnementService.findByStatut(statut).forEach(System.out::println);
                    }
                    case "8" -> {
                        System.out.print("User ID : ");
                        Long userId = Long.parseLong(sc.nextLine());
                        abonnementService.findByUserId(userId).forEach(System.out::println);
                    }
                    case "9" -> {
                        System.out.print("Jours : ");
                        int jours = Integer.parseInt(sc.nextLine());
                        abonnementService.findAbonnementsProchesExpiration(jours).forEach(System.out::println);
                    }
                    case "10" -> {
                        System.out.print("ID : ");
                        Long id = Long.parseLong(sc.nextLine());
                        System.out.print("Points à ajouter : ");
                        int points = Integer.parseInt(sc.nextLine());
                        abonnementService.ajouterPoints(id, points);
                        System.out.println("✅ Points ajoutés !");
                    }
                    case "11" -> {
                        System.out.print("ID : ");
                        Long id = Long.parseLong(sc.nextLine());
                        System.out.print("Points à utiliser : ");
                        int points = Integer.parseInt(sc.nextLine());
                        if (abonnementService.utiliserPoints(id, points)) {
                            System.out.println("✅ Points utilisés !");
                        } else {
                            System.out.println("❌ Points insuffisants.");
                        }
                    }
                    case "12" -> {
                        System.out.print("Date (AAAA-MM-JJ) : ");
                        LocalDate date = LocalDate.parse(sc.nextLine());
                        abonnementService.findByDateFinBefore(date).forEach(System.out::println);
                    }
                    case "13" -> {
                        System.out.print("Date début (AAAA-MM-JJ) : ");
                        LocalDate debut = LocalDate.parse(sc.nextLine());
                        System.out.print("Date fin (AAAA-MM-JJ) : ");
                        LocalDate fin = LocalDate.parse(sc.nextLine());
                        abonnementService.findByDateFinBetween(debut, fin).forEach(System.out::println);
                    }
                    case "14" -> {
                        System.out.print("Auto-renew (true/false) : ");
                        boolean auto = Boolean.parseBoolean(sc.nextLine());
                        abonnementService.findByAutoRenew(auto).forEach(System.out::println);
                    }
                    case "15" -> {
                        System.out.print("Points min : ");
                        int min = Integer.parseInt(sc.nextLine());
                        abonnementService.findByPointsMinimum(min).forEach(System.out::println);
                    }
                    case "16" -> {
                        System.out.print("ID : ");
                        Long id = Long.parseLong(sc.nextLine());
                        System.out.print("Nouveau type (MENSUEL/ANNUEL/PREMIUM) : ");
                        Abonnement.TypeAbonnement type = Abonnement.TypeAbonnement.valueOf(sc.nextLine().toUpperCase());
                        Abonnement upgraded = abonnementService.upgradeAbonnement(id, type);
                        System.out.println("✅ Upgradé : " + upgraded);
                    }
                    case "17" -> {
                        System.out.print("ID : ");
                        Long id = Long.parseLong(sc.nextLine());
                        System.out.print("Nouveau type (MENSUEL/ANNUEL/PREMIUM) : ");
                        Abonnement.TypeAbonnement type = Abonnement.TypeAbonnement.valueOf(sc.nextLine().toUpperCase());
                        Abonnement downgraded = abonnementService.downgradeAbonnement(id, type);
                        System.out.println("✅ Downgradé : " + downgraded);
                    }
                    case "18" -> {
                        System.out.print("ID : ");
                        Long id = Long.parseLong(sc.nextLine());
                        Abonnement renewed = abonnementService.renouvelerAbonnement(id);
                        System.out.println("✅ Renouvelé : " + renewed);
                    }
                    case "19" -> {
                        System.out.print("ID : ");
                        Long id = Long.parseLong(sc.nextLine());
                        System.out.print("Raison : ");
                        String raison = sc.nextLine();
                        if (abonnementService.suspendreAbonnement(id, raison)) {
                            System.out.println("✅ Suspendu !");
                        } else {
                            System.out.println("❌ Échec.");
                        }
                    }
                    case "20" -> {
                        System.out.print("ID : ");
                        Long id = Long.parseLong(sc.nextLine());
                        if (abonnementService.reactiverAbonnement(id)) {
                            System.out.println("✅ Reactivé !");
                        } else {
                            System.out.println("❌ Échec.");
                        }
                    }
                    case "21" -> {
                        System.out.print("Limite : ");
                        int limite = Integer.parseInt(sc.nextLine());
                        abonnementService.findTopUtilisateursParPoints(limite).forEach(System.out::println);
                    }
                    case "22" -> {
                        System.out.print("Statut (ACTIF/EXPIRE/SUSPENDU/EN_ATTENTE) : ");
                        Abonnement.StatutAbonnement statut = Abonnement.StatutAbonnement.valueOf(sc.nextLine().toUpperCase());
                        System.out.println("Count : " + abonnementService.countByStatut(statut));
                    }
                    case "23" -> {
                        System.out.print("Type (MENSUEL/ANNUEL/PREMIUM) : ");
                        Abonnement.TypeAbonnement type = Abonnement.TypeAbonnement.valueOf(sc.nextLine().toUpperCase());
                        System.out.println("Count : " + abonnementService.countByType(type));
                    }
                    case "24" -> System.out.println("Revenu total : " + abonnementService.calculerRevenuTotal());
                    case "25" -> {
                        System.out.print("Mois (1-12) : ");
                        int mois = Integer.parseInt(sc.nextLine());
                        System.out.print("Année : ");
                        int annee = Integer.parseInt(sc.nextLine());
                        System.out.println("Revenu : " + abonnementService.calculerRevenuParMois(mois, annee));
                    }
                    case "26" -> {
                        System.out.print("Seuil churn (0.0-1.0) : ");
                        double seuil = Double.parseDouble(sc.nextLine());
                        abonnementService.findAbonnementsRisqueChurn(seuil).forEach(System.out::println);
                    }
                    case "27" -> {
                        System.out.print("Mois : ");
                        int mois = Integer.parseInt(sc.nextLine());
                        System.out.println("Taux retention : " + abonnementService.calculerTauxRetention(mois));
                    }
                    case "28" -> abonnementService.findAbonnementsAvecParticipationsActives().forEach(System.out::println);
                    case "29" -> {
                        System.out.print("Derniers mois : ");
                        int mois = Integer.parseInt(sc.nextLine());
                        abonnementService.findAbonnementsSansParticipation(mois).forEach(System.out::println);
                    }
                    case "30" -> {
                        System.out.print("ID : ");
                        Long id = Long.parseLong(sc.nextLine());
                        System.out.println("Peut supprimer : " + abonnementService.peutEtreSupprime(id));
                    }
                    case "31" -> {
                        System.out.print("ID : ");
                        Long id = Long.parseLong(sc.nextLine());
                        System.out.print("Auto-renew (true/false) : ");
                        boolean auto = Boolean.parseBoolean(sc.nextLine());
                        if (abonnementService.toggleAutoRenew(id, auto)) {
                            System.out.println("✅ Mis à jour !");
                        } else {
                            System.out.println("❌ Échec.");
                        }
                    }
                    case "0" -> quitter = true;
                    default -> System.out.println("⚠️ Choix invalide !");
                }
            } catch (Exception e) {
                System.out.println("❌ Erreur : " + e.getMessage());
            }
        }
    }
    private static void creerAbonnementAvecControles() {
        Abonnement a = new Abonnement();
        boolean valide = false;

        while (!valide) {
            try {
                // ───────────────────────────────────────
                // Champs obligatoires / guidés
                // ───────────────────────────────────────
                System.out.print("User ID (>0) : ");
                a.setUserId(Long.parseLong(sc.nextLine().trim()));

                System.out.print("Type (MENSUEL / ANNUEL / PREMIUM) : ");
                String typeSaisie = sc.nextLine().trim().toUpperCase();
                a.setType(Abonnement.TypeAbonnement.valueOf(typeSaisie));

                System.out.print("Date début (AAAA-MM-JJ) : ");
                a.setDateDebut(LocalDate.parse(sc.nextLine().trim()));

                System.out.print("Date fin   (AAAA-MM-JJ) : ");
                a.setDateFin(LocalDate.parse(sc.nextLine().trim()));

                System.out.print("Prix (ex: 19.99) : ");
                a.setPrix(new BigDecimal(sc.nextLine().trim()));

                System.out.print("Statut (ACTIF / EN_ATTENTE) [défaut: ACTIF] : ");
                String statutStr = sc.nextLine().trim().toUpperCase();
                if (statutStr.isEmpty()) statutStr = "ACTIF";
                a.setStatut(Abonnement.StatutAbonnement.valueOf(statutStr));

                System.out.print("Auto-renew (true/false) [défaut: true] : ");
                String autoStr = sc.nextLine().trim();
                a.setAutoRenew(autoStr.isEmpty() || autoStr.equalsIgnoreCase("true"));

                // ───────────────────────────────────────
                // Champs optionnels (points & churn)
                // ───────────────────────────────────────
                System.out.print("Points accumulés (0-10000) [Entrée = 0] : ");
                String pointsStr = sc.nextLine().trim();
                a.setPointsAccumules(pointsStr.isEmpty() ? 0 : Integer.parseInt(pointsStr));

                System.out.print("Churn score (0.0-1.0) [Entrée = 0.0] : ");
                String churnStr = sc.nextLine().trim();
                a.setChurnScore(churnStr.isEmpty() ? 0.0 : Double.parseDouble(churnStr));

                // Valeur par défaut pour avantages si tu en veux
                a.setAvantages(new HashMap<>());

                // On tente la création
                Abonnement created = abonnementService.create(a);
                System.out.println("\n✅ Abonnement créé avec succès !");
                System.out.println(created);
                valide = true;

            } catch (NumberFormatException e) {
                System.out.println("❌ Format invalide (nombre attendu).");
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ Erreur de validation : " + e.getMessage());
                System.out.println("Veuillez corriger et réessayer.\n");
            } catch (DateTimeParseException e) {
                System.out.println("❌ Format de date invalide. Utilisez AAAA-MM-JJ\n");
            } catch (Exception e) {
                System.out.println("❌ Erreur inattendue : " + e.getMessage());
                e.printStackTrace(); // pour le debug
            }
        }
    }

    private static void mettreAJourAbonnementAvecControles() {
        System.out.print("ID à mettre à jour : ");
        Long id = Long.parseLong(sc.nextLine());
        abonnementService.findById(id).ifPresentOrElse(existing -> {
            Abonnement a = existing; // Modifier l'existant
            boolean valide = false;
            while (!valide) {
                try {
                    System.out.print("Nouveau type (MENSUEL/ANNUEL/PREMIUM, vide pour garder) : ");
                    String typeStr = sc.nextLine();
                    if (!typeStr.isBlank()) a.setType(Abonnement.TypeAbonnement.valueOf(typeStr.toUpperCase()));

                    System.out.print("Nouvelle date début (AAAA-MM-JJ, vide pour garder) : ");
                    String debutStr = sc.nextLine();
                    if (!debutStr.isBlank()) a.setDateDebut(parseDate(debutStr));

                    System.out.print("Nouvelle date fin (AAAA-MM-JJ, vide pour garder) : ");
                    String finStr = sc.nextLine();
                    if (!finStr.isBlank()) a.setDateFin(parseDate(finStr));

                    System.out.print("Nouveau prix (ex: 19.99, vide pour garder) : ");
                    String prixStr = sc.nextLine();
                    if (!prixStr.isBlank()) a.setPrix(new BigDecimal(prixStr));

                    System.out.print("Nouveau statut (ACTIF/EXPIRE/SUSPENDU/EN_ATTENTE, vide pour garder) : ");
                    String statutStr = sc.nextLine();
                    if (!statutStr.isBlank()) a.setStatut(Abonnement.StatutAbonnement.valueOf(statutStr.toUpperCase()));

                    System.out.print("Nouveau auto-renew (true/false, vide pour garder) : ");
                    String autoStr = sc.nextLine();
                    if (!autoStr.isBlank()) a.setAutoRenew(Boolean.parseBoolean(autoStr));

                    System.out.print("Nouveaux points (vide pour garder) : ");
                    String pointsStr = sc.nextLine();
                    if (!pointsStr.isBlank()) a.setPointsAccumules(Integer.parseInt(pointsStr));

                    System.out.print("Nouveau churn score (vide pour garder) : ");
                    String churnStr = sc.nextLine();
                    if (!churnStr.isBlank()) a.setChurnScore(Double.parseDouble(churnStr));

                    Abonnement updated = abonnementService.update(a);
                    System.out.println("✅ Mis à jour : " + updated);
                    valide = true;
                } catch (IllegalArgumentException e) {
                    System.out.println("⚠️ Erreur : " + e.getMessage());
                    System.out.println("Veuillez corriger et réessayer.");
                } catch (Exception e) {
                    System.out.println("❌ Erreur inattendue : " + e.getMessage());
                }
            }
        }, () -> System.out.println("Introuvable"));
    }

    private static LocalDate parseDate(String str) {
        try {
            return LocalDate.parse(str, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format date invalide : AAAA-MM-JJ");
        }
    }


    // ======================= PROGRAMME RECOMMENDER =======================
    private static final ProgrammeRecommenderService programmeRecommenderService = new ProgrammeRecommenderService();
    private static final ProgrammeRecommenderController programmeRecommenderController = new ProgrammeRecommenderController();

    private static void gestionProgrammeRecommender() {
        int choix;
        do {
            System.out.println("\n======================================");
            System.out.println("   GESTION DES PROGRAMMES RECOMMANDÉS   ");
            System.out.println("======================================");
            System.out.println("1) Générer et sauvegarder programmes recommandés pour une participation");
            System.out.println("2) Lister tous les programmes recommandés");
            System.out.println("3) Lister les programmes d'une participation spécifique");
            System.out.println("4) Afficher les programmes par ambiance");
            System.out.println("5) Rechercher les programmes par mot-clé dans l'activité");
            System.out.println("6) Lister les programmes avec horaires valides");
            System.out.println("7) Lister les programmes recommandés (recommande = true)");
            System.out.println("8) Lister les programmes non recommandés (recommande = false)");
            System.out.println("9) Supprimer tous les programmes d'une participation");
            System.out.println("10) Afficher statistiques rapides des programmes");
            System.out.println("0) Retour au menu principal");
            System.out.println("======================================");
            System.out.print("👉 Votre choix : ");

            String line = sc.nextLine().trim();

            try {
                choix = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("\n→ Choix invalide. Veuillez entrer un nombre entre 0 et 10.");
                choix = -1;
                continue;
            }

            System.out.println();

            try {
                switch (choix) {
                    case 1  -> genererEtSauvegarderProgrammes();
                    case 2  -> afficherTousLesProgrammes();
                    case 3  -> listerProgrammesParParticipation();
                    case 4  -> filtrerParAmbiance();
                    case 5  -> rechercherParActivite();
                    case 6  -> afficherProgrammesValides();
                    case 7  -> afficherProgrammesRecommandes();
                    case 8  -> afficherProgrammesNonRecommandes();
                    case 9  -> supprimerProgrammesParParticipation();
                    case 10 -> afficherStatsProgrammes();
                    case 0  -> System.out.println("Retour au menu principal...");
                    default -> System.out.println("Choix invalide ! Veuillez entrer un numéro entre 0 et 10.");
                }
            } catch (Exception e) {
                System.out.println("❌ Erreur : " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println();
        } while (choix != 0);
    }

    // ────────────────────────────────────────────────
// 1. Générer et sauvegarder programmes recommandés
// ────────────────────────────────────────────────
    private static void genererEtSauvegarderProgrammes() {
        try {
            System.out.print("ID de la participation : ");
            String idStr = sc.nextLine().trim();
            if (idStr.isEmpty()) throw new IllegalArgumentException("ID participation obligatoire");
            Long participationId = Long.parseLong(idStr);

            System.out.println("[DEBUG] ID saisi : " + participationId);

            Participation participation = new Participation();
            participation.setId(participationId);

            System.out.print("Contexte social (COUPLE/AMIS/FAMILLE/SOLO/PROFESSIONNEL) : ");
            String contexteStr = sc.nextLine().trim().toUpperCase();
            if (contexteStr.isEmpty()) {
                System.out.println("→ Contexte non fourni → génération impossible.");
                return;
            }

            try {
                participation.setContexteSocial(Participation.ContexteSocial.valueOf(contexteStr));
                System.out.println("[DEBUG] Contexte validé : " + participation.getContexteSocial());
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Contexte invalide. Valeurs possibles : COUPLE, AMIS, FAMILLE, SOLO, PROFESSIONNEL");
                return;
            }

            List<ProgrammeRecommender> programmes = programmeRecommenderService.genererProgramme(participation);

            System.out.println("[DEBUG] Nombre de programmes générés : " + programmes.size());

            if (programmes.isEmpty()) {
                System.out.println("→ Aucun programme généré pour ce contexte.");
                return;
            }

            System.out.println("\nProgrammes générés (" + programmes.size() + ") :");
            programmes.forEach(prog -> System.out.println("  • " + prog));

            System.out.print("\nVoulez-vous sauvegarder ces programmes en base ? (oui/non) : ");
            String reponse = sc.nextLine().trim().toLowerCase();

            if (!reponse.equals("oui") && !reponse.equals("o") && !reponse.isEmpty()) {
                System.out.println("[DEBUG] Sauvegarde annulée par l'utilisateur");
                return;
            }

            System.out.println("[DEBUG] Début de la sauvegarde...");

            int savedCount = 0;
            for (ProgrammeRecommender prog : programmes) {
                savedCount++;
                System.out.print("  Insertion " + savedCount + "/" + programmes.size() + " → " + prog.getActivite() + " ... ");
                try {
                    programmeRecommenderController.save(prog);
                    System.out.println("OK (ID = " + prog.getId() + ")");
                } catch (Exception ex) {
                    System.out.println("ÉCHEC ! " + ex.getClass().getSimpleName() + " : " + ex.getMessage());
                }
            }

            System.out.println("[DEBUG] Sauvegarde terminée (" + savedCount + " insertions tentées)");

        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur de saisie : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur globale : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ────────────────────────────────────────────────
// 2. Lister tous les programmes recommandés
// ────────────────────────────────────────────────
    private static void afficherTousLesProgrammes() {
        List<ProgrammeRecommender> all = programmeRecommenderController.findAll();
        if (all.isEmpty()) {
            System.out.println("Aucun programme recommandé enregistré pour le moment.");
        } else {
            System.out.println("Tous les programmes recommandés (" + all.size() + ") :");
            all.forEach(System.out::println);
        }
    }

    // ────────────────────────────────────────────────
// 3. Lister les programmes d'une participation spécifique
// ────────────────────────────────────────────────
    private static void listerProgrammesParParticipation() {
        try {
            System.out.print("ID de la participation : ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) throw new IllegalArgumentException("ID obligatoire");
            Long participationId = Long.parseLong(input);

            System.out.println("[DEBUG] Recherche pour participation_id = " + participationId);

            List<ProgrammeRecommender> programmes = programmeRecommenderController.findByParticipation(participationId);

            System.out.println("[DEBUG] Nombre de programmes trouvés : " + programmes.size());

            if (programmes.isEmpty()) {
                System.out.println("Aucun programme associé à la participation " + participationId);
                System.out.println("[DEBUG] Vérification manuelle suggérée : SELECT * FROM programme_recommande WHERE participation_id = " + participationId + ";");
            } else {
                System.out.println("\nProgrammes recommandés pour participation " + participationId + " (" + programmes.size() + ") :");
                programmes.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un nombre valide");
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ────────────────────────────────────────────────
// 4. Afficher les programmes par ambiance
// ────────────────────────────────────────────────
    private static void filtrerParAmbiance() {
        try {
            System.out.print("Ambiance (CALME / FESTIVE / SOCIALE / AVENTURE / CULTURELLE) : ");
            String ambianceStr = sc.nextLine().trim().toUpperCase();
            if (ambianceStr.isEmpty()) throw new IllegalArgumentException("Ambiance obligatoire");

            ProgrammeRecommender.Ambiance ambiance = ProgrammeRecommender.Ambiance.valueOf(ambianceStr);

            System.out.print("ID participation (vide = tous) : ");
            String idStr = sc.nextLine().trim();

            List<ProgrammeRecommender> source;
            if (idStr.isEmpty()) {
                source = programmeRecommenderController.findAll();
            } else {
                Long participationId = Long.parseLong(idStr);
                source = programmeRecommenderController.findByParticipation(participationId);
            }

            List<ProgrammeRecommender> filtered = source.stream()
                    .filter(p -> p.getAmbiance() == ambiance)
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                System.out.println("Aucun programme avec l'ambiance " + ambiance);
            } else {
                System.out.println("\nProgrammes en ambiance " + ambiance + " (" + filtered.size() + ") :");
                filtered.forEach(System.out::println);
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ ID participation invalide");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 5. Rechercher les programmes par mot-clé dans l'activité
// ────────────────────────────────────────────────
    private static void rechercherParActivite() {
        try {
            System.out.print("Mot-clé dans l'activité : ");
            String motCle = sc.nextLine().trim().toLowerCase();
            if (motCle.isEmpty()) throw new IllegalArgumentException("Mot-clé obligatoire");

            System.out.print("ID participation (vide = tous) : ");
            String idStr = sc.nextLine().trim();

            List<ProgrammeRecommender> source;
            if (idStr.isEmpty()) {
                source = programmeRecommenderController.findAll();
            } else {
                Long participationId = Long.parseLong(idStr);
                source = programmeRecommenderController.findByParticipation(participationId);
            }

            List<ProgrammeRecommender> result = source.stream()
                    .filter(p -> p.getActivite().toLowerCase().contains(motCle))
                    .collect(Collectors.toList());

            if (result.isEmpty()) {
                System.out.println("Aucun programme contenant '" + motCle + "'");
            } else {
                System.out.println("\nRésultats pour '" + motCle + "' (" + result.size() + ") :");
                result.forEach(System.out::println);
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ ID participation invalide");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 6. Lister les programmes avec horaires valides
// ────────────────────────────────────────────────
    private static void afficherProgrammesValides() {
        try {
            System.out.print("ID participation (vide = tous) : ");
            String idStr = sc.nextLine().trim();

            List<ProgrammeRecommender> source;
            if (idStr.isEmpty()) {
                source = programmeRecommenderController.findAll();
            } else {
                Long participationId = Long.parseLong(idStr);
                source = programmeRecommenderController.findByParticipation(participationId);
            }

            List<ProgrammeRecommender> valides = source.stream()
                    .filter(ProgrammeRecommender::estValide)
                    .collect(Collectors.toList());

            if (valides.isEmpty()) {
                System.out.println("Aucun programme avec des horaires valides.");
            } else {
                System.out.println("\nProgrammes avec horaires valides (" + valides.size() + ") :");
                valides.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID participation invalide");
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 7. Lister les programmes recommandés (recommande = true)
// ────────────────────────────────────────────────
    private static void afficherProgrammesRecommandes() {
        try {
            System.out.print("ID participation (vide = tous) : ");
            String idStr = sc.nextLine().trim();

            List<ProgrammeRecommender> source;
            if (idStr.isEmpty()) {
                source = programmeRecommenderController.findAll();
            } else {
                Long participationId = Long.parseLong(idStr);
                source = programmeRecommenderController.findByParticipation(participationId);
            }

            List<ProgrammeRecommender> recommandes = source.stream()
                    .filter(ProgrammeRecommender::isRecommande)
                    .collect(Collectors.toList());

            if (recommandes.isEmpty()) {
                System.out.println("Aucun programme recommandé.");
            } else {
                System.out.println("\nProgrammes recommandés (" + recommandes.size() + ") :");
                recommandes.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID participation invalide");
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 8. Lister les programmes non recommandés (recommande = false)
// ────────────────────────────────────────────────
    private static void afficherProgrammesNonRecommandes() {
        try {
            System.out.print("ID participation (vide = tous) : ");
            String idStr = sc.nextLine().trim();

            List<ProgrammeRecommender> source;
            if (idStr.isEmpty()) {
                source = programmeRecommenderController.findAll();
            } else {
                Long participationId = Long.parseLong(idStr);
                source = programmeRecommenderController.findByParticipation(participationId);
            }

            List<ProgrammeRecommender> nonRecommandes = source.stream()
                    .filter(p -> !p.isRecommande())
                    .collect(Collectors.toList());

            if (nonRecommandes.isEmpty()) {
                System.out.println("Aucun programme non recommandé.");
            } else {
                System.out.println("\nProgrammes non recommandés (" + nonRecommandes.size() + ") :");
                nonRecommandes.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID participation invalide");
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 9. Supprimer tous les programmes d'une participation
// ────────────────────────────────────────────────
    private static void supprimerProgrammesParParticipation() {
        try {
            System.out.print("ID de la participation à nettoyer : ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) throw new IllegalArgumentException("ID obligatoire");
            Long participationId = Long.parseLong(input);

            System.out.println("[DEBUG] Suppression des programmes pour participation_id = " + participationId);

            programmeRecommenderController.deleteByParticipation(participationId);
            System.out.println("→ Tous les programmes de la participation " + participationId + " ont été supprimés avec succès.");
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un nombre valide");
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
// 10. Afficher statistiques rapides des programmes
// ────────────────────────────────────────────────
    private static void afficherStatsProgrammes() {
        System.out.println("\n=== Statistiques programmes recommandés ===");
        try {
            System.out.print("ID participation (vide = tous) : ");
            String idStr = sc.nextLine().trim();

            List<ProgrammeRecommender> source;
            if (idStr.isEmpty()) {
                source = programmeRecommenderController.findAll();
                System.out.println("[DEBUG] Statistiques globales (tous programmes)");
            } else {
                Long participationId = Long.parseLong(idStr);
                source = programmeRecommenderController.findByParticipation(participationId);
                System.out.println("[DEBUG] Statistiques pour participation " + participationId);
            }

            long total = source.size();
            long calmes = source.stream().filter(p -> p.getAmbiance() == ProgrammeRecommender.Ambiance.CALME).count();
            long festifs = source.stream().filter(p -> p.getAmbiance() == ProgrammeRecommender.Ambiance.FESTIVE).count();
            long sociaux = source.stream().filter(p -> p.getAmbiance() == ProgrammeRecommender.Ambiance.SOCIALE).count();
            long recommandes = source.stream().filter(ProgrammeRecommender::isRecommande).count();

            System.out.printf("Total programmes              : %d%n", total);
            System.out.printf("Recommandés                   : %d (%.1f%%)%n",
                    recommandes, total > 0 ? (double) recommandes / total * 100 : 0);
            System.out.printf("Ambiance CALME                : %d%n", calmes);
            System.out.printf("Ambiance FESTIVE              : %d%n", festifs);
            System.out.printf("Ambiance SOCIALE              : %d%n", sociaux);

        } catch (NumberFormatException e) {
            System.out.println("❌ ID participation invalide");
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'affichage des stats : " + e.getMessage());
        }
    }

}

