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
	    setSize(700, 600);
	    setResizable(false); 
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setLocationRelativeTo(null); 

	    // === Barre de menu ===
	    JMenuBar menuBar = new JMenuBar();

	    JMenu clientMenu = new JMenu("Clients");
	    JMenuItem ajouterClient = new JMenuItem("Ajouter");
	    JMenuItem listeClient = new JMenuItem("Liste");
	    clientMenu.add(ajouterClient);
	    clientMenu.add(listeClient);

	    JMenu voitureMenu = new JMenu("Voitures");
	    JMenuItem ajouterVoiture = new JMenuItem("Ajouter une voiture");
	    JMenuItem listeVoiture = new JMenuItem("Liste des voitures");
	    voitureMenu.add(ajouterVoiture);
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

	    JMenu paiementMenu = new JMenu("Paiements");
	    JMenuItem ajouterPaiement = new JMenuItem("Ajouter un paiement");
	    JMenuItem listePaiements = new JMenuItem("Liste des paiements");
	    paiementMenu.add(ajouterPaiement);
	    paiementMenu.add(listePaiements);

	    JMenu choixMenu = new JMenu("Options (Choix)");
	    JMenuItem ajouterChoix = new JMenuItem("Ajouter un choix");
	    JMenuItem listeChoix = new JMenuItem("Liste des choix");
	    choixMenu.add(ajouterChoix);
	    choixMenu.add(listeChoix);

	    menuBar.add(clientMenu);
	    menuBar.add(voitureMenu);
	    menuBar.add(reservationMenu);
	    menuBar.add(agenceMenu);
	    menuBar.add(paiementMenu);
	    menuBar.add(choixMenu);
	    setJMenuBar(menuBar);

	    // === Panneau de recherche central ===
	    JPanel panelRechercheMultiple = new JPanel();
	    panelRechercheMultiple.setLayout(new BoxLayout(panelRechercheMultiple, BoxLayout.Y_AXIS));
	    panelRechercheMultiple.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50)); // marges globales

	    // Fonction utilitaire pour créer les blocs de recherche
	    panelRechercheMultiple.add(createRecherchePanel("🔍 Rechercher un client", e -> {
	        String q = tfClient.getText().toLowerCase().trim();
	        if (q.matches(".*(toyota|bmw|mazda|ford|audi).*")) {
	            JOptionPane.showMessageDialog(this, "❌ Ceci ressemble à une voiture. Veuillez utiliser la section Voiture.");
	        } else {
	            new client.ClientList(q);
	        }
	    }));

	    panelRechercheMultiple.add(createRecherchePanel("🚗 Rechercher une voiture", e -> {
	        String q = tfVoiture.getText().toLowerCase().trim();
	        if (q.matches(".*(dupont|martin|ratefy|tremblay).*")) {
	            JOptionPane.showMessageDialog(this, "❌ Ceci semble être un nom de client. Utilisez la section Client.");
	        } else {
	            new voiture.VoitureList(q);
	        }
	    }));

	    panelRechercheMultiple.add(createRecherchePanel("📅 Rechercher une réservation", e -> {
	        String q = tfReservation.getText().toLowerCase().trim();
	        if (q.length() < 2) {
	            JOptionPane.showMessageDialog(this, "❌ Entrez au moins 2 lettres pour rechercher.");
	        } else {
	            new reservation.ReservationList(q);
	        }
	    }));

	    panelRechercheMultiple.add(createRecherchePanel("🏢 Rechercher une agence", e -> {
	        String q = tfAgence.getText().toLowerCase().trim();
	        if (q.matches(".*(mazda|ford|dupont|client).*")) {
	            JOptionPane.showMessageDialog(this, "❌ Mot-clé incohérent pour une agence.");
	        } else {
	            new agence.AgenceList(q);
	        }
	    }));

	    add(panelRechercheMultiple, BorderLayout.CENTER);

	    // === Actions des menus ===
	    ajouterClient.addActionListener(e -> new client.ClientForm());
	    listeClient.addActionListener(e -> new client.ClientList());
	    ajouterVoiture.addActionListener(e -> new voiture.VoitureForm());
	    listeVoiture.addActionListener(e -> new voiture.VoitureList());
	    ajouterResa.addActionListener(e -> new reservation.ReservationForm());
	    listeResa.addActionListener(e -> new reservation.ReservationList());
	    ajouterAgence.addActionListener(e -> new agence.AgenceForm());
	    listeAgences.addActionListener(e -> new agence.AgenceList());
	    ajouterPaiement.addActionListener(e -> new paiement.PaiementForm());
	    listePaiements.addActionListener(e -> new paiement.PaiementList());
	    ajouterChoix.addActionListener(e -> new choix.ChoixForm());
	    listeChoix.addActionListener(e -> new choix.ChoixList());

	    setVisible(true);
	}

	// === Déclarations partagées pour les champs de recherche ===
	private JTextField tfClient = new JTextField(20);
	private JTextField tfVoiture = new JTextField(20);
	private JTextField tfReservation = new JTextField(20);
	private JTextField tfAgence = new JTextField(20);

	// === Méthode utilitaire ===
	private JPanel createRecherchePanel(String title, java.awt.event.ActionListener searchAction) {
	    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
	    panel.setBorder(BorderFactory.createTitledBorder(title));
	    JTextField tf = switch (title) {
	        case "🔍 Rechercher un client" -> tfClient;
	        case "🚗 Rechercher une voiture" -> tfVoiture;
	        case "📅 Rechercher une réservation" -> tfReservation;
	        case "🏢 Rechercher une agence" -> tfAgence;
	        default -> new JTextField(20);
	    };
	    JButton btn = new JButton("Rechercher");
	    btn.addActionListener(searchAction);
	    panel.add(tf);
	    panel.add(btn);
	    return panel;
	}



    public static void main(String[] args) {
        new MainMenu();
    }
}
