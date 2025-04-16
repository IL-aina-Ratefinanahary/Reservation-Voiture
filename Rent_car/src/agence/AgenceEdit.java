package agence;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import pack.DatabaseConnection;

public class AgenceEdit extends JFrame {
    private JTextField tfNom, tfAdresse, tfTelephone, tfEmail;
    private int idAgence;

    public AgenceEdit(int idAgence) {
        this.idAgence = idAgence;

        setTitle("Modifier une agence");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(5, 2, 10, 10));

        tfNom = new JTextField();
        tfAdresse = new JTextField();
        tfTelephone = new JTextField();
        tfEmail = new JTextField();

        add(new JLabel("Nom de l'agence :"));    add(tfNom);
        add(new JLabel("Adresse :"));            add(tfAdresse);
        add(new JLabel("Téléphone :"));          add(tfTelephone);
        add(new JLabel("Email :"));              add(tfEmail);

        JButton btnModifier = new JButton("Modifier");
        JButton btnRetour = new JButton("Retour");

        btnModifier.addActionListener(e -> modifierAgence());
        btnRetour.addActionListener(e -> this.dispose());

        add(btnModifier);
        add(btnRetour);

        chargerAgence();
        setVisible(true);
    }

    private void chargerAgence() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM AGENCE WHERE IdAgence = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idAgence);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                tfNom.setText(rs.getString("Nom_agence"));
                tfAdresse.setText(rs.getString("Adresse_agence"));
                tfTelephone.setText(rs.getString("Telephone_agence"));
                tfEmail.setText(rs.getString("Email_agence"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement agence : " + e.getMessage());
        }
    }

    private void modifierAgence() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE AGENCE SET Nom_agence = ?, Adresse_agence = ?, Telephone_agence = ?, Email_agence = ? WHERE IdAgence = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tfNom.getText());
            ps.setString(2, tfAdresse.getText());
            ps.setString(3, tfTelephone.getText());
            ps.setString(4, tfEmail.getText());
            ps.setInt(5, idAgence);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Agence modifiée avec succès !");
            this.dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur de modification : " + e.getMessage());
        }
    }
}
