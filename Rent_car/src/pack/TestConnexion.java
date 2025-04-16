package pack;

import java.sql.*;

public class TestConnexion {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/projetgr01",
                "root",
                "Mdp"        // a changer selon la BD
            );
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}

