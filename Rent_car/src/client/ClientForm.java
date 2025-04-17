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
        setSize(500, 350);
        setResizable(false); // Empêche le redimensionnement
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // === Panel formulaire ===
        JPanel panelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tfNom = new JTextField(20);
        tfPrenom = new JTextField(20);
        tfEmail = new JTextField(20);
        tfTelephone = new JTextField(20);
        tfAdresse = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0; panelForm.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1; panelForm.add(tfNom, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panelForm.add(new JLabel("Prénom:"), gbc);
        gbc.gridx = 1; panelForm.add(tfPrenom, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panelForm.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; panelForm.add(tfEmail, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panelForm.add(new JLabel("Téléphone:"), gbc);
        gbc.gridx = 1; panelForm.add(tfTelephone, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panelForm.add(new JLabel("Adresse:"), gbc);
        gbc.gridx = 1; panelForm.add(tfAdresse, gbc);

        add(panelForm, BorderLayout.CENTER);

        // === Panel boutons ===
        btnAjouter = new JButton("Ajouter");
        btnRetour = new JButton("Retour au menu");

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelButtons.add(btnAjouter);
        panelButtons.add(btnRetour);
        add(panelButtons, BorderLayout.SOUTH);

        // === Actions ===
        btnAjouter.addActionListener(e -> ajouterClient());
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        setVisible(true);
    }

    private void ajouterClient() {
        String nom = tfNom.getText().trim();
        String prenom = tfPrenom.getText().trim();
        String email = tfEmail.getText().trim();
        String telephone = tfTelephone.getText().trim();
        String adresse = tfAdresse.getText().trim();

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

            JOptionPane.showMessageDialog(this, "✅ Client ajouté avec succès !");
            this.dispose();
            new MainMenu();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + ex.getMessage());
        }
    }
}
