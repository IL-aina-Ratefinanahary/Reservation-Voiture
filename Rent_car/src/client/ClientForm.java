package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import pack.DatabaseConnection;

public class ClientForm extends JFrame {
    private JTextField tfNom, tfPrenom, tfEmail, tfTelephone, tfAdresse;
    private JButton btnAjouter;

    public ClientForm() {
        setTitle("Ajouter un client");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(6, 2, 5, 5));

        // Champs
        add(new JLabel("Nom:"));
        tfNom = new JTextField();
        add(tfNom);

        add(new JLabel("Prénom:"));
        tfPrenom = new JTextField();
        add(tfPrenom);

        add(new JLabel("Email:"));
        tfEmail = new JTextField();
        add(tfEmail);

        add(new JLabel("Téléphone:"));
        tfTelephone = new JTextField();
        add(tfTelephone);

        add(new JLabel("Adresse:"));
        tfAdresse = new JTextField();
        add(tfAdresse);

        // Bouton
        btnAjouter = new JButton("Ajouter");
        add(btnAjouter);

        btnAjouter.addActionListener(e -> ajouterClient());

        // Case vide pour mise en forme
        add(new JLabel(""));

        setVisible(true);
    }

    private void ajouterClient() {
        String nom = tfNom.getText();
        String prenom = tfPrenom.getText();
        String email = tfEmail.getText();
        String telephone = tfTelephone.getText();
        String adresse = tfAdresse.getText();

        if (nom.isEmpty() || prenom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nom et prénom sont obligatoires !");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO CLIENT (Nom, Prenom, Email_client, Telephone_client, Adresse_client) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, email);
            ps.setString(4, telephone);
            ps.setString(5, adresse);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Client ajouté avec succès !");
            this.dispose(); // Ferme la fenêtre
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + ex.getMessage());
        }
    }
}
