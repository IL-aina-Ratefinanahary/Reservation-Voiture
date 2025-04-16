package choix;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

import pack.DatabaseConnection;
import pack.MainMenu;

public class ChoixForm extends JFrame {
    private JTextField tfNom, tfDescription, tfPrix;
    private JButton btnAjouter, btnRetour;

    public ChoixForm() {
        setTitle("Ajouter un choix (option)");
        setSize(450, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // === Formulaire ===
        JPanel panelForm = new JPanel(new GridLayout(4, 2, 10, 10));

        tfNom = new JTextField();
        tfDescription = new JTextField();
        tfPrix = new JTextField();

        panelForm.add(new JLabel("Nom de l’option :"));     panelForm.add(tfNom);
        panelForm.add(new JLabel("Description :"));         panelForm.add(tfDescription);
        panelForm.add(new JLabel("Prix ($) :"));            panelForm.add(tfPrix);

        add(panelForm, BorderLayout.CENTER);

        // === Boutons ===
        btnAjouter = new JButton("Ajouter");
        btnRetour = new JButton("Retour");

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnRetour);
        add(panelBoutons, BorderLayout.SOUTH);

        // === Actions ===
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        btnAjouter.addActionListener(e -> ajouterChoix());

        setVisible(true);
    }

    private void ajouterChoix() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String nom = tfNom.getText().trim();
            String description = tfDescription.getText().trim();
            double prix = Double.parseDouble(tfPrix.getText());

            if (nom.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.");
                return;
            }

            String sql = "INSERT INTO CHOIX (Nom_choix, Description_choix, Prix_choix) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nom);
            ps.setString(2, description);
            ps.setDouble(3, prix);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Option ajoutée avec succès !");
            this.dispose();
            new MainMenu();

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Le prix doit être un nombre.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage());
        }
    }
}
