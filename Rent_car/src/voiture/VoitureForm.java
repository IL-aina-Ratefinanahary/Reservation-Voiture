package voiture;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class VoitureForm extends JFrame {
    private JTextField tfMarque, tfModele, tfImmat, tfPrix;
    private JCheckBox chkDispo;
    private JComboBox<String> cbAgence;
    private JButton btnAjouter, btnRetour;

    public VoitureForm() {
        setTitle("Ajouter une voiture");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(8, 2, 5, 5));

        tfMarque = new JTextField();
        tfModele = new JTextField();
        tfImmat = new JTextField();
        tfPrix = new JTextField();
        chkDispo = new JCheckBox("Disponible");
        cbAgence = new JComboBox<>();

        add(new JLabel("Marque:"));           add(tfMarque);
        add(new JLabel("Modèle:"));           add(tfModele);
        add(new JLabel("Immatriculation:"));  add(tfImmat);
        add(new JLabel("Prix par jour:"));    add(tfPrix);
        add(new JLabel("Disponibilité:"));    add(chkDispo);
        add(new JLabel("Agence:"));           add(cbAgence);

        btnAjouter = new JButton("Ajouter");
        btnRetour = new JButton("Retour au menu");

        btnAjouter.addActionListener(e -> ajouterVoiture());
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        add(btnAjouter);
        add(btnRetour);

        chargerAgences();
        setVisible(true);
    }

    private void chargerAgences() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT IdAgence, Nom_agence FROM AGENCE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("IdAgence");
                String nom = rs.getString("Nom_agence");
                cbAgence.addItem(id + " - " + nom); // Ex: "2 - LocationPro"
            }

            if (cbAgence.getItemCount() == 0) {
                cbAgence.addItem("Aucune agence dispo");
                cbAgence.setEnabled(false);
                btnAjouter.setEnabled(false);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement agences : " + e.getMessage());
        }
    }

    private void ajouterVoiture() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO VOITURE (Marque, Modele, Immatriculation, Prix_jour, Disponibilite, IdAgence) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tfMarque.getText());
            ps.setString(2, tfModele.getText());
            ps.setString(3, tfImmat.getText());
            ps.setDouble(4, Double.parseDouble(tfPrix.getText()));
            ps.setBoolean(5, chkDispo.isSelected());

            // Récupérer l'ID depuis "2 - AgenceX"
            String selected = (String) cbAgence.getSelectedItem();
            int idAgence = Integer.parseInt(selected.split(" - ")[0]);
            ps.setInt(6, idAgence);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Voiture ajoutée avec succès !");
            this.dispose();
            new MainMenu();
        } catch (SQLException | NumberFormatException | NullPointerException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }
}
