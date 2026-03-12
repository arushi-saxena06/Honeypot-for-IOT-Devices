package com.iot.honeypot.ui;

import com.iot.honeypot.db.DatabaseConnection;
import com.iot.honeypot.server.HoneypotServer;
import com.iot.honeypot.service.AttackService;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class IoTHoneypotUI extends JFrame implements HoneypotServer.LogListener {
    private final AttackService attackService;
    private HoneypotServer honeypotServer;
    private JTextPane logPane;
    private JButton startButton;
    private JLabel dbStatusLabel;
    private Timer dbCheckTimer;

    // UI components for attack alert
    private final JPanel warningPanel = new JPanel();
    private final JLabel warningLabel = new JLabel("! MALICIOUS TRAFFIC !");
    private Timer alertFlickerTimer;

    // Colors
    private final Color bgColor = new Color(12, 12, 12);
    private final Color paneColor = new Color(18, 18, 18);
    private final Color neonGreen = new Color(57, 255, 20);
    private final Color neonYellow = new Color(255, 204, 0);
    private final Color neonRed = new Color(255, 60, 60);
    private final Color subtleGray = new Color(180, 180, 180);

    public IoTHoneypotUI() {
        this.attackService = new AttackService();
        setupUI();
        initDatabaseConnection();
        startDatabaseCheck();
        setupWindowListener();
    }

    private void setupUI() {
        setTitle("IoT Honeypot — Console");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Overall dark background
        getContentPane().setBackground(bgColor);

        // Top status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(8, 8, 8));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        dbStatusLabel = new JLabel("Database: Checking...");
        dbStatusLabel.setForeground(neonYellow);
        dbStatusLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));

        // Warning panel (hidden by default)
        warningPanel.setBackground(new Color(0, 0, 0, 0));
        warningPanel.setVisible(false);
        warningLabel.setForeground(neonRed);
        warningLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        warningPanel.add(warningLabel);

        statusPanel.add(dbStatusLabel, BorderLayout.WEST);
        statusPanel.add(warningPanel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.NORTH);

        // Log pane (styled)
        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setBackground(paneColor);
        logPane.setForeground(subtleGray);
        logPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        logPane.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40)));
        JScrollPane scroll = new JScrollPane(logPane);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        controlPanel.setBackground(bgColor);

        startButton = new JButton("Start Honeypot");
        startButton.setEnabled(false);
        startButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        startButton.setBackground(neonGreen);
        startButton.setForeground(Color.BLACK);
        startButton.setOpaque(true);
        startButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        startButton.addActionListener(e -> startHoneypots());

        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        clearButton.setBackground(new Color(40, 40, 40));
        clearButton.setForeground(subtleGray);
        clearButton.setOpaque(true);
        clearButton.addActionListener(e -> logPane.setText(""));

        controlPanel.add(startButton);
        controlPanel.add(clearButton);
        add(controlPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initDatabaseConnection() {
        try {
            DatabaseConnection.getConnection();
            updateDatabaseStatus(true);
            appendLog("Successfully connected to database", subtleGray, false);
            startButton.setEnabled(true);
        } catch (RuntimeException e) {
            updateDatabaseStatus(false);
            appendLog("Failed to connect to database: " + e.getMessage(), neonRed, true);
            JOptionPane.showMessageDialog(this,
                    "Database connection failed: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startDatabaseCheck() {
        dbCheckTimer = new Timer(5000, e -> {
            boolean isConnected = DatabaseConnection.isConnected();
            updateDatabaseStatus(isConnected);
            if (!isConnected) {
                startButton.setEnabled(false);
                appendLog("Database connection lost!", neonYellow, true);
            }
        });
        dbCheckTimer.start();
    }

    private void updateDatabaseStatus(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            if (connected) {
                dbStatusLabel.setText("Database: Connected");
                dbStatusLabel.setForeground(neonGreen);
            } else {
                dbStatusLabel.setText("Database: Disconnected");
                dbStatusLabel.setForeground(neonRed);
            }
        });
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (dbCheckTimer != null) {
                    dbCheckTimer.stop();
                }
                DatabaseConnection.closeConnection();
                System.exit(0);
            }
        });
    }

    private void startHoneypots() {
        if (honeypotServer != null) {
            appendLog("Honeypots already running", subtleGray, false);
            return;
        }

        honeypotServer = new HoneypotServer(8080, 2323, attackService);
        honeypotServer.addLogListener(this);
        honeypotServer.start();
        startButton.setEnabled(false);
        appendLog("Honeypots started! Listening HTTP:8080 Telnet:2323", neonYellow, true);
    }

    /**
     * Append a log line to the styled log pane with a given color and boldness.
     */
    private void appendLog(String text, Color color, boolean bold) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = logPane.getStyledDocument();
                SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setForeground(attr, color);
                StyleConstants.setFontFamily(attr, Font.MONOSPACED);
                StyleConstants.setFontSize(attr, 13);
                StyleConstants.setBold(attr, bold);
                doc.insertString(doc.getLength(), text + "\n", attr);
                logPane.setCaretPosition(doc.getLength());
            } catch (Exception ex) {
                // fallback to plain text append
                logPane.setText(logPane.getText() + text + "\n");
            }
        });
    }

    /**
     * Trigger a short flickering alert in the warning panel to signal an active attack.
     */
    private void triggerAttackAlert() {
        SwingUtilities.invokeLater(() -> {
            if (alertFlickerTimer != null && alertFlickerTimer.isRunning()) {
                return; // already alerting
            }
            warningPanel.setVisible(true);

            final int[] cycles = {0};
            final int maxCycles = 8; // total toggles

            alertFlickerTimer = new Timer(200, e -> {
                boolean on = (cycles[0] % 2 == 0);
                warningPanel.setBackground(on ? neonRed : new Color(20, 20, 20));
                warningLabel.setForeground(on ? Color.BLACK : neonYellow);
                cycles[0]++;
                if (cycles[0] > maxCycles) {
                    alertFlickerTimer.stop();
                    warningPanel.setVisible(false);
                }
            });
            alertFlickerTimer.start();
        });
    }

    @Override
    public void onNewAttack(String log) {
        // If the incoming log indicates an attack, style it specially and trigger the visual alert.
        boolean isAttack = log.toLowerCase().contains("attack") || log.toLowerCase().contains("malicious") || log.toLowerCase().contains("exploit");
        if (isAttack) {
            appendLog(log, neonRed, true);
            triggerAttackAlert();
        } else {
            appendLog(log, subtleGray, false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IoTHoneypotUI());
    }
}