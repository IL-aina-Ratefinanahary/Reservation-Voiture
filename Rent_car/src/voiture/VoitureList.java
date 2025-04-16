package voiture;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class VoitureList extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnModifier, btnSupprimer;
    private JComboBox<String> cbFiltre;

    public VoitureList() {
        setTitle("Liste des voitures");
        setSize(900, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // === Filtre ===
        JPanel panelFiltre = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblFiltre = new JLabel("Filtrer :");
        cbFiltre = new JComboBox<>(new String[] {"Toutes", "Disponibles", "Non disponibles"});
        panelFiltre.add(lblFiltre);
        panelFiltre.add(cbFiltre);
        add(panelFiltre, BorderLayout.NORTH);
        
        JLabel lblPrixMin = new JLabel("Prix min :");
        JTextField tfPrixMin = new JTextField(6);
        JLabel lblPrixMax = new JLabel("Prix max :");
        JTextField tfPrixMax = new JTextField(6);
        JButton btnFiltrerPrix = new JButton("Filtrer");

        panelFiltre.add(Box.createHorizontalStrut(20));
        panelFiltre.add(lblPrixMin);
        panelFiltre.add(tfPrixMin);
        panelFiltre.add(lblPrixMax);
        panelFiltre.add(tfPrixMax);
        panelFiltre.add(btnFiltrerPrix);
        
        


        // === Tableau ===
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[] {
            "IdVoiture", "Marque", "Modèle", "Immatriculation", "Prix/Jour", "Dispo", "IdAgence"
        });

        table = new JTable(model);
     // === Cellule colorée pour disponibilité ===
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String dispo = (String) value;
                if (dispo.contains("Oui")) {
                    c.setBackground(new Color(204, 255, 204)); // Vert clair
                    c.setForeground(Color.BLACK);
                } else if (dispo.contains("Non")) {
                    c.setBackground(new Color(255, 204, 204)); // Rouge clair
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Color.WHITE);
                }

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }

                return c;
            }
        });

       
        
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // === Boutons ===
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        btnModifier.setPreferredSize(new Dimension(120, 30));
        btnSupprimer.setPreferredSize(new Dimension(120, 30));

        JButton btnRetour = new JButton("Retour au menu");
        btnRetour.setPreferredSize(new Dimension(150, 30));
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        JPanel panelActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelActions.add(btnModifier);
        panelActions.add(btnSupprimer);
        panelActions.add(btnRetour);
        add(panelActions, BorderLayout.SOUTH);

        // === Actions ===
        btnModifier.addActionListener(e -> modifierVoiture());
        btnSupprimer.addActionListener(e -> supprimerVoiture());
        
        cbFiltre.addActionListener(e -> chargerVoitures(cbFiltre.getSelectedItem().toString()));
        
        btnFiltrerPrix.addActionListener(e -> {
            try {
                double prixMin = tfPrixMin.getText().isEmpty() ? 0 : Double.parseDouble(tfPrixMin.getText());
                double prixMax = tfPrixMax.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(tfPrixMax.getText());
                String dispoChoisie = cbFiltre.getSelectedItem().toString();
                appliquerFiltrePrixEtDispo(prixMin, prixMax, dispoChoisie);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Veuillez entrer des valeurs numériques valides.");
            }
        });



        // === Chargement initial ===
        chargerVoitures("Toutes");
        setVisible(true);
    }

    private void appliquerFiltrePrixEtDispo(double min, double max, String dispo) {
        model.setRowCount(0);

        String sql = "SELECT * FROM VOITURE WHERE Prix_jour BETWEEN ? AND ?";
        if (dispo.equals("Disponibles")) {
            sql += " AND Disponibilite = TRUE";
        } else if (dispo.equals("Non disponibles")) {
            sql += " AND Disponibilite = FALSE";
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, min);
            ps.setDouble(2, max);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String dispoStr = rs.getBoolean("Disponibilite") ? "🟢 Oui" : "🔴 Non";
                model.addRow(new Object[] {
                    rs.getInt("IdVoiture"),
                    rs.getString("Marque"),
                    rs.getString("Modele"),
                    rs.getString("Immatriculation"),
                    rs.getDouble("Prix_jour"),
                    dispoStr,
                    rs.getInt("IdAgence")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur filtrage combiné : " + e.getMessage());
        }
    }



	private void chargerVoitures(String filtre) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM VOITURE";
            if (filtre.equals("Disponibles")) {
                sql += " WHERE Disponibilite = TRUE";
            } else if (filtre.equals("Non disponibles")) {
                sql += " WHERE Disponibilite = FALSE";
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String dispoStr = rs.getBoolean("Disponibilite") ? "🟢 Oui" : "🔴 Non";
                model.addRow(new Object[] {
                    rs.getInt("IdVoiture"),
                    rs.getString("Marque"),
                    rs.getString("Modele"),
                    rs.getString("Immatriculation"),
                    rs.getDouble("Prix_jour"),
                    dispoStr,
                    rs.getInt("IdAgence")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }

    private void rechargerListe() {
        String filtre = cbFiltre.getSelectedItem().toString();
        chargerVoitures(filtre);
    }

    private void modifierVoiture() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une voiture à modifier.");
            return;
        }

        int idVoiture = (int) model.getValueAt(selectedRow, 0);
        VoitureEdit fenetre = new VoitureEdit(idVoiture);
        fenetre.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                rechargerListe();
            }
        });
    }

    private void supprimerVoiture() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une voiture à supprimer.");
            return;
        }

        int idVoiture = (int) model.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer la voiture ID " + idVoiture + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM VOITURE WHERE IdVoiture = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, idVoiture);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    rechargerListe();
                    JOptionPane.showMessageDialog(this, "Voiture supprimée !");
                } else {
                    JOptionPane.showMessageDialog(this, "Aucune suppression effectuée.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage());
            }
        }
    }
    
    public VoitureList(String filtreTexte) {
        this(); // Appelle le constructeur par défaut
        cbFiltre.setSelectedItem("Toutes"); // Annule le filtre booléen
        appliquerFiltreTexte(filtreTexte);
    }
    
    private void appliquerFiltreTexte(String texte) {
        texte = texte.toLowerCase();
        model.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT * FROM VOITURE 
                WHERE LOWER(Marque) LIKE ? OR LOWER(Modele) LIKE ?
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + texte + "%");
            ps.setString(2, "%" + texte + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String dispoStr = rs.getBoolean("Disponibilite") ? "🟢 Oui" : "🔴 Non";
                model.addRow(new Object[] {
                    rs.getInt("IdVoiture"),
                    rs.getString("Marque"),
                    rs.getString("Modele"),
                    rs.getString("Immatriculation"),
                    rs.getDouble("Prix_jour"),
                    dispoStr,
                    rs.getInt("IdAgence")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur recherche : " + e.getMessage());
        }
    }


}
