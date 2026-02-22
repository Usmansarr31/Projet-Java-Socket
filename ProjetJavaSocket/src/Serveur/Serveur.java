package Serveur;

import java.io.*;
import java.net.*;
import java.util.*;

public class Serveur {

    public static final int PORT = 5000;

    private ServerSocket serverSocket;
    private final Map<String, GestionClient> clientsEnLigne = Collections.synchronizedMap(new LinkedHashMap<>());
    private ServeurGUI gui;
    private boolean actif = false;

    public Serveur(ServeurGUI gui) {
        this.gui = gui;
        GestionDonnees.initialiser();
    }

    public void demarrer() {
        try {
            serverSocket = new ServerSocket(PORT);
            actif = true;
            log("[SERVEUR] Démarré sur le port " + PORT);
            gui.mettreAJourStatut(true);

            while (actif) {
                try {
                    Socket socket = serverSocket.accept();
                    log("[CONNEXION] Nouvelle connexion depuis " + socket.getInetAddress().getHostAddress());
                    GestionClient gc = new GestionClient(socket, this);
                    new Thread(gc).start();
                } catch (IOException e) {
                    if (actif) log("[ERREUR] Acceptation connexion : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log("[ERREUR] Démarrage serveur : " + e.getMessage());
            gui.mettreAJourStatut(false);
        }
    }

    public void arreter() {
        actif = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            log("[ERREUR] Arrêt serveur : " + e.getMessage());
        }
        log("[SERVEUR] Arrêté.");
        gui.mettreAJourStatut(false);
    }

    public synchronized void ajouterClient(GestionClient gc) {
        clientsEnLigne.put(gc.getPseudo().toLowerCase(), gc);
        gui.mettreAJourClients(getClientsEnLigne());
    }

    public synchronized void retirerClient(GestionClient gc) {
        if (gc.getPseudo() != null) {
            clientsEnLigne.remove(gc.getPseudo().toLowerCase());
            gui.mettreAJourClients(getClientsEnLigne());
        }
    }

    public synchronized GestionClient getClient(String pseudo) {
        return clientsEnLigne.get(pseudo.toLowerCase());
    }

    public synchronized boolean estEnLigne(String pseudo) {
        return clientsEnLigne.containsKey(pseudo.toLowerCase());
    }

    public synchronized List<String> getClientsEnLigne() {
        List<String> liste = new ArrayList<>();
        for (GestionClient gc : clientsEnLigne.values()) {
            liste.add(gc.getPseudo());
        }
        return liste;
    }

    public synchronized void diffuserNotification(String message, GestionClient expediteur) {
        for (GestionClient gc : clientsEnLigne.values()) {
            if (gc != expediteur) gc.envoyer(message);
        }
    }

    public void log(String message) {
        String heure = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        gui.afficherLog("[" + heure + "] " + message);
    }

    
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ServeurGUI gui = new ServeurGUI();
            gui.setVisible(true);
        });
    }
}