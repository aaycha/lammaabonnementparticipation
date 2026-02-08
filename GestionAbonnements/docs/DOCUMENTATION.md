# Documentation détaillée – Module Abonnements, Participations et Recommandations

## 1. Vue d’ensemble du module

Module dédié à la gestion des **abonnements** (memberships récurrents pour accès privilégié aux événements), des **participations** (inscriptions individuelles ou groupées aux événements : camping, randonnées, hébergements) et des **recommandations personnalisées** basées sur le contexte social et les préférences (ex. couple → tente 2p matelas queen, amis → tente 5p barbecue/vin).

### 1.1 Technologies

- **Java 17**, **Maven**, **IntelliJ IDEA**
- **MySQL** (WAMP / LAMMA) : base `lamma_db`
- **JavaFX** : interface (FXML)
- **Structure** : `controllers` (controllers), `entities` (entities), `services` (services), `interfaces` (contrats service), `criteria` (recherche/tri), `tools` (DBConnection)

---

## 2. Entités et contraintes métier

### 2.1 Abonnement

| Attribut | Type | Contraintes |
|----------|------|-------------|
| ID | PK | Auto |
| UserID | FK | Référence utilisateurs |
| Type | enum | MENSUEL, ANNUEL, PREMIUM |
| DateDébut, DateFin | date | DateFin > DateDébut + 1 mois |
| Prix | decimal | > 0 |
| Statut | enum | ACTIF, EXPIRE, SUSPENDU, EN_ATTENTE |
| Avantages | JSON | {discounts, prioriteWaiting, ...} |
| AutoRenew | bool | Défaut false, consentement GDPR |
| PointsAccumulés | int | ≥ 0 |
| ChurnScore | float | ML (TensorFlow predict sur historique) |

**Relations** : One-to-Many avec Participation et Recommandation ; Many-to-One avec User.  
**Contraintes** : unique (User, Type) ; Auto-renew avec consentement ; Prix > 0 ; DateFin > DateDébut + 1 mois.

### 2.2 Participation

| Attribut | Type | Contraintes |
|----------|------|-------------|
| ID | PK | Auto |
| UserID, ÉvénementID | FK | User, Événement |
| DateInscription | timestamp | |
| Type | enum | SIMPLE, HEBERGEMENT, GROUPE |
| Statut | enum | EN_ATTENTE, CONFIRME, ANNULE, EN_LISTE_ATTENTE |
| HébergementNuits | int | ≤ durée Événement |
| ContexteSocial | enum | COUPLE, AMIS, FAMILLE, SOLO, PROFESSIONNEL |
| ÉquipementsRecommandés | JSON | {tente, extras, ...} |
| BadgeAssocié | string | Ex. "Aventurier Bronze" après 3 participations |

**Relations** : Many-to-One User, Événement ; One-to-Many Paiement ; Many-to-Many Recommandation (via jointure).  
**Contraintes** : capacité événement vérifiée en transaction ; HébergementNuits ≤ durée événement ; Contexte requis pour recommandations IA.

### 2.3 Recommandation

| Attribut | Type | Contraintes |
|----------|------|-------------|
| ID | PK | Auto |
| UserID, ÉvénementSuggéréID | FK | |
| Score | float | 0–1 ; > 0.5 pour push |
| Raison | text | IA-generated |
| AlgorithmeUsed | enum | COLLABORATIVE, CONTENT, NLP, HYBRIDE, ML_TENSORFLOW, CLUSTERING |
| ÉquipementBundle | JSON | {pour_couple, pour_amis, ...} |
| SourceScraped | url | Optionnel |
| DateExpiration | timestamp | Expiration 7 jours si non utilisée |

**Contraintes** : Score > 0.5 pour notifications push ; JSON validé ; nettoyage des reco obsolètes (ex. événement annulé).

---

## 3. SCRUD (Search, Create, Read, Update, Delete)

### 3.1 Abonnement

- **Create**  
  - Valider User actif, calculer Prix selon Type (ex. annuel = mensuel×10 −10 %), générer Avantages JSON, AutoRenew par défaut false, ChurnScore initial (ML/TensorFlow sur historique).  
  - Workflow : email de confirmation + webhook Stripe premier paiement (logué).

- **Read**  
  - Filtrage : par Statut, DateFin proche (rappel renouvellement).  
  - Agrégation : total PointsAccumulés.  
  - API équivalente : `GET /api/abonnements?statut=actif&sort=dateDebut DESC`.

- **Update**  
  - Changer Type (upgrade/downgrade avec prorata), toggle AutoRenew (consentement logué).  
  - Si Statut → expiré : notifier user et bloquer accès premium.

