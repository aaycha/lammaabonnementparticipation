/*package com.gestion;

import com.gestion.entities.*;
import com.gestion.services.*;
import com.gestion.interfaces.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class ConsoleTestMain {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        ParticipationService participationService = new ParticipationServiceImpl();
        RecommandationService recommandationService = new RecommandationServiceImpl();
        TicketService ticketService = new TicketServiceImpl();
        AbonnementService abonnementService = new AbonnementServiceImpl();
        ProgrammeService programmeService = new ProgrammerecomanderServiceImpl();

        int choix;

        do {
            afficherMenuPrincipal();
            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> menuParticipation(participationService);
                case 2 -> menuRecommandation(recommandationService);
                case 3 -> menuTicket(ticketService);
                case 4 -> menuAbonnement(abonnementService);
                case 5 -> menuProgramme(programmeService);
                case 0 -> System.out.println("Fin du programme.");
                default -> System.out.println("Choix invalide !");
            }

        } while (choix != 0);
    }

    // =======================
    // MENU PRINCIPAL
    // =======================

    private static void afficherMenuPrincipal() {
        System.out.println("\n====== MENU PRINCIPAL ======");
        System.out.println("1. Gestion des Participations");
        System.out.println("2. Gestion des Recommandations");
        System.out.println("3. Gestion des Tickets");
        System.out.println("4. Gestion des Abonnements");
        System.out.println("5. Gestion des Programmes");
        System.out.println("0. Quitter");
        System.out.print("Votre choix : ");
    }

    // =======================
    // MENU PARTICIPATION
    // =======================

    private static void menuParticipation(ParticipationService service) {
        int choix;

        do {
            System.out.println("\n--- MENU PARTICIPATION ---");
            System.out.println("1. Ajouter participation");
            System.out.println("2. Afficher toutes les participations");
            System.out.println("3. Rechercher par statut");
            System.out.println("4. Confirmer participation");
            System.out.println("5. Annuler participation");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> ajouterParticipation(service);
                case 2 -> afficherParticipations(service.findAll());
                case 3 -> rechercherParticipationParStatut(service);
                case 4 -> confirmerParticipation(service);
                case 5 -> annulerParticipation(service);
            }

        } while (choix != 0);
    }

    private static void ajouterParticipation(ParticipationService service) {
        try {
            System.out.print("User ID : ");
            Long userId = scanner.nextLong();

            System.out.print("Evenement ID : ");
            Long eventId = scanner.nextLong();
            scanner.nextLine();

            System.out.print("Type (SIMPLE/HEBERGEMENT/GROUPE) : ");
            String typeStr = scanner.nextLine();
            Participation.TypeParticipation type = Participation.TypeParticipation.valueOf(typeStr.toUpperCase());

            System.out.print("Contexte (COUPLE/AMIS/FAMILLE/SOLO/PROFESSIONNEL) : ");
            String contexteStr = scanner.nextLine();
            Participation.ContexteSocial contexte = Participation.ContexteSocial.valueOf(contexteStr.toUpperCase());

            Participation p = new Participation(userId, eventId, type, contexte);
            service.create(p);
            System.out.println("Participation ajoutée avec succès !");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void afficherParticipations(List<Participation> list) {
        if (list.isEmpty()) {
            System.out.println("Aucune participation trouvée.");
            return;
        }
        list.forEach(System.out::println);
    }

    private static void rechercherParticipationParStatut(ParticipationService service) {
        System.out.println("Statut (EN_ATTENTE / CONFIRME / ANNULE / EN_LISTE_ATTENTE) : ");
        try {
            Participation.StatutParticipation statut =
                    Participation.StatutParticipation.valueOf(scanner.nextLine().toUpperCase());
            afficherParticipations(service.findByStatut(statut));
        } catch (IllegalArgumentException e) {
            System.out.println("Statut invalide !");
        }
    }

    private static void confirmerParticipation(ParticipationService service) {
        System.out.print("ID participation : ");
        try {
            Long id = scanner.nextLong();
            service.confirmerParticipation(id);
            System.out.println("Participation confirmée !");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void annulerParticipation(ParticipationService service) {
        System.out.print("ID participation : ");
        try {
            Long id = scanner.nextLong();
            scanner.nextLine();
            System.out.print("Raison : ");
            String raison = scanner.nextLine();
            service.annulerParticipation(id, raison);
            System.out.println("Participation annulée !");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // =======================
    // MENU RECOMMANDATION
    // =======================

    private static void menuRecommandation(RecommandationService service) {
        int choix;

        do {
            System.out.println("\n--- MENU RECOMMANDATION ---");
            System.out.println("1. Générer recommandation");
            System.out.println("2. Afficher toutes les recommandations");
            System.out.println("3. Marquer recommandation utilisée");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> genererRecommandation(service);
                case 2 -> afficherRecommandations(service.findAll());
                case 3 -> marquerRecommandation(service);
            }

        } while (choix != 0);
    }

    private static void genererRecommandation(RecommandationService service) {
        try {
            System.out.print("User ID : ");
            Long userId = scanner.nextLong();

            System.out.print("Evenement ID suggéré : ");
            Long eventId = scanner.nextLong();
            scanner.nextLine();

            System.out.print("Score (0.0-1.0) : ");
            double score = scanner.nextDouble();
            scanner.nextLine();

            System.out.print("Raison : ");
            String raison = scanner.nextLine();

            System.out.print("Algorithme (COLLABORATIVE/CONTENT_BASED/NLP/HYBRIDE/ML_TENSORFLOW/CLUSTERING) : ");
            String algoStr = scanner.nextLine();
            Recommandation.AlgorithmeReco algo = Recommandation.AlgorithmeReco.valueOf(algoStr.toUpperCase());

            Recommandation r = new Recommandation(userId, eventId, score, raison, algo);
            service.create(r);
            System.out.println("Recommandation générée !");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void afficherRecommandations(List<Recommandation> list) {
        if (list.isEmpty()) {
            System.out.println("Aucune recommandation trouvée.");
            return;
        }
        list.forEach(System.out::println);
    }

    private static void marquerRecommandation(RecommandationService service) {
        System.out.print("ID recommandation : ");
        try {
            Long id = scanner.nextLong();
            service.marquerCommeUtilisee(id);
            System.out.println("Recommandation utilisée !");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // =======================
    // MENU TICKET
    // =======================

    private static void menuTicket(TicketService service) {
        int choix;

        do {
            System.out.println("\n--- MENU TICKET ---");
            System.out.println("1. Créer ticket selon coordonnées et choix");
            System.out.println("2. Afficher tous les tickets");
            System.out.println("3. Rechercher par participation");
            System.out.println("4. Rechercher par coordonnées");
            System.out.println("5. Valider ticket par code");
            System.out.println("6. Marquer ticket comme utilisé");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> creerTicketSelonChoix(service);
                case 2 -> afficherTickets(service.findAll());
                case 3 -> rechercherTicketParParticipation(service);
                case 4 -> rechercherTicketParCoordonnees(service);
                case 5 -> validerTicketParCode(service);
                case 6 -> marquerTicketUtilise(service);
            }

        } while (choix != 0);
    }

    private static void creerTicketSelonChoix(TicketService service) {
        try {
            System.out.print("Participation ID : ");
            Long participationId = scanner.nextLong();

            System.out.print("User ID : ");
            Long userId = scanner.nextLong();
            scanner.nextLine();

            System.out.print("Type (TICKET/BADGE/PASS) : ");
            String typeStr = scanner.nextLine();
            Ticket.TypeTicket type = Ticket.TypeTicket.valueOf(typeStr.toUpperCase());

            System.out.print("Latitude (optionnel, appuyez Entrée pour ignorer) : ");
            String latStr = scanner.nextLine();
            Double latitude = latStr.isEmpty() ? null : Double.parseDouble(latStr);

            System.out.print("Longitude (optionnel, appuyez Entrée pour ignorer) : ");
            String lonStr = scanner.nextLine();
            Double longitude = lonStr.isEmpty() ? null : Double.parseDouble(lonStr);

            System.out.print("Lieu : ");
            String lieu = scanner.nextLine();

            System.out.print("Format (NUMERIQUE/PHYSIQUE/HYBRIDE) : ");
            String formatStr = scanner.nextLine();
            Ticket.FormatTicket format = Ticket.FormatTicket.valueOf(formatStr.toUpperCase());

            Ticket ticket = service.creerTicketSelonChoix(participationId, userId, type, latitude, longitude, lieu, format);
            System.out.println("Ticket créé avec succès ! " + ticket);
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void afficherTickets(List<Ticket> list) {
        if (list.isEmpty()) {
            System.out.println("Aucun ticket trouvé.");
            return;
        }
        list.forEach(System.out::println);
    }

    private static void rechercherTicketParParticipation(TicketService service) {
        System.out.print("Participation ID : ");
        try {
            Long participationId = scanner.nextLong();
            afficherTickets(service.findByParticipationId(participationId));
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void rechercherTicketParCoordonnees(TicketService service) {
        try {
            System.out.print("Latitude : ");
            Double latitude = scanner.nextDouble();

            System.out.print("Longitude : ");
            Double longitude = scanner.nextDouble();

            System.out.print("Rayon (km) : ");
            Double rayon = scanner.nextDouble();

            afficherTickets(service.findByCoordonnees(latitude, longitude, rayon));
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void validerTicketParCode(TicketService service) {
        System.out.print("Code unique du ticket : ");
        String code = scanner.nextLine();
        boolean valide = service.validerTicket(code);
        System.out.println("Ticket " + (valide ? "valide" : "invalide ou expiré"));
    }

    private static void marquerTicketUtilise(TicketService service) {
        System.out.print("ID ticket : ");
        try {
            Long id = scanner.nextLong();
            service.marquerCommeUtilise(id);
            System.out.println("Ticket marqué comme utilisé !");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // =======================
    // MENU ABONNEMENT
    // =======================

    private static void menuAbonnement(AbonnementService service) {
        int choix;

        do {
            System.out.println("\n--- MENU ABONNEMENT ---");
            System.out.println("1. Créer abonnement");
            System.out.println("2. Afficher tous les abonnements");
            System.out.println("3. Rechercher par utilisateur");
            System.out.println("4. Rechercher par statut");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> creerAbonnement(service);
                case 2 -> afficherAbonnements(service.findAll());
                case 3 -> rechercherAbonnementParUser(service);
                case 4 -> rechercherAbonnementParStatut(service);
            }

        } while (choix != 0);
    }

    private static void creerAbonnement(AbonnementService service) {
        try {
            System.out.print("User ID : ");
            Long userId = scanner.nextLong();
            scanner.nextLine();

            System.out.print("Type (MENSUEL/ANNUEL/PREMIUM) : ");
            String typeStr = scanner.nextLine();
            Abonnement.TypeAbonnement type = Abonnement.TypeAbonnement.valueOf(typeStr.toUpperCase());

            System.out.print("Date début (YYYY-MM-DD) : ");
            String dateStr = scanner.nextLine();
            LocalDate dateDebut = LocalDate.parse(dateStr);

            System.out.print("Prix : ");
            BigDecimal prix = scanner.nextBigDecimal();
            scanner.nextLine();

            System.out.print("Auto-renew (true/false) : ");
            boolean autoRenew = scanner.nextBoolean();
            scanner.nextLine();

            Abonnement abonnement = new Abonnement(userId, type, dateDebut, prix, autoRenew);
            service.create(abonnement);
            System.out.println("Abonnement créé avec succès !");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void afficherAbonnements(List<Abonnement> list) {
        if (list.isEmpty()) {
            System.out.println("Aucun abonnement trouvé.");
            return;
        }
        list.forEach(System.out::println);
    }

    private static void rechercherAbonnementParUser(AbonnementService service) {
        System.out.print("User ID : ");
        try {
            Long userId = scanner.nextLong();
            afficherAbonnements(service.findByUserId(userId));
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void rechercherAbonnementParStatut(AbonnementService service) {
        System.out.print("Statut (ACTIF/EXPIRE/SUSPENDU/EN_ATTENTE) : ");
        try {
            String statutStr = scanner.nextLine();
            Abonnement.StatutAbonnement statut = Abonnement.StatutAbonnement.valueOf(statutStr.toUpperCase());
            afficherAbonnements(service.findByStatut(statut));
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // =======================
    // MENU PROGRAMME
    // =======================

    private static void menuProgramme(ProgrammeService service) {
        int choix;

        do {
            System.out.println("\n--- MENU PROGRAMME ---");
            System.out.println("1. Ajouter programme");
            System.out.println("2. Afficher tous les programmes");
            System.out.println("3. Rechercher par événement");
            System.out.println("4. Programmes en cours");
            System.out.println("5. Programmes à venir");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> ajouterProgramme(service);
                case 2 -> afficherProgrammes(service.findAll());
                case 3 -> rechercherProgrammeParEvent(service);
                case 4 -> afficherProgrammes(service.findProgrammesEnCours());
                case 5 -> afficherProgrammes(service.findProgrammesAVenir());
            }

        } while (choix != 0);
    }

    private static void ajouterProgramme(ProgrammeService service) {
        try {
            System.out.print("Event ID : ");
            Long eventId = scanner.nextLong();
            scanner.nextLine();

            System.out.print("Titre : ");
            String titre = scanner.nextLine();

            System.out.print("Date début (YYYY-MM-DDTHH:mm:ss) : ");
            String debutStr = scanner.nextLine();
            LocalDateTime debut = LocalDateTime.parse(debutStr);

            System.out.print("Date fin (YYYY-MM-DDTHH:mm:ss) : ");
            String finStr = scanner.nextLine();
            LocalDateTime fin = LocalDateTime.parse(finStr);

            ProgrammeRecommender programme = new ProgrammeRecommender(eventId, titre, debut, fin);
            service.create(programme);
            System.out.println("Programme ajouté avec succès !");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void afficherProgrammes(List<ProgrammeRecommender> list) {
        if (list.isEmpty()) {
            System.out.println("Aucun programme trouvé.");
            return;
        }
        list.forEach(System.out::println);
    }

    private static void rechercherProgrammeParEvent(ProgrammeService service) {
        System.out.print("Event ID : ");
        try {
            Long eventId = scanner.nextLong();
            afficherProgrammes(service.findByEventId(eventId));
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}*/
package com.gestion;

