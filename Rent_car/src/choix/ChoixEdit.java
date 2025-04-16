package choix;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

import pack.DatabaseConnection;

public class ChoixEdit extends JFrame {
    private int idChoix;
    private JTextField tfNom, tfDescription, tfPrix;
    private JButton btnModifier, btnRetour;

    public ChoixEdit(int idChoix) {
        this.idChoix = idChoix;
        setTitle("Modifier un choix (option)");
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
        btnModifier = new JButton("Modifier");
        btnRetour = new JButton("Retour");

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnRetour);
        add(panelBoutons, BorderLayout.SOUTH);

        // === Actions ===
        btnRetour.addActionListener(e -> this.dispose());
        btnModifier.addActionListener(e -> modifierChoix());

        chargerChoix();
        setVisible(true);
    }

    private void chargerChoix() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM CHOIX WHERE IdChoix = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idChoix);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                tfNom.setText(rs.getString("Nom_choix"));
                tfDescription.setText(rs.getString("Description_choix"));
                tfPrix.setText(String.valueOf(rs.getDouble("Prix_choix")));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement : " + e.getMessage());
        }
    }

    private void modifierChoix() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String nom = tfNom.getText().trim();
            String description = tfDescription.getText().trim();
            double prix = Double.parseDouble(tfPrix.getText());

            if (nom.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.");
                return;
            }

            String sql = "UPDATE CHOIX SET Nom_choix=?, Description_choix=?, Prix_choix=? WHERE IdChoix=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nom);
            ps.setString(2, description);
            ps.setDouble(3, prix);
            ps.setInt(4, idChoix);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Option modifiée !");
            this.dispose();

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Le prix doit être un nombre valide.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage());
        }
    }
}
