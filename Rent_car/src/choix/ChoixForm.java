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
        setSize(500, 300);
        setResizable(false); // 🔒 empêche le redimensionnement
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // === Panel Formulaire avec GridBagLayout ===
        JPanel panelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tfNom = new JTextField(20);
        tfDescription = new JTextField(20);
        tfPrix = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelForm.add(new JLabel("Nom de l’option :"), gbc);
        gbc.gridx = 1;
        panelForm.add(tfNom, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panelForm.add(new JLabel("Description :"), gbc);
        gbc.gridx = 1;
        panelForm.add(tfDescription, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panelForm.add(new JLabel("Prix ($) :"), gbc);
        gbc.gridx = 1;
        panelForm.add(tfPrix, gbc);

        add(panelForm, BorderLayout.CENTER);

        // === Boutons centrés ===
        btnAjouter = new JButton("Ajouter");
        btnRetour = new JButton("Retour");

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelButtons.add(btnAjouter);
        panelButtons.add(btnRetour);

        add(panelButtons, BorderLayout.SOUTH);

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
