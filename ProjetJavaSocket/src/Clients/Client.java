package Clients;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    public static final String HOTE = "localhost";
    public static final int    PORT = 5000;

    private Socket         socket;
    private PrintWriter    out;
    private BufferedReader in;
    private volatile boolean connecte = false;
    private String         pseudo;
    private ClientListener listener;

    public interface ClientListener {
        void onLoginOk(String pseudo);
        void onErreur(String message);
        void onMessageRecu(String expediteur, String heure, String msgId, String contenu);
        void onMessageEnvoye(String destinataire, String heure, String msgId, String contenu);
        void onDelivered(String destinataire, String msgId);   
        void onRead(String destinataire, String msgId);        
        void onUsersListe(List<String> users);
        void onOnlineListe(List<String> online);
        void onHistorique(List<String[]> messages);
        void onConversationsListe(List<String> convs);
        void onConversationsListeAvecMessages(List<String[]> convs);
        void onUserConnecte(String pseudo);
        void onUserDeconnecte(String pseudo);
        void onDeconnexion();
    }

    public Client(ClientListener listener) {
        this.listener = listener;
    }

    public boolean connecterAuServeur() {
        try {
            socket = new Socket(HOTE, PORT);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            connecte = true;
            new Thread(this::ecouterServeur, "Client-Listener").start();
            return true;
        } catch (IOException e) {
            listener.onErreur("Impossible de se connecter : " + e.getMessage());
            return false;
        }
    }

    private void ecouterServeur() {
        try {
            String ligne;
            while (connecte && (ligne = in.readLine()) != null) {
                traiterReponse(ligne);
            }
        } catch (IOException e) {
            if (connecte) listener.onDeconnexion();
        } finally {
            connecte = false;
        }
    }

    private void traiterReponse(String ligne) {
        if (ligne.startsWith("OK:")) {
            String data = ligne.substring(3);
            if (pseudo == null && !data.isEmpty()) { pseudo = data; listener.onLoginOk(pseudo); }

        } else if (ligne.startsWith("ERR:")) {
            listener.onErreur(ligne.substring(4));

        } else if (ligne.startsWith("MSG_IN:")) {
        	
            String[] p = ligne.substring(7).split(":", 4);
            if (p.length == 4) listener.onMessageRecu(p[0], p[1], p[2], p[3]);

        } else if (ligne.startsWith("MSG_SENT:")) {
        	
            String[] p = ligne.substring(9).split(":", 4);
            if (p.length == 4) listener.onMessageEnvoye(p[0], p[1], p[2], p[3]);

        } else if (ligne.startsWith("DELIVERED:")) {
        	
            String[] p = ligne.substring(10).split(":", 2);
            if (p.length == 2) listener.onDelivered(p[0], p[1]);

        } else if (ligne.startsWith("READ:")) {
        	
            String[] p = ligne.substring(5).split(":", 2);
            if (p.length == 2) listener.onRead(p[0], p[1]);

        } else if (ligne.startsWith("USERS_LIST:")) {
            String data = ligne.substring(11);
            listener.onUsersListe(data.isEmpty() ? new ArrayList<>() : Arrays.asList(data.split(",")));

        } else if (ligne.startsWith("ONLINE_LIST:")) {
            String data = ligne.substring(12);
            listener.onOnlineListe(data.isEmpty() ? new ArrayList<>() : Arrays.asList(data.split(",")));

        } else if (ligne.startsWith("CONV_HISTORY:")) {
            String data = ligne.substring(13);
            List<String[]> hist = new ArrayList<>();
            if (!data.isEmpty()) {
                for (String e : data.split("(?<!;);(?!;)")) {
                    String[] parts = e.split("\\|", 4);
                    if (parts.length >= 3) {
                        hist.add(new String[]{
                            parts[0],
                            parts[1].replace(";;",";"),
                            parts[2],
                            parts.length > 3 ? parts[3] : "0"
                        });
                    }
                }
            }
            listener.onHistorique(hist);

        } else if (ligne.startsWith("CONVS_LIST:")) {
            String data = ligne.substring(11);
            
            List<String[]> convs = new ArrayList<>();
            if (!data.isEmpty()) {
                for (String entry : data.split(",")) {
                    String[] parts = entry.split("\\|", 3);
                    if (parts.length == 3) {
                        convs.add(new String[]{parts[0], parts[1].replace("{{c}}",","), parts[2]});
                    } else if (parts.length == 1) {
                        convs.add(new String[]{parts[0], "", ""});
                    }
                }
            }
            listener.onConversationsListeAvecMessages(convs);

        } else if (ligne.startsWith("USER_CONNECTED:")) {
            listener.onUserConnecte(ligne.substring(15));

        } else if (ligne.startsWith("USER_DISCONNECTED:")) {
            listener.onUserDeconnecte(ligne.substring(18));
        }
    }

    public void inscrire(String pseudo, String mdp)               { envoyer("REGISTER:" + pseudo + ":" + mdp); }
    public void connecter(String pseudo, String mdp)              { envoyer("LOGIN:" + pseudo + ":" + mdp); }
    public void envoyerMessage(String dest, String contenu)       { envoyer("MSG:" + dest + ":" + contenu); }
    public void signalerLu(String expediteur, String msgId)       { envoyer("READ:" + expediteur + ":" + msgId); }
    public void demanderTousUsers()                               { envoyer("GET_USERS"); }
    public void demanderOnline()                                  { envoyer("GET_ONLINE"); }
    public void demanderHistorique(String autre)                  { envoyer("GET_CONV:" + autre); }
    public void demanderConversations()                           { envoyer("GET_CONVS"); }
    public void deconnecter() {
        connecte = false;
        envoyer("LOGOUT");
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    private void envoyer(String message) {
        if (out != null && connecte) out.println(message);
    }

    public void setListener(ClientListener l) { this.listener = l; }
    public String getPseudo()     { return pseudo; }
    public boolean isConnecte()   { return connecte; }
}