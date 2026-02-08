-- =====================================================
-- Base de données LAMMA - Gestion Abonnements et Participations
-- Version : 1.0.0
-- Auteur : Système de Gestion
-- =====================================================

-- Création de la base de données
CREATE DATABASE IF NOT EXISTS lamma_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE lamma_db;

-- =====================================================
-- TABLE DES UTILISATEURS
-- =====================================================
CREATE TABLE IF NOT EXISTS utilisateurs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    telephone VARCHAR(20),
    date_naissance DATE,
    adresse TEXT,
    ville VARCHAR(100),
    code_postal VARCHAR(10),
    pays VARCHAR(50) DEFAULT 'Tunisie',
    preferences JSON,
    statut ENUM('ACTIF', 'INACTIF', 'SUSPENDU') DEFAULT 'ACTIF',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_mise_a_jour TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_statut (statut),
    INDEX idx_ville (ville)
) ENGINE=InnoDB;

-- =====================================================
-- TABLE DES ÉVÉNEMENTS
-- =====================================================
CREATE TABLE IF NOT EXISTS evenement (
    id_event INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT DEFAULT NULL,
    date_debut DATETIME NOT NULL,
    date_fin DATETIME DEFAULT NULL,
    lieu VARCHAR(255) NOT NULL,
    type VARCHAR(30) NOT NULL,
    
    INDEX idx_titre (titre),
    INDEX idx_dates (date_debut, date_fin),
    INDEX idx_lieu (lieu),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE DES ABONNEMENTS
-- =====================================================
CREATE TABLE IF NOT EXISTS abonnements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type ENUM('MENSUEL', 'ANNUEL', 'PREMIUM') NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    prix DECIMAL(10,2) NOT NULL CHECK (prix > 0),
    statut ENUM('ACTIF', 'EXPIRE', 'SUSPENDU', 'EN_ATTENTE') NOT NULL DEFAULT 'ACTIF',
    avantages JSON,
    auto_renew BOOLEAN DEFAULT FALSE,
    points_accumules INT DEFAULT 0 CHECK (points_accumules >= 0),
    churn_score FLOAT DEFAULT 0.0 CHECK (churn_score >= 0 AND churn_score <= 1),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_statut (statut),
    INDEX idx_dates (date_debut, date_fin),
    INDEX idx_auto_renew (auto_renew),
    INDEX idx_churn_score (churn_score),
    
    -- Contraintes métier
    CONSTRAINT chk_date_fin CHECK (date_fin > date_debut),
    CONSTRAINT chk_duree_minimale CHECK (DATEDIFF(date_fin, date_debut) >= 30)
) ENGINE=InnoDB;

-- =====================================================
-- TABLE DES PARTICIPATIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS participations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    evenement_id BIGINT NOT NULL,
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type ENUM('SIMPLE', 'HEBERGEMENT', 'GROUPE') NOT NULL DEFAULT 'SIMPLE',
    statut ENUM('EN_ATTENTE', 'CONFIRME', 'ANNULE', 'EN_LISTE_ATTENTE') NOT NULL DEFAULT 'EN_ATTENTE',
    hebergement_nuits INT DEFAULT 0 CHECK (hebergement_nuits >= 0),
    contexte_social ENUM('COUPLE', 'AMIS', 'FAMILLE', 'SOLO', 'PROFESSIONNEL'),
    badge_associe VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (evenement_id) REFERENCES evenements(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_evenement_id (evenement_id),
    INDEX idx_statut (statut),
    INDEX idx_type (type),
    INDEX idx_contexte_social (contexte_social),
    INDEX idx_date_inscription (date_inscription),
    INDEX idx_badge (badge_associe),
    
    -- Contrainte d'unicité : un utilisateur ne peut participer qu'une fois par événement
    UNIQUE KEY uk_user_evenement (user_id, evenement_id),
    
    -- Contrainte métier
    CONSTRAINT chk_hebergement_type CHECK (
        (type = 'HEBERGEMENT' AND hebergement_nuits > 0) OR 
        (type != 'HEBERGEMENT' AND hebergement_nuits = 0)
    )
) ENGINE=InnoDB;

