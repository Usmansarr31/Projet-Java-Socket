package Serveur;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;


public class ServeurGUI extends JFrame {

    private static final Color BG_DARK    = new Color(18, 18, 28);
    private static final Color BG_PANEL   = new Color(28, 28, 42);
    private static final Color BG_SIDEBAR = new Color(22, 22, 35);
    private static final Color ACCENT     = new Color(99, 102, 241);
    private static final Color GREEN      = new Color(52, 211, 153);
    private static final Color RED        = new Color(239, 68, 68);
    private static final Color TEXT_MAIN  = new Color(226, 232, 240);
    private static final Color TEXT_DIM   = new Color(148, 163, 184);
    private static final Color BORDER_COL = new Color(45, 45, 65);

    private JTextArea    areaLogs;
    private JList<String> listeClients;
    private DefaultListModel<String> modelClients;
    private JLabel       labelStatut;
    private JLabel       labelNbClients;
    private JButton      btnDemarrer;
    private JButton      btnArreter;
    private Serveur      serveur;

    public ServeurGUI() {
        setTitle("Serveur Chat TCP — Port 5000");
        setSize(820, 560);
        setMinimumSize(new Dimension(700, 450));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(BG_DARK);

        construireUI();

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (serveur != null) serveur.arreter();
            }
        });
    }

    private void construireUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        setContentPane(root);

        root.add(construireHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                construirePanneauLogs(), construireSidebar());
        split.setDividerLocation(580);
        split.setDividerSize(3);
        split.setBackground(BORDER_COL);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);
    }

    private JPanel construireHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COL));
        header.setPreferredSize(new Dimension(0, 58));

        JLabel titre = new JLabel("CHAT SERVER");
        titre.setFont(new Font("SansSerif", Font.BOLD, 16));
        titre.setForeground(TEXT_MAIN);
        header.add(titre, BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        droite.setOpaque(false);

        labelStatut = new JLabel("Hors ligne");
        labelStatut.setForeground(RED);
        labelStatut.setFont(new Font("SansSerif", Font.BOLD, 12));

        btnDemarrer = creerBouton("Démarrer", GREEN);
        btnArreter  = creerBouton("Arrêter",  RED);
        btnArreter.setEnabled(false);

        btnDemarrer.addActionListener(e -> {
            serveur = new Serveur(this);
            btnDemarrer.setEnabled(false);
            new Thread(() -> serveur.demarrer()).start();
        });
        btnArreter.addActionListener(e -> {
            if (serveur != null) serveur.arreter();
        });

        droite.add(labelStatut);
        droite.add(btnDemarrer);
        droite.add(btnArreter);
        header.add(droite, BorderLayout.EAST);
        return header;
    }

    private JPanel construirePanneauLogs() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(10, 12, 10, 6));

        JLabel titre = new JLabel("Logs du serveur");
        titre.setForeground(TEXT_DIM);
        titre.setFont(new Font("SansSerif", Font.BOLD, 12));
        titre.setBorder(new EmptyBorder(0, 0, 6, 0));
        panel.add(titre, BorderLayout.NORTH);

        areaLogs = new JTextArea();
        areaLogs.setEditable(false);
        areaLogs.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaLogs.setBackground(new Color(14, 14, 22));
        areaLogs.setForeground(new Color(134, 239, 172));
        areaLogs.setLineWrap(true);
        areaLogs.setWrapStyleWord(true);
        areaLogs.setMargin(new Insets(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(areaLogs);
        scroll.setBorder(new LineBorder(BORDER_COL, 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construireSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(new MatteBorder(0, 1, 0, 0, BORDER_COL));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JPanel titrePan = new JPanel(new BorderLayout());
        titrePan.setBackground(BG_PANEL);
        titrePan.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COL),
            new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel titreLabel = new JLabel("Clients connectés");
        titreLabel.setForeground(TEXT_MAIN);
        titreLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        labelNbClients = new JLabel("0");
        labelNbClients.setForeground(ACCENT);
        labelNbClients.setFont(new Font("SansSerif", Font.BOLD, 13));
        titrePan.add(titreLabel,    BorderLayout.WEST);
        titrePan.add(labelNbClients, BorderLayout.EAST);
        sidebar.add(titrePan, BorderLayout.NORTH);

        modelClients = new DefaultListModel<>();
        listeClients = new JList<>(modelClients);
        listeClients.setBackground(BG_SIDEBAR);
        listeClients.setForeground(TEXT_MAIN);
        listeClients.setFont(new Font("SansSerif", Font.PLAIN, 13));
        listeClients.setSelectionBackground(new Color(99, 102, 241, 60));
        listeClients.setCellRenderer(new ClientCellRenderer());
        listeClients.setFixedCellHeight(38);

        JScrollPane scroll = new JScrollPane(listeClients);
        scroll.setBorder(null);
        scroll.setBackground(BG_SIDEBAR);
        sidebar.add(scroll, BorderLayout.CENTER);
        return sidebar;
    }


    public void afficherLog(String message) {
        SwingUtilities.invokeLater(() -> {
            areaLogs.append(message + "\n");
            areaLogs.setCaretPosition(areaLogs.getDocument().getLength());
        });
    }

    public void mettreAJourStatut(boolean actif) {
        SwingUtilities.invokeLater(() -> {
            if (actif) {
                labelStatut.setText("En ligne");
                labelStatut.setForeground(GREEN);
                btnDemarrer.setEnabled(false);
                btnArreter.setEnabled(true);
            } else {
                labelStatut.setText("Hors ligne");
                labelStatut.setForeground(RED);
                btnDemarrer.setEnabled(true);
                btnArreter.setEnabled(false);
            }
        });
    }

    public void mettreAJourClients(List<String> clients) {
        SwingUtilities.invokeLater(() -> {
            modelClients.clear();
            for (String c : clients) modelClients.addElement(c);
            labelNbClients.setText(String.valueOf(clients.size()));
        });
    }


    private JButton creerBouton(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setBackground(couleur);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(7, 16, 7, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private class ClientCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            cell.setPreferredSize(new Dimension(0, 38));
            cell.setBackground(isSelected ? new Color(99, 102, 241, 60) : BG_SIDEBAR);

            JLabel dot = new JLabel("⬤");
            dot.setForeground(GREEN);
            dot.setFont(new Font("SansSerif", Font.PLAIN, 9));

            JLabel nom = new JLabel(value.toString());
            nom.setForeground(TEXT_MAIN);
            nom.setFont(new Font("SansSerif", Font.PLAIN, 13));

            cell.add(dot);
            cell.add(nom);
            return cell;
        }
    }
}