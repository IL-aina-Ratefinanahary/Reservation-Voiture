package client;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import pack.DatabaseConnection;
import pack.MainMenu;

public class ClientDelete extends JFrame {
    private JTextField tfId;
    private JButton btnSupprimer, btnRetour;

    public ClientDelete() {
        setTitle("Supprimer un client");
        setSize(400, 180);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // ✅ Layout principal

        // Panel formulaire avec ID
        JPanel panelForm = new JPanel(new GridLayout(1, 2, 5, 5));
        panelForm.add(new JLabel("ID Client:"));
        tfId = new JTextField();
        panelForm.add(tfId);
        add(panelForm, BorderLayout.CENTER);

        // Boutons en bas
        btnSupprimer = new JButton("Supprimer");
        btnRetour = new JButton("Retour au menu");

        JPanel panelActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelActions.add(btnSupprimer);
        panelActions.add(btnRetour);
        add(panelActions, BorderLayout.SOUTH);

        // Action boutons
        btnSupprimer.addActionListener(e -> supprimerClient());
        btnRetour.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        setVisible(true);
    }

    private void supprimerClient() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM CLIENT WHERE IdClient = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(tfId.getText()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Client supprimé !");
                this.dispose();
                new MainMenu(); // Optionnel si tu veux revenir automatiquement
            } else {
                JOptionPane.showMessageDialog(this, "Aucun client trouvé.");
            }
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }
}
