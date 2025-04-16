package client;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import pack.DatabaseConnection;

public class ClientEdit extends JFrame {
    private JTextField tfId, tfNom, tfPrenom, tfEmail, tfTelephone, tfAdresse;
    private JButton btnCharger, btnModifier;

    public ClientEdit() {
        setTitle("Modifier un client");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(8, 2, 5, 5));

        add(new JLabel("ID Client:"));
        tfId = new JTextField();
        add(tfId);

        btnCharger = new JButton("Retour");
        add(btnCharger);
        add(new JLabel(""));

        add(new JLabel("Nom:"));       tfNom = new JTextField(); add(tfNom);
        add(new JLabel("Prénom:"));    tfPrenom = new JTextField(); add(tfPrenom);
        add(new JLabel("Email:"));     tfEmail = new JTextField(); add(tfEmail);
        add(new JLabel("Téléphone:")); tfTelephone = new JTextField(); add(tfTelephone);
        add(new JLabel("Adresse:"));   tfAdresse = new JTextField(); add(tfAdresse);

        btnModifier = new JButton("Modifier");
        add(btnModifier);

        btnCharger.addActionListener(e -> chargerClient());
        btnModifier.addActionListener(e -> modifierClient());
        
        JButton btnRetour = new JButton("Retour au menu");
        btnRetour.addActionListener(e -> {
            this.dispose();
            new pack.MainMenu();
        });
        add(btnRetour);


        setVisible(true);
    }

    private void chargerClient() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM CLIENT WHERE IdClient = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(tfId.getText()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tfNom.setText(rs.getString("Nom"));
                tfPrenom.setText(rs.getString("Prenom"));
                tfEmail.setText(rs.getString("Email_client"));
                tfTelephone.setText(rs.getString("Telephone_client"));
                tfAdresse.setText(rs.getString("Adresse_client"));
            } else {
                JOptionPane.showMessageDialog(this, "Client non trouvé.");
            }
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void modifierClient() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE CLIENT SET Nom = ?, Prenom = ?, Email_client = ?, Telephone_client = ?, Adresse_client = ? WHERE IdClient = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tfNom.getText());
            ps.setString(2, tfPrenom.getText());
            ps.setString(3, tfEmail.getText());
            ps.setString(4, tfTelephone.getText());
            ps.setString(5, tfAdresse.getText());
            ps.setInt(6, Integer.parseInt(tfId.getText()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Modification réussie !");
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Aucune modification.");
            }
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }
    public ClientEdit(int id) {
        this(); // Appelle le constructeur par défaut
        tfId.setText(String.valueOf(id));
        tfId.setEditable(false); // Désactive la modification
        chargerClient(); // Charge directement les données
    }

}
