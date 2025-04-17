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
        setSize(500, 400);
        setResizable(false); // Empêche l'utilisateur de redimensionner la fenêtre
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // === Formulaire (centre) ===
        JPanel panelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tfMarque = new JTextField(20);
        tfModele = new JTextField(20);
        tfImmat = new JTextField(20);
        tfPrix = new JTextField(20);
        chkDispo = new JCheckBox("Disponible");
        cbAgence = new JComboBox<>();

        gbc.gridx = 0; gbc.gridy = 0; panelForm.add(new JLabel("Marque:"), gbc);
        gbc.gridx = 1; panelForm.add(tfMarque, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panelForm.add(new JLabel("Modèle:"), gbc);
        gbc.gridx = 1; panelForm.add(tfModele, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panelForm.add(new JLabel("Immatriculation:"), gbc);
        gbc.gridx = 1; panelForm.add(tfImmat, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panelForm.add(new JLabel("Prix par jour:"), gbc);
        gbc.gridx = 1; panelForm.add(tfPrix, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panelForm.add(new JLabel("Disponibilité:"), gbc);
        gbc.gridx = 1; panelForm.add(chkDispo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panelForm.add(new JLabel("Agence:"), gbc);
        gbc.gridx = 1; panelForm.add(cbAgence, gbc);

        add(panelForm, BorderLayout.CENTER);

        // === Boutons (bas) ===
        btnAjouter = new JButton("Ajouter");
        btnRetour = new JButton("Retour au menu");

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelButtons.add(btnAjouter);
        panelButtons.add(btnRetour);
        add(panelButtons, BorderLayout.SOUTH);

        // === Actions ===
        btnAjouter.addActionListener(e -> ajouterVoiture());
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

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
                cbAgence.addItem(id + " - " + nom);
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
            ps.setString(1, tfMarque.getText().trim());
            ps.setString(2, tfModele.getText().trim());
            ps.setString(3, tfImmat.getText().trim());
            ps.setDouble(4, Double.parseDouble(tfPrix.getText().trim()));
            ps.setBoolean(5, chkDispo.isSelected());

            String selected = (String) cbAgence.getSelectedItem();
            int idAgence = Integer.parseInt(selected.split(" - ")[0]);
            ps.setInt(6, idAgence);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Voiture ajoutée !");
            this.dispose();
            new MainMenu();
        } catch (SQLException | NumberFormatException | NullPointerException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }
}