import com.gestion.entities.*;
import com.gestion.interfaces.*;
import com.gestion.services.*;

import com.gestion.controllers.EvenementDAO;
import com.gestion.controllers.ProgrammeDAO;
import com.gestion.entities.Evenement;
import com.gestion.entities.Programme;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

class ConsoleTestMain {

    private static final Scanner sc = new Scanner(System.in);

    // ===== SERVICES METIER =====
    private static final ParticipationService participationService = new ParticipationServiceImpl();
    private static final RecommandationService recommandationService = new RecommandationServiceImpl();
    private static final TicketService ticketService = new TicketServiceImpl();
    private static final AbonnementService abonnementService = new AbonnementServiceImpl();
    private static final ProgrammeService programmeRecoService = new ProgrammerecomanderServiceImpl();

    // ===== DAO JDBC =====
    private static final EvenementDAO eventDAO = new EvenementDAO();
    private static final ProgrammeDAO programmeDAO = new ProgrammeDAO();

    // ======================= MAIN =======================
    public static void main(String[] args) {
        while (true) {
            System.out.println("\n==============================");
            System.out.println("   MAIN DE TEST - CONSOLE");
            System.out.println("==============================");
            System.out.println("1) Evenements (CRUD JDBC)");
            System.out.println("2) Programmes EVENEMENT (JDBC)");
            System.out.println("3) Programmes RECOMMANDÉS");
            System.out.println("4) Participations");
            System.out.println("5) Recommandations");
            System.out.println("6) Tickets");
            System.out.println("7) Abonnements");
            System.out.println("0) Quitter");
            System.out.print("Choix : ");

            switch (sc.nextLine()) {
                case "1" -> afficherEvenements();
                case "2" -> afficherProgrammesEvent();
                case "3" -> afficherProgrammesRecommandes();
                case "4" -> testerParticipation();
                case "5" -> testerRecommandation();
                case "6" -> testerTicket();
                case "7" -> testerAbonnement();
                case "0" -> {
                    System.out.println("👋 Fin du test");
                    return;
                }
                default -> System.out.println("❌ Choix invalide");
            }
        }
    }

