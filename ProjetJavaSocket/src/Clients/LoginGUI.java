package Clients;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginGUI extends JFrame {

    private static final Color BG       = new Color(15, 15, 25);
    private static final Color CARD     = new Color(25, 25, 40);
    private static final Color ACCENT   = new Color(99, 102, 241);
    private static final Color ACCENT2  = new Color(139, 92, 246);
    private static final Color TEXT     = new Color(226, 232, 240);
    private static final Color TEXT_DIM = new Color(148, 163, 184);
    private static final Color BORDER_C = new Color(55, 55, 80);
    private static final Color INPUT_BG = new Color(18, 18, 32);
    private static final Color ERR_COL  = new Color(239, 68, 68);
    private static final Color GREEN    = new Color(52, 211, 153);

    private JTextField     champPseudoLogin;
    private JPasswordField champMdpLogin;
    private JLabel         labelErrLogin;

    private JTextField     champPseudoReg;
    private JPasswordField champMdpReg;
    private JPasswordField champMdpReg2;
    private JLabel         labelErrReg;

    private Client client;
    private CardLayout cardLayout;
    private JPanel     cardPanel;

    public LoginGUI() {
        setTitle("ChatApp — Connexion");
        setSize(440, 580);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG);
        setContentPane(root);

        JPanel carte = new JPanel(new BorderLayout(0, 0));
        carte.setBackground(CARD);
        carte.setPreferredSize(new Dimension(370, 510));
        carte.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(28, 32, 28, 32)
        ));

        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel logo = new JLabel("💬");
        logo.setFont(new Font("SansSerif", Font.PLAIN, 40));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titreLbl = new JLabel("ChatApp");
        titreLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        titreLbl.setForeground(TEXT);
        titreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sousLbl = new JLabel("Connectez-vous pour continuer");
        sousLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sousLbl.setForeground(TEXT_DIM);
        sousLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoPanel.add(logo);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(titreLbl);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(sousLbl);
        carte.add(logoPanel, BorderLayout.NORTH);

        JButton btnTabLogin = creerBoutonOnglet("Connexion",   true);
        JButton btnTabReg   = creerBoutonOnglet("Inscription", false);

        JPanel onglets = new JPanel(new GridLayout(1, 2, 0, 0));
        onglets.setBackground(new Color(18, 18, 32));
        onglets.setPreferredSize(new Dimension(0, 40));
        onglets.setBorder(new MatteBorder(1, 1, 0, 1, BORDER_C));
        onglets.add(btnTabLogin);
        onglets.add(btnTabReg);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(CARD);
        cardPanel.setBorder(new MatteBorder(1, 1, 1, 1, BORDER_C));
        cardPanel.add(construireOngletLogin(),       "LOGIN");
        cardPanel.add(construireOngletInscription(), "REGISTER");

        btnTabLogin.addActionListener(e -> {
            cardLayout.show(cardPanel, "LOGIN");
            btnTabLogin.setBackground(CARD);      btnTabLogin.setForeground(ACCENT);
            btnTabReg.setBackground(new Color(18,18,32)); btnTabReg.setForeground(TEXT_DIM);
        });
        btnTabReg.addActionListener(e -> {
            cardLayout.show(cardPanel, "REGISTER");
            btnTabReg.setBackground(CARD);        btnTabReg.setForeground(ACCENT2);
            btnTabLogin.setBackground(new Color(18,18,32)); btnTabLogin.setForeground(TEXT_DIM);
        });

        JPanel centre = new JPanel(new BorderLayout());
        centre.setOpaque(false);
        centre.add(onglets,   BorderLayout.NORTH);
        centre.add(cardPanel, BorderLayout.CENTER);
        carte.add(centre, BorderLayout.CENTER);

        root.add(carte);
        connecterAuServeur();
    }

    private JPanel construireOngletLogin() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        p.setBorder(new EmptyBorder(22, 16, 20, 16));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;

        champPseudoLogin = creerChamp();
        champMdpLogin    = creerChampMdp();
        labelErrLogin    = creerLabelErr();
        JButton btnLogin = creerBoutonAction("✔   Se connecter", ACCENT);

        btnLogin.addActionListener(e -> tentativeLogin());
        champPseudoLogin.addActionListener(e -> tentativeLogin());
        champMdpLogin.addActionListener(e -> tentativeLogin());

        g.gridy = 0; g.insets = new Insets(0, 0, 4, 0);  p.add(label("Pseudo"), g);
        g.gridy = 1; g.insets = new Insets(0, 0, 14, 0); p.add(champPseudoLogin, g);
        g.gridy = 2; g.insets = new Insets(0, 0, 4, 0);  p.add(label("Mot de passe"), g);
        g.gridy = 3; g.insets = new Insets(0, 0, 6, 0);  p.add(champMdpLogin, g);
        g.gridy = 4; g.insets = new Insets(0, 0, 14, 0); p.add(labelErrLogin, g);
        g.gridy = 5; g.insets = new Insets(0, 0, 0, 0);  p.add(btnLogin, g);

        return p;
    }

    private JPanel construireOngletInscription() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;

        champPseudoReg = creerChamp();
        champMdpReg    = creerChampMdp();
        champMdpReg2   = creerChampMdp();
        labelErrReg    = creerLabelErr();
        JButton btnReg = creerBoutonAction("✔   Créer le compte", ACCENT2);
        btnReg.addActionListener(e -> tentativeInscription());

        g.gridy = 0; g.insets = new Insets(0, 0, 4, 0);  p.add(label("Pseudo"), g);
        g.gridy = 1; g.insets = new Insets(0, 0, 10, 0); p.add(champPseudoReg, g);
        g.gridy = 2; g.insets = new Insets(0, 0, 4, 0);  p.add(label("Mot de passe"), g);
        g.gridy = 3; g.insets = new Insets(0, 0, 10, 0); p.add(champMdpReg, g);
        g.gridy = 4; g.insets = new Insets(0, 0, 4, 0);  p.add(label("Confirmer le mot de passe"), g);
        g.gridy = 5; g.insets = new Insets(0, 0, 6, 0);  p.add(champMdpReg2, g);
        g.gridy = 6; g.insets = new Insets(0, 0, 10, 0); p.add(labelErrReg, g);
        g.gridy = 7; g.insets = new Insets(0, 0, 0, 0);  p.add(btnReg, g);

        return p;
    }

    private void connecterAuServeur() {
        client = new Client(new Client.ClientListener() {
            @Override public void onLoginOk(String pseudo) {
                SwingUtilities.invokeLater(() -> ouvrirMainGUI(pseudo));
            }
            @Override public void onErreur(String msg) {
                SwingUtilities.invokeLater(() -> {
                    labelErrLogin.setText(msg);
                    labelErrReg.setText(msg);
                });
            }
            @Override public void onMessageRecu(String e, String h, String id, String c) {}
            @Override public void onMessageEnvoye(String d, String h, String id, String c) {}
            @Override public void onDelivered(String dest, String msgId) {}
            @Override public void onRead(String dest, String msgId) {}
            @Override public void onUsersListe(java.util.List<String> u) {}
            @Override public void onOnlineListe(java.util.List<String> o) {}
            @Override public void onHistorique(java.util.List<String[]> m) {}
            @Override public void onConversationsListe(java.util.List<String> c) {}
            @Override public void onConversationsListeAvecMessages(java.util.List<String[]> c) {}
            @Override public void onUserConnecte(String p) {}
            @Override public void onUserDeconnecte(String p) {}
            @Override public void onDeconnexion() {
                SwingUtilities.invokeLater(() ->
                    labelErrLogin.setText("Serveur inaccessible — démarrez le serveur."));
            }
        });
        client.connecterAuServeur();
    }

    private void tentativeLogin() {
        labelErrLogin.setText("");
        String pseudo = champPseudoLogin.getText().trim();
        String mdp    = new String(champMdpLogin.getPassword());
        if (pseudo.isEmpty() || mdp.isEmpty()) {
            labelErrLogin.setText("Veuillez remplir tous les champs."); return;
        }
        client.connecter(pseudo, mdp);
    }

    private void tentativeInscription() {
        labelErrReg.setForeground(ERR_COL);
        labelErrReg.setText("");
        String pseudo = champPseudoReg.getText().trim();
        String mdp    = new String(champMdpReg.getPassword());
        String mdp2   = new String(champMdpReg2.getPassword());

        if (pseudo.isEmpty() || mdp.isEmpty() || mdp2.isEmpty()) {
            labelErrReg.setText("Veuillez remplir tous les champs"); return;
        }
        if (pseudo.length() < 3) {
            labelErrReg.setText("Pseudo trop court"); return;
        }
        if (!mdp.equals(mdp2)) {
            labelErrReg.setText("Les mots de passe ne correspondent pas"); return;
        }
        client.inscrire(pseudo, mdp);

        Timer t = new Timer(600, ev -> {
            if (labelErrReg.getText().isEmpty()) {
                labelErrReg.setForeground(GREEN);
                labelErrReg.setText("Compte créé avec succés");
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void ouvrirMainGUI(String pseudo) {
        MainGUI main = new MainGUI(client, pseudo);
        main.setVisible(true);
        dispose();
    }

    private JTextField creerChamp() {
        JTextField f = new JTextField();
        styliser(f); return f;
    }

    private JPasswordField creerChampMdp() {
        JPasswordField f = new JPasswordField();
        styliser(f); return f;
    }

    private void styliser(JTextField f) {
        f.setBackground(INPUT_BG);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(0, 38));
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private JLabel label(String texte) {
        JLabel l = new JLabel(texte);
        l.setForeground(TEXT_DIM);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        return l;
    }

    private JLabel creerLabelErr() {
        JLabel l = new JLabel(" ");
        l.setForeground(ERR_COL);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return l;
    }

    private JButton creerBoutonAction(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setBackground(couleur);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(0, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(couleur.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(couleur); }
        });
        return btn;
    }

    private JButton creerBoutonOnglet(String texte, boolean actif) {
        JButton btn = new JButton(texte);
        btn.setBackground(actif ? CARD : new Color(18, 18, 32));
        btn.setForeground(actif ? ACCENT : TEXT_DIM);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}