package agence;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class AgenceList extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnModifier, btnSupprimer;


    public AgenceList() {
        setTitle("Liste des agences");
        setSize(800, 450);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // === Modèle de table ===
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[] {
            "IdAgence", "Nom", "Adresse", "Téléphone", "Email"
        });

        // === Table ===
        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);

        // Centrage des cellules
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < model.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // === Boutons ===
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        JButton btnRetour = new JButton("Retour au menu");

        btnModifier.setPreferredSize(new Dimension(120, 30));
        btnSupprimer.setPreferredSize(new Dimension(120, 30));
        btnRetour.setPreferredSize(new Dimension(150, 30));

        btnModifier.addActionListener(e -> modifierAgence());
        btnSupprimer.addActionListener(e -> supprimerAgence());
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        JPanel panelBas = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBas.add(btnModifier);
        panelBas.add(btnSupprimer);
        panelBas.add(btnRetour);
        add(panelBas, BorderLayout.SOUTH);

        chargerAgences();
        setVisible(true);
    }


    private void chargerAgences() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM AGENCE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getInt("IdAgence"),
                    rs.getString("Nom_agence"),
                    rs.getString("Adresse_agence"),
                    rs.getString("Telephone_agence"),
                    rs.getString("Email_agence")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement : " + e.getMessage());
        }
    }
    
    private void modifierAgence() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une agence.");
            return;
        }

        int idAgence = (int) model.getValueAt(row, 0);
        AgenceEdit fenetre = new AgenceEdit(idAgence);
        fenetre.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                chargerAgences(); // Rafraîchir après modif
            }
        });
    }
    
    private void supprimerAgence() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une agence à supprimer.");
            return;
        }

        int idAgence = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirmer la suppression de l'agence ID " + idAgence + " ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM AGENCE WHERE IdAgence = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, idAgence);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    chargerAgences();
                    JOptionPane.showMessageDialog(this, "Agence supprimée !");
                } else {
                    JOptionPane.showMessageDialog(this, "Échec de la suppression.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage());
            }
        }
    }
    
    public AgenceList(String motCle) {
        this(); // Appelle le constructeur par défaut
        appliquerFiltre(motCle);
    }
    
    private void appliquerFiltre(String texte) {
        texte = texte.toLowerCase();
        model.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM AGENCE WHERE LOWER(Nom_agence) LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + texte + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getInt("IdAgence"),
                    rs.getString("Nom_agence"),
                    rs.getString("Adresse_agence"),
                    rs.getString("Telephone_agence"),
                    rs.getString("Email_agence")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur recherche agence : " + e.getMessage());
        }
    }



}
