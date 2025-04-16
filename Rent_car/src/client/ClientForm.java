package client;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class ClientForm extends JFrame {
    private JTextField tfNom, tfPrenom, tfEmail, tfTelephone, tfAdresse;
    private JButton btnAjouter, btnRetour;

    public ClientForm() {
        setTitle("Ajouter un client");
        setSize(450, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // === Panel pour les champs ===
        JPanel panelForm = new JPanel(new GridLayout(5, 2, 10, 10));
        tfNom = new JTextField();
        tfPrenom = new JTextField();
        tfEmail = new JTextField();
        tfTelephone = new JTextField();
        tfAdresse = new JTextField();

        panelForm.add(new JLabel("Nom:"));        panelForm.add(tfNom);
        panelForm.add(new JLabel("Prénom:"));     panelForm.add(tfPrenom);
        panelForm.add(new JLabel("Email:"));      panelForm.add(tfEmail);
        panelForm.add(new JLabel("Téléphone:"));  panelForm.add(tfTelephone);
        panelForm.add(new JLabel("Adresse:"));    panelForm.add(tfAdresse);

        add(panelForm, BorderLayout.CENTER);

        // === Panel pour les boutons ===
        btnAjouter = new JButton("Ajouter");
        btnRetour = new JButton("Retour au menu");

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnRetour);
        add(panelBoutons, BorderLayout.SOUTH);

        // Actions des boutons
        btnAjouter.addActionListener(e -> ajouterClient());
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

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
            this.dispose();
            new MainMenu();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + ex.getMessage());
        }
    }
}