-- =====================================================
-- TABLE DES TICKETS
-- =====================================================
CREATE TABLE IF NOT EXISTS tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    participation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type ENUM('TICKET', 'BADGE', 'PASS') NOT NULL,
    code_unique VARCHAR(100) UNIQUE NOT NULL,
    latitude DOUBLE,
    longitude DOUBLE,
    lieu VARCHAR(200),
    statut ENUM('VALIDE', 'UTILISE', 'EXPIRE', 'ANNULE') NOT NULL DEFAULT 'VALIDE',
    format ENUM('NUMERIQUE', 'PHYSIQUE', 'HYBRIDE') NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_expiration TIMESTAMP,
    qr_code VARCHAR(200),
    informations_supplementaires TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (participation_id) REFERENCES participations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_participation_id (participation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_statut (statut),
    INDEX idx_format (format),
    INDEX idx_code_unique (code_unique),
    INDEX idx_date_creation (date_creation),
    INDEX idx_date_expiration (date_expiration),
    INDEX idx_coordonnees (latitude, longitude),
    INDEX idx_lieu (lieu)
) ENGINE=InnoDB;

-- =====================================================
-- TABLE DES RECOMMANDATIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS recommandations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    evenement_suggere_id BIGINT NOT NULL,
    score FLOAT NOT NULL CHECK (score >= 0 AND score <= 1),
    raison TEXT,
    algorithme_used ENUM('COLLABORATIVE', 'CONTENT_BASED', 'NLP', 'HYBRIDE', 'ML_TENSORFLOW', 'CLUSTERING') NOT NULL,
    equipement_bundle JSON,
    source_scraped VARCHAR(500),
    date_generation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_expiration TIMESTAMP NOT NULL,
    est_utilisee BOOLEAN DEFAULT FALSE,
    interaction_count INT DEFAULT 0,
    conversion_score FLOAT DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (evenement_suggere_id) REFERENCES evenement(id_event) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_evenement_suggere_id (evenement_suggere_id),
    INDEX idx_score (score),
    INDEX idx_algorithme (algorithme_used),
    INDEX idx_date_generation (date_generation),
    INDEX idx_date_expiration (date_expiration),
    INDEX idx_est_utilisee (est_utilisee),
    INDEX idx_conversion_score (conversion_score),
    
    -- Contrainte métier
    CONSTRAINT chk_date_expiration CHECK (date_expiration > date_generation)
) ENGINE=InnoDB;

-- =====================================================
-- TABLE DES PAIEMENTS
-- =====================================================
CREATE TABLE IF NOT EXISTS paiements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    abonnement_id BIGINT,
    participation_id BIGINT,
    montant DECIMAL(10,2) NOT NULL CHECK (montant > 0),
    devise VARCHAR(3) DEFAULT 'TND',
    methode ENUM('CARTE', 'VIREMENT', 'ESPECE', 'STRIPE', 'PAYPAL') NOT NULL,
    statut ENUM('EN_ATTENTE', 'CONFIRME', 'ECHOUE', 'REMBOURSE') NOT NULL DEFAULT 'EN_ATTENTE',
    transaction_id VARCHAR(100),
    date_paiement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_confirmation TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (abonnement_id) REFERENCES abonnements(id) ON DELETE SET NULL,
    FOREIGN KEY (participation_id) REFERENCES participations(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_abonnement_id (abonnement_id),
    INDEX idx_participation_id (participation_id),
    INDEX idx_statut (statut),
    INDEX idx_date_paiement (date_paiement),
    INDEX idx_montant (montant),
    
    -- Contrainte : soit abonnement soit participation, pas les deux
    CONSTRAINT chk_type_paiement CHECK (
        (abonnement_id IS NOT NULL AND participation_id IS NULL) OR 
        (abonnement_id IS NULL AND participation_id IS NOT NULL) OR
        (abonnement_id IS NULL AND participation_id IS NULL)
    )
) ENGINE=InnoDB;

-- =====================================================
-- TABLE DES NOTIFICATIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type ENUM('INFO', 'SUCCESS', 'WARNING', 'ERROR', 'PROMOTION') NOT NULL,
    titre VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    url_action VARCHAR(500),
    est_lue BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_lecture TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_est_lue (est_lue),
    INDEX idx_date_creation (date_creation)
) ENGINE=InnoDB;

