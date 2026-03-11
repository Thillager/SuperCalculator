import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

public class Main extends JFrame {
    private JTextField inputField, resultField;
    private JButton calcBinom, calcNormal, solveBtn, clearBtn, copyBtn, tempumrechBtn, switchThemeBtn, autoAusBtn, einheitBtn, prozentBtn, wurzelBtn, extendedBtn, langBtn;
    private JLabel ergLabel, label;
    private String letzterBinomVerlauf = "";
    private boolean isExtended = false;
    private boolean isEnglish = false;
    private Map<String, String[]> texts = new HashMap<>();
    private JPanel sideBar;
    
    public Main() {
        setTitle("Super Taschenrechner");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new BorderLayout(10, 10));

        initTexts();
        initializeUI();
        setupListeners();
    }

    // language support

    private void initTexts() {
        texts.put("task", new String[]{"Aufgabe:", "Task:"});
        texts.put("resultLabel", new String[]{"Ergebnis:", "Result:"});
        texts.put("calcNormal", new String[]{"Normales Rechnen", "Standard Calc"});
        texts.put("calcBinom", new String[]{"Binomische Formel", "Binomial Formula"});
        texts.put("solve", new String[]{"Gleichung lösen", "Solve Equation"});
        texts.put("temp", new String[]{"Temperatur", "Temperature"});
        texts.put("units", new String[]{"Einheiten", "Units"});
        texts.put("percent", new String[]{"Prozent", "Percent"});
        texts.put("copy", new String[]{"Kopieren", "Copy"});
        texts.put("clear", new String[]{"Löschen", "Clear"});
        texts.put("theme", new String[]{"Theme", "Theme"});
        texts.put("extended", new String[]{"Erweitert", "Extended"});
        texts.put("standard", new String[]{"Standard", "Basic"});
        texts.put("wait", new String[]{"Warte auf Eingabe...", "Waiting for input..."});
        texts.put("unitErr", new String[]{"Keine Einheiten erkannt!", "No units detected!"});
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

        if (resultField.getText().contains("Warte") || resultField.getText().contains("Waiting")) {
            resultField.setText(texts.get("wait")[idx]);
        }
    }

    // hilfsmethoden

    private String basisBereinigung(String s) {
        if (s == null) return "";
        return s.replace(",", ".").replaceAll("\\s+", "").toLowerCase();
    }

    private String vorbereiten(String s) {
        s = basisBereinigung(s);
        if (s.isEmpty()) return "0";
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)([+-])(\\d+\\.?\\d*)%");
        Matcher m = p.matcher(s);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;
        while (m.find()) {
            sb.append(s, lastEnd, m.start());
            sb.append(m.group(1)).append("*(").append(m.group(2).equals("+") ? "1+" : "1-").append(m.group(3)).append("/100)");
            lastEnd = m.end();
        }
        sb.append(s.substring(lastEnd));
        s = sb.toString().replace("von", "*").replace("of", "*").replace("%", "/100").replace("sqrt*(", "sqrt(");
        return s.replaceAll("(\\d)([a-zA-Z])", "$1*$2").replaceAll("(\\d)(\\()", "$1*$2")
                .replaceAll("([a-zA-Z])(\\()", "$1*$2").replaceAll("(\\))(\\d)", "$1*$2")
                .replaceAll("(\\))([a-zA-Z])", "$1*$2").replaceAll("(\\))(\\()", "$1*$2");
    }

    private static String formatZahl(double wert) {
        if (wert == (long) wert) return String.valueOf((long) wert);
        return String.format(Locale.US, "%.2f", wert);
    }

    // Methoden für die verschiedenen Rechenmodi

    private void starteAuto() {
        String text = inputField.getText().trim();
        String clean = basisBereinigung(text);
        if (text.contains("=")) starteGleichung();
        else if (clean.matches(".*\\d+[+\\-*/^].*\\d+.*") && !clean.matches(".*[°cf]")) starteNormal();
        else if (clean.matches(".*[°]?f$|.*[°]?c$")) starteTemp();
        else if (clean.matches(".*\\d+(m|cm|mm|km|g|kg|t|min|h|s).*")) starteEinheitenRechner();
        else starteNormal();
    }

    private void starteNormal() {
        try {
            Polynomial res = new Parser(vorbereiten(inputField.getText())).parse();
            resultField.setText(res.toString());
        } catch (Exception e) { resultField.setText(isEnglish ? "Syntax Error!" : "Syntax-Fehler!"); }
    }

    private void starteEinheitenRechner() {
        String text = basisBereinigung(inputField.getText());
        if (text.isEmpty()) return;

        // 1. Einheiten extrahieren und in Basiswerte (m, g, s) umrechnen
        Pattern unitPattern = Pattern.compile("([+-]?\\d+\\.?\\d*)(mm|cm|m|km|g|kg|t|min|h|s)");
        Matcher m = unitPattern.matcher(text);

        double meter = 0, gramm = 0, sek = 0;
        while (m.find()) {
            double w = Double.parseDouble(m.group(1));
            String unit = m.group(2);
            switch (unit) {
                case "km": meter += w * 1000; break;
                case "m":  meter += w; break;
                case "cm": meter += w / 100.0; break;
                case "mm": meter += w / 1000.0; break;
                case "t":  gramm += w * 1000000; break;
                case "kg": gramm += w * 1000; break;
                case "g":  gramm += w; break;
                case "h":  sek += w * 3600; break;
                case "min":sek += w * 60; break;
                case "s":  sek += w; break;
            }
        }

        // 2. Den Rest (Zahlen ohne Einheiten) isolieren
        String rest = text.replaceAll("([+-]?\\d+\\.?\\d*)(mm|cm|m|km|g|kg|t|min|h|s)", "");

        // 3. Wenn noch Zahlen ohne Einheit da sind, diese normal berechnen
        double restWert = 0;
        if (rest.matches(".*\\d+.*")) {
            try {
                // Nutze deinen vorhandenen Parser für den Restwert
                Polynomial res = new Parser(vorbereiten(rest)).parse();
                // Wir nehmen an, dass der "nackte" Wert einfach addiert werden soll (oder als Meter zählt)
                restWert = res.terms.getOrDefault("", 0.0);
            } catch (Exception e) { /* Fehler ignorieren oder behandeln */ }
        }

        // 4. Ergebnis zusammenbauen (Restwert wird hier beispielhaft zu Metern gezählt)
        StringBuilder sb = new StringBuilder();
        if (meter + restWert != 0) sb.append(formatEinheit(meter + restWert, "m")).append(" ");
        if (gramm != 0) sb.append(formatEinheit(gramm, "g")).append(" ");
        if (sek != 0)   sb.append(formatEinheit(sek, "s")).append(" ");

        resultField.setText(sb.toString().trim().isEmpty() ? "0" : sb.toString().trim());
    }
    private String formatEinheit(double w, String t) {
        double absW = Math.abs(w);
        if (t.equals("m")) {
            if (absW >= 1000) return formatZahl(w / 1000) + " km";
            if (absW < 1.0 && w != 0) return formatZahl(w * 100) + " cm";
            return formatZahl(w) + " m";
        }
        if (t.equals("g")) {
            if (absW >= 1000000) return formatZahl(w / 1000000) + " t";
            if (absW >= 1000) return formatZahl(w / 1000) + " kg";
            return formatZahl(w) + " g";
        }
        if (t.equals("s")) {
            if (absW >= 3600) return formatZahl(w / 3600) + " h";
            if (absW >= 60) return formatZahl(w / 60) + " min";
            return formatZahl(w) + " s";
        }
        return "";
    }

    private void starteBinom() {
        try {
            String input = inputField.getText().trim();
            if (input.endsWith("^2")) input = input.substring(0, input.length() - 2).trim();
            if (input.startsWith("(") && input.endsWith(")")) input = input.substring(1, input.length() - 1).trim();
            String[] parts = input.split("(?<=\\d|[a-zA-Z])(?=[+-])|(?<=[+-])(?=\\d|[a-zA-Z])");
            Polynomial polyA, polyB;
            int opIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("+") || parts[i].equals("-")) { opIndex = i; break; }
            }
            if (opIndex != -1) {
                StringBuilder sbA = new StringBuilder();
                for (int i = 0; i < opIndex; i++) sbA.append(parts[i]);
                polyA = new Parser(vorbereiten(sbA.toString())).parse();
                StringBuilder sbB = new StringBuilder();
                for (int i = opIndex; i < parts.length; i++) sbB.append(parts[i]);
                polyB = new Parser(vorbereiten(sbB.toString())).parse();
            } else {
                polyA = new Parser(vorbereiten(input)).parse();
                polyB = new Polynomial(0, "");
            }
            Polynomial ergebnis = polyA.add(polyB).mul(polyA.add(polyB));
            resultField.setText(ergebnis.toString());
        } catch (Exception e) { resultField.setText("Error: (a+b)^2"); }
    }

    private void starteGleichung() {
        try {
            String[] seiten = inputField.getText().split("=");
            Polynomial links = new Parser(vorbereiten(seiten[0])).parse();
            Polynomial rechts = new Parser(vorbereiten(seiten[1])).parse();
            String varName = "";
            for (String k : links.terms.keySet()) if (!k.isEmpty()) varName = k;
            if (varName.isEmpty()) for (String k : rechts.terms.keySet()) if (!k.isEmpty()) varName = k;
            double diffVar = links.terms.getOrDefault(varName, 0.0) - rechts.terms.getOrDefault(varName, 0.0);
            double diffConst = rechts.terms.getOrDefault("", 0.0) - links.terms.getOrDefault("", 0.0);
            if (diffVar == 0) resultField.setText(diffConst == 0 ? "Infinite" : "No solution");
            else resultField.setText(varName + " = " + formatZahl(diffConst / diffVar));
        } catch (Exception e) { resultField.setText("Error!"); }
    }

    private void starteTemp() {
        String text = basisBereinigung(inputField.getText());
        try {
            if (text.endsWith("f") || text.contains("°f")) {
                double f = Double.parseDouble(text.replaceAll("[^0-9.-]", ""));
                resultField.setText(formatZahl((f - 32) * 5 / 9) + " °C");
            } else {
                double c = Double.parseDouble(text.replaceAll("[^0-9.-]", ""));
                resultField.setText(formatZahl((c * 9 / 5) + 32) + " °F");
            }
        } catch (Exception e) { resultField.setText("Error: 32°F / 0°C"); }
    }

    private void starteProzent() {
        String text = basisBereinigung(inputField.getText());
        try {
            Pattern kaufm = Pattern.compile("(\\d+\\.?\\d*)([+-])(\\d+\\.?\\d*)%");
            Matcher m = kaufm.matcher(text);
            if (m.find()) {
                double b = Double.parseDouble(m.group(1)), p = Double.parseDouble(m.group(3));
                resultField.setText(formatZahl(m.group(2).equals("+") ? b * (1 + p / 100) : b * (1 - p / 100)));
            } else starteNormal();
        } catch (Exception e) { resultField.setText("Error!"); }
    }

    //GUI SETUP

    private void initializeUI() {
        // 1. Die Seitenleiste erstellen
        sideBar = new JPanel();
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS)); // Buttons untereinander
        sideBar.setBackground(Color.BLACK);
        sideBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sideBar.setPreferredSize(new Dimension(180, 550));

        // 2. Das Hauptfeld für Eingabe und Ergebnis
        JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        mainPanel.setBackground(Color.BLACK);

        // Labels und Textfelder
        label = new JLabel(""); 
        label.setForeground(Color.WHITE);
        inputField = new JTextField(30);
        inputField.setBackground(Color.BLACK); 
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);

        // Buttons initialisieren
        calcNormal = new JButton(); styleButton(calcNormal, new Color(52, 152, 219));
        calcBinom = new JButton(); styleButton(calcBinom, new Color(46, 204, 113));
        solveBtn = new JButton(); styleButton(solveBtn, new Color(155, 89, 182));
        tempumrechBtn = new JButton(); styleButton(tempumrechBtn, new Color(243, 156, 18));
        einheitBtn = new JButton(); styleButton(einheitBtn, new Color(150, 75, 70));
        prozentBtn = new JButton(); styleButton(prozentBtn, new Color(50, 10, 180));
        copyBtn = new JButton(); styleButton(copyBtn, new Color(149, 165, 166));
        clearBtn = new JButton(); styleButton(clearBtn, new Color(231, 76, 60));
        switchThemeBtn = new JButton(); styleButton(switchThemeBtn, new Color(52, 73, 94));
        autoAusBtn = new JButton("Auto"); styleButton(autoAusBtn, new Color(100, 100, 100));
        wurzelBtn = new JButton("√"); styleButton(wurzelBtn, new Color(13, 90, 70));
        extendedBtn = new JButton(); styleButton(extendedBtn, new Color(37, 89, 69));
        langBtn = new JButton(); styleButton(langBtn, new Color(100, 50, 150));

        // Buttons in die Seitenleiste packen
        sideBar.add(extendedBtn);
        sideBar.add(Box.createVerticalStrut(10)); // Kleiner Abstandhalter

        // Die ausklappbaren Buttons
        JButton[] extendedList = {calcBinom, solveBtn, tempumrechBtn, einheitBtn, prozentBtn, wurzelBtn, calcNormal};
        for (JButton b : extendedList) {
            b.setVisible(false);
            b.setMaximumSize(new Dimension(150, 30)); // Einheitliche Breite
            sideBar.add(b);
            sideBar.add(Box.createVerticalStrut(5));
        }

        // Standard-Buttons ins Hauptpanel
        mainPanel.add(label);
        mainPanel.add(inputField);
        mainPanel.add(autoAusBtn);
        mainPanel.add(copyBtn);
        mainPanel.add(clearBtn);
        mainPanel.add(switchThemeBtn);
        mainPanel.add(langBtn);

        // Ergebnis-Bereich
        ergLabel = new JLabel(""); 
        ergLabel.setForeground(Color.WHITE);
        resultField = new JTextField("", 40);
        resultField.setEditable(false); 
        resultField.setBorder(null); 
        resultField.setBackground(null);
        resultField.setHorizontalAlignment(JTextField.CENTER);
        resultField.setFont(new Font("Monospaced", Font.BOLD, 22));
        resultField.setForeground(Color.WHITE);

        mainPanel.add(ergLabel);
        mainPanel.add(resultField);

        // Alles zum Fenster hinzufügen
        add(sideBar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        updateLanguage();
    }

    private void styleButton(JButton b, Color c) {
        b.setBackground(c); b.setForeground(Color.WHITE); b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 12)); b.setFocusable(false);
    }

    private void setupListeners() {
        calcNormal.addActionListener(e -> starteNormal());
        inputField.addActionListener(e -> starteAuto());
        calcBinom.addActionListener(e -> starteBinom());
        solveBtn.addActionListener(e -> starteGleichung());
        clearBtn.addActionListener(e -> { inputField.setText(""); resultField.setText(texts.get("wait")[isEnglish?1:0]); });
        tempumrechBtn.addActionListener(e -> starteTemp());
        einheitBtn.addActionListener(e -> starteEinheitenRechner());
        prozentBtn.addActionListener(e -> starteProzent());
        wurzelBtn.addActionListener(e -> { inputField.setText(inputField.getText() + "sqrt("); inputField.requestFocus(); });
        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(resultField.getText()), null);
            JOptionPane.showMessageDialog(this, isEnglish ? "Copied!" : "Kopiert!");
        });
        switchThemeBtn.addActionListener(e -> themeSwitch());
        autoAusBtn.addActionListener(e -> starteAuto());
        langBtn.addActionListener(e -> { isEnglish = !isEnglish; updateLanguage(); });
        extendedBtn.addActionListener(e -> {
            isExtended = !isExtended;

            // Sichtbarkeit der Buttons umschalten
            JButton[] extendedList = {tempumrechBtn, einheitBtn, prozentBtn, wurzelBtn, calcBinom, solveBtn, calcNormal};
            for (JButton b : extendedList) b.setVisible(isExtended);

            // Border-Logik
            if (isExtended) {
                // MatteBorder für die Linie rechts, EmptyBorder für den Innenabstand
                sideBar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
            } else {
                // Nur Innenabstand, wenn eingeklappt
                sideBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            }

            updateLanguage();
            revalidate(); 
            repaint();
        }); // <-- Wichtig: Schließende Klammer für das Lambda
    }

    private void themeSwitch() {
        boolean isDark = getContentPane().getBackground() == Color.BLACK;
        Color bg = isDark ? Color.WHITE : Color.BLACK;
        Color fg = isDark ? Color.BLACK : Color.WHITE;
        getContentPane().setBackground(bg);
        inputField.setBackground(bg); inputField.setForeground(fg); inputField.setCaretColor(fg);
        resultField.setForeground(fg); ergLabel.setForeground(fg); label.setForeground(fg);
    }

    //ALGEBRA LOGIK

    static class Polynomial {
        Map<String, Double> terms = new TreeMap<>(); //"Wörterbuch" für die Variablen und ihre Werte"
        
        Polynomial(double v, String var) { if (v != 0) terms.put(var, v); } //neuer Term, wenn der wert nicht 0 ist
        
        Polynomial() {}
        void addTerm(String var, double val) { terms.put(var, terms.getOrDefault(var, 0.0) + val); }
        Polynomial add(Polynomial o) { Polynomial r = new Polynomial(); r.terms.putAll(this.terms); o.terms.forEach(r::addTerm); return r; }
    //macht plusrechnung
        
        Polynomial sub(Polynomial o) { Polynomial r = new Polynomial(); r.terms.putAll(this.terms); o.terms.forEach((k, v) -> r.addTerm(k, -v)); return r; }
    //macht minusrechnung
        
        Polynomial mul(Polynomial o) {
            Polynomial r = new Polynomial();
            for (var e1 : terms.entrySet()) for (var e2 : o.terms.entrySet()) {
                char[] c = (e1.getKey() + e2.getKey()).toCharArray(); Arrays.sort(c);
                r.addTerm(new String(c), e1.getValue() * e2.getValue());
            }
    //macht malrechnung            
            return r;
        }
        @Override
        public String toString() {
            if (terms.isEmpty()) return "0";
            StringBuilder sb = new StringBuilder();
            for (var e : terms.entrySet()) {
                double v = e.getValue(); String var = e.getKey();
                if (sb.length() > 0) sb.append(v > 0 ? " + " : " - "); else if (v < 0) sb.append("-");
                if (Math.abs(v) != 1 || var.isEmpty()) sb.append(formatZahl(Math.abs(v)));
                sb.append(var);
            }
            return sb.toString();
        }
    }

    static class Parser {
        String s; int pos = -1, ch;
        Parser(String s) { this.s = s; next(); }
        void next() { ch = (++pos < s.length()) ? s.charAt(pos) : -1; }
        boolean eat(int c) { while (ch == ' ') next(); if (ch == c) { next(); return true; } return false; }
        Polynomial parse() { return sum(); }
        Polynomial sum() {
            Polynomial x = prod();
            for (; ; ) { if (eat('+')) x = x.add(prod()); else if (eat('-')) x = x.sub(prod()); else return x; }
        }
        Polynomial prod() {
            Polynomial x = power();
            for (; ; ) {
                if (eat('*')) x = x.mul(power());
                else if (eat('/')) {
                    double d = power().terms.getOrDefault("", 1.0);
                    Polynomial r = new Polynomial();
                    x.terms.forEach((k, v) -> r.addTerm(k, v / d));
                    x = r;
                } else return x;
            }
        }
        Polynomial power() {
            Polynomial b = fact();
            if (eat('^')) {
                double exp = power().terms.getOrDefault("", 1.0);
                if (b.terms.size() == 1 && b.terms.containsKey("")) return new Polynomial(Math.pow(b.terms.get(""), exp), "");
                Polynomial r = new Polynomial(1, "");
                for (int i = 0; i < (int) exp; i++) r = r.mul(b);
                return r;
            }
            return b;
        }
        Polynomial fact() {
            if (eat('+')) return fact(); if (eat('-')) return fact().mul(new Polynomial(-1, ""));
            Polynomial x;
            if (eat('(')) { x = sum(); eat(')'); }
            else if (ch == '√' || (s.startsWith("sqrt", pos))) {
                if (ch == '√') next(); else pos += 3;
                x = (eat('(')) ? sum() : fact();
                if (eat(')')) ;
                return new Polynomial(Math.sqrt(x.terms.getOrDefault("", 0.0)), "");
            } else if (Character.isLetter(ch)) {
                StringBuilder sb = new StringBuilder();
                while (Character.isLetter(ch)) { sb.append((char) ch); next(); }
                x = new Polynomial(1, sb.toString());
            } else {
                StringBuilder sb = new StringBuilder();
                while (Character.isDigit(ch) || ch == '.') { sb.append((char) ch); next(); }
                x = new Polynomial(Double.parseDouble(sb.toString()), "");
            }
            return x;
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new Main().setVisible(true)); }
}