package Serveur;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GestionDonnees {

    private static final String DOSSIER       = "data/";
    private static final String FICHIER_USERS = DOSSIER + "utilisateurs.json";

    public static void initialiser() {
        new File(DOSSIER).mkdirs();
        if (!new File(FICHIER_USERS).exists()) ecrireFichier(FICHIER_USERS, "[]");
    }


    public static synchronized boolean creerCompte(String pseudo, String mdp) {
        if (pseudoExiste(pseudo)) return false;
        List<String[]> users = chargerUtilisateurs();
        users.add(new String[]{pseudo, mdp});
        sauvegarderUtilisateurs(users);
        return true;
    }

    public static synchronized boolean verifierLogin(String pseudo, String mdp) {
        for (String[] u : chargerUtilisateurs())
            if (u[0].equalsIgnoreCase(pseudo) && u[1].equals(mdp)) return true;
        return false;
    }

    public static synchronized boolean pseudoExiste(String pseudo) {
        for (String[] u : chargerUtilisateurs())
            if (u[0].equalsIgnoreCase(pseudo)) return true;
        return false;
    }

    public static synchronized List<String> getTousLesUtilisateurs() {
        List<String> pseudos = new ArrayList<>();
        for (String[] u : chargerUtilisateurs()) pseudos.add(u[0]);
        return pseudos;
    }


    public static synchronized void sauvegarderMessage(String exp, String dest, String contenu, String msgId) {
        String fichier  = getFichierConv(exp, dest);
        String existant = lireFichier(fichier);
        String heure    = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy").format(new Date());
        String entree   = "{\"de\":\"" + esc(exp) + "\",\"a\":\"" + esc(dest) + "\","
                        + "\"msg\":\"" + esc(contenu) + "\",\"heure\":\"" + heure
                        + "\",\"id\":\"" + msgId + "\"}";
        String json = (existant.trim().equals("[]") || existant.trim().isEmpty())
            ? "[" + entree + "]"
            : existant.trim().substring(0, existant.trim().length()-1) + "," + entree + "]";
        ecrireFichier(fichier, json);
    }

    public static synchronized List<String[]> getHistorique(String a, String b) {
        List<String[]> msgs = new ArrayList<>();
        String json = lireFichier(getFichierConv(a, b)).trim();
        if (json.equals("[]") || json.isEmpty()) return msgs;
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length()-1);
        for (String obj : separerObjets(json)) {
            String de    = extraire(obj, "de");
            String msg   = extraire(obj, "msg");
            String heure = extraire(obj, "heure");
            String id    = extraire(obj, "id");
            if (de != null && msg != null)
                msgs.add(new String[]{de, msg, heure != null ? heure : "", id != null ? id : "0"});
        }
        return msgs;
    }

    public static synchronized List<String> getConversationsExistantes(String pseudo) {
        List<String> res = new ArrayList<>();
        File[] fichiers = new File(DOSSIER).listFiles();
        if (fichiers == null) return res;
        for (File f : fichiers) {
            String n = f.getName();
            if (n.startsWith("conv_") && n.endsWith(".json")) {
                String corps = n.substring(5, n.length()-5);
                String[] parts = corps.split("_", 2);
                if (parts.length == 2) {
                    if (parts[0].equalsIgnoreCase(pseudo)) res.add(parts[1]);
                    else if (parts[1].equalsIgnoreCase(pseudo)) res.add(parts[0]);
                }
            }
        }
        return res;
    }

    public static synchronized List<String[]> getConversationsAvecDernierMessage(String pseudo) {
        List<String[]> res = new ArrayList<>();
        File[] fichiers = new File(DOSSIER).listFiles();
        if (fichiers == null) return res;
        for (File f : fichiers) {
            String n = f.getName();
            if (n.startsWith("conv_") && n.endsWith(".json")) {
                String corps = n.substring(5, n.length()-5);
                String[] parts = corps.split("_", 2);
                if (parts.length == 2) {
                    String partenaire = null;
                    if (parts[0].equalsIgnoreCase(pseudo)) partenaire = parts[1];
                    else if (parts[1].equalsIgnoreCase(pseudo)) partenaire = parts[0];
                    if (partenaire != null) {
                        List<String[]> hist = getHistorique(pseudo, partenaire);
                        if (!hist.isEmpty()) {
                            String[] dernier = hist.get(hist.size()-1);
                            String heure = dernier[2].length() >= 5 ? dernier[2].substring(0,5) : dernier[2];
                            res.add(new String[]{partenaire, dernier[1], heure});
                        } else {
                            res.add(new String[]{partenaire, "", ""});
                        }
                    }
                }
            }
        }
        return res;
    }


    public static synchronized void ajouterMessageEnAttente(String dest, String exp, String contenu, String heure, String msgId) {
        String fichier  = DOSSIER + "attente_" + dest.toLowerCase() + ".json";
        String existant = lireFichier(fichier).trim();
        String entree   = "{\"de\":\"" + esc(exp) + "\",\"msg\":\"" + esc(contenu)
                        + "\",\"heure\":\"" + heure + "\",\"id\":\"" + msgId + "\"}";
        String json = (existant.equals("[]") || existant.isEmpty())
            ? "[" + entree + "]"
            : existant.substring(0, existant.length()-1) + "," + entree + "]";
        ecrireFichier(fichier, json);
    }

    public static synchronized List<String[]> getMessagesEnAttente(String pseudo) {
        List<String[]> msgs = new ArrayList<>();
        String fichier = DOSSIER + "attente_" + pseudo.toLowerCase() + ".json";
        String json    = lireFichier(fichier).trim();
        if (json.equals("[]") || json.isEmpty()) return msgs;
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]"))   json = json.substring(0, json.length()-1);
        for (String obj : separerObjets(json)) {
            String de    = extraire(obj, "de");
            String msg   = extraire(obj, "msg");
            String heure = extraire(obj, "heure");
            String id    = extraire(obj, "id");
            if (de != null && msg != null)
                msgs.add(new String[]{de, msg, heure != null ? heure : "", id != null ? id : "0"});
        }
        return msgs;
    }

    public static synchronized void clearMessagesEnAttente(String pseudo) {
        ecrireFichier(DOSSIER + "attente_" + pseudo.toLowerCase() + ".json", "[]");
    }


    private static String getFichierConv(String a, String b) {
        String[] n = {a.toLowerCase(), b.toLowerCase()};
        Arrays.sort(n);
        return DOSSIER + "conv_" + n[0] + "_" + n[1] + ".json";
    }

    private static List<String[]> chargerUtilisateurs() {
        List<String[]> users = new ArrayList<>();
        String json = lireFichier(FICHIER_USERS).trim();
        if (json.equals("[]") || json.isEmpty()) return users;
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length()-1);
        for (String obj : separerObjets(json)) {
            String p = extraire(obj, "pseudo"), m = extraire(obj, "motDePasse");
            if (p != null && m != null) users.add(new String[]{p, m});
        }
        return users;
    }

    private static void sauvegarderUtilisateurs(List<String[]> users) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            sb.append("{\"pseudo\":\"").append(esc(users.get(i)[0]))
              .append("\",\"motDePasse\":\"").append(esc(users.get(i)[1])).append("\"}");
            if (i < users.size()-1) sb.append(",");
        }
        ecrireFichier(FICHIER_USERS, sb.append("]").toString());
    }

    private static String lireFichier(String chemin) {
        try {
            File f = new File(chemin);
            return f.exists() ? new String(Files.readAllBytes(f.toPath())) : "[]";
        } catch (IOException e) { return "[]"; }
    }

    private static void ecrireFichier(String chemin, String contenu) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(chemin))) { pw.print(contenu); }
        catch (IOException e) { System.err.println("[ERR] Écriture: " + chemin); }
    }

    private static String extraire(String json, String cle) {
        String cherche = "\"" + cle + "\":\"";
        int debut = json.indexOf(cherche);
        if (debut == -1) return null;
        debut += cherche.length();
        int fin = debut;
        while (fin < json.length()) {
            if (json.charAt(fin) == '"' && json.charAt(fin-1) != '\\') break;
            fin++;
        }
        return json.substring(debut, fin).replace("\\\"","\"").replace("\\n","\n");
    }

    private static List<String> separerObjets(String json) {
        List<String> objets = new ArrayList<>();
        int prof = 0, debut = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (prof++ == 0) debut = i; }
            else if (c == '}') { if (--prof == 0 && debut != -1) { objets.add(json.substring(debut, i+1)); debut = -1; } }
        }
        return objets;
    }

    private static String esc(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
    }
}