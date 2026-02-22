package Clients;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class MainGUI extends JFrame implements Client.ClientListener {

    private static final Color BG_APP       = new Color(15,  23,  42);   
    private static final Color SIDEBAR_BG   = new Color(17,  27,  50);   
    private static final Color HEADER_BG    = new Color(21,  33,  61);   
    private static final Color ITEM_BG      = new Color(17,  27,  50);
    private static final Color ITEM_SEL     = new Color(30,  46,  80);
    private static final Color ITEM_HOV     = new Color(24,  37,  66);
    private static final Color CHAT_BG      = new Color(11,  19,  38);   
    private static final Color MSG_OUT      = new Color(0,   84,  166);  
    private static final Color MSG_IN       = new Color(24,  37,  66);   
    private static final Color ACCENT       = new Color(59,  130, 246);  
    private static final Color GREEN_DOT    = new Color(34,  197, 94);
    private static final Color BADGE_COL    = new Color(59,  130, 246);
    private static final Color TEXT         = new Color(229, 236, 255);
    private static final Color TEXT_DIM     = new Color(148, 163, 200);
    private static final Color TEXT_TIME    = new Color(100, 120, 170);
    private static final Color BORDER_C     = new Color(30,  45,  80);
    private static final Color INPUT_BG     = new Color(21,  33,  61);
    private static final Color TICK_GREY    = new Color(148, 163, 200);
    private static final Color TICK_BLUE    = new Color(59,  130, 246);
    private static final Color SEARCH_BG    = new Color(24,  37,  66);

    private final Client client;
    private final String pseudo;

    private final Map<String, String>          derniersMessages = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<String, String>          heuresMessages   = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Integer>         nonLusMap        = Collections.synchronizedMap(new HashMap<>());
    private final Set<String>                  enLigne          = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, ConversationGUI> fenetresConv     = new HashMap<>();
    private List<String>                       tousLesUsers     = new ArrayList<>();
    private String                             convSelectionnee = null;

    private JPanel     sidebarConvPanel;
    private JSplitPane splitPane;

    public MainGUI(Client client, String pseudo) {
        this.client = client;
        this.pseudo = pseudo;
        client.setListener(this);
        client.demanderConversations();
        client.demanderOnline();
        client.demanderTousUsers();

        setTitle("ChatApp — " + pseudo);
        setSize(1000, 660);
        setMinimumSize(new Dimension(750, 520));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmerDeconnexion(); }
        });
        construireUI();
    }

    private void construireUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_APP);
        setContentPane(root);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                construireSidebar(), construirePlaceholder());
        splitPane.setDividerLocation(320);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        splitPane.setBackground(BORDER_C);
        root.add(splitPane, BorderLayout.CENTER);
    }

    private JPanel construireSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_C));

        sidebar.add(construireHeaderSidebar(), BorderLayout.NORTH);
        sidebar.add(construireListeConversations(), BorderLayout.CENTER);
        sidebar.add(construireFooterSidebar(), BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel construireHeaderSidebar() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(creerAvatar(pseudo, 38));
        JLabel nom = new JLabel(pseudo);
        nom.setForeground(TEXT);
        nom.setFont(new Font("SansSerif", Font.BOLD, 15));
        left.add(nom);
        header.add(left, BorderLayout.WEST);

        JButton btnDeco = creerIconeBtn("⏻", new Color(239, 68, 68));
        btnDeco.setToolTipText("Déconnexion");
        btnDeco.addActionListener(e -> confirmerDeconnexion());
        header.add(btnDeco, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(HEADER_BG);
        wrapper.add(header, BorderLayout.CENTER);
        wrapper.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));
        return wrapper;
    }

    private JScrollPane construireListeConversations() {
        sidebarConvPanel = new JPanel();
        sidebarConvPanel.setLayout(new BoxLayout(sidebarConvPanel, BoxLayout.Y_AXIS));
        sidebarConvPanel.setBackground(SIDEBAR_BG);

        JScrollPane scroll = new JScrollPane(sidebarConvPanel);
        scroll.setBorder(null);
        scroll.setBackground(SIDEBAR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getVerticalScrollBar().setBackground(SIDEBAR_BG);
        return scroll;
    }

    private JPanel construireFooterSidebar() {
        JButton btnNouv = new JButton("✉   Nouvelle conversation");
        btnNouv.setBackground(ACCENT);
        btnNouv.setForeground(Color.WHITE);
        btnNouv.setFocusPainted(false);
        btnNouv.setBorderPainted(false);
        btnNouv.setOpaque(true);
        btnNouv.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnNouv.setPreferredSize(new Dimension(0, 48));
        btnNouv.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNouv.addMouseListener(hoverEffect(btnNouv, ACCENT));
        btnNouv.addActionListener(e -> ouvrirSelectionUser());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(HEADER_BG);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
        footer.add(btnNouv);
        return footer;
    }

    private JPanel construirePlaceholder() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CHAT_BG);
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel emoji = new JLabel("💬");
        emoji.setFont(new Font("SansSerif", Font.PLAIN, 52));
        emoji.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("Sélectionnez une conversation");
        msg.setForeground(TEXT_DIM);
        msg.setFont(new Font("SansSerif", Font.PLAIN, 15));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(emoji);
        center.add(Box.createVerticalStrut(10));
        center.add(msg);
        p.add(center);
        return p;
    }

    private void rafraichirSidebar() {
        SwingUtilities.invokeLater(() -> {
            sidebarConvPanel.removeAll();
            synchronized (derniersMessages) {
                for (Map.Entry<String, String> e : derniersMessages.entrySet()) {
                    String cle      = e.getKey();
                    String apercu   = e.getValue();
                    String heure    = heuresMessages.getOrDefault(cle, "");
                    int    nonLus   = nonLusMap.getOrDefault(cle, 0);
                    String affich   = pseudoOriginal(cle);
                    sidebarConvPanel.add(creerItemConv(cle, affich, apercu, heure, nonLus));
                }
            }
            if (derniersMessages.isEmpty()) {
                JLabel vide = new JLabel("  Aucune conversation");
                vide.setForeground(TEXT_DIM);
                vide.setFont(new Font("SansSerif", Font.ITALIC, 12));
                vide.setBorder(new EmptyBorder(24, 16, 0, 0));
                sidebarConvPanel.add(vide);
            }
            sidebarConvPanel.revalidate();
            sidebarConvPanel.repaint();
        });
    }

    private JPanel creerItemConv(String cle, String affich, String apercu, String heure, int nonLus) {
        boolean sel = cle.equals(convSelectionnee);

        JPanel item = new JPanel(new BorderLayout(12, 0));
        item.setBackground(sel ? ITEM_SEL : ITEM_BG);
        item.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_C),
            new EmptyBorder(10, 14, 10, 14)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel avWrap = new JPanel(null);
        avWrap.setOpaque(false);
        avWrap.setPreferredSize(new Dimension(48, 46));
        JLabel av = creerAvatar(affich, 42);
        av.setBounds(0, 2, 42, 42);
        avWrap.add(av);
        if (enLigne.contains(cle)) {
            JLabel dot = new JLabel();
            dot.setBackground(GREEN_DOT);
            dot.setOpaque(true);
            dot.setBounds(31, 31, 11, 11);
            dot.setBorder(new LineBorder(SIDEBAR_BG, 2));
            avWrap.add(dot);
        }
        item.add(avWrap, BorderLayout.WEST);

        JPanel centre = new JPanel(new BorderLayout(0, 3));
        centre.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel nomLbl = new JLabel(affich);
        nomLbl.setForeground(TEXT);
        nomLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel heureLbl = new JLabel(heure);
        heureLbl.setForeground(nonLus > 0 ? ACCENT : TEXT_TIME);
        heureLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        top.add(nomLbl,   BorderLayout.WEST);
        top.add(heureLbl, BorderLayout.EAST);
        centre.add(top, BorderLayout.NORTH);

        JPanel bot = new JPanel(new BorderLayout(6, 0));
        bot.setOpaque(false);
        String txt = apercu.length() > 35 ? apercu.substring(0,35)+"…" : apercu;
        JLabel apercuLbl = new JLabel(txt.isEmpty() ? "Nouvelle conversation" : txt);
        apercuLbl.setForeground(nonLus > 0 ? TEXT : TEXT_DIM);
        apercuLbl.setFont(new Font("SansSerif", nonLus > 0 ? Font.BOLD : Font.PLAIN, 12));
        bot.add(apercuLbl, BorderLayout.CENTER);


        centre.add(bot, BorderLayout.SOUTH);
        item.add(centre, BorderLayout.CENTER);

        if (nonLus > 0) {
            String nbTxt = nonLus > 99 ? "99+" : String.valueOf(nonLus);
            JLabel badge = new JLabel(nbTxt, SwingConstants.CENTER);
            badge.setFont(new Font("SansSerif", Font.BOLD, 10));
            badge.setForeground(Color.WHITE);
            badge.setBackground(GREEN_DOT);
            badge.setOpaque(true);
            int bw = nonLus > 9 ? 24 : 20;
            badge.setPreferredSize(new Dimension(bw, 20));
            JPanel badgePanel = new JPanel(new GridBagLayout());
            badgePanel.setOpaque(false);
            badgePanel.setPreferredSize(new Dimension(bw + 8, 46));
            badgePanel.add(badge);
            item.add(badgePanel, BorderLayout.EAST);
        }

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { ouvrirConversation(cle); }
            @Override public void mouseEntered(MouseEvent e) { if (!sel) item.setBackground(ITEM_HOV); }
            @Override public void mouseExited(MouseEvent e)  { if (!sel) item.setBackground(sel ? ITEM_SEL : ITEM_BG); }
        });
        return item;
    }

    private void ouvrirConversation(String cle) {
        convSelectionnee = cle;
        nonLusMap.put(cle, 0);
        rafraichirSidebar();

        String affich = pseudoOriginal(cle);
        ConversationGUI conv = fenetresConv.computeIfAbsent(cle, k -> {
            ConversationGUI c = new ConversationGUI(client, pseudo, affich, enLigne.contains(k));
            c.setOnMessageSentListener((dest, ap) -> {
                String d = dest.toLowerCase();
                derniersMessages.put(d, ap);
                heuresMessages.put(d, heureActuelle());
                rafraichirSidebar();
            });
            c.setOnCloseListener(() -> {
                convSelectionnee = null;
                splitPane.setRightComponent(construirePlaceholder());
                splitPane.revalidate();
                splitPane.repaint();
                rafraichirSidebar();
            });
            return c;
        });

        splitPane.setRightComponent((JPanel) conv.getContentPane());
        splitPane.revalidate();
        splitPane.repaint();
        derniersMessages.putIfAbsent(cle, "");
        client.demanderHistorique(affich);
    }

    private void ouvrirSelectionUser() {
        client.demanderTousUsers();
        client.demanderOnline();
        new Timer(300, e -> afficherDialogue()) {{ setRepeats(false); start(); }};
    }

    private void afficherDialogue() {
        SwingUtilities.invokeLater(() -> {
            List<String> autres = new ArrayList<>();
            for (String u : tousLesUsers)
                if (!u.equalsIgnoreCase(pseudo)) autres.add(u);
            if (autres.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Aucun autre utilisateur.", "Nouveau", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JDialog dlg = new JDialog(this, "Nouvelle conversation", true);
            dlg.setSize(300, 420);
            dlg.setLocationRelativeTo(this);

            JPanel p = new JPanel(new BorderLayout(0, 0));
            p.setBackground(HEADER_BG);

            // Titre
            JLabel titre = new JLabel("  Choisir un contact");
            titre.setForeground(TEXT);
            titre.setFont(new Font("SansSerif", Font.BOLD, 14));
            titre.setPreferredSize(new Dimension(0, 46));
            titre.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));
            p.add(titre, BorderLayout.NORTH);

            DefaultListModel<String> model = new DefaultListModel<>();
            autres.forEach(model::addElement);
            JList<String> liste = new JList<>(model);
            liste.setBackground(SIDEBAR_BG);
            liste.setForeground(TEXT);
            liste.setFont(new Font("SansSerif", Font.PLAIN, 13));
            liste.setFixedCellHeight(54);
            liste.setCellRenderer(new UserCellRenderer());
            liste.setSelectionBackground(ITEM_SEL);
            JScrollPane sc = new JScrollPane(liste);
            sc.setBorder(null);
            p.add(sc, BorderLayout.CENTER);

            JButton ok = new JButton("Ouvrir →");
            ok.setBackground(ACCENT); ok.setForeground(Color.WHITE);
            ok.setFocusPainted(false); ok.setBorderPainted(false); ok.setOpaque(true);
            ok.setFont(new Font("SansSerif", Font.BOLD, 13));
            ok.setPreferredSize(new Dimension(0, 46));
            ok.addActionListener(ev -> {
                String sel = liste.getSelectedValue();
                if (sel != null) { dlg.dispose(); ouvrirConversation(sel.toLowerCase()); }
            });
            liste.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && liste.getSelectedValue() != null) {
                        dlg.dispose();
                        ouvrirConversation(liste.getSelectedValue().toLowerCase());
                    }
                }
            });
            JPanel foot = new JPanel(new BorderLayout());
            foot.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
            foot.add(ok);
            p.add(foot, BorderLayout.SOUTH);
            dlg.setContentPane(p);
            dlg.setVisible(true);
        });
    }

    private void confirmerDeconnexion() {
        int r = JOptionPane.showConfirmDialog(this,
            "Voulez-vous vous déconnecter ?", "Déconnexion", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            client.deconnecter();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
        }
    }

    @Override public void onLoginOk(String pseudo) {}

    @Override
    public void onErreur(String message) {
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE));
    }

    @Override
    public void onMessageRecu(String expediteur, String heure, String msgId, String contenu) {
        String cle = expediteur.toLowerCase();
        derniersMessages.put(cle, contenu);
        heuresMessages.put(cle, heure);

        if (!cle.equals(convSelectionnee)) {
            nonLusMap.merge(cle, 1, Integer::sum);
            setTitle("(" + expediteur + ") — ChatApp");
            new Timer(3000, e -> setTitle("ChatApp — " + pseudo)) {{ setRepeats(false); start(); }};
        }
        rafraichirSidebar();

        ConversationGUI conv = fenetresConv.computeIfAbsent(cle, k -> {
            ConversationGUI c = new ConversationGUI(client, pseudo, expediteur, enLigne.contains(k));
            c.setOnMessageSentListener((dest, ap) -> {
                String d = dest.toLowerCase();
                derniersMessages.put(d, ap);
                heuresMessages.put(d, heureActuelle());
                rafraichirSidebar();
            });
            return c;
        });
        conv.ajouterMessageRecu(expediteur, heure, msgId, contenu);
        if (cle.equals(convSelectionnee)) {
            splitPane.setRightComponent((JPanel) conv.getContentPane());
            splitPane.revalidate();
        }
    }

    @Override
    public void onMessageEnvoye(String destinataire, String heure, String msgId, String contenu) {
        String cle = destinataire.toLowerCase();
        derniersMessages.put(cle, contenu);
        heuresMessages.put(cle, heure);
        rafraichirSidebar();
        ConversationGUI conv = fenetresConv.get(cle);
        if (conv != null) conv.ajouterMessageEnvoye(heure, msgId, contenu);
    }

    @Override
    public void onDelivered(String dest, String msgId) {
        ConversationGUI c = fenetresConv.get(dest.toLowerCase());
        if (c != null) c.mettreAJourStatutDelivered(msgId);
    }

    @Override
    public void onRead(String dest, String msgId) {
        ConversationGUI c = fenetresConv.get(dest.toLowerCase());
        if (c != null) c.mettreAJourStatutLu(msgId);
    }

    @Override
    public void onUsersListe(List<String> users) { tousLesUsers = new ArrayList<>(users); }

    @Override
    public void onOnlineListe(List<String> online) {
        enLigne.clear();
        for (String u : online) enLigne.add(u.toLowerCase());
        rafraichirSidebar();
        fenetresConv.forEach((k, c) -> c.mettreAJourStatut(enLigne.contains(k)));
    }

    @Override
    public void onHistorique(List<String[]> messages) {
        if (convSelectionnee == null) return;
        ConversationGUI conv = fenetresConv.get(convSelectionnee);
        if (conv != null) conv.chargerHistorique(messages, pseudo);
    }

    @Override
    public void onConversationsListe(List<String> convs) {

        for (String c : convs) derniersMessages.putIfAbsent(c.toLowerCase(), "");
        rafraichirSidebar();
    }

    @Override
    public void onConversationsListeAvecMessages(List<String[]> convs) {
        for (String[] c : convs) {
            String cle = c[0].toLowerCase();
            derniersMessages.put(cle, c[1]);
            if (!c[2].isEmpty()) heuresMessages.put(cle, c[2]);
        }
        rafraichirSidebar();
    }

    @Override
    public void onUserConnecte(String p) {
        enLigne.add(p.toLowerCase()); rafraichirSidebar();
        ConversationGUI c = fenetresConv.get(p.toLowerCase());
        if (c != null) c.mettreAJourStatut(true);
    }

    @Override
    public void onUserDeconnecte(String p) {
        enLigne.remove(p.toLowerCase()); rafraichirSidebar();
        ConversationGUI c = fenetresConv.get(p.toLowerCase());
        if (c != null) c.mettreAJourStatut(false);
    }

    @Override
    public void onDeconnexion() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                "Connexion perdue.", "Déconnecté", JOptionPane.WARNING_MESSAGE);
            dispose();
            new LoginGUI().setVisible(true);
        });
    }

    private String pseudoOriginal(String cle) {
        for (String u : tousLesUsers)
            if (u.equalsIgnoreCase(cle)) return u;
        return cle.isEmpty() ? cle :
               Character.toUpperCase(cle.charAt(0)) + cle.substring(1);
    }

    private String heureActuelle() {
        return new SimpleDateFormat("HH:mm").format(new Date());
    }

    private JLabel creerAvatar(String nom, int taille) {
        String ini = nom.isEmpty() ? "?" : String.valueOf(nom.charAt(0)).toUpperCase();
        JLabel av = new JLabel(ini, SwingConstants.CENTER);
        av.setPreferredSize(new Dimension(taille, taille));
        av.setMinimumSize(new Dimension(taille, taille));
        av.setMaximumSize(new Dimension(taille, taille));
        av.setFont(new Font("SansSerif", Font.BOLD, taille / 3));
        av.setForeground(Color.WHITE);
        av.setBackground(couleurAvatar(nom));
        av.setOpaque(true);
        return av;
    }

    private Color couleurAvatar(String nom) {
        Color[] c = {
            new Color(59, 130, 246), new Color(139, 92, 246), new Color(16, 185, 129),
            new Color(245, 158, 11), new Color(239, 68, 68),  new Color(6, 182, 212)
        };
        return c[Math.abs(nom.toLowerCase().hashCode()) % c.length];
    }

    private JButton creerIconeBtn(String icone, Color couleur) {
        JButton btn = new JButton(icone);
        btn.setBackground(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 40));
        btn.setForeground(couleur);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private MouseAdapter hoverEffect(JButton btn, Color base) {
        return new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(base.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(base); }
        };
    }

    private class UserCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int idx, boolean sel, boolean focus) {
            JPanel cell = new JPanel(new BorderLayout(12, 0));
            cell.setBackground(sel ? ITEM_SEL : SIDEBAR_BG);
            cell.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_C),
                new EmptyBorder(8, 14, 8, 14)
            ));
            String nom = value.toString();
            cell.add(creerAvatar(nom, 38), BorderLayout.WEST);
            JPanel info = new JPanel(new BorderLayout(0, 3));
            info.setOpaque(false);
            JLabel nl = new JLabel(nom);
            nl.setForeground(TEXT);
            nl.setFont(new Font("SansSerif", Font.BOLD, 13));
            boolean ol = enLigne.contains(nom.toLowerCase());
            JLabel st = new JLabel(ol ? "● En ligne" : "○ Hors ligne");
            st.setForeground(ol ? GREEN_DOT : TEXT_DIM);
            st.setFont(new Font("SansSerif", Font.PLAIN, 11));
            info.add(nl, BorderLayout.NORTH);
            info.add(st, BorderLayout.SOUTH);
            cell.add(info, BorderLayout.CENTER);
            return cell;
        }
    }
}