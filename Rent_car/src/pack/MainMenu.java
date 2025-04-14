package pack;

import javax.swing.*;
import java.awt.event.*;

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
        // Ajoute éléments pour voiture ici…

        menuBar.add(clientMenu);
        menuBar.add(voitureMenu);
        setJMenuBar(menuBar);

        // Actions
        ajouterClient.addActionListener(e -> new client.ClientForm());
        listeClient.addActionListener(e -> new client.ClientList());

        setVisible(true);
    }

    public static void main(String[] args) {
        new MainMenu();
    }
}