-- =====================================================
-- TABLE DES LOGS ACTIVITÉS
-- =====================================================
CREATE TABLE IF NOT EXISTS activite_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entite VARCHAR(50) NOT NULL, -- 'ABONNEMENT', 'PARTICIPATION', 'RECOMMANDATION'
    entite_id BIGINT,
    details JSON,
    adresse_ip VARCHAR(45),
    user_agent TEXT,
    date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_entite (entite),
    INDEX idx_date_action (date_action)
) ENGINE=InnoDB;

-- =====================================================
-- VUES UTILES
-- =====================================================

-- Vue des statistiques des abonnements
CREATE OR REPLACE VIEW v_stat_abonnements AS
SELECT 
    type,
    statut,
    COUNT(*) as nombre,
    AVG(prix) as prix_moyen,
    SUM(prix) as revenu_total,
    AVG(points_accumules) as points_moyens,
    AVG(churn_score) as churn_moyen
FROM abonnements 
GROUP BY type, statut;

-- Vue des participations par événement
CREATE OR REPLACE VIEW v_participations_evenement AS
SELECT 
    e.id as evenement_id,
    e.titre,
    e.categorie,
    e.date_debut,
    e.capacite,
    COUNT(p.id) as nb_inscrits,
    SUM(CASE WHEN p.statut = 'CONFIRME' THEN 1 ELSE 0 END) as nb_confirmes,
    SUM(CASE WHEN p.statut = 'EN_LISTE_ATTENTE' THEN 1 ELSE 0 END) as nb_liste_attente,
    ROUND(COUNT(p.id) * 100.0 / e.capacite, 2) as taux_remplissage
FROM evenements e
LEFT JOIN participations p ON e.id = p.evenement_id
GROUP BY e.id, e.titre, e.categorie, e.date_debut, e.capacite;

