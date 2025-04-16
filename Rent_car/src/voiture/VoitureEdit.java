package voiture;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class VoitureEdit extends JFrame {
    private JTextField tfId, tfMarque, tfModele, tfImmat, tfPrix;
    private JCheckBox chkDispo;
    private JComboBox<String> cbAgence;
    private JButton btnModifier, btnRetour;

    public VoitureEdit(int idVoiture) {
        setTitle("Modifier une voiture");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // === Panel formulaire ===
        JPanel panelForm = new JPanel(new GridLayout(7, 2, 10, 10));
        tfId = new JTextField();
        tfId.setEditable(false);
        tfMarque = new JTextField();
        tfModele = new JTextField();
        tfImmat = new JTextField();
        tfPrix = new JTextField();
        chkDispo = new JCheckBox("Disponible");
        cbAgence = new JComboBox<>();

        panelForm.add(new JLabel("ID Voiture:"));         panelForm.add(tfId);
        panelForm.add(new JLabel("Marque:"));             panelForm.add(tfMarque);
        panelForm.add(new JLabel("Modèle:"));             panelForm.add(tfModele);
        panelForm.add(new JLabel("Immatriculation:"));    panelForm.add(tfImmat);
        panelForm.add(new JLabel("Prix par jour:"));      panelForm.add(tfPrix);
        panelForm.add(new JLabel("Disponibilité:"));      panelForm.add(chkDispo);
        panelForm.add(new JLabel("Agence:"));             panelForm.add(cbAgence);

        add(panelForm, BorderLayout.CENTER);

        // === Panel boutons ===
        btnModifier = new JButton("Modifier");
        btnRetour = new JButton("Retour au menu");

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelButtons.add(btnModifier);
        panelButtons.add(btnRetour);
        add(panelButtons, BorderLayout.SOUTH);

        // Actions
        btnModifier.addActionListener(e -> modifierVoiture());
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        chargerAgences();
        chargerVoiture(idVoiture);

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

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement agences : " + e.getMessage());
        }
    }

    private void chargerVoiture(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM VOITURE WHERE IdVoiture = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tfId.setText(String.valueOf(rs.getInt("IdVoiture")));
                tfMarque.setText(rs.getString("Marque"));
                tfModele.setText(rs.getString("Modele"));
                tfImmat.setText(rs.getString("Immatriculation"));
                tfPrix.setText(String.valueOf(rs.getDouble("Prix_jour")));
                chkDispo.setSelected(rs.getBoolean("Disponibilite"));

                // Sélectionner automatiquement l'agence dans la combo
                int idAgence = rs.getInt("IdAgence");
                for (int i = 0; i < cbAgence.getItemCount(); i++) {
                    String item = cbAgence.getItemAt(i);
                    if (item.startsWith(idAgence + " -")) {
                        cbAgence.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement : " + e.getMessage());
        }
    }

    private void modifierVoiture() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE VOITURE SET Marque=?, Modele=?, Immatriculation=?, Prix_jour=?, Disponibilite=?, IdAgence=? WHERE IdVoiture=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tfMarque.getText());
            ps.setString(2, tfModele.getText());
            ps.setString(3, tfImmat.getText());
            ps.setDouble(4, Double.parseDouble(tfPrix.getText()));
            ps.setBoolean(5, chkDispo.isSelected());

            // ID Agence depuis combo
            String selected = (String) cbAgence.getSelectedItem();
            int idAgence = Integer.parseInt(selected.split(" - ")[0]);
            ps.setInt(6, idAgence);

            ps.setInt(7, Integer.parseInt(tfId.getText()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Modification réussie !");
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Aucune modification effectuée.");
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }
}
