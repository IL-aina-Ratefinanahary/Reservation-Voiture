package client;

import javax.swing.*;
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
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[] {
            "IdClient", "Nom", "Prénom", "Email", "Téléphone", "Adresse"
        });

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Boutons d'action
        btnSupprimer = new JButton("Supprimer");
        btnModifier = new JButton("Modifier");

        btnSupprimer.setPreferredSize(new Dimension(120, 30));
        btnModifier.setPreferredSize(new Dimension(120, 30));

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnModifier);
        add(panelBoutons, BorderLayout.SOUTH);
        
        JButton btnRetour = new JButton("Retour au menu");
        btnRetour.setPreferredSize(new Dimension(150, 30));
        btnRetour.addActionListener(e -> {
            this.dispose();
            new pack.MainMenu();
        });
        panelBoutons.add(btnRetour);


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
