package client;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import pack.DatabaseConnection;

public class ClientList extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnSupprimer, btnModifier;

    public ClientList() {
        setTitle("Liste des clients");
        setSize(900, 400);
        setLocationRelativeTo(null); // Centré à l'écran
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // === Table et modèle ===
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[] {
            "IdClient", "Nom", "Prénom", "Email", "Téléphone", "Adresse"
        });

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);

        // Centre le contenu de toutes les colonnes
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // === Boutons ===
        btnSupprimer = new JButton("Supprimer");
        btnModifier = new JButton("Modifier");
        JButton btnRetour = new JButton("Retour au menu");

        btnSupprimer.setPreferredSize(new Dimension(130, 30));
        btnModifier.setPreferredSize(new Dimension(130, 30));
        btnRetour.setPreferredSize(new Dimension(160, 30));

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnRetour);

        add(panelBoutons, BorderLayout.SOUTH);

        // === Actions ===
        btnRetour.addActionListener(e -> {
            this.dispose();
            new pack.MainMenu();
        });

        btnSupprimer.addActionListener(e -> supprimerClientSelectionne());
        btnModifier.addActionListener(e -> modifierClientSelectionne());

        chargerClients();

        setVisible(true);
    }


    private void chargerClients() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM CLIENT";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getInt("IdClient"),
                    rs.getString("Nom"),
                    rs.getString("Prenom"),
                    rs.getString("Email_client"),
                    rs.getString("Telephone_client"),
                    rs.getString("Adresse_client")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }

    private void supprimerClientSelectionne() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client à supprimer.");
            return;
        }

        int idClient = (int) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer le client ID " + idClient + " ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM CLIENT WHERE IdClient = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, idClient);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    model.removeRow(selectedRow); // Supprime du tableau
                    JOptionPane.showMessageDialog(this, "Client supprimé !");
                } else {
                    JOptionPane.showMessageDialog(this, "Aucun client supprimé.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage());
            }
        }
    }

    private void modifierClientSelectionne() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client à modifier.");
            return;
        }

        int idClient = (int) model.getValueAt(selectedRow, 0);

        // Ouvre la fenêtre de modification
        ClientEdit fenetreModification = new ClientEdit(idClient);

        // Quand la fenêtre se ferme, on recharge les clients
        fenetreModification.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                rechargerListe();
            }
        });
    }

	private void rechargerListe() {
            model.setRowCount(0);  // Vide le tableau
            chargerClients();      // Recharge depuis la base
    }
	
	public ClientList(String filtreTexte) {
	    this(); // Appelle le constructeur sans paramètre
	    appliquerFiltreTexte(filtreTexte);
	}
	
	private void appliquerFiltreTexte(String texte) {
	    texte = texte.toLowerCase();
	    model.setRowCount(0);

	    try (Connection conn = DatabaseConnection.getConnection()) {
	        String sql = """
	            SELECT * FROM CLIENT
	            WHERE LOWER(Nom) LIKE ? 
	               OR LOWER(Prenom) LIKE ?
	               OR LOWER(Email_client) LIKE ?
	        """;

	        PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, "%" + texte + "%");
	        ps.setString(2, "%" + texte + "%");
	        ps.setString(3, "%" + texte + "%");

	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) {
	            model.addRow(new Object[] {
	                rs.getInt("IdClient"),
	                rs.getString("Nom"),
	                rs.getString("Prenom"),
	                rs.getString("Email_client"),
	                rs.getString("Telephone_client"),
	                rs.getString("Adresse_client")
	            });
	        }

	    } catch (SQLException e) {
	        JOptionPane.showMessageDialog(this, "Erreur recherche : " + e.getMessage());
	    }
	}


}