- **Delete**  
  - Autorisé seulement s’il n’y a pas de participations actives liées.

- **Recherche et tri**  
  - Critères : `AbonnementCriteria` (statut, type, userId, dateFinAvant/Apres, autoRenew, pointsMinimum, sortBy, sortOrder).  
  - Méthodes : `search(AbonnementCriteria)`, `findAll(sortBy, sortOrder)`, `getTotalPointsAccumules()`, `getTotalPointsAccumulesByUserId(userId)`.

### 3.2 Participation

- **Create**  
  - Vérifier capacité événement (locking DB row), définir ContexteSocial (saisie ou inférence IA/NLP), générer ÉquipementsRecommandés (TensorFlow + scraping).  
  - Workflow : liste d’attente si complet ; badge si récurrent (ex. après 3 → "Aventurier Bronze").

- **Read**  
  - Détails liés (Événement titre, User nom), filtrage par ContexteSocial (ex. couple).  
  - API : `GET /api/participations/event/:eventId` (organisateur).

- **Update**  
  - Changer Statut (ex. waiting → confirmé), modifier HébergementNuits avec reco ajustée.  
  - Si annulé < 48 h avant : pénalité 50 % (Paiement) ; AR preview on-update.

- **Delete**  
  - Libérer place (capacité), notifier liste d’attente.  
  - Remboursement auto si > 7 jours avant ; log analytics churn.

- **Recherche et tri**  
  - Critères : `ParticipationCriteria` (userId, evenementId, statut, type, contexteSocial, dateFrom/To, sortBy, sortOrder).  
  - Méthodes : `search(ParticipationCriteria)`, `findAll(sortBy, sortOrder)`.

### 3.3 Recommandation

- **Create**  
  - Création manuelle ou par algorithme (collaborative, NLP, hybride, etc.).  
  - Contraintes : score 0–1, raison, algorithme, date d’expiration (7 jours par défaut).

- **Read**  
  - Personnalisé par User : top N par Score DESC.  
  - API : `GET /api/recommandations/user/:userId?contexte=couple` (ex. top 5).

- **Update**  
  - Mise à jour des champs (score, raison, equipementBundle, dateExpiration, estUtilisee).  
  - Utilisé pour marquer comme utilisée ou prolonger.

- **Delete**  
  - Si reco obsolète (ex. événement annulé).  
  - Workflow : clean-up périodique.

- **Recherche et tri**  
  - Critères : `RecommandationCriteria` (userId, contexte, scoreMinimum, algorithme, validesSeulement, limite, sortBy, sortOrder).  
  - Méthodes : `search(RecommandationCriteria)`, `findTopByUserId(userId, contexte, limite)`, `findAll(sortBy, sortOrder)`.

---

## 4. Utilisation des Streams (Java)

Les services utilisent l’API **Stream** pour le filtrage, le tri et les agrégations en mémoire (après chargement depuis la BDD lorsque c’est le cas).

### 4.1 AbonnementServiceImpl

- **search(criteria)** :  
  `findAll()` puis `stream()` avec `filter` sur statut, type, userId, dateFin, autoRenew, pointsMinimum ; tri via `sortAbonnements()` avec `Comparator` (dateDebut, dateFin, prix, statut, pointsAccumules, churnScore) et ordre ASC/DESC.

- **getTotalPointsAccumules()** :  
  `findAll().stream().map(a -> BigDecimal.valueOf(a.getPointsAccumules())).reduce(BigDecimal.ZERO, BigDecimal::add)`.

- **sortAbonnements()** :  
  `list.stream().sorted(cmp).toList()` avec `Comparator` selon `sortBy` / `sortOrder`.

### 4.2 ParticipationServiceImpl

- **search(criteria)** :  
  `findAll().stream()` + filtres sur userId, evenementId, statut, type, contexteSocial, dateFrom/To ; puis `sortParticipations()` avec comparateur sur dateInscription, statut, type, hebergementNuits, contexte.

- **findListeAttente(evenementId)** :  
  `findByEvenementId(...).stream().filter(p -> statut == EN_LISTE_ATTENTE).sorted(...).collect(Collectors.toList())`.

### 4.3 RecommandationServiceImpl

- **findTopByUserId(userId, contexte, limite)** :  
  `findByUserId(userId).stream().filter(score >= 0.5).filter(non expirée).filter(non utilisée).filter(contexte si fourni).sorted(score DESC).limit(limite).toList()`.

