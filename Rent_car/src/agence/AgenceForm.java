package agence;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class AgenceForm extends JFrame {
    private JTextField tfNom, tfAdresse, tfTelephone, tfEmail;

    public AgenceForm() {
        setTitle("Ajouter une agence");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(5, 2, 10, 10));

        add(new JLabel("Nom de l'agence :"));
        tfNom = new JTextField();
        add(tfNom);

        add(new JLabel("Adresse :"));
        tfAdresse = new JTextField();
        add(tfAdresse);

        add(new JLabel("Téléphone :"));
        tfTelephone = new JTextField();
        add(tfTelephone);

        add(new JLabel("Email :"));
        tfEmail = new JTextField();
        add(tfEmail);

        JButton btnAjouter = new JButton("Ajouter");
        btnAjouter.addActionListener(e -> ajouterAgence());
        add(btnAjouter);

        JButton btnRetour = new JButton("Retour au menu");
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });
        add(btnRetour);

        setVisible(true);
    }

    private void ajouterAgence() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO AGENCE (Nom_agence, Adresse_agence, Telephone_agence, Email_agence) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tfNom.getText());
            ps.setString(2, tfAdresse.getText());
            ps.setString(3, tfTelephone.getText());
            ps.setString(4, tfEmail.getText());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Agence ajoutée !");
            this.dispose();
            new MainMenu();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }
}