    // ======================= EVENEMENTS =======================
    private static void afficherEvenements() {
        try {
            List<Evenement> list = eventDAO.findAll();
            list.forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Erreur événements : " + e.getMessage());
        }
    }

    // ======================= PROGRAMME (JDBC) =======================
   private static void afficherProgrammesEvent() {
        try {
            System.out.print("ID événement : ");
            int eventId = Integer.parseInt(sc.nextLine());
            List<Programme> list = programmeDAO.findByEventId(eventId);
            list.forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Erreur programme événement : " + e.getMessage());
        }
    }

    // ======================= PROGRAMME RECOMMANDÉ =======================
    private static void afficherProgrammesRecommandes() {
        try {
            ProgrammeRecommender p = new ProgrammeRecommender(
                    1L,
                    "Programme suggéré IA",
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(1).plusHours(2)
            );
            programmeRecoService.create(p);
            programmeRecoService.findAll().forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Erreur programme recommandé : " + e.getMessage());
        }
    }

    // ======================= PARTICIPATION =======================
    private static void testerParticipation() {
        try {
            Participation p = new Participation(
                    1L,
                    1L,
                    Participation.TypeParticipation.SIMPLE,
                    Participation.ContexteSocial.AMIS
            );
            participationService.create(p);
            participationService.findAll().forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Erreur participation : " + e.getMessage());
        }
    }

