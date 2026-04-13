import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Main extends JFrame {
    private JTextField inputField, resultField;
    private JButton calcBinom, calcNormal, solveBtn, clearBtn, copyBtn, tempumrechBtn, switchThemeBtn, autoAusBtn,
            einheitBtn, prozentBtn, wurzelBtn, extendedBtn, langBtn, speakBtn, simulationBtn, simulationStopBtn, helpBtn, würfelBtn, baumBtn, betterEinheitenBtn;
    private JLabel ergLabel, label;
    private boolean isExtended = false;
    private boolean isEnglish = false;
    private Map<String, String[]> texts = new HashMap<>();
    private JPanel sideBar, topBar, mainPanel;

    // Simulation
    int aktuellerVersuch = 0;
    int trefferLive = 0;
    Timer simulationTimer;

    public Main() {
        setTitle("Super Taschenrechner");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);

        // Wichtig: Layout erst setzen, dann Komponenten initialisieren
        setLayout(new BorderLayout());

        initTexts();
        initComponents(); // Erstellt alle Button-Objekte
        initializeUI(); // Baut die Panels zusammen
        setupListeners();
    }

    private void initTexts() {
        texts.put("task", new String[] { "Aufgabe:", "Task:" });
        texts.put("resultLabel", new String[] { "Ergebnis:", "Result:" });
        texts.put("calcNormal", new String[] { "Normales Rechnen", "Standard Calc" });
        texts.put("calcBinom", new String[] { "Binomische Formel", "Binomial Formula" });
        texts.put("solve", new String[] { "Gleichung lösen", "Solve Equation" });
        texts.put("temp", new String[] { "Temperatur", "Temperature" });
        texts.put("units", new String[] { "Einheiten", "Units" });
        texts.put("percent", new String[] { "Prozent", "Percent" });
        texts.put("copy", new String[] { "Kopieren", "Copy" });
        texts.put("clear", new String[] { "Löschen", "Clear" });
        texts.put("theme", new String[] { "Theme", "Theme" });
        texts.put("extended", new String[] { "Erweitert", "Extended" });
        texts.put("standard", new String[] { "Standard", "Basic" });
        texts.put("wait", new String[] { "Warte auf Eingabe...", "Waiting for input..." });
        texts.put("speak", new String[] { "Vorlesen", "Speak" });
        texts.put("würfel", new String[] { "Würfel", "Dice" });

        //help
        texts.put("help", new String[]{"Hilfe", "Help"});
        texts.put("guideTitle", new String[]{"Bedienungsanleitung", "User Guide"});
        texts.put("guideContent", new String[]{
            "Willkommen beim Super Taschenrechner!\n\n" +
            "Dieser Taschenrechner ist noch in arbeit, es sind also 'devtools' eingebaut.\n" +
            "Die erweiterten Funktionen sollest du eigentlich nie nutzen, esseiden du entwickelst etwas, das Ergebnis wirkt fasch, oder du willst die Simulation nutzen.\n" +    
            "Indem du Enter drückst oder auf Auto klickst, wird automatisch die richtige Funktion genutzt.\n" +
            "Einige Buttons, die eine Erklärung brauchen:\n" +
            "- Binomische Formel: Entfaltet Ausdrücke wie (x+2)^2 zu x^2 + 4x + 4\n" +
            "- Gleichung lösen: Löst einfache Gleichungen wie 2x + 3 = 7 nach x auf\n" +
            "- Einheiten: Rechnet Einheiten um, z.B. 5km zu 5000m oder 2h zu 120min und kann mit einheiten rechnen\n" +
            "- Prozent: Berechnet prozentuale Änderungen, z.B. 100 + 10% = 110 oder 100 - 10% = 90\n" +
            "- Simulation: Simuliert eine Anzahl von Versuchen mit einer gegebenen Erfolgswahrscheinlichkeit, z.B. 30% 1000 wird 1000 Versuche mit einer 30%igen Erfolgswahrscheinlichkeit simulieren und Echtzeitergebnisse anzeigen\n" +
            "\nFalls du Fragen hast oder Vorschläge möchtest, zögere nicht, mich zu kontaktieren!",

            "Welcome to the Super Calculator!\n\n" +
            "This calculator is still in development, so 'devtools' are included.\n" + 
            "You should never need the extended functions unless you're developing something, the result looks cool, or you want to use the simulation.\n" +
            "By pressing Enter or clicking Auto, the correct function will be used automatically.\n" +
            "Some buttons that need explanation:\n" +
            "- Binomial Formula: Expands expressions like (x+2)^2 into x^2 + 4x + 4\n" +
            "- Solve Equation: Solves simple equations like 2x + 3 = 7 for x\n" +
            "- Units: Converts units like 5km to 5000m or 2h to 120min and can calculate with units\n" +
            "- Percent: Calculates percentage changes like 100 + 10% = 110 or 100 - 10% = 90\n" +
            "- Simulation: Simulates a number of trials with a given success probability, e.g. 30% 1000 will simulate 1000 trials with a 30% success chance and show live results\n" +
            "\nIf you have any questions or suggestions, feel free to contact me!"
        });
    }

    private void initComponents() {
        // Alle Buttons erstellen, bevor sie Panels hinzugefügt werden
        calcNormal = new JButton();
        styleButton(calcNormal, new Color(52, 152, 219));
        calcBinom = new JButton();
        styleButton(calcBinom, new Color(46, 204, 113));
        solveBtn = new JButton();
        styleButton(solveBtn, new Color(155, 89, 182));
        tempumrechBtn = new JButton();
        styleButton(tempumrechBtn, new Color(243, 156, 18));
        einheitBtn = new JButton();
        styleButton(einheitBtn, new Color(150, 75, 70));
        prozentBtn = new JButton();
        styleButton(prozentBtn, new Color(50, 10, 180));
        copyBtn = new JButton();
        styleButton(copyBtn, new Color(149, 165, 166));
        clearBtn = new JButton();
        styleButton(clearBtn, new Color(231, 76, 60));
        switchThemeBtn = new JButton();
        styleButton(switchThemeBtn, new Color(52, 73, 94));
        autoAusBtn = new JButton("Auto");
        styleButton(autoAusBtn, new Color(100, 100, 100));
        wurzelBtn = new JButton("√");
        styleButton(wurzelBtn, new Color(13, 90, 70));
        extendedBtn = new JButton();
        styleButton(extendedBtn, new Color(37, 89, 69));
        langBtn = new JButton();
        styleButton(langBtn, new Color(100, 50, 150));
        speakBtn = new JButton();
        styleButton(speakBtn, new Color(41, 128, 185));
        simulationBtn = new JButton("Simulation");
        styleButton(simulationBtn, new Color(22, 160, 133));
        simulationStopBtn = new JButton("Stop simulation");
        styleButton(simulationStopBtn, new Color(192, 57, 43));
        helpBtn = new JButton();
        styleButton(helpBtn, new Color(250, 0, 0));
        würfelBtn = new JButton();
        styleButton(würfelBtn, new Color(0, 98, 255));
        baumBtn = new JButton("Baumdiagramm");
        styleButton(baumBtn, new Color(100, 100, 100));
        betterEinheitenBtn = new JButton("Better Einheiten");
        styleButton(betterEinheitenBtn, new Color(100, 100, 100));

        label = new JLabel("");
        label.setForeground(Color.WHITE);
        ergLabel = new JLabel("");
        ergLabel.setForeground(Color.WHITE);

        inputField = new JTextField(30);
        inputField.setBackground(Color.BLACK);
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);

        resultField = new JTextField("", 40);
        resultField.setEditable(false);
        resultField.setBorder(null);
        resultField.setBackground(null);
        resultField.setHorizontalAlignment(JTextField.CENTER);
        resultField.setFont(new Font("Monospaced", Font.BOLD, 22));
        resultField.setForeground(Color.WHITE);
    }

    private void initializeUI() {
        // 1. SIDEBAR (Links, volle Höhe)
        sideBar = new JPanel();
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setBackground(Color.BLACK);
        sideBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sideBar.setPreferredSize(new Dimension(180, 600));

        sideBar.add(extendedBtn);
        extendedBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        sideBar.add(Box.createVerticalStrut(15));

        for (JButton b : extendedList()) {
            b.setVisible(false);
            b.setMaximumSize(new Dimension(160, 30));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            sideBar.add(b);
            sideBar.add(Box.createVerticalStrut(5));
        }

        // 2. TOPBAR (Oben rechts im Center-Bereich)
        topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
        topBar.setBackground(Color.BLACK);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topBar.add(Box.createHorizontalGlue()); // Schiebt Buttons nach rechts
        topBar.add(langBtn);
        topBar.add(Box.createHorizontalStrut(10));
        topBar.add(switchThemeBtn);
        topBar.add(Box.createHorizontalStrut(10));
        topBar.add(helpBtn);

        // 3. MAIN CONTENT (Mitte)
        mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        mainPanel.setBackground(Color.BLACK);
        mainPanel.add(label);
        mainPanel.add(inputField);
        mainPanel.add(autoAusBtn);
        mainPanel.add(copyBtn);
        mainPanel.add(clearBtn);
        mainPanel.add(ergLabel);
        mainPanel.add(resultField);

        // 4. WRAPPER (Kombiniert TopBar und MainPanel)
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(Color.BLACK);
        centerWrapper.add(topBar, BorderLayout.NORTH);
        centerWrapper.add(mainPanel, BorderLayout.CENTER);

        centerWrapper.add(mainPanel, BorderLayout.CENTER);

        // Alles dem Frame hinzufügen
        add(sideBar, BorderLayout.WEST);
        add(centerWrapper, BorderLayout.CENTER);

        updateLanguage();
    }

    private void styleButton(JButton b, Color c) {
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setFocusable(false);
    }

    private void updateLanguage() {
        int idx = isEnglish ? 1 : 0;
        label.setText(texts.get("task")[idx]);
        ergLabel.setText(texts.get("resultLabel")[idx]);
        calcNormal.setText(texts.get("calcNormal")[idx]);
        calcBinom.setText(texts.get("calcBinom")[idx]);
        solveBtn.setText(texts.get("solve")[idx]);
        tempumrechBtn.setText(texts.get("temp")[idx]);
        einheitBtn.setText(texts.get("units")[idx]);
        prozentBtn.setText(texts.get("percent")[idx]);
        copyBtn.setText(texts.get("copy")[idx]);
        clearBtn.setText(texts.get("clear")[idx]);
        switchThemeBtn.setText(texts.get("theme")[idx]);
        extendedBtn.setText(isExtended ? texts.get("standard")[idx] : texts.get("extended")[idx]);
        langBtn.setText(isEnglish ? "DE" : "EN");
        speakBtn.setText(texts.get("speak")[idx]);

        if (resultField.getText().isEmpty() || resultField.getText().contains("Warte")
                || resultField.getText().contains("Waiting")) {
            resultField.setText(texts.get("wait")[idx]);
        }
        helpBtn.setText(texts.get("help")[idx]);
        würfelBtn.setText(texts.get("würfel")[idx]);
        betterEinheitenBtn.setText("Better Einheiten");
        
    }

    private void setupListeners() {
        calcNormal.addActionListener(e -> starteNormal());
        inputField.addActionListener(e -> starteAuto());
        calcBinom.addActionListener(e -> starteBinom());
        solveBtn.addActionListener(e -> starteGleichung());
        clearBtn.addActionListener(e -> {
            inputField.setText("");
            resultField.setText(texts.get("wait")[isEnglish ? 1 : 0]);
        });
        tempumrechBtn.addActionListener(e -> starteTemp());
        einheitBtn.addActionListener(e -> starteEinheitenRechner());
        prozentBtn.addActionListener(e -> starteProzent());
        wurzelBtn.addActionListener(e -> {
            inputField.setText(inputField.getText() + "√(");
            inputField.requestFocus();
        });
        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(resultField.getText()),
                    null);
            JOptionPane.showMessageDialog(this, isEnglish ? "Copied!" : "Kopiert!");
        });
        switchThemeBtn.addActionListener(e -> themeSwitch());
        autoAusBtn.addActionListener(e -> starteAuto());
        langBtn.addActionListener(e -> {
            isEnglish = !isEnglish;
            updateLanguage();
        });
        extendedBtn.addActionListener(e -> {
            isExtended = !isExtended;
            for (JButton b : extendedList())
                b.setVisible(isExtended);
            sideBar.setBorder(isExtended ? BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)) : BorderFactory.createEmptyBorder(10, 10, 10, 10));
            updateLanguage();
            revalidate();
            repaint();
        });
        speakBtn.addActionListener(e -> vorlesen(resultField.getText()));
        baumBtn.addActionListener(e -> zeigeBaum());
        simulationBtn.addActionListener(e -> simulation());
        simulationStopBtn.addActionListener(e -> {
            if (simulationTimer != null && simulationTimer.isRunning()) {
                simulationTimer.stop();
            }
        });
        helpBtn.addActionListener(e -> help());
        würfelBtn.addActionListener(e -> würfel());
        betterEinheitenBtn.addActionListener(e -> startebetterEinheitenRechner());
    }

    private JButton[] extendedList() {
        return new JButton[] { tempumrechBtn, einheitBtn, prozentBtn, wurzelBtn, calcBinom, solveBtn, calcNormal,
                simulationBtn, simulationStopBtn, speakBtn, würfelBtn, baumBtn, betterEinheitenBtn};
    }

    // --- RECHENLOGIK & HELPER ---

    private String basisBereinigung(String s) {
        if (s == null)
            return "";
        return s.replace(",", ".").replaceAll("\\s+", "").toLowerCase();
    }

    private String vorbereiten(String s) {
        s = basisBereinigung(s);
        if (s.isEmpty())
            return "0";
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)([+-])(\\d+\\.?\\d*)%");
        Matcher m = p.matcher(s);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;
        while (m.find()) {
            sb.append(s, lastEnd, m.start());
            sb.append(m.group(1)).append("*(").append(m.group(2).equals("+") ? "1+" : "1-").append(m.group(3))
                    .append("/100)");
            lastEnd = m.end();
        }
        sb.append(s.substring(lastEnd));
        s = sb.toString().replace("von", "*").replace("of", "*").replace("%", "/100").replace("sqrt", "√");
        s = s.replaceAll("(\\d)([a-zA-Z])", "$1*$2").replaceAll("(\\d)(\\()", "$1*$2")
                .replaceAll("([a-zA-Z])(\\()", "$1*$2").replaceAll("(\\))(\\d)", "$1*$2")
                .replaceAll("(\\))([a-zA-Z])", "$1*$2").replaceAll("(\\))(\\()", "$1*$2")
                .replaceAll("(\\d)(√)", "$1*$2");
        return s;
    }

    private static String formatZahl(double wert) {
        if (wert == (long) wert)
            return String.valueOf((long) wert);
        return String.format(Locale.US, "%.2f", wert);
    }

    private void starteAuto() {
        String text = inputField.getText().trim();
        String clean = basisBereinigung(text);
        if (text.contains("="))
            starteGleichung();
        else if (clean.contains("/") && (clean.contains("%") || clean.contains("prozent")))
            starteBruchZuProzent();
        else if (clean.matches(".*\\d+(mm|cm|km|m|kg|g|t|min|h|s).*"))
            starteEinheitenRechner();
        else if (clean.matches(".*[°]?f$|.*[°]?c$"))
            starteTemp();
        else
            starteNormal();
    }

    private void starteNormal() {
        try {
            Polynomial res = new Parser(vorbereiten(inputField.getText())).parse();
            resultField.setText(res.toString());
        } catch (Exception e) {
            resultField.setText(isEnglish ? "Syntax Error!" : "Syntax-Fehler!");
        }
    }


    private void help() {
        int idx = isEnglish ? 1 : 0;
        String title = texts.get("guideTitle")[idx];
        String content = texts.get("guideContent")[idx];

        // Textfeld für den Guide erstellen
        javax.swing.JTextArea textArea = new javax.swing.JTextArea(content);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));

        // Farben an aktuelles Theme anpassen
        textArea.setBackground(getContentPane().getBackground());
        textArea.setForeground(inputField.getForeground());

        // ScrollPane hinzufügen, damit man scrollen kann, falls der Text länger wird
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 250));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Pop-up anzeigen
        JOptionPane.showMessageDialog(this, scrollPane, title,                 JOptionPane.INFORMATION_MESSAGE);
    }

    private void würfel() {
        try {
            String text = basisBereinigung(inputField.getText());
            String[] teile = text.split("d");
            int anzahl = Integer.parseInt(teile[0]);
            int seiten = Integer.parseInt(teile[1]);
            int summe = 0;
            for (int i = 0; i < anzahl; i++) {
                summe += (int) (Math.random() * seiten) + 1;
            }
            resultField.setText(String.valueOf(summe));
        }
        catch (Exception e) {resultField.setText("Error!");
                            }
    }


    private void zeigeBaum() {
        try {
            String text = inputField.getText().trim().toLowerCase();

            java.util.List<Branch[]> levels = new java.util.ArrayList<>();

            // Mehrere Stufen mit ";" getrennt
            // Beispiel:
            // kopf:50 zahl:50; kopf:50 zahl:50

            String[] stufen = text.split(";");

            for (String stufe : stufen) {
                levels.add(parseBranches(stufe));
            }

            if (levels.size() < 1) {
                resultField.setText("Mindestens 1 Stufe!");
                return;
            }

            JFrame frame = new JFrame("Baumdiagramm");
            frame.add(new BaumPanel(levels));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        } catch (Exception e) {
            resultField.setText("Error!");
        }
    }
    
    private void simulation() {
        try {
            String text = basisBereinigung(inputField.getText());
            String[] teile = text.split("%");
            double p = Double.parseDouble(teile[0]) / 100.0;
            int versuche = Integer.parseInt(teile[1]);
            aktuellerVersuch = 0;
            trefferLive = 0;

            if (simulationTimer != null)
                simulationTimer.stop();
            simulationTimer = new Timer(10, e -> {
                if (aktuellerVersuch < versuche) {
                    if (Math.random() < p)
                        trefferLive++;
                    aktuellerVersuch++;
                    resultField.setText(trefferLive + "/" + aktuellerVersuch + " von " + versuche + " ("
                            + formatZahl((double) trefferLive / aktuellerVersuch * 100) + "%)");
                } else {
                    ((Timer) e.getSource()).stop();
                }
            });
            simulationTimer.start();
        } catch (Exception e) {
            resultField.setText("Error!");
        }
    }

    private void startebetterEinheitenRechner() {
        mainPanel.removeAll();
        mainPanel.setLayout(new java.awt.BorderLayout());
        mainPanel.add(new betterEinheitenUmrechner(), BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void starteBruchZuProzent() {
        try {
            String text = basisBereinigung(inputField.getText());
            String[] teile = text.split("/");
            double zaehler = Double.parseDouble(teile[0].replaceAll("[^0-9.-]", ""));
            double nenner = Double.parseDouble(teile[1].replaceAll("[^0-9.-]", ""));
            resultField.setText(formatZahl((zaehler / nenner) * 100) + "%");
        } catch (Exception e) {
            resultField.setText("Error!");
        }
    }

    private void vorlesen(String text) {
        if (text == null || text.isEmpty() || text.contains("Warte"))
            return;
        String sprechText = text.replace("=", " ist gleich ").replace("*", " mal ").replace("/", " geteilt durch ")
                .replace("√", " Wurzel aus ");
        new Thread(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    String langTag = isEnglish ? "en" : "de";
                    String cmd = "Add-Type -AssemblyName System.Speech; $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
                            +
                            "$voice = $synth.GetInstalledVoices() | Where-Object { $_.VoiceInfo.Culture.TwoLetterISOLanguageName -eq '"
                            + langTag + "' } | Select-Object -First 1; " +
                            "if ($voice) { $synth.SelectVoice($voice.VoiceInfo.Name); } $synth.Speak('" + sprechText
                            + "')";
                    new ProcessBuilder("powershell", "-Command", cmd).start();
                } else if (os.contains("mac")) {
                    new ProcessBuilder("say", "-v", isEnglish ? "Samantha" : "Anna", sprechText).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void starteEinheitenRechner() {
        String text = basisBereinigung(inputField.getText());
        text = text.replaceAll("(\\d+)km", "($1*1000)m").replaceAll("(\\d+)cm", "($1*0.01)m")
                .replaceAll("(\\d+)mm", "($1*0.001)m").replaceAll("(\\d+)kg", "($1*1000)g")
                .replaceAll("(\\d+)t", "($1*1000000)g").replaceAll("(\\d+)min", "($1*60)s")
                .replaceAll("(\\d+)h", "($1*3600)s");
        try {
            Polynomial res = new Parser(vorbereiten(text)).parse();
            resultField.setText(res.toString());
        } catch (Exception e) {
            resultField.setText("Error!");
        }
    }

    private void starteBinom() {
        try {
            String input = inputField.getText().trim();
            if (input.endsWith("^2"))
                input = input.substring(0, input.length() - 2).trim();
            if (input.startsWith("(") && input.endsWith(")"))
                input = input.substring(1, input.length() - 1).trim();
            String[] parts = input.split("(?<=\\d|[a-zA-Z])(?=[+-])|(?<=[+-])(?=\\d|[a-zA-Z])");
            Polynomial pA, pB;
            int opIdx = -1;
            for (int i = 0; i < parts.length; i++)
                if (parts[i].equals("+") || parts[i].equals("-")) {
                    opIdx = i;
                    break;
                }
            if (opIdx != -1) {
                StringBuilder sA = new StringBuilder();
                for (int i = 0; i < opIdx; i++)
                    sA.append(parts[i]);
                pA = new Parser(vorbereiten(sA.toString())).parse();
                StringBuilder sB = new StringBuilder();
                for (int i = opIdx; i < parts.length; i++)
                    sB.append(parts[i]);
                pB = new Parser(vorbereiten(sB.toString())).parse();
            } else {
                pA = new Parser(vorbereiten(input)).parse();
                pB = new Polynomial(0, "");
            }
            resultField.setText(pA.add(pB).mul(pA.add(pB)).toString());
        } catch (Exception e) {
            resultField.setText("Error!");
        }
    }

    private void starteGleichung() {
        try {
            String[] seiten = inputField.getText().split("=");
            Polynomial links = new Parser(vorbereiten(seiten[0])).parse();
            Polynomial rechts = new Parser(vorbereiten(seiten[1])).parse();
            String var = "";
            for (String k : links.terms.keySet())
                if (!k.isEmpty())
                    var = k;
            if (var.isEmpty())
                for (String k : rechts.terms.keySet())
                    if (!k.isEmpty())
                        var = k;
            double dV = links.terms.getOrDefault(var, 0.0) - rechts.terms.getOrDefault(var, 0.0);
            double dC = rechts.terms.getOrDefault("", 0.0) - links.terms.getOrDefault("", 0.0);
            if (dV == 0)
                resultField.setText(dC == 0 ? "Infinite" : "No solution");
            else
                resultField.setText(var + " = " + formatZahl(dC / dV));
        } catch (Exception e) {
            resultField.setText("Error!");
        }
    }

    private void starteTemp() {
        try {
            String text = basisBereinigung(inputField.getText());
            double val = Double.parseDouble(text.replaceAll("[^0-9.-]", ""));
            if (text.contains("f"))
                resultField.setText(formatZahl((val - 32) * 5 / 9) + " °C");
            else
                resultField.setText(formatZahl((val * 9 / 5) + 32) + " °F");
        } catch (Exception e) {
            resultField.setText("Error!");
        }
    }

    private void starteProzent() {
        try {
            String text = basisBereinigung(inputField.getText());
            Matcher m = Pattern.compile("(\\d+\\.?\\d*)([+-])(\\d+\\.?\\d*)%").matcher(text);
            if (m.find()) {
                double b = Double.parseDouble(m.group(1)), p = Double.parseDouble(m.group(3));
                resultField.setText(formatZahl(m.group(2).equals("+") ? b * (1 + p / 100) : b * (1 - p / 100)));
            } else
                starteNormal();
        } catch (Exception e) {
            resultField.setText("Error!");
        }
    }


    private Branch[] parseBranches(String input) {
        String[] parts = input.trim().split("\\s+");
        Branch[] result = new Branch[parts.length];

        double sum = 0;

        for (int i = 0; i < parts.length; i++) {
            String[] split = parts[i].split(":");

            String name = split[0];
            double prob = Double.parseDouble(split[1].replace("%", "")) / 100.0;

            result[i] = new Branch(name, prob);
            sum += prob;
        }

        // Optional: Warnung wenn Summe ≠ 1
        if (Math.abs(sum - 1.0) > 0.001) {
            JOptionPane.showMessageDialog(this, "Wahrscheinlichkeiten ergeben nicht 100%!");
        }

        return result;
    }


    
    private void themeSwitch() {
        boolean isDark = sideBar.getBackground() == Color.BLACK;
        Color bg = isDark ? Color.WHITE : Color.BLACK;
        Color fg = isDark ? Color.BLACK : Color.WHITE;

        getContentPane().setBackground(bg);
        sideBar.setBackground(bg);
        topBar.setBackground(bg);
        inputField.setBackground(bg);
        inputField.setForeground(fg);
        inputField.setCaretColor(fg);
        resultField.setForeground(fg);
        label.setForeground(fg);
        ergLabel.setForeground(fg);

        // Panels im Frame suchen
        for (Component c : getContentPane().getComponents())
            if (c instanceof JPanel)
                c.setBackground(bg);
        mainPanel.setBackground(bg);
        sideBar.setBackground(bg);
        topBar.setBackground(bg);
        revalidate();
        repaint();
    }

    // --- POLYNOMIAL & PARSER ---

    static class Polynomial {
        Map<String, Double> terms = new TreeMap<>();

        Polynomial(double v, String var) {
            if (v != 0)
                terms.put(var, v);
        }

        Polynomial() {
        }

        void addTerm(String var, double val) {
            terms.put(var, terms.getOrDefault(var, 0.0) + val);
        }

        Polynomial add(Polynomial o) {
            Polynomial r = new Polynomial();
            r.terms.putAll(this.terms);
            o.terms.forEach(r::addTerm);
            return r;
        }

        Polynomial sub(Polynomial o) {
            Polynomial r = new Polynomial();
            r.terms.putAll(this.terms);
            o.terms.forEach((k, v) -> r.addTerm(k, -v));
            return r;
        }

        Polynomial mul(Polynomial o) {
            Polynomial r = new Polynomial();
            for (var e1 : terms.entrySet())
                for (var e2 : o.terms.entrySet()) {
                    char[] c = (e1.getKey() + e2.getKey()).toCharArray();
                    Arrays.sort(c);
                    r.addTerm(new String(c), e1.getValue() * e2.getValue());
                }
            return r;
        }

        @Override
        public String toString() {
            if (terms.isEmpty())
                return "0";
            StringBuilder sb = new StringBuilder();
            for (var e : terms.entrySet()) {
                double v = e.getValue();
                String var = e.getKey();
                if (sb.length() > 0)
                    sb.append(v > 0 ? " + " : " - ");
                else if (v < 0)
                    sb.append("-");
                double absV = Math.abs(v);
                if (absV != 1 || var.isEmpty())
                    sb.append(formatZahl(absV));
                sb.append(var);
            }
            return sb.toString();
        }
    }

    static class Parser {
        String s;
        int pos = -1, ch;

        Parser(String s) {
            this.s = s;
            next();
        }

        void next() {
            ch = (++pos < s.length()) ? s.charAt(pos) : -1;
        }

        boolean eat(int c) {
            while (ch == ' ')
                next();
            if (ch == c) {
                next();
                return true;
            }
            return false;
        }

        Polynomial parse() {
            return sum();
        }

        Polynomial sum() {
            Polynomial x = prod();
            for (;;) {
                if (eat('+'))
                    x = x.add(prod());
                else if (eat('-'))
                    x = x.sub(prod());
                else
                    return x;
            }
        }

        Polynomial prod() {
            Polynomial x = power();
            for (;;) {
                if (eat('*'))
                    x = x.mul(power());
                else if (eat('/')) {
                    double d = power().terms.getOrDefault("", 1.0);
                    Polynomial r = new Polynomial();
                    x.terms.forEach((k, v) -> r.addTerm(k, v / d));
                    x = r;
                } else
                    return x;
            }
        }

        Polynomial power() {
            Polynomial b = fact();
            if (eat('^')) {
                double exp = power().terms.getOrDefault("", 1.0);
                Polynomial r = new Polynomial(1, "");
                for (int i = 0; i < (int) exp; i++)
                    r = r.mul(b);
                return r;
            }
            return b;
        }

        Polynomial fact() {
            if (eat('+'))
                return fact();
            if (eat('-'))
                return fact().mul(new Polynomial(-1, ""));
            Polynomial x;
            if (eat('(')) {
                x = sum();
                eat(')');
            } else if (eat('√')) {
                x = eat('(') ? sum() : fact();
                eat(')'); // Falls Klammer da war
                return new Polynomial(Math.sqrt(x.terms.getOrDefault("", 0.0)), "");
            } else if (Character.isLetter(ch)) {
                StringBuilder sb = new StringBuilder();
                while (Character.isLetter(ch)) {
                    sb.append((char) ch);
                    next();
                }
                x = new Polynomial(1, sb.toString());
            } else {
                StringBuilder sb = new StringBuilder();
                while (Character.isDigit(ch) || ch == '.') {
                    sb.append((char) ch);
                    next();
                }
                x = new Polynomial(Double.parseDouble(sb.toString().isEmpty() ? "0" : sb.toString()), "");
            }
            return x;
        }
    }


    private double parseProbability(String s) {
        boolean isPercent = s.contains("%");
        s = s.replace("%", "").trim();
        double val = Double.parseDouble(s);
        return isPercent ? val / 100.0 : val;
    }


    class BaumPanel extends JPanel {

        java.util.List<Branch[]> treeLevels;

        public BaumPanel(java.util.List<Branch[]> treeLevels) {
            this.treeLevels = treeLevels;
            setPreferredSize(new Dimension(900, 500));
            setBackground(Color.WHITE);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int startX = 50;
            int startY = getHeight() / 2;

            drawTree(g, startX, startY, 0, 1.0, "");
        }

        private void drawTree(Graphics g, int x, int y, int level, double currentProb, String path) {

            g.setColor(Color.BLACK);

            if (!path.isEmpty()) {
                g.drawString(path + " (" + Main.formatZahl(currentProb * 100) + "%)", x - 60, y - 5);
            }

            if (level >= treeLevels.size()) return;

            Branch[] branches = treeLevels.get(level);

            int nextX = x + 180;

            int total = branches.length;
            int height = 200;
            int spacing = height / (total + 1);

            for (int i = 0; i < total; i++) {

                int newY = y - height / 2 + (i + 1) * spacing;

                Branch b = branches[i];

                // Linie
                g.setColor(Color.BLUE);
                g.drawLine(x, y, nextX, newY);

                // Text auf Linie
                String label = b.name + " (" + Main.formatZahl(b.probability * 100) + "%)";
                g.drawString(label, (x + nextX) / 2, (y + newY) / 2);

                // Rekursion
                drawTree(
                    g,
                    nextX,
                    newY,
                    level + 1,
                    currentProb * b.probability,
                    path.isEmpty() ? b.name : path + " → " + b.name
                );
            }
        }
    }

    class betterEinheitenUmrechner extends javax.swing.JPanel {

        // Einheiten-Tabellen
        private java.util.Map<String, Double> längen = new java.util.HashMap<>();
        private java.util.Map<String, Double> gewichte = new java.util.HashMap<>();

        // UI Komponenten
        private javax.swing.JComboBox<String> kategorieBox;
        private javax.swing.JComboBox<String> boxVon = new javax.swing.JComboBox<>();
        private javax.swing.JComboBox<String> boxZu = new javax.swing.JComboBox<>();
        private javax.swing.JTextField eingabeFeld = new javax.swing.JTextField();
        private javax.swing.JLabel ergebnisLabel =
                new javax.swing.JLabel("Ergebnis: ---", javax.swing.SwingConstants.CENTER);
        private javax.swing.JButton berechnenButton =
                new javax.swing.JButton("Umrechnen");

        public betterEinheitenUmrechner() {

            // -------------------------
            // 1. Einheiten definieren
            // -------------------------
            längen.put("Millimeter", 0.001);
            längen.put("Zentimeter", 0.01);
            längen.put("Dezimeter", 0.1);
            längen.put("Meter", 1.0);
            längen.put("Kilometer", 1000.0);
            längen.put("Zoll (Inch)", 0.0254);
            längen.put("Fuss (Foot)", 0.3048);
            längen.put("Meile", 1609.34);

            gewichte.put("Milligramm", 0.001);
            gewichte.put("Gramm", 1.0);
            gewichte.put("Kilogramm", 1000.0);
            gewichte.put("Tonne", 1000000.0);
            gewichte.put("Pfund (lbs)", 453.592);

            // -------------------------
            // 2. Kategorie Auswahl
            // -------------------------
            kategorieBox = new javax.swing.JComboBox<>(new String[]{"Länge", "Gewicht"});

            kategorieBox.addActionListener(e -> {
                boxVon.removeAllItems();
                boxZu.removeAllItems();

                if (kategorieBox.getSelectedItem().equals("Länge")) {
                    for (String s : längen.keySet()) {
                        boxVon.addItem(s);
                        boxZu.addItem(s);
                    }
                } else {
                    for (String s : gewichte.keySet()) {
                        boxVon.addItem(s);
                        boxZu.addItem(s);
                    }
                }
            });

            // -------------------------
            // 3. Layout
            // -------------------------
            this.setLayout(new java.awt.GridLayout(10, 1, 5, 5));
            this.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

            this.add(new javax.swing.JLabel("1. Kategorie wählen:", javax.swing.SwingConstants.CENTER));
            this.add(kategorieBox);

            this.add(new javax.swing.JLabel("2. Wert eingeben:", javax.swing.SwingConstants.CENTER));
            this.add(eingabeFeld);

            this.add(new javax.swing.JLabel("3. Von:", javax.swing.SwingConstants.CENTER));
            this.add(boxVon);

            this.add(new javax.swing.JLabel("4. Nach:", javax.swing.SwingConstants.CENTER));
            this.add(boxZu);

            berechnenButton.setBackground(new java.awt.Color(70, 130, 180));
            berechnenButton.setForeground(java.awt.Color.WHITE);
            berechnenButton.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));

            this.add(berechnenButton);

            ergebnisLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
            this.add(ergebnisLabel);

            // Button-Action
            berechnenButton.addActionListener(e -> umrechnen());

            // Initiale Befüllung
            for (String s : längen.keySet()) {
                boxVon.addItem(s);
                boxZu.addItem(s);
            }
        }

        // -------------------------
        // 4. Umrechnungslogik
        // -------------------------
        private void umrechnen() {
            try {
                String gewählteKat = (String) kategorieBox.getSelectedItem();

                java.util.Map<String, Double> aktiveMap =
                        gewählteKat.equals("Länge") ? längen : gewichte;

                double wert = Double.parseDouble(eingabeFeld.getText().replace(",", "."));

                double vonFaktor = aktiveMap.get(boxVon.getSelectedItem());
                double zuFaktor = aktiveMap.get(boxZu.getSelectedItem());

                double ergebnis = (wert * vonFaktor) / zuFaktor;

                ergebnisLabel.setText(String.format("Ergebnis: %.4f", ergebnis));
                ergebnisLabel.setForeground(java.awt.Color.BLACK);

            } catch (Exception ex) {
                ergebnisLabel.setText("Fehler: Zahl prüfen!");
                ergebnisLabel.setForeground(java.awt.Color.RED);
            }
        }
    }

    class Branch {
        String name;
        double probability;

        Branch(String name, double probability) {
            this.name = name;
            this.probability = probability;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}