-- Vue des recommandations performantes
CREATE OR REPLACE VIEW v_performance_recommandations AS
SELECT 
    algorithme_used,
    COUNT(*) as nb_recommandations,
    AVG(score) as score_moyen,
    SUM(CASE WHEN est_utilisee THEN 1 ELSE 0 END) as nb_utilisees,
    ROUND(SUM(CASE WHEN est_utilisee THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as taux_conversion,
    AVG(conversion_score) as conversion_moyenne
FROM recommandations 
WHERE date_generation >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY algorithme_used;

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Trigger pour mettre à jour la popularité des événements
DELIMITER //
CREATE TRIGGER tr_update_evenement_popularite
AFTER INSERT ON participations
FOR EACH ROW
BEGIN
    UPDATE evenements 
    SET popularite = (
        SELECT COUNT(*) 
        FROM participations 
        WHERE evenement_id = NEW.evenement_id AND statut = 'CONFIRME'
    )
    WHERE id_event = NEW.evenement_id;
END//
DELIMITER ;

-- Trigger pour logger les activités importantes
DELIMITER //
CREATE TRIGGER tr_log_abonnement_creation
AFTER INSERT ON abonnements
FOR EACH ROW
BEGIN
    INSERT INTO activite_logs (user_id, action, entite, entite_id, details)
    VALUES (NEW.user_id, 'CREATION', 'ABONNEMENT', NEW.id, 
            JSON_OBJECT('type', NEW.type, 'prix', NEW.prix));
END//
DELIMITER ;

-- =====================================================
-- DONNÉES DE TEST
-- =====================================================

-- Insertion d'utilisateurs de test
INSERT IGNORE INTO utilisateurs (id, nom, email, ville) VALUES
(1, 'Admin Système', 'admin@lamma.com', 'Tunis'),
(2, 'Mohamed Ben Ali', 'mohamed.benali@email.com', 'Sfax'),
(3, 'Sonia Trabelsi', 'sonia.trabelsi@email.com', 'Sousse'),
(4, 'Karim Mejri', 'karim.mejri@email.com', 'Monastir'),
(5, 'Leila Khaled', 'leila.khaled@email.com', 'Nabeul');

-- Insertion d'événements de test (selon structure gestion_evenements.sql)
INSERT IGNORE INTO evenement (id_event, titre, description, date_debut, date_fin, lieu, type) VALUES
(1, 'Camping Ain Draham', 'Week-end nature', '2026-02-13 16:26:21', '2026-02-14 16:26:21', 'Ain Draham', ''),
(3, 'soiree saint valentin', 'soiree pour couple', '2025-02-14 19:00:00', '2025-02-15 00:00:00', 'barbie', ''),
(4, 'fuego', 'soire', '2026-02-06 19:00:00', NULL, 'sousse', 'SOIREE'),
(5, 'soire', '14bla bla', '2025-02-17 19:00:00', NULL, '74', 'SOIREE');

-- Insertion de programmes de test
INSERT IGNORE INTO programme (id_prog, event_id, titre, debut, fin) VALUES
(1, 1, 'Installation', '2026-02-13 17:26:21', '2026-02-13 18:26:21'),
(2, 1, 'Feu de camp', '2026-02-13 21:26:21', '2026-02-13 23:26:21'),
(4, 3, 'prog soire', '2025-02-14 19:00:00', '2025-02-14 21:00:00');

-- Insertion d'abonnements de test
INSERT IGNORE INTO abonnements (user_id, type, date_debut, date_fin, prix, statut, auto_renew, points_accumules) VALUES
(2, 'MENSUEL', '2024-01-01', '2024-12-31', 29.99, 'ACTIF', TRUE, 250),
(3, 'ANNUEL', '2024-01-01', '2024-12-31', 299.99, 'ACTIF', TRUE, 500),
(4, 'PREMIUM', '2024-02-15', '2024-12-31', 499.99, 'ACTIF', TRUE, 750),
(5, 'MENSUEL', '2024-03-01', '2024-12-31', 29.99, 'SUSPENDU', FALSE, 120);

-- Insertion de participations de test
INSERT IGNORE INTO participations (user_id, evenement_id, type, statut, hebergement_nuits, contexte_social, badge_associe) VALUES
(2, 1, 'HEBERGEMENT', 'CONFIRME', 2, 'COUPLE', 'Romantique_Aventure'),
(3, 2, 'GROUPE', 'CONFIRME', 2, 'AMIS', 'Esprit_Equipe'),
(4, 3, 'SIMPLE', 'EN_LISTE_ATTENTE', 0, 'SOLO', 'Explorateur_Solitaire'),
(5, 4, 'HEBERGEMENT', 'CONFIRME', 2, 'FAMILLE', 'Famille_Unie');

-- Insertion de tickets de test
INSERT IGNORE INTO tickets (participation_id, user_id, type, code_unique, latitude, longitude, lieu, statut, format, qr_code) VALUES
(1, 2, 'TICKET', 'TKT-1700000000-1', 36.8065, 10.1815, 'Parc National Belvédère', 'VALIDE', 'NUMERIQUE', 'QR-TKT-1700000000-1-36.8065-10.1815'),
(2, 3, 'BADGE', 'TKT-1700000001-2', 36.4029, 10.1425, 'Montagne Zaghouan', 'VALIDE', 'PHYSIQUE', 'QR-TKT-1700000001-2-36.4029-10.1425'),
(4, 5, 'PASS', 'TKT-1700000002-4', 33.8080, 10.8530, 'Resort Djerba', 'VALIDE', 'HYBRIDE', 'QR-TKT-1700000002-4-33.8080-10.8530');

-- Insertion de recommandations de test
INSERT IGNORE INTO recommandations (user_id, evenement_suggere_id, score, raison, algorithme_used, equipement_bundle, date_expiration) VALUES
(2, 4, 0.92, 'Parfait pour couple avec spa et détente (score IA: 92%)', 'NLP', 
 '{"pour_couple": {"tente": "2p_confort_queen", "matelas": "epais_luxe"}}', 
 DATE_ADD(NOW(), INTERVAL 7 DAY)),
(3, 1, 0.85, 'Recommandé basé sur vos préférences et celles d''utilisateurs similaires (score: 85%)', 'COLLABORATIVE', 
 '{"pour_couple": {"tente": "couple_matelas_queen", "loisir": "spa_portable"}}', 
 DATE_ADD(NOW(), INTERVAL 7 DAY)),
(4, 2, 0.78, 'Idéal pour groupe d''amis avec activités extérieures (score: 78%)', 'HYBRIDE', 
 '{"pour_amis": {"tente": "5p_spacieuse", "extras": "barbecue_vin"}}', 
 DATE_ADD(NOW(), INTERVAL 7 DAY));

-- =====================================================
-- PROCÉDURES STOCKÉES
-- =====================================================

DELIMITER //
-- Procédure pour calculer le churn score
CREATE PROCEDURE sp_calculer_churn_score(IN p_user_id BIGINT)
BEGIN
    DECLARE v_nb_participations INT DEFAULT 0;
    DECLARE v_derniere_participation DATE;
    DECLARE v_score FLOAT DEFAULT 0.0;
    
    -- Compter les participations des 6 derniers mois
    SELECT COUNT(*), MAX(DATE(p.date_inscription))
    INTO v_nb_participations, v_derniere_participation
    FROM participations p
    WHERE p.user_id = p_user_id 
    AND p.date_inscription >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
    AND p.statut = 'CONFIRME';
    
    -- Calculer le score de churn
    IF v_nb_participations < 3 THEN
        SET v_score = 0.8;
    ELSEIF v_nb_participations < 5 THEN
        SET v_score = 0.5;
    ELSE
        SET v_score = 0.2;
    END IF;
    
    -- Ajuster selon la date de dernière participation
    IF v_derniere_participation < DATE_SUB(NOW(), INTERVAL 3 MONTH) THEN
        SET v_score = LEAST(1.0, v_score + 0.2);
    END IF;
    
    -- Mettre à jour le score
    UPDATE abonnements 
    SET churn_score = v_score 
    WHERE user_id = p_user_id AND statut = 'ACTIF';
    
    SELECT v_score as churn_score_calculé;
END//

-- Procédure pour générer les recommandations automatiques
CREATE PROCEDURE sp_generer_recommandations_automatiques(IN p_user_id BIGINT, IN p_limite INT)
BEGIN
    DECLARE v_evenement_id BIGINT;
    DECLARE v_score FLOAT;
    DECLARE v_raison TEXT;
    DECLARE done INT DEFAULT FALSE;
    
    -- Curseur pour les événements candidats
    DECLARE curseur_evenements CURSOR FOR
        SELECT e.id, 
               (SELECT AVG(r.score) FROM recommandations r WHERE r.evenement_suggere_id = e.id) as score_moyen
        FROM evenements e
        WHERE e.id NOT IN (
            SELECT evenement_id FROM participations WHERE user_id = p_user_id
        )
        AND e.date_debut > NOW()
        ORDER BY score_moyen DESC, e.popularite DESC
        LIMIT p_limite;
    
    OPEN curseur_evenements;
    
    read_loop: LOOP
        FETCH curseur_evenements INTO v_evenement_id, v_score;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Générer une recommandation
        SET v_raison = CONCAT('Recommandation automatique basée sur la popularité (score: ', ROUND(v_score * 100, 1), '%)');
        
        INSERT INTO recommandations (user_id, evenement_suggere_id, score, raison, algorithme_used, date_expiration)
        VALUES (p_user_id, v_evenement_id, v_score, v_raison, 'HYBRIDE', DATE_ADD(NOW(), INTERVAL 7 DAY));
        
    END LOOP;
    
    CLOSE curseur_evenements;
END//

DELIMITER ;

-- =====================================================
-- FIN DU SCRIPT
-- =====================================================

-- Affichage des statistiques initiales
SELECT 'Base de données LAMMA initialisée avec succès!' as message;
SELECT COUNT(*) as nb_utilisateurs FROM utilisateurs;
SELECT COUNT(*) as nb_evenements FROM evenements;
SELECT COUNT(*) as nb_abonnements FROM abonnements;
SELECT COUNT(*) as nb_participations FROM participations;
SELECT COUNT(*) as nb_recommandations FROM recommandations;
