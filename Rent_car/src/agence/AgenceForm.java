package agence;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class AgenceForm extends JFrame {
    private JTextField tfNom, tfAdresse, tfTelephone, tfEmail;

    public AgenceForm() {
        setTitle("Ajouter une agence");
        setSize(500, 300);
        setResizable(false); // Empêche le redimensionnement
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // === Formulaire avec GridBagLayout ===
        JPanel panelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tfNom = new JTextField(20);
        tfAdresse = new JTextField(20);
        tfTelephone = new JTextField(20);
        tfEmail = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelForm.add(new JLabel("Nom de l'agence :"), gbc);
        gbc.gridx = 1;
        panelForm.add(tfNom, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panelForm.add(new JLabel("Adresse :"), gbc);
        gbc.gridx = 1;
        panelForm.add(tfAdresse, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panelForm.add(new JLabel("Téléphone :"), gbc);
        gbc.gridx = 1;
        panelForm.add(tfTelephone, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panelForm.add(new JLabel("Email :"), gbc);
        gbc.gridx = 1;
        panelForm.add(tfEmail, gbc);

        add(panelForm, BorderLayout.CENTER);

        // === Boutons ===
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnRetour = new JButton("Retour au menu");

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelButtons.add(btnAjouter);
        panelButtons.add(btnRetour);
        add(panelButtons, BorderLayout.SOUTH);

        // === Actions ===
        btnAjouter.addActionListener(e -> ajouterAgence());

        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        setVisible(true);
    }

    private void ajouterAgence() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO AGENCE (Nom_agence, Adresse_agence, Telephone_agence, Email_agence) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tfNom.getText().trim());
            ps.setString(2, tfAdresse.getText().trim());
            ps.setString(3, tfTelephone.getText().trim());
            ps.setString(4, tfEmail.getText().trim());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Agence ajoutée avec succès !");
            this.dispose();
            new MainMenu();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Erreur SQL : " + e.getMessage());
        }
    }
}
