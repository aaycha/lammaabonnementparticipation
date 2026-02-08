/*package com.gestion.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    // Configuration de la base de données
    private static final String URL = "jdbc:mysql://localhost:3306/LAMA";
    private static final String LOGIN = "root";
    private static final String PWD = "";

    private static MyConnection instance;
    private Connection cnx;

    // Constructeur privé (Singleton)
    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(URL, LOGIN, PWD);
            System.out.println("Connexion à la base de données LAMA établie avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    // Retourne l'instance unique
    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    // Retourne la connexion active
    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = DriverManager.getConnection(URL, LOGIN, PWD);
                System.out.println("Reconnexion à la base de données LAMA réussie !");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ouverture de la connexion : " + e.getMessage());
        }
        return cnx;
    }

    // Fermer la connexion proprement
    public void closeConnection() {
        try {
            if (cnx != null && !cnx.isClosed()) {
                cnx.close();
                System.out.println("Connexion à la base de données fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }
}*/


package com.gestion.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton de connexion à la base de données LAMA
 */
public class MyConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/lamma_db";
    private static final String LOGIN = "root";
    private static final String PWD = "";

    private static MyConnection instance;
    private static Connection cnx;

    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(URL, LOGIN, PWD);
            System.out.println("Connexion à la base de données LAMMA établie !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    // ✅ MÉTHODE UTILISÉE DANS LE SERVICE
    public static Connection getConnection() {
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = DriverManager.getConnection(URL, LOGIN, PWD);
            }
        } catch (SQLException e) {
            System.err.println("Erreur ouverture connexion : " + e.getMessage());
        }
        return cnx;
    }

    public void closeConnection() {
        try {
            if (cnx != null && !cnx.isClosed()) {
                cnx.close();
                System.out.println("Connexion fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur fermeture connexion : " + e.getMessage());
        }
    }
}

