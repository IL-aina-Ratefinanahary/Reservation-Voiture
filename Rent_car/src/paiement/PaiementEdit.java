package paiement;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.jdatepicker.impl.*;
import pack.DatabaseConnection;

public class PaiementEdit extends JFrame {
    private int idPaiement;
    private JDatePickerImpl datePicker;
    private JTextField tfMontant;
    private JComboBox<String> cbModePaiement, cbStatutPaiement;
    private JButton btnModifier, btnRetour;

    public PaiementEdit(int idPaiement) {
        this.idPaiement = idPaiement;
        setTitle("Modifier un paiement");
        setSize(500, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panelForm = new JPanel(new GridLayout(4, 2, 10, 10));

        tfMontant = new JTextField();
        cbModePaiement = new JComboBox<>(new String[] {"Carte", "Espèces", "Virement"});
        cbStatutPaiement = new JComboBox<>(new String[] {"Payé", "En attente", "Échoué"});
        datePicker = createDatePicker();

        panelForm.add(new JLabel("Date de paiement :")); panelForm.add(datePicker);
        panelForm.add(new JLabel("Montant :"));          panelForm.add(tfMontant);
        panelForm.add(new JLabel("Mode :"));             panelForm.add(cbModePaiement);
        panelForm.add(new JLabel("Statut :"));           panelForm.add(cbStatutPaiement);

        add(panelForm, BorderLayout.CENTER);

        btnModifier = new JButton("Modifier");
        btnRetour = new JButton("Retour");
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelButtons.add(btnModifier);
        panelButtons.add(btnRetour);
        add(panelButtons, BorderLayout.SOUTH);

        btnRetour.addActionListener(e -> this.dispose());
        btnModifier.addActionListener(e -> modifierPaiement());

        chargerPaiement();
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

    private void chargerPaiement() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM PAIEMENT WHERE IdPaiement = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idPaiement);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Date date = rs.getDate("Date_paiement");
                if (date != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    datePicker.getModel().setDate(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    );
                    datePicker.getModel().setSelected(true);
                }

                tfMontant.setText(String.valueOf(rs.getDouble("Montant")));
                cbModePaiement.setSelectedItem(rs.getString("Mode_paiement"));
                cbStatutPaiement.setSelectedItem(rs.getString("Statut_paiement"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement paiement : " + e.getMessage());
        }
    }

    private void modifierPaiement() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Date utilDate = (Date) datePicker.getModel().getValue();
            if (utilDate == null) {
                JOptionPane.showMessageDialog(this, "Veuillez choisir une date.");
                return;
            }

            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            double montant = Double.parseDouble(tfMontant.getText());
            String mode = cbModePaiement.getSelectedItem().toString();
            String statut = cbStatutPaiement.getSelectedItem().toString();

            String sql = """
                UPDATE PAIEMENT 
                SET Date_paiement=?, Montant=?, Mode_paiement=?, Statut_paiement=?
                WHERE IdPaiement=?
            """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, sqlDate);
            ps.setDouble(2, montant);
            ps.setString(3, mode);
            ps.setString(4, statut);
            ps.setInt(5, idPaiement);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Paiement mis à jour !");
            this.dispose();

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Le montant n'est pas valide.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage());
        }
    }
}
