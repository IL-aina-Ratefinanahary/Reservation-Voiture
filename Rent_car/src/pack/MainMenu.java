package pack;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.*;

import reservation.ReservationForm;
import reservation.ReservationList;
import voiture.VoitureForm;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Menu Principal");
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();

        JMenu clientMenu = new JMenu("Clients");
        JMenuItem ajouterClient = new JMenuItem("Ajouter");
        JMenuItem listeClient = new JMenuItem("Liste");
        clientMenu.add(ajouterClient);
        clientMenu.add(listeClient);

        JMenu voitureMenu = new JMenu("Voitures");
        JMenuItem ajouterVoiture = new JMenuItem("Ajouter une voiture");
        voitureMenu.add(ajouterVoiture);
        JMenuItem listeVoiture = new JMenuItem("Liste des voitures");
        voitureMenu.add(listeVoiture);

        JMenu reservationMenu = new JMenu("Réservations");
        JMenuItem ajouterResa = new JMenuItem("Ajouter une réservation");
        JMenuItem listeResa = new JMenuItem("Liste des réservations");
        reservationMenu.add(ajouterResa);
        reservationMenu.add(listeResa);

        JMenu agenceMenu = new JMenu("Agences");
        JMenuItem ajouterAgence = new JMenuItem("Ajouter une agence");
        JMenuItem listeAgences = new JMenuItem("Liste des agences");
        agenceMenu.add(ajouterAgence);
        agenceMenu.add(listeAgences);

        menuBar.add(clientMenu);
        menuBar.add(voitureMenu);
        menuBar.add(reservationMenu);
        menuBar.add(agenceMenu);
        
        JMenu paiementMenu = new JMenu("Paiements");
        JMenuItem ajouterPaiement = new JMenuItem("Ajouter un paiement");
        JMenuItem listePaiements = new JMenuItem("Liste des paiements");
        paiementMenu.add(ajouterPaiement);
        paiementMenu.add(listePaiements);
        menuBar.add(paiementMenu);
        
        setJMenuBar(menuBar);
        
        
        JMenu choixMenu = new JMenu("Options (Choix)");
        JMenuItem ajouterChoix = new JMenuItem("Ajouter un choix");
        JMenuItem listeChoix = new JMenuItem("Liste des choix");

        choixMenu.add(ajouterChoix);
        choixMenu.add(listeChoix);
        menuBar.add(choixMenu);


        // === Barres de recherche multiples ===
        JPanel panelRechercheMultiple = new JPanel();
        panelRechercheMultiple.setLayout(new GridLayout(4, 1, 10, 10));

        // --- Client ---
        JPanel panelClient = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelClient.setBorder(BorderFactory.createTitledBorder("🔍 Rechercher un client"));
        JTextField tfClient = new JTextField(20);
        JButton btnClient = new JButton("Rechercher");
        panelClient.add(tfClient);
        panelClient.add(btnClient);
        panelRechercheMultiple.add(panelClient);

        // --- Voiture ---
        JPanel panelVoiture = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelVoiture.setBorder(BorderFactory.createTitledBorder("🚗 Rechercher une voiture"));
        JTextField tfVoiture = new JTextField(20);
        JButton btnVoiture = new JButton("Rechercher");
        panelVoiture.add(tfVoiture);
        panelVoiture.add(btnVoiture);
        panelRechercheMultiple.add(panelVoiture);

        // --- Réservation ---
        JPanel panelReservation = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelReservation.setBorder(BorderFactory.createTitledBorder("📅 Rechercher une réservation"));
        JTextField tfReservation = new JTextField(20);
        JButton btnReservation = new JButton("Rechercher");
        panelReservation.add(tfReservation);
        panelReservation.add(btnReservation);
        panelRechercheMultiple.add(panelReservation);

        // --- Agence ---
        JPanel panelAgence = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAgence.setBorder(BorderFactory.createTitledBorder("🏢 Rechercher une agence"));
        JTextField tfAgence = new JTextField(20);
        JButton btnAgence = new JButton("Rechercher");
        panelAgence.add(tfAgence);
        panelAgence.add(btnAgence);
        panelRechercheMultiple.add(panelAgence);

        add(panelRechercheMultiple, BorderLayout.NORTH);

        // === Actions des boutons ===
        btnClient.addActionListener(e -> {
            String q = tfClient.getText().toLowerCase().trim();
            if (q.matches(".*(toyota|bmw|mazda|ford|audi).*")) {
                JOptionPane.showMessageDialog(this, "❌ Ceci ressemble à une voiture. Veuillez utiliser la section Voiture.");
            } else {
                new client.ClientList(q);
            }
        });

        btnVoiture.addActionListener(e -> {
            String q = tfVoiture.getText().toLowerCase().trim();
            if (q.matches(".*(dupont|martin|ratefy|tremblay).*")) {
                JOptionPane.showMessageDialog(this, "❌ Ceci semble être un nom de client. Utilisez la section Client.");
            } else {
                new voiture.VoitureList(q);
            }
        });

        btnAgence.addActionListener(e -> {
            String q = tfAgence.getText().toLowerCase().trim();
            if (q.matches(".*(mazda|ford|dupont|client).*")) {
                JOptionPane.showMessageDialog(this, "❌ Mot-clé incohérent pour une agence. Essayez un vrai nom d’agence.");
            } else {
                new agence.AgenceList(q);
            }
        });

        btnReservation.addActionListener(e -> {
            String q = tfReservation.getText().toLowerCase().trim();
            if (q.length() < 2) {
                JOptionPane.showMessageDialog(this, "❌ Entrez au moins 2 lettres pour rechercher.");
            } else {
                new reservation.ReservationList(q);
            }
        });
        
        ajouterPaiement.addActionListener(e -> new paiement.PaiementForm());
        listePaiements.addActionListener(e -> new paiement.PaiementList());


        
        ajouterVoiture.addActionListener(e -> new VoitureForm());
        listeVoiture.addActionListener(e -> new voiture.VoitureList());
        ajouterClient.addActionListener(e -> new client.ClientForm());
        listeClient.addActionListener(e -> new client.ClientList());
        ajouterResa.addActionListener(e -> new ReservationForm());
        listeResa.addActionListener(e -> new ReservationList());
        ajouterAgence.addActionListener(e -> new agence.AgenceForm());
        listeAgences.addActionListener(e -> new agence.AgenceList());
        ajouterChoix.addActionListener(e -> new choix.ChoixForm());
        listeChoix.addActionListener(e -> new choix.ChoixList());


        setVisible(true);
    }

    public static void main(String[] args) {
        new MainMenu();
    }
}
