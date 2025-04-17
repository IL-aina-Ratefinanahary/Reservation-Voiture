package paiement;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Date;
import java.util.Properties;

import org.jdatepicker.impl.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class PaiementForm extends JFrame {
    private JComboBox<String> cbReservation;
    private JDatePickerImpl datePicker;
    private JTextField tfMontant;
    private JComboBox<String> cbModePaiement, cbStatutPaiement;
    private JButton btnAjouter, btnRetour;
    private JLabel lblPrixReservation = new JLabel("0.00");


    public PaiementForm() {
        setTitle("Ajouter un paiement");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        setResizable(false);


     // === Formulaire ===
        cbReservation = new JComboBox<>();
        tfMontant = new JTextField();
        cbModePaiement = new JComboBox<>(new String[] {"Carte", "Espèces", "Virement"});
        cbStatutPaiement = new JComboBox<>(new String[] {"Payé", "En attente", "Échoué"});
        datePicker = createDatePicker();

        JPanel panelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // marge autour des composants
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panelForm.add(new JLabel("Réservation :"), gbc);
        gbc.gridx = 1; panelForm.add(cbReservation, gbc);
        gbc.gridx = 0; gbc.gridy++; panelForm.add(new JLabel("Date de paiement :"), gbc);
        gbc.gridx = 1; panelForm.add(datePicker, gbc);
        gbc.gridx = 0; gbc.gridy++; panelForm.add(new JLabel("Montant :"), gbc);
        gbc.gridx = 1; panelForm.add(tfMontant, gbc);
        gbc.gridx = 0; gbc.gridy++; panelForm.add(new JLabel("Mode :"), gbc);
        gbc.gridx = 1; panelForm.add(cbModePaiement, gbc);
        gbc.gridx = 0; gbc.gridy++; panelForm.add(new JLabel("Statut :"), gbc);
        gbc.gridx = 1; panelForm.add(cbStatutPaiement, gbc);
        gbc.gridx = 0; gbc.gridy++; panelForm.add(new JLabel("Montant réservation ($) :"), gbc);
        gbc.gridx = 1; panelForm.add(lblPrixReservation, gbc);


        add(panelForm, BorderLayout.CENTER);

        // === Boutons ===
        btnAjouter = new JButton("Ajouter");
        btnRetour = new JButton("Retour");

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelButtons.add(btnAjouter);
        panelButtons.add(btnRetour);
        add(panelButtons, BorderLayout.SOUTH);

        // === Actions ===
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        btnAjouter.addActionListener(e -> ajouterPaiement());

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
        return new JDatePickerImpl(panel, new reservation.DateLabelFormatter());
    }

    private void chargerReservations() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT R.IdReservation, C.Nom, V.Marque
                FROM RESERVATION R
                JOIN CLIENT C ON R.IdClient = C.IdClient
                JOIN VOITURE V ON R.IdVoiture = V.IdVoiture
            """;

            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("IdReservation");
                String label = rs.getString("Nom") + " - " + rs.getString("Marque");
                cbReservation.addItem(id + " - " + label);
                cbReservation.addActionListener(e -> {
                    try (Connection conn2 = DatabaseConnection.getConnection()) {
                        int idres = Integer.parseInt(cbReservation.getSelectedItem().toString().split(" - ")[0]);
                        String prixSQL = "SELECT PrixTotal FROM RESERVATION WHERE IdReservation = ?";
                        PreparedStatement ps2 = conn2.prepareStatement(prixSQL);
                        ps2.setInt(1, idres);
                        ResultSet rs2 = ps2.executeQuery();
                        if (rs2.next()) {
                            lblPrixReservation.setText(String.format("%.2f", rs2.getDouble("PrixTotal")));
                        }
                    } catch (Exception ex) {
                        lblPrixReservation.setText("Erreur");
                    }
                });

            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement réservations : " + e.getMessage());
        }
    }

    private void ajouterPaiement() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (cbReservation.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une réservation.");
                return;
            }

            Date utilDate = (Date) datePicker.getModel().getValue();
            if (utilDate == null) {
                JOptionPane.showMessageDialog(this, "Veuillez choisir une date de paiement.");
                return;
            }

            double montant = Double.parseDouble(tfMontant.getText());
            int idReservation = Integer.parseInt(cbReservation.getSelectedItem().toString().split(" - ")[0]);
            String mode = cbModePaiement.getSelectedItem().toString();
            String statut = cbStatutPaiement.getSelectedItem().toString();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

            String sql = """
                INSERT INTO PAIEMENT (Date_paiement, Montant, Mode_paiement, Statut_paiement, IdReservation)
                VALUES (?, ?, ?, ?, ?)
            """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, sqlDate);
            ps.setDouble(2, montant);
            ps.setString(3, mode);
            ps.setString(4, statut);
            ps.setInt(5, idReservation);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Paiement ajouté !");
            this.dispose();
            new MainMenu();

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Le montant doit être un nombre valide.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage());
        }
    }
}
