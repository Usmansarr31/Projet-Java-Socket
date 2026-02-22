package Serveur;

import java.io.*;
import java.net.*;
import java.util.List;


public class GestionClient implements Runnable {

    private final Socket socket;
    private final Serveur serveur;
    private PrintWriter    out;
    private BufferedReader in;
    private String pseudo = null;

    public GestionClient(Socket socket, Serveur serveur) {
        this.socket  = socket;
        this.serveur = serveur;
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(),  "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            String ligne;
            while ((ligne = in.readLine()) != null) {
                traiterCommande(ligne.trim());
            }
        } catch (IOException e) {
            
        } finally {
            deconnecter();
        }
    }

    private void traiterCommande(String commande) {
        if (commande.startsWith("REGISTER:"))        traiterRegister(commande.substring(9));
        else if (commande.startsWith("LOGIN:"))      traiterLogin(commande.substring(6));
        else if (commande.startsWith("MSG:"))        traiterMsg(commande.substring(4));
        else if (commande.equals("GET_USERS"))       traiterGetUsers();
        else if (commande.equals("GET_ONLINE"))      traiterGetOnline();
        else if (commande.startsWith("GET_CONV:"))   traiterGetConv(commande.substring(9));
        else if (commande.equals("GET_CONVS"))       traiterGetConvs();
        else if (commande.startsWith("READ:"))       traiterRead(commande.substring(5));
        else if (commande.equals("LOGOUT"))          deconnecter();
    }

    private void traiterRegister(String data) {
        String[] parts = data.split(":", 2);
        if (parts.length < 2) { envoyer("ERR:Format invalide"); return; }
        String p = parts[0].trim(), mdp = parts[1].trim();
        if (p.isEmpty() || mdp.isEmpty()) { envoyer("ERR:Champs vides"); return; }
        if (GestionDonnees.creerCompte(p, mdp)) {
            envoyer("OK:Compte créé");
            serveur.log("[REGISTER] " + p);
        } else {
            envoyer("ERR:Pseudo déjà utilisé");
        }
    }

    private void traiterLogin(String data) {
        String[] parts = data.split(":", 2);
        if (parts.length < 2) { envoyer("ERR:Format invalide"); return; }
        String p = parts[0].trim(), mdp = parts[1].trim();
        if (!GestionDonnees.verifierLogin(p, mdp)) {
            envoyer("ERR:Pseudo ou mot de passe incorrect"); return;
        }
        if (serveur.estEnLigne(p)) {
            envoyer("ERR:Utilisateur déjà connecté"); return;
        }
        this.pseudo = p;
        serveur.ajouterClient(this);
        envoyer("OK:" + pseudo);
        serveur.log("[LOGIN] " + pseudo);
        serveur.diffuserNotification("USER_CONNECTED:" + pseudo, this);

        List<String[]> enAttente = GestionDonnees.getMessagesEnAttente(pseudo);
        for (String[] m : enAttente) {
            
            envoyer("MSG_IN:" + m[0] + ":" + m[2] + ":" + m[3] + ":" + m[1]);
            
            GestionClient exp = serveur.getClient(m[0]);
            if (exp != null) exp.envoyer("DELIVERED:" + pseudo + ":" + m[3]);
        }
        GestionDonnees.clearMessagesEnAttente(pseudo);
    }

    private void traiterMsg(String data) {
        if (pseudo == null) { envoyer("ERR:Non connecté"); return; }
        int idx = data.indexOf(':');
        if (idx == -1) { envoyer("ERR:Format invalide"); return; }
        String dest    = data.substring(0, idx).trim();
        String contenu = data.substring(idx + 1).trim();
        if (contenu.isEmpty()) { envoyer("ERR:Message vide"); return; }

        String heure  = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
        String msgId  = String.valueOf(System.currentTimeMillis());

        GestionDonnees.sauvegarderMessage(pseudo, dest, contenu, msgId);

        envoyer("MSG_SENT:" + dest + ":" + heure + ":" + msgId + ":" + contenu);


        GestionClient destClient = serveur.getClient(dest);
        if (destClient != null) {
            destClient.envoyer("MSG_IN:" + pseudo + ":" + heure + ":" + msgId + ":" + contenu);
            
            envoyer("DELIVERED:" + dest + ":" + msgId);
        } else {
            
            GestionDonnees.ajouterMessageEnAttente(dest, pseudo, contenu, heure, msgId);
        }
        serveur.log("[MSG] " + pseudo + " → " + dest);
    }

    private void traiterRead(String data) {
       
        int idx = data.indexOf(':');
        if (idx == -1) return;
        String expediteur = data.substring(0, idx);
        String msgId      = data.substring(idx + 1);
        GestionClient expClient = serveur.getClient(expediteur);
        if (expClient != null) {
            expClient.envoyer("READ:" + pseudo + ":" + msgId);
        }
    }

    private void traiterGetUsers() {
        List<String> users = GestionDonnees.getTousLesUtilisateurs();
        envoyer("USERS_LIST:" + String.join(",", users));
    }

    private void traiterGetOnline() {
        List<String> online = serveur.getClientsEnLigne();
        envoyer("ONLINE_LIST:" + String.join(",", online));
    }

    private void traiterGetConv(String autre) {
        if (pseudo == null) { envoyer("ERR:Non connecté"); return; }
        List<String[]> hist = GestionDonnees.getHistorique(pseudo, autre);
        StringBuilder sb = new StringBuilder("CONV_HISTORY:");
        for (int i = 0; i < hist.size(); i++) {
            String[] m = hist.get(i);
            
            sb.append(m[0]).append("|")
              .append(m[1].replace(";",";;")).append("|")
              .append(m[2]).append("|")
              .append(m.length > 3 ? m[3] : "0");
            if (i < hist.size() - 1) sb.append(";");
        }
        envoyer(sb.toString());
    }

    private void traiterGetConvs() {
        if (pseudo == null) { envoyer("ERR:Non connecté"); return; }
        List<String[]> convs = GestionDonnees.getConversationsAvecDernierMessage(pseudo);
        StringBuilder sbConv = new StringBuilder("CONVS_LIST:");
        for (int i = 0; i < convs.size(); i++) {
            String[] c = convs.get(i);
            sbConv.append(c[0]).append("|").append(c[1].replace(",","{{c}}")).append("|").append(c[2]);
            if (i < convs.size()-1) sbConv.append(",");
        }
        envoyer(sbConv.toString());
    }

    public void envoyer(String message) {
        if (out != null) out.println(message);
    }

    private void deconnecter() {
        if (pseudo != null) {
            serveur.retirerClient(this);
            serveur.diffuserNotification("USER_DISCONNECTED:" + pseudo, this);
            serveur.log("[LOGOUT] " + pseudo);
            pseudo = null;
        }
        try { if (!socket.isClosed()) socket.close(); } catch (IOException ignored) {}
    }

    public String getPseudo() { return pseudo; }
}