    // ======================= RECOMMANDATION =======================
    private static void testerRecommandation() {
        try {
            Recommandation r = new Recommandation(
                    1L,
                    1L,
                    0.92,
                    "Basé sur l’historique utilisateur",
                    Recommandation.AlgorithmeReco.HYBRIDE
            );
            recommandationService.create(r);
            recommandationService.findAll().forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Erreur recommandation : " + e.getMessage());
        }
    }

    // ======================= TICKET =======================
    private static void testerTicket() {
        try {
            Ticket t = ticketService.creerTicketSelonChoix(
                    1L,
                    1L,
                    Ticket.TypeTicket.TICKET,
                    36.8,
                    10.2,
                    "Tunis",
                    Ticket.FormatTicket.NUMERIQUE
            );
            System.out.println(t);
        } catch (Exception e) {
            System.out.println("Erreur ticket : " + e.getMessage());
        }
    }

    // ======================= ABONNEMENT =======================
    private static void testerAbonnement() {
        try {
            Abonnement a = new Abonnement(
                    1L,
                    Abonnement.TypeAbonnement.PREMIUM,
                    LocalDate.now(),
                    new BigDecimal("59.99"),
                    true
            );
            abonnementService.create(a);
            abonnementService.findAll().forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Erreur abonnement : " + e.getMessage());
        }
    }
}

