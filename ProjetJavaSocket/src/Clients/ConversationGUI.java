package Clients;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ConversationGUI extends JFrame {

    private static final Color HEADER_BG  = new Color(21,  33,  61);
    private static final Color CHAT_BG    = new Color(11,  19,  38);
    private static final Color MSG_OUT    = new Color(0,   84,  166);
    private static final Color MSG_IN     = new Color(24,  37,  66);
    private static final Color INPUT_BG   = new Color(21,  33,  61);
    private static final Color ACCENT     = new Color(59,  130, 246);
    private static final Color GREEN      = new Color(34,  197, 94);
    private static final Color TEXT       = new Color(229, 236, 255);
    private static final Color TEXT_DIM   = new Color(148, 163, 200);
    private static final Color TEXT_TIME  = new Color(140, 160, 200);
    private static final Color BORDER_C   = new Color(30,  45,  80);
    private static final Color TICK_GREY  = new Color(148, 163, 200);
    private static final Color TICK_BLUE  = new Color(59,  130, 246);

    private final Client client;
    private final String moiPseudo;
    private final String interlocuteur;

    private JPanel      panneauMessages;
    private JScrollPane scrollMessages;
    private JTextField  champMessage;
    private JLabel      labelStatut;

    private final Map<String, JLabel> statutLabels = new HashMap<>();

    public interface OnMessageSentListener {
        void onMessageSent(String interlocuteur, String apercu);
    }
    private OnMessageSentListener onMessageSentListener;
    public void setOnMessageSentListener(OnMessageSentListener l) { this.onMessageSentListener = l; }

    public interface OnCloseListener { void onClose(); }
    private OnCloseListener onCloseListener;
    public void setOnCloseListener(OnCloseListener l) { this.onCloseListener = l; }

    public ConversationGUI(Client client, String moiPseudo, String interlocuteur, boolean enLigne) {
        this.client        = client;
        this.moiPseudo     = moiPseudo;
        this.interlocuteur = interlocuteur;

        construireUI(enLigne);
    }

    private void construireUI(boolean enLigne) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CHAT_BG);
        setContentPane(root);
        root.add(construireHeader(enLigne),  BorderLayout.NORTH);
        root.add(construireZoneMessages(),    BorderLayout.CENTER);
        root.add(construireZoneSaisie(),      BorderLayout.SOUTH);
    }

    private JPanel construireHeader(boolean enLigne) {
        JPanel h = new JPanel(new BorderLayout(10, 0));
        h.setBackground(HEADER_BG);
        h.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_C),
            new EmptyBorder(10, 10, 10, 16)
        ));

        JButton btnRetour = new JButton("< ");
        btnRetour.setBackground(new Color(30, 46, 80));
        btnRetour.setForeground(new Color(148, 163, 200));
        btnRetour.setFocusPainted(false);
        btnRetour.setBorderPainted(false);
        btnRetour.setOpaque(true);
        btnRetour.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnRetour.setPreferredSize(new Dimension(42, 38));
        btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRetour.setToolTipText("Retour");
        btnRetour.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnRetour.setBackground(new Color(40, 60, 100)); }
            @Override public void mouseExited(MouseEvent e)  { btnRetour.setBackground(new Color(30, 46, 80)); }
        });
        btnRetour.addActionListener(e -> { if (onCloseListener != null) onCloseListener.onClose(); });
        h.add(btnRetour, BorderLayout.WEST);

        JPanel centre = new JPanel(new BorderLayout(10, 0));
        centre.setOpaque(false);
        JLabel av = creerAvatar(interlocuteur, 40);
        centre.add(av, BorderLayout.WEST);

        JPanel infos = new JPanel(new BorderLayout(0, 2));
        infos.setOpaque(false);
        JLabel nom = new JLabel(interlocuteur);
        nom.setForeground(TEXT);
        nom.setFont(new Font("SansSerif", Font.BOLD, 15));
        labelStatut = new JLabel(enLigne ? "En ligne" : "Hors ligne");
        labelStatut.setForeground(enLigne ? GREEN : TEXT_DIM);
        labelStatut.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infos.add(nom,        BorderLayout.NORTH);
        infos.add(labelStatut, BorderLayout.SOUTH);
        centre.add(infos, BorderLayout.CENTER);
        h.add(centre, BorderLayout.CENTER);
        return h;
    }

    private JScrollPane construireZoneMessages() {
        panneauMessages = new JPanel();
        panneauMessages.setLayout(new BoxLayout(panneauMessages, BoxLayout.Y_AXIS));
        panneauMessages.setBackground(CHAT_BG);
        panneauMessages.setBorder(new EmptyBorder(14, 12, 14, 12));

        scrollMessages = new JScrollPane(panneauMessages);
        scrollMessages.setBorder(null);
        scrollMessages.setBackground(CHAT_BG);
        scrollMessages.getViewport().setBackground(CHAT_BG);
        scrollMessages.getVerticalScrollBar().setUnitIncrement(16);
        return scrollMessages;
    }

    private JPanel construireZoneSaisie() {
        JPanel zone = new JPanel(new BorderLayout(10, 0));
        zone.setBackground(HEADER_BG);
        zone.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, BORDER_C),
            new EmptyBorder(10, 14, 10, 14)
        ));

        champMessage = new JTextField();
        champMessage.setBackground(new Color(30, 46, 80));
        champMessage.setForeground(TEXT);
        champMessage.setCaretColor(TEXT);
        champMessage.setFont(new Font("SansSerif", Font.PLAIN, 14));
        champMessage.setPreferredSize(new Dimension(0, 42));
        champMessage.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));
        champMessage.addActionListener(e -> envoyerMessage());

        JButton btnEnv = new JButton("➤");
        btnEnv.setBackground(ACCENT);
        btnEnv.setForeground(Color.WHITE);
        btnEnv.setFocusPainted(false);
        btnEnv.setBorderPainted(false);
        btnEnv.setOpaque(true);
        btnEnv.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnEnv.setPreferredSize(new Dimension(50, 42));
        btnEnv.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEnv.addActionListener(e -> envoyerMessage());

        zone.add(champMessage, BorderLayout.CENTER);
        zone.add(btnEnv,       BorderLayout.EAST);
        return zone;
    }

    private void envoyerMessage() {
        String contenu = champMessage.getText().trim();
        if (contenu.isEmpty()) return;
        champMessage.setText("");
        client.envoyerMessage(interlocuteur, contenu);
        if (onMessageSentListener != null)
            onMessageSentListener.onMessageSent(interlocuteur, contenu);
    }

    public void ajouterMessageEnvoye(String heure, String msgId, String contenu) {
        SwingUtilities.invokeLater(() -> {
            JLabel statut = new JLabel("✓");
            statut.setForeground(TICK_GREY);
            statut.setFont(new Font("SansSerif", Font.PLAIN, 11));
            statutLabels.put(msgId, statut);
            panneauMessages.add(creerBulle(contenu, heure, statut, true));
            panneauMessages.add(Box.createVerticalStrut(3));
            scrollerBas();
        });
    }

    public void ajouterMessageRecu(String expediteur, String heure, String msgId, String contenu) {
        SwingUtilities.invokeLater(() -> {
            panneauMessages.add(creerBulle(contenu, heure, null, false));
            panneauMessages.add(Box.createVerticalStrut(3));
            scrollerBas();
            client.signalerLu(expediteur, msgId);
        });
    }

    public void mettreAJourStatutDelivered(String msgId) {
        SwingUtilities.invokeLater(() -> {
            JLabel l = statutLabels.get(msgId);
            if (l != null) { l.setText("✓✓"); l.setForeground(TICK_GREY); }
        });
    }

    public void mettreAJourStatutLu(String msgId) {
        SwingUtilities.invokeLater(() -> {
            JLabel l = statutLabels.get(msgId);
            if (l != null) { l.setText("✓✓"); l.setForeground(TICK_BLUE); }
        });
    }

    public void chargerHistorique(List<String[]> messages, String moiPseudo) {
        SwingUtilities.invokeLater(() -> {
            panneauMessages.removeAll();

            for (String[] m : messages) {
                boolean estMoi = m[0].equalsIgnoreCase(moiPseudo);
                String  heure  = m[2].length() >= 5 ? m[2].substring(0, 5) : m[2];
                JLabel  statut = null;
                if (estMoi) {
                    statut = new JLabel("✓");
                    statut.setForeground(TICK_BLUE);
                    statut.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    if (m.length > 3) statutLabels.put(m[3], statut);
                }
                panneauMessages.add(creerBulle(m[1], heure, statut, estMoi));
                panneauMessages.add(Box.createVerticalStrut(3));
            }
            panneauMessages.revalidate();
            panneauMessages.repaint();
            scrollerBas();
        });
    }

    public void mettreAJourStatut(boolean enLigne) {
        SwingUtilities.invokeLater(() -> {
            labelStatut.setText(enLigne ? "En ligne" : "Hors ligne");
            labelStatut.setForeground(enLigne ? GREEN : TEXT_DIM);
        });
    }

    private JPanel creerBulle(String contenu, String heure, JLabel statutLbl, boolean estMoi) {
        
        JPanel ligne = new JPanel(new FlowLayout(estMoi ? FlowLayout.RIGHT : FlowLayout.LEFT, 6, 2));
        ligne.setOpaque(false);
        ligne.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel bulle = new JPanel(new BorderLayout(0, 4));
        bulle.setBackground(estMoi ? MSG_OUT : MSG_IN);
        bulle.setMaximumSize(new Dimension(340, Integer.MAX_VALUE));

        JTextArea txt = new JTextArea(contenu);
        txt.setEditable(false);
        txt.setOpaque(false);
        txt.setForeground(TEXT);
        txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setFocusable(false);
        txt.setMargin(new Insets(8, 12, 2, 12));
        bulle.add(txt, BorderLayout.CENTER);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        bas.setOpaque(false);
        JLabel hl = new JLabel(heure);
        hl.setForeground(estMoi ? new Color(160, 200, 240) : TEXT_DIM);
        hl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        bas.add(hl);
        if (estMoi && statutLbl != null) bas.add(statutLbl);
        bulle.add(bas, BorderLayout.SOUTH);

        bulle.setBorder(new CompoundBorder(
            new LineBorder(estMoi ? MSG_OUT.darker() : BORDER_C, 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));

        ligne.add(bulle);
        return ligne;
    }

    private void scrollerBas() {
        panneauMessages.revalidate();
        panneauMessages.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar sb = scrollMessages.getVerticalScrollBar();
            sb.setValue(sb.getMaximum());
        });
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
            new Color(59,130,246), new Color(139,92,246), new Color(16,185,129),
            new Color(245,158,11), new Color(239,68,68),  new Color(6,182,212)
        };
        return c[Math.abs(nom.toLowerCase().hashCode()) % c.length];
    }
}