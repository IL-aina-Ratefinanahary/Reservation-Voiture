package reservation;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Properties;
import java.util.Date;

import org.jdatepicker.impl.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class ReservationForm extends JFrame {
    private JComboBox<String> cbClient, cbVoiture;
    private JDatePickerImpl dateDebutPicker, dateFinPicker;
    private JTextField tfPrixTotal;
    private JButton btnAjouter, btnRetour;
    private JPanel panelChoix;
    private java.util.List<JCheckBox> checkChoix = new java.util.ArrayList<>();
    private java.util.List<JTextField> quantitesChoix = new java.util.ArrayList<>();
    private JLabel lblOptionsTotal = new JLabel("0.00");
    private JLabel lblTotalFinal = new JLabel("0.00");


    public ReservationForm() {
        setTitle("Nouvelle réservation");
        setSize(1000, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(100, 100));
        
        setResizable(false);

        
        cbClient = new JComboBox<>();
        cbVoiture = new JComboBox<>();
        tfPrixTotal = new JTextField();
        
        cbClient.setPreferredSize(new Dimension(150, 35));
        cbVoiture.setPreferredSize(new Dimension(150, 35));
        tfPrixTotal.setPreferredSize(new Dimension(150, 35));
        
        tfPrixTotal.setEditable(false);

        
        
        // Création des pickers de dates
        dateDebutPicker = createDatePicker();
        dateFinPicker = createDatePicker();

        JPanel panelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.weightx = 1; 


        // Ligne 1 : Client
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelForm.add(new JLabel("Client :"), gbc);

        gbc.gridx = 1;
        panelForm.add(cbClient, gbc);

        // Ligne 2 : Voiture
        gbc.gridx = 0;
        gbc.gridy++;
        panelForm.add(new JLabel("Voiture :"), gbc);

        gbc.gridx = 1;
        panelForm.add(cbVoiture, gbc);

        // Ligne 3 : Date début
        gbc.gridx = 0;
        gbc.gridy++;
        panelForm.add(new JLabel("Date début :"), gbc);

        gbc.gridx = 1;
        panelForm.add(dateDebutPicker, gbc);

        // Ligne 4 : Date fin
        gbc.gridx = 0;
        gbc.gridy++;
        panelForm.add(new JLabel("Date fin :"), gbc);

        gbc.gridx = 1;
        panelForm.add(dateFinPicker, gbc);

        // Ligne 5 : Prix voiture
        gbc.gridx = 0;
        gbc.gridy++;
        panelForm.add(new JLabel("Prix total ($) :"), gbc);

        gbc.gridx = 1;
        panelForm.add(tfPrixTotal, gbc);

        // Ligne 6 : Prix options
        gbc.gridx = 0;
        gbc.gridy++;
        panelForm.add(new JLabel("Options ($) :"), gbc);

        gbc.gridx = 1;
        panelForm.add(lblOptionsTotal, gbc);

        // Ligne 7 : Total final
        gbc.gridx = 0;
        gbc.gridy++;
        panelForm.add(new JLabel("Total final ($) :"), gbc);

        gbc.gridx = 1;
        panelForm.add(lblTotalFinal, gbc);



        add(panelForm, BorderLayout.CENTER);
        
        panelChoix = new JPanel(new GridLayout(0, 2, 5, 5));
        panelChoix.setBorder(BorderFactory.createTitledBorder("Options disponibles (quantité)"));
        
        JScrollPane scrollPaneChoix = new JScrollPane(panelChoix);
        
        scrollPaneChoix.setPreferredSize(new Dimension(500, 500));
        
        add(scrollPaneChoix, BorderLayout.EAST);


        chargerChoix();


        // === Panel boutons ===
        btnAjouter = new JButton("Ajouter");
        btnRetour = new JButton("Retour au menu");

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelButtons.add(btnAjouter);
        panelButtons.add(btnRetour);
        add(panelButtons, BorderLayout.SOUTH);

        // Actions
        btnAjouter.addActionListener(e -> ajouterReservation());
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        // Auto-calcule du prix
        dateDebutPicker.addActionListener(e -> calculerPrixTotal());
        dateFinPicker.addActionListener(e -> calculerPrixTotal());
        cbVoiture.addActionListener(e -> calculerPrixTotal());

        chargerClients();
        chargerVoituresDisponibles();

        setVisible(true);
    }
    
    private void calculerTotalOptions() {
        double total = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT Prix_choix FROM CHOIX WHERE IdChoix = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

            for (int i = 0; i < checkChoix.size(); i++) {
                JCheckBox cb = checkChoix.get(i);
                if (cb.isSelected()) {
                    int idChoix = Integer.parseInt(cb.getName());
                    
                    String text = quantitesChoix.get(i).getText().trim();
                    int qte = text.isEmpty() ? 0 : Integer.parseInt(text);
 

                    ps.setInt(1, idChoix);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        total += rs.getDouble("Prix_choix") * qte;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur calcul options : " + e.getMessage());
        }

        lblOptionsTotal.setText(String.format("%.2f", total));
        calculerTotalFinal(); 
    }



    private void calculerTotalFinal() {
    try {
    	double prixVoiture = Double.parseDouble(tfPrixTotal.getText().trim().replace(",", "."));
    	double prixOptions = Double.parseDouble(lblOptionsTotal.getText().trim().replace(",", "."));

        lblTotalFinal.setText(String.format("%.2f", prixVoiture + prixOptions));
    } catch (Exception e) {
        lblTotalFinal.setText("0.00");
    }
    }


    private void chargerChoix() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM CHOIX";
            ResultSet rs = conn.createStatement().executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("IdChoix");
                String nom = rs.getString("Nom_choix") + " ($" + rs.getDouble("Prix_choix") + ")";
                JCheckBox cb = new JCheckBox(nom);
                cb.setName(String.valueOf(id));

                JTextField tfQte = new JTextField("0");
                
                tfQte.setHorizontalAlignment(JTextField.CENTER);
                
                tfQte.setPreferredSize(new Dimension(50, 50));

                checkChoix.add(cb);
                quantitesChoix.add(tfQte);

                panelChoix.add(cb);
                panelChoix.add(tfQte);

                cb.addActionListener(e -> calculerTotalOptions());
                tfQte.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { calculerTotalOptions(); }
                    public void removeUpdate(javax.swing.event.DocumentEvent e) { calculerTotalOptions(); }
                    public void insertUpdate(javax.swing.event.DocumentEvent e) { calculerTotalOptions(); }
                });
            }

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
                int id = rs.getInt("IdClient");
                String nom = rs.getString("Nom") + " " + rs.getString("Prenom");
                cbClient.addItem(id + " - " + nom);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement clients : " + e.getMessage());
        }
    }

    private void chargerVoituresDisponibles() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT IdVoiture, Marque, Modele FROM VOITURE WHERE Disponibilite = TRUE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("IdVoiture");
                String nom = rs.getString("Marque") + " " + rs.getString("Modele");
                cbVoiture.addItem(id + " - " + nom);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement voitures : " + e.getMessage());
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
                    double prixTotal = prixJour * days;
                    tfPrixTotal.setText(String.format("%.2f", prixTotal));
                    calculerTotalFinal();  
                }

            }

        } catch (Exception e) {
            tfPrixTotal.setText("0.00");
            calculerTotalFinal();
        }
    }

    private void ajouterReservation() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);  

            try {
                Date dateDebut = (Date) dateDebutPicker.getModel().getValue();
                Date dateFin = (Date) dateFinPicker.getModel().getValue();

                if (dateDebut == null || dateFin == null) {
                    JOptionPane.showMessageDialog(this, "Veuillez choisir les dates.");
                    return;
                }

                int idClient = Integer.parseInt(((String) cbClient.getSelectedItem()).split(" - ")[0]);
                int idVoiture = Integer.parseInt(((String) cbVoiture.getSelectedItem()).split(" - ")[0]);

                // Vérifie les conflits de réservation
                String verifSQL = """
                    SELECT COUNT(*) FROM RESERVATION 
                    WHERE IdVoiture = ? 
                      AND (? < Date_fin AND ? > Date_debut)
                """;

                PreparedStatement check = conn.prepareStatement(verifSQL);
                check.setInt(1, idVoiture);
                check.setDate(2, new java.sql.Date(dateFin.getTime()));
                check.setDate(3, new java.sql.Date(dateDebut.getTime()));
                ResultSet result = check.executeQuery();
                result.next();
                if (result.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Cette voiture est déjà réservée sur cette période.");
                    return;
                }

                // Calcul du prix final
                double prixVoiture = Double.parseDouble(tfPrixTotal.getText().replace(",", "."));
                double prixOptions = Double.parseDouble(lblOptionsTotal.getText().replace(",", "."));
                double prixTotal = prixVoiture + prixOptions;

                // Insertion de la réservation
                String sql = "INSERT INTO RESERVATION (Date_debut, Date_fin, PrixTotal, IdClient, IdVoiture) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setDate(1, new java.sql.Date(dateDebut.getTime()));
                ps.setDate(2, new java.sql.Date(dateFin.getTime()));
                ps.setDouble(3, prixTotal);
                ps.setInt(4, idClient);
                ps.setInt(5, idVoiture);
                ps.executeUpdate();

                // Récupération de l'ID de la réservation
                ResultSet rsId = ps.getGeneratedKeys();
                int idReservation = -1;
                if (rsId.next()) {
                    idReservation = rsId.getInt(1);
                }

                // Insertion des choix
                String insertChoix = "INSERT INTO CHOIX_RESERVATION (IdReservation, IdChoix, Quantite) VALUES (?, ?, ?)";
                PreparedStatement psChoix = conn.prepareStatement(insertChoix);

                for (int i = 0; i < checkChoix.size(); i++) {
                    JCheckBox cb = checkChoix.get(i);
                    if (cb.isSelected()) {
                        int idChoix = Integer.parseInt(cb.getName());
                        int qte = Integer.parseInt(quantitesChoix.get(i).getText());

                        psChoix.setInt(1, idReservation);
                        psChoix.setInt(2, idChoix);
                        psChoix.setInt(3, qte);
                        psChoix.executeUpdate();
                    }
                }

                // Mise à jour de disponibilité
                String update = "UPDATE VOITURE SET Disponibilite = FALSE WHERE IdVoiture = ?";
                PreparedStatement ps2 = conn.prepareStatement(update);
                ps2.setInt(1, idVoiture);
                ps2.executeUpdate();

                conn.commit();  

                JOptionPane.showMessageDialog(this, "Réservation ajoutée !");
                this.dispose();
                new MainMenu();

            } catch (Exception innerEx) {
                conn.rollback();  
                throw innerEx;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur transactionnelle : " + e.getMessage());
        }
    }



}
