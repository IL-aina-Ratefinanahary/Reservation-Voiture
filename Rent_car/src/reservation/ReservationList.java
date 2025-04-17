package reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.impl.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Properties;

import pack.DatabaseConnection;
import pack.MainMenu;

public class ReservationList extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField tfRecherche;
    private JButton btnModifier, btnSupprimer, btnRetour;

    public ReservationList() {
        setTitle("Liste des réservations");
        setSize(1000, 520);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // === Table ===
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[] {
            "ID", "Date début", "Date fin", "Prix Total", "Client", "Voiture", "Options"
        });

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);

        // Centrage des cellules
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < model.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        // === Zone recherche & filtre ===
        JPanel panelRecherche = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelRecherche.setBorder(BorderFactory.createTitledBorder("🔍 Recherche par mot-clé (Client ou Voiture)"));
        tfRecherche = new JTextField(20);
        panelRecherche.add(tfRecherche);

        JPanel panelFiltreDate = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelFiltreDate.setBorder(BorderFactory.createTitledBorder("📅 Filtrer par date"));
        JLabel lblDe = new JLabel("De :");
        JDatePickerImpl dpDebut = createDatePicker();
        JLabel lblA = new JLabel("À :");
        JDatePickerImpl dpFin = createDatePicker();
        JButton btnFiltrer = new JButton("Filtrer");

        panelFiltreDate.add(lblDe);
        panelFiltreDate.add(dpDebut);
        panelFiltreDate.add(lblA);
        panelFiltreDate.add(dpFin);
        panelFiltreDate.add(btnFiltrer);

        JPanel panelTop = new JPanel(new GridLayout(2, 1));
        panelTop.add(panelRecherche);
        panelTop.add(panelFiltreDate);
        add(panelTop, BorderLayout.NORTH);

        // === Boutons bas ===
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        btnRetour = new JButton("Retour au menu");

        btnModifier.setPreferredSize(new Dimension(130, 30));
        btnSupprimer.setPreferredSize(new Dimension(130, 30));
        btnRetour.setPreferredSize(new Dimension(150, 30));

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

        btnModifier.addActionListener(e -> modifierReservation());
        btnSupprimer.addActionListener(e -> supprimerReservation());

        btnFiltrer.addActionListener(e -> {
            java.util.Date d1 = (java.util.Date) dpDebut.getModel().getValue();
            java.util.Date d2 = (java.util.Date) dpFin.getModel().getValue();
            if (d1 == null || d2 == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner deux dates.");
                return;
            }

            java.sql.Date sqlD1 = new java.sql.Date(d1.getTime());
            java.sql.Date sqlD2 = new java.sql.Date(d2.getTime());
            String motCle = tfRecherche.getText().trim().toLowerCase();

            appliquerFiltreCombiné(motCle, sqlD1, sqlD2);
        });

        chargerReservations();
        setVisible(true);
    }


    private JDatePickerImpl createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Aujourd'hui");
        p.put("text.month", "Mois");
        p.put("text.year", "Année");
        JDatePanelImpl panel = new JDatePanelImpl(model, p);
        return new JDatePickerImpl(panel, new DateLabelFormatter());
    }

    private void chargerReservations() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
        	String sql = """
        		    SELECT R.IdReservation, R.Date_debut, R.Date_fin, R.PrixTotal,
        		           CONCAT(C.Nom, ' ', C.Prenom) AS NomClient,
        		           CONCAT(V.Marque, ' ', V.Modele) AS NomVoiture,
        		           (
        		               SELECT GROUP_CONCAT(CONCAT(CH.Nom_choix, ' x', CR.Quantite) SEPARATOR ', ')
        		               FROM CHOIX_RESERVATION CR
        		               JOIN CHOIX CH ON CR.IdChoix = CH.IdChoix
        		               WHERE CR.IdReservation = R.IdReservation
        		           ) AS Options
        		    FROM RESERVATION R
        		    JOIN CLIENT C ON R.IdClient = C.IdClient
        		    JOIN VOITURE V ON R.IdVoiture = V.IdVoiture
        		""";

            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
            	model.addRow(new Object[] {
            		    rs.getInt("IdReservation"),
            		    rs.getDate("Date_debut"),
            		    rs.getDate("Date_fin"),
            		    rs.getDouble("PrixTotal"),
            		    rs.getString("NomClient"),
            		    rs.getString("NomVoiture"),
            		    rs.getString("Options") == null ? "—" : rs.getString("Options")
            		});

            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement : " + e.getMessage());
        }
    }

    private void supprimerReservation() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une réservation à supprimer.");
            return;
        }

        int idResa = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer la réservation ID " + idResa + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {

                // 1. Supprimer les choix liés à la réservation
                String delChoixSQL = "DELETE FROM CHOIX_RESERVATION WHERE IdReservation = ?";
                PreparedStatement psChoix = conn.prepareStatement(delChoixSQL);
                psChoix.setInt(1, idResa);
                psChoix.executeUpdate();

                // 2. Récupère IdVoiture
                String getSQL = "SELECT IdVoiture FROM RESERVATION WHERE IdReservation = ?";
                PreparedStatement psGet = conn.prepareStatement(getSQL);
                psGet.setInt(1, idResa);
                ResultSet rs = psGet.executeQuery();
                int idVoiture = rs.next() ? rs.getInt("IdVoiture") : -1;

                // 3. Supprimer la réservation
                String deleteSQL = "DELETE FROM RESERVATION WHERE IdReservation = ?";
                PreparedStatement psDelete = conn.prepareStatement(deleteSQL);
                psDelete.setInt(1, idResa);
                int rows = psDelete.executeUpdate();

                if (rows > 0 && idVoiture != -1) {
                    // 4. Rend la voiture disponible
                    String updateSQL = "UPDATE VOITURE SET Disponibilite = TRUE WHERE IdVoiture = ?";
                    PreparedStatement psUpdate = conn.prepareStatement(updateSQL);
                    psUpdate.setInt(1, idVoiture);
                    psUpdate.executeUpdate();

                    model.removeRow(row);
                    JOptionPane.showMessageDialog(this, "Réservation supprimée !");
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur suppression : " + e.getMessage());
            }
        }
    }


    private void modifierReservation() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une réservation à modifier.");
            return;
        }

        int idResa = (int) model.getValueAt(row, 0);
        ReservationEdit fenetre = new ReservationEdit(idResa);
        fenetre.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                chargerReservations();
            }
        });
    }

    public ReservationList(String motCle) {
        this();
        appliquerFiltreTexte(motCle);
    }

    public ReservationList(String motCle, java.sql.Date dateDebut, java.sql.Date dateFin) {
        this();
        appliquerFiltreCombiné(motCle, dateDebut, dateFin);
    }

    private void appliquerFiltreTexte(String texte) {
        texte = texte.toLowerCase();
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT R.IdReservation, R.Date_debut, R.Date_fin, R.PrixTotal,
                       CONCAT(C.Nom, ' ', C.Prenom) AS NomClient,
                       CONCAT(V.Marque, ' ', V.Modele) AS NomVoiture
                FROM RESERVATION R
                JOIN CLIENT C ON R.IdClient = C.IdClient
                JOIN VOITURE V ON R.IdVoiture = V.IdVoiture
                WHERE LOWER(C.Nom) LIKE ? OR LOWER(C.Prenom) LIKE ?
                   OR LOWER(V.Marque) LIKE ? OR LOWER(V.Modele) LIKE ?
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 1; i <= 4; i++) ps.setString(i, "%" + texte + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getInt("IdReservation"),
                    rs.getDate("Date_debut"),
                    rs.getDate("Date_fin"),
                    rs.getDouble("PrixTotal"),
                    rs.getString("NomClient"),
                    rs.getString("NomVoiture")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur recherche : " + e.getMessage());
        }
    }

    private void appliquerFiltreCombiné(String texte, java.sql.Date d1, java.sql.Date d2) {
        texte = texte.toLowerCase();
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT R.IdReservation, R.Date_debut, R.Date_fin, R.PrixTotal,
                       CONCAT(C.Nom, ' ', C.Prenom) AS NomClient,
                       CONCAT(V.Marque, ' ', V.Modele) AS NomVoiture
                FROM RESERVATION R
                JOIN CLIENT C ON R.IdClient = C.IdClient
                JOIN VOITURE V ON R.IdVoiture = V.IdVoiture
                WHERE (LOWER(C.Nom) LIKE ? OR LOWER(C.Prenom) LIKE ?
                       OR LOWER(V.Marque) LIKE ? OR LOWER(V.Modele) LIKE ?)
                  AND R.Date_debut >= ? AND R.Date_fin <= ?
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 1; i <= 4; i++) ps.setString(i, "%" + texte + "%");
            ps.setDate(5, d1);
            ps.setDate(6, d2);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getInt("IdReservation"),
                    rs.getDate("Date_debut"),
                    rs.getDate("Date_fin"),
                    rs.getDouble("PrixTotal"),
                    rs.getString("NomClient"),
                    rs.getString("NomVoiture")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur combinée : " + e.getMessage());
        }
    }
}
