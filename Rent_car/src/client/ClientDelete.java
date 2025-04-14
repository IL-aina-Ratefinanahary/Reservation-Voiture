package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import pack.DatabaseConnection;

public class ClientDelete extends JFrame {
    private JTextField tfId;
    private JButton btnSupprimer;

    public ClientDelete() {
        setTitle("Supprimer un client");
        setSize(300, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // Le bon layout ici

        // Partie formulaire (ID uniquement)
        JPanel panelForm = new JPanel(new GridLayout(1, 2, 5, 5));
        panelForm.add(new JLabel("ID Client:"));
        tfId = new JTextField();
        panelForm.add(tfId);
        add(panelForm, BorderLayout.CENTER);

        // Partie bouton
        btnSupprimer = new JButton("Supprimer");
        btnSupprimer.setPreferredSize(new Dimension(150, 30));

        JPanel panelBouton = new JPanel(); // FlowLayout par défaut = centré
        panelBouton.add(btnSupprimer);
        add(panelBouton, BorderLayout.SOUTH);

        btnSupprimer.addActionListener(e -> supprimerClient());

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
            } else {
                JOptionPane.showMessageDialog(this, "Aucun client trouvé.");
            }
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }
}
