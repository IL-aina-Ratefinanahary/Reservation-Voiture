package reservation;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Properties;
import java.util.Calendar;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;

import org.jdatepicker.impl.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class ReservationEdit extends JFrame {
    private int idReservation;
    private JComboBox<String> cbClient, cbVoiture;
    private JDatePickerImpl dateDebutPicker, dateFinPicker;
    private JTextField tfPrixTotal;
    private JButton btnModifier, btnRetour;
    private JPanel panelChoix;
    private java.util.List<JCheckBox> checkChoix = new java.util.ArrayList<>();
    private java.util.List<JTextField> quantitesChoix = new java.util.ArrayList<>();
    private JLabel lblOptionsTotal = new JLabel("0.00");
    private JLabel lblTotalFinal = new JLabel("0.00");




    public ReservationEdit(int idReservation) {
        this.idReservation = idReservation;
        setTitle("Modifier une réservation");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panelForm = new JPanel(new GridLayout(5, 2, 10, 10));

        cbClient = new JComboBox<>();
        cbVoiture = new JComboBox<>();
        tfPrixTotal = new JTextField();
        tfPrixTotal.setEditable(false);

        dateDebutPicker = createDatePicker();
        dateFinPicker = createDatePicker();

        panelForm.add(new JLabel("Client :"));          panelForm.add(cbClient);
        panelForm.add(new JLabel("Voiture :"));         panelForm.add(cbVoiture);
        panelForm.add(new JLabel("Date début :"));      panelForm.add(dateDebutPicker);
        panelForm.add(new JLabel("Date fin :"));        panelForm.add(dateFinPicker);
        panelForm.add(new JLabel("Prix total ($) :"));  panelForm.add(tfPrixTotal);
        panelForm.add(new JLabel("Options ($) :")); panelForm.add(lblOptionsTotal);
        panelForm.add(new JLabel("Total final ($) :")); panelForm.add(lblTotalFinal);




        add(panelForm, BorderLayout.CENTER);
        
        panelChoix = new JPanel(new GridLayout(0, 2, 5, 5));
        panelChoix.setBorder(BorderFactory.createTitledBorder("Options associées à la réservation"));
        add(panelChoix, BorderLayout.EAST);

        chargerChoixEtSelection();

        

        btnModifier = new JButton("Modifier");
        btnRetour = new JButton("Retour au menu");

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelButtons.add(btnModifier);
        panelButtons.add(btnRetour);
        add(panelButtons, BorderLayout.SOUTH);

        btnModifier.addActionListener(e -> modifierReservation());
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        // Recalcul automatique du prix
        dateDebutPicker.addActionListener(e -> calculerPrixTotal());
        dateFinPicker.addActionListener(e -> calculerPrixTotal());
        cbVoiture.addActionListener(e -> calculerPrixTotal());

        chargerClients();
        chargerVoituresDisponibles();
        chargerReservation();

        setVisible(true);
    }
    
    private void calculerTotalOptions() {
        double total = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String prixSQL = "SELECT Prix_choix FROM CHOIX WHERE IdChoix = ?";
            PreparedStatement ps = conn.prepareStatement(prixSQL);

            for (int i = 0; i < checkChoix.size(); i++) {
                JCheckBox cb = checkChoix.get(i);
                if (cb.isSelected()) {
                    int idChoix = Integer.parseInt(cb.getName());
                    int qte = Integer.parseInt(quantitesChoix.get(i).getText());

                    ps.setInt(1, idChoix);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        total += rs.getDouble("Prix_choix") * qte;
                    }
                }
            }

        } catch (Exception e) {
            // Silencieux mais visible si besoin
            System.err.println("Erreur calcul options : " + e.getMessage());
        }

        lblOptionsTotal.setText(String.format("%.2f", total));
        
        calculerTotalFinal();

    }
    
    private void calculerTotalFinal() {
        try {
            double prixVoiture = Double.parseDouble(tfPrixTotal.getText());
            double prixOptions = Double.parseDouble(lblOptionsTotal.getText());
            double total = prixVoiture + prixOptions;
            lblTotalFinal.setText(String.format("%.2f", total));
        } catch (Exception e) {
            lblTotalFinal.setText("0.00");
        }
    }



    private void chargerChoixEtSelection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM CHOIX";
            ResultSet rs = conn.createStatement().executeQuery(sql);

            // Charger les choix disponibles
            while (rs.next()) {
                int id = rs.getInt("IdChoix");
                String nom = rs.getString("Nom_choix") + " ($" + rs.getDouble("Prix_choix") + ")";
                JCheckBox cb = new JCheckBox(nom);
                cb.setName(String.valueOf(id));

                JTextField tfQte = new JTextField("1");
                tfQte.setPreferredSize(new Dimension(50, 25));

                checkChoix.add(cb);
                quantitesChoix.add(tfQte);

                panelChoix.add(cb);
                panelChoix.add(tfQte);

                // Listeners dynamiques
                cb.addActionListener(e -> calculerTotalOptions());
                tfQte.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { calculerTotalOptions(); }
                    public void removeUpdate(javax.swing.event.DocumentEvent e) { calculerTotalOptions(); }
                    public void insertUpdate(javax.swing.event.DocumentEvent e) { calculerTotalOptions(); }
                });
            }

            // Cocher ceux déjà liés
            String assocSQL = "SELECT * FROM CHOIX_RESERVATION WHERE IdReservation = ?";
            PreparedStatement ps = conn.prepareStatement(assocSQL);
            ps.setInt(1, idReservation);
            ResultSet rsAssoc = ps.executeQuery();

            while (rsAssoc.next()) {
                int idChoix = rsAssoc.getInt("IdChoix");
                int quantite = rsAssoc.getInt("Quantite");

                for (int i = 0; i < checkChoix.size(); i++) {
                    if (checkChoix.get(i).getName().equals(String.valueOf(idChoix))) {
                        checkChoix.get(i).setSelected(true);
                        quantitesChoix.get(i).setText(String.valueOf(quantite));
                        break;
                    }
                }
            }

            // ✅ Un seul appel final
            calculerTotalOptions();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement options : " + e.getMessage());
        }
    }



	private JDatePickerImpl createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Aujourd'hui");
        p.put("text.month", "Mois");
        p.put("text.year", "Année");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        return new JDatePickerImpl(datePanel, new DateLabelFormatter());
    }

    private void chargerClients() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT IdClient, Nom, Prenom FROM CLIENT";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                cbClient.addItem(rs.getInt("IdClient") + " - " + rs.getString("Nom") + " " + rs.getString("Prenom"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement clients : " + e.getMessage());
        }
    }

    private void chargerVoituresDisponibles() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT IdVoiture, Marque, Modele FROM VOITURE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                cbVoiture.addItem(rs.getInt("IdVoiture") + " - " + rs.getString("Marque") + " " + rs.getString("Modele"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement voitures : " + e.getMessage());
        }
    }

    private void chargerReservation() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM RESERVATION WHERE IdReservation = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idReservation);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Dates
                Date debut = rs.getDate("Date_debut");
                Date fin = rs.getDate("Date_fin");

                Calendar calDebut = Calendar.getInstance();
                calDebut.setTime(debut);
                dateDebutPicker.getModel().setDate(
                    calDebut.get(Calendar.YEAR),
                    calDebut.get(Calendar.MONTH),
                    calDebut.get(Calendar.DAY_OF_MONTH)
                );
                
                dateDebutPicker.getModel().setSelected(true);

                Calendar calFin = Calendar.getInstance();
                calFin.setTime(fin);
                dateFinPicker.getModel().setDate(
                    calFin.get(Calendar.YEAR),
                    calFin.get(Calendar.MONTH),
                    calFin.get(Calendar.DAY_OF_MONTH)
                );
                dateFinPicker.getModel().setSelected(true);


                // Sélection client
                int idClient = rs.getInt("IdClient");
                for (int i = 0; i < cbClient.getItemCount(); i++) {
                    if (cbClient.getItemAt(i).startsWith(idClient + " -")) {
                        cbClient.setSelectedIndex(i);
                        break;
                    }
                }

                // Sélection voiture
                int idVoiture = rs.getInt("IdVoiture");
                for (int i = 0; i < cbVoiture.getItemCount(); i++) {
                    if (cbVoiture.getItemAt(i).startsWith(idVoiture + " -")) {
                        cbVoiture.setSelectedIndex(i);
                        break;
                    }
                }

                tfPrixTotal.setText(String.valueOf(rs.getDouble("PrixTotal")));
                calculerPrixTotal();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement réservation : " + e.getMessage());
        }
    }

    private void calculerPrixTotal() {
        try {
            Date d1 = (Date) dateDebutPicker.getModel().getValue();
            Date d2 = (Date) dateFinPicker.getModel().getValue();
            if (d1 == null || d2 == null || cbVoiture.getSelectedItem() == null) return;

            LocalDate date1 = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate date2 = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            long days = java.time.temporal.ChronoUnit.DAYS.between(date1, date2);
            if (days <= 0) {
                tfPrixTotal.setText("0.00");
                return;
            }

            String selected = (String) cbVoiture.getSelectedItem();
            int idVoiture = Integer.parseInt(selected.split(" - ")[0]);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT Prix_jour FROM VOITURE WHERE IdVoiture = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, idVoiture);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double prixJour = rs.getDouble("Prix_jour");
                    tfPrixTotal.setText(String.format("%.2f", prixJour * days));
                }
            }

        } catch (Exception e) {
            tfPrixTotal.setText("0.00");
        }
        
        calculerTotalFinal();

    }

    private void modifierReservation() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Récupération des valeurs du formulaire
            Date dateDebut = (Date) dateDebutPicker.getModel().getValue();
            Date dateFin = (Date) dateFinPicker.getModel().getValue();
            int idClient = Integer.parseInt(cbClient.getSelectedItem().toString().split(" - ")[0]);
            int idVoiture = Integer.parseInt(cbVoiture.getSelectedItem().toString().split(" - ")[0]);

            // ✅ Calcul total combiné
            double prixVoiture = Double.parseDouble(tfPrixTotal.getText());
            double prixOptions = Double.parseDouble(lblOptionsTotal.getText());
            double prixTotal = prixVoiture + prixOptions;

            // ✅ Vérification chevauchement
            String verifSQL = """
                SELECT COUNT(*) FROM RESERVATION
                WHERE IdVoiture = ?
                  AND IdReservation <> ?
                  AND (? < Date_fin AND ? > Date_debut)
            """;
            PreparedStatement check = conn.prepareStatement(verifSQL);
            check.setInt(1, idVoiture);
            check.setInt(2, idReservation); // exclut soi-même
            check.setDate(3, new java.sql.Date(dateFin.getTime()));
            check.setDate(4, new java.sql.Date(dateDebut.getTime()));
            ResultSet rs = check.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "❌ Cette voiture est déjà réservée à cette période.");
                return;
            }

            // ✅ Mise à jour de la réservation avec prix total combiné
            String sql = "UPDATE RESERVATION SET Date_debut=?, Date_fin=?, PrixTotal=?, IdClient=?, IdVoiture=? WHERE IdReservation=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(dateDebut.getTime()));
            ps.setDate(2, new java.sql.Date(dateFin.getTime()));
            ps.setDouble(3, prixTotal); // ✅ total = voiture + options
            ps.setInt(4, idClient);
            ps.setInt(5, idVoiture);
            ps.setInt(6, idReservation);
            ps.executeUpdate();

            // Supprimer les anciennes options
            String delete = "DELETE FROM CHOIX_RESERVATION WHERE IdReservation = ?";
            PreparedStatement psDelete = conn.prepareStatement(delete);
            psDelete.setInt(1, idReservation);
            psDelete.executeUpdate();

            // Réinsérer les choix sélectionnés
            String insert = "INSERT INTO CHOIX_RESERVATION (IdReservation, IdChoix, Quantite) VALUES (?, ?, ?)";
            PreparedStatement psInsert = conn.prepareStatement(insert);

            for (int i = 0; i < checkChoix.size(); i++) {
                JCheckBox cb = checkChoix.get(i);
                if (cb.isSelected()) {
                    int idChoix = Integer.parseInt(cb.getName());
                    int qte = Integer.parseInt(quantitesChoix.get(i).getText());

                    psInsert.setInt(1, idReservation);
                    psInsert.setInt(2, idChoix);
                    psInsert.setInt(3, qte);
                    psInsert.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "✅ Réservation mise à jour !");
            this.dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }


}
