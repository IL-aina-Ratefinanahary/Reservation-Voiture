package choix;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

import pack.DatabaseConnection;
import pack.MainMenu;

public class ChoixList extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnModifier, btnSupprimer, btnRetour;

    public ChoixList() {
        setTitle("Liste des options (choix)");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // === Tableau ===
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[] {
            "ID", "Nom", "Description", "Prix ($)"
        });

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // === Boutons bas ===
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        btnRetour = new JButton("Retour");

        JPanel panelBas = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBas.add(btnModifier);
        panelBas.add(btnSupprimer);
        panelBas.add(btnRetour);
        add(panelBas, BorderLayout.SOUTH);

        // === Actions ===
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        btnModifier.addActionListener(e -> modifierChoix());
        btnSupprimer.addActionListener(e -> supprimerChoix());

        chargerChoix();
        setVisible(true);
    }

    private void chargerChoix() {
        model.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM CHOIX";
            ResultSet rs = conn.createStatement().executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getInt("IdChoix"),
                    rs.getString("Nom_choix"),
                    rs.getString("Description_choix"),
                    rs.getDouble("Prix_choix")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement : " + e.getMessage());
        }
    }

    private void modifierChoix() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un choix à modifier.");
            return;
        }

        int idChoix = (int) model.getValueAt(selectedRow, 0);
        ChoixEdit fenetre = new ChoixEdit(idChoix);
        fenetre.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                chargerChoix();
            }
        });
    }

    private void supprimerChoix() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un choix à supprimer.");
            return;
        }

        int idChoix = (int) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer l’option ID " + idChoix + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM CHOIX WHERE IdChoix = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, idChoix);
                int rows = ps.executeUpdate();

                if (rows > 0) {
                    model.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Option supprimée !");
                } else {
                    JOptionPane.showMessageDialog(this, "Échec de la suppression.");
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage());
            }
        }
    }
}
