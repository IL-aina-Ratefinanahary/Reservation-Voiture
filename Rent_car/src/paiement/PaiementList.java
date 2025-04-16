package paiement;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class PaiementList extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cbFiltreStatut;
    private JTextField tfRechercheClient;
    private JButton btnModifier, btnSupprimer, btnRetour, btnRechercher;

    public PaiementList() {
        setTitle("Liste des paiements");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // === Tableau ===
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[] {
            "ID", "Date", "Montant", "Mode", "Statut", "Client", "Voiture", "Total Réservation"
        });

        table = new JTable(model);

        // Coloration conditionnelle
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String statut = (String) value;

                if ("Payé".equalsIgnoreCase(statut)) {
                    c.setBackground(new Color(204, 255, 204)); // vert
                } else {
                    c.setBackground(new Color(255, 204, 204)); // rouge
                }

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else {
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // === Zone recherche combinée ===
        JPanel panelFiltre = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltre.setBorder(BorderFactory.createTitledBorder("🔎 Recherche par client + statut"));

        tfRechercheClient = new JTextField(15);
        cbFiltreStatut = new JComboBox<>(new String[] {"Tous", "Payé", "En attente", "Échoué"});
        btnRechercher = new JButton("Rechercher");

        panelFiltre.add(new JLabel("Client :"));
        panelFiltre.add(tfRechercheClient);
        panelFiltre.add(new JLabel("Statut :"));
        panelFiltre.add(cbFiltreStatut);
        panelFiltre.add(btnRechercher);

        add(panelFiltre, BorderLayout.NORTH);

        // === Boutons bas ===
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        btnRetour = new JButton("Retour");

        JPanel panelBas = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBas.add(btnModifier);
        panelBas.add(btnSupprimer);
        panelBas.add(btnRetour);
        add(panelBas, BorderLayout.SOUTH);

        // === Actions ===
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        btnModifier.addActionListener(e -> modifierPaiement());
        btnSupprimer.addActionListener(e -> supprimerPaiement());

        btnRechercher.addActionListener(e -> {
            String client = tfRechercheClient.getText().trim().toLowerCase();
            String statut = cbFiltreStatut.getSelectedItem().toString();
            chargerPaiementsParClientEtStatut(client, statut);
        });

        chargerPaiementsParClientEtStatut("", "Tous");
        setVisible(true);
    }

    private void modifierPaiement() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un paiement à modifier.");
            return;
        }

        int idPaiement = (int) model.getValueAt(selectedRow, 0);
        PaiementEdit fenetre = new PaiementEdit(idPaiement);
        fenetre.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                String client = tfRechercheClient.getText().trim().toLowerCase();
                String statut = cbFiltreStatut.getSelectedItem().toString();
                chargerPaiementsParClientEtStatut(client, statut);
            }
        });
    }

    private void supprimerPaiement() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un paiement à supprimer.");
            return;
        }

        int idPaiement = (int) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer le paiement ID " + idPaiement + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM PAIEMENT WHERE IdPaiement = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, idPaiement);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    model.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Paiement supprimé !");
                } else {
                    JOptionPane.showMessageDialog(this, "Aucune suppression effectuée.");
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage());
            }
        }
    }

    private void chargerPaiementsParClientEtStatut(String nomClient, String statut) {
        model.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT P.IdPaiement, P.Date_paiement, P.Montant, P.Mode_paiement, P.Statut_paiement,
                       CONCAT(C.Nom, ' ', C.Prenom) AS NomClient,
                       CONCAT(V.Marque, ' ', V.Modele) AS NomVoiture , 
                       R.PrixTotal AS TotalReservation
                FROM PAIEMENT P
                JOIN RESERVATION R ON P.IdReservation = R.IdReservation
                JOIN CLIENT C ON R.IdClient = C.IdClient
                JOIN VOITURE V ON R.IdVoiture = V.IdVoiture
                WHERE (LOWER(C.Nom) LIKE ? OR LOWER(C.Prenom) LIKE ?)
            """;

            if (!statut.equals("Tous")) {
                sql += " AND P.Statut_paiement = ?";
            }

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + nomClient + "%");
            ps.setString(2, "%" + nomClient + "%");
            if (!statut.equals("Tous")) {
                ps.setString(3, statut);
            }

            ResultSet rs = ps.executeQuery(); 
            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getInt("IdPaiement"),
                    rs.getDate("Date_paiement"),
                    rs.getDouble("Montant"),
                    rs.getString("Mode_paiement"),
                    rs.getString("Statut_paiement"),
                    rs.getString("NomClient"),
                    rs.getString("NomVoiture"),
                    rs.getDouble("TotalReservation") 
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement : " + e.getMessage());
        }
    }
}