- **search(criteria)** :  
  Stream sur liste (par userId ou findAll) avec filtres scoreMin, algorithme, validesSeulement, contexte ; puis `sortRecommandations()` et `limit(criteria.getLimite())`.

- **findByScoreMinimum**, **findRecommandationsValides**, **findRecommandationsPrioritaires** :  
  `findAll().stream().filter(...).collect(Collectors.toList())`.

---

## 5. Controllers (SCRUD + recherche + tri)

- **AbonnementController**  
  - Create, getById, getAll, getAll(sortBy, sortOrder), search(statut, type, userId, sortBy, sortOrder), search(AbonnementCriteria), findProchesExpiration, getTotalPointsAccumules, getTotalPointsAccumulesByUser, update, delete, toggleAutoRenew.

- **ParticipationController**  
  - Create, getById, getAll, getAll(sortBy, sortOrder), getByEvenementId, getByUserId, search(statut, type, contexte, evenementId, userId, sortBy, sortOrder), search(ParticipationCriteria), update, delete, confirmer, annuler, getPlacesDisponibles.

- **RecommandationController**  
  - Create, getById, getAll, getAll(sortBy, sortOrder), getTopByUser(userId, contexte, limite), getByUserId, search(RecommandationCriteria), search(userId, contexte, scoreMin, sortBy, sortOrder, limite), update, delete.

Le **MainController** JavaFX utilise les services (et critères pour les abonnements) pour alimenter les tables, filtres et recherche.

---

## 6. Base de données (MySQL / WAMP)

- **Script** : `docs/database.sql`.  
- **Base** : `lamma_db` (CREATE DATABASE si nécessaire).  
- **Tables** : `utilisateurs`, `evenements`, `abonnements`, `participations`, `recommandations`, `paiements`, `notifications`, `activite_logs`.  
- **Contraintes** : FK, CHECK (prix > 0, date_fin > date_debut, DATEDIFF ≥ 30 pour abonnements, score 0–1 pour recommandations, etc.), UNIQUE (user_id, evenement_id) pour participations.  
- **Vues** : `v_stat_abonnements`, `v_participations_evenement`, `v_performance_recommandations`.  
- **Triggers** : mise à jour popularité événement, log création abonnement.  
- **Procédures** : `sp_calculer_churn_score`, `sp_generer_recommandations_automatiques`.  
- **Connexion** : `com.gestion.tools.DBConnection` (Singleton, URL `jdbc:mysql://localhost:3306/lamma_db`, user/password à adapter pour WAMP).

---

## 7. Équivalence API (référence)

Pour une future couche REST, le comportement peut s’aligner sur :

- **Abonnements**  
  - `GET /api/abonnements?statut=actif&sort=dateDebut DESC`  
  - Création, mise à jour, suppression via AbonnementController + critères.

- **Participations**  
  - `GET /api/participations/event/:eventId`  
  - Filtrage par ContexteSocial, statut, type ; tri par dateInscription, statut, etc.

- **Recommandations**  
  - `GET /api/recommandations/user/:userId?contexte=couple`  
  - Top 5 (ou N) par score DESC, score > 0.5 pour push.

---

## 8. Fichiers principaux

| Rôle | Fichiers |
|------|----------|
| Entités | `entities/Abonnement.java`, `Participation.java`, `Recommandation.java` |
| Interfaces | `interfaces/AbonnementService.java`, `ParticipationService.java`, `RecommandationService.java` |
| Services | `services/AbonnementServiceImpl.java`, `ParticipationServiceImpl.java`, `RecommandationServiceImpl.java` |
| Critères | `criteria/AbonnementCriteria.java`, `ParticipationCriteria.java`, `RecommandationCriteria.java` |
| Controllers | `controllers/AbonnementController.java`, `ParticipationController.java`, `RecommandationController.java`, `MainController.java` |
| DB | `tools/DBConnection.java`, `docs/database.sql` |

---

## 9. Résumé des validations métier

- **Abonnement** : User actif, Prix > 0, DateFin > DateDébut + 1 mois, unique (User, Type), pas de suppression si participations actives, AutoRenew avec consentement.  
- **Participation** : Capacité événement, HébergementNuits ≤ durée événement, ContexteSocial requis pour reco, statut et annulation avec pénalité/remboursement.  
- **Recommandation** : Score 0–1, > 0.5 pour push, expiration 7 jours, suppression si obsolète (événement annulé), JSON et algorithme cohérents.

Cette documentation décrit le SCRUD complet, la recherche, le tri, les validations métier, l’usage des Streams, les controllers et la base MySQL (LAMMA / WAMP) du module.
