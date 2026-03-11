import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

public class Main extends JFrame {
    private JTextField inputField, resultField;
    private JButton calcBinom, calcNormal, solveBtn, clearBtn, copyBtn, tempumrechBtn, switchThemeBtn, autoAusBtn, einheitBtn, prozentBtn, wurzelBtn, extendedBtn, langBtn, speakBtn;
    private JLabel ergLabel, label;
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

    private String basisBereinigung(String s) {
        if (s == null) return "";
        return s.replace(",", ".").replaceAll("\\s+", "").toLowerCase();
    }

    private String vorbereiten(String s) {
        s = basisBereinigung(s);
        if (s.isEmpty()) return "0";

        // Prozentrechnung
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
        s = sb.toString().replace("von", "*").replace("of", "*").replace("%", "/100");

        // 1. Alle "sqrt" in "√" umwandeln, das macht den Parser VIEL einfacher
        s = s.replace("sqrt", "√");

        // 2. Automatische Multiplikation (Buchstaben und Klammern, aber KEINE Malzeichen in die Wurzel fummeln!)
        s = s.replaceAll("(\\d)([a-zA-Z])", "$1*$2")
             .replaceAll("(\\d)(\\()", "$1*$2")
             .replaceAll("([a-zA-Z])(\\()", "$1*$2")
             .replaceAll("(\\))(\\d)", "$1*$2")
             .replaceAll("(\\))([a-zA-Z])", "$1*$2")
             .replaceAll("(\\))(\\()", "$1*$2");

        // 3. Fall: Jemand tippt "2√9". Daraus muss "2*√9" werden!
        s = s.replaceAll("(\\d)(√)", "$1*$2");

        return s;
    }

    private static String formatZahl(double wert) {
        if (wert == (long) wert) return String.valueOf((long) wert);
        return String.format(Locale.US, "%.2f", wert);
    }

    private void starteAuto() {
        String text = inputField.getText().trim();
        String clean = basisBereinigung(text);
        if (text.contains("=")) starteGleichung();
        else if (clean.contains("/") && (clean.contains("%") || clean.contains("prozent")))
        starteBruchZuProzent();
        else if (clean.matches(".*\\d+(mm|cm|km|m|kg|g|t|min|h|s).*")) starteEinheitenRechner();
        else if (clean.matches(".*[°]?f$|.*[°]?c$")) starteTemp();
        else starteNormal();
    }

    private void starteNormal() {
        try {
            Polynomial res = new Parser(vorbereiten(inputField.getText())).parse();
            resultField.setText(res.toString());
        } catch (Exception e) { resultField.setText(isEnglish ? "Syntax Error!" : "Syntax-Fehler!"); }
    }

    private void starteBruchZuProzent() {
        try {
        String text = basisBereinigung(inputField.getText());

        String[] teile = text.split("/");
        double zaehler = Double.parseDouble(teile[0].replaceAll("[^0-9.-]", ""));
        double nenner = Double.parseDouble(teile[1].replaceAll("[^0-9.-]", ""));

        double prozent = (zaehler / nenner) * 100;
        resultField.setText(formatZahl(prozent) + "%");
    } 
    catch (Exception e) { resultField.setText("Error!"); 

        }
    }

    private void vorlesen(String text) {
    if (text == null || text.isEmpty() || text.contains("Warte") || text.contains("Waiting")) return;

    String sprechText = text.replace("=", " ist gleich ")
                            .replace("*", " mal ")
                            .replace("/", " geteilt durch ")
                            .replace("√", " Wurzel aus ");

    new Thread(() -> {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // WINDOWS LOGIK
                // Wir suchen nach einer Stimme, die zur gewählten Sprache passt
                String langTag = isEnglish ? "en" : "de";
                
                String shellCommand = "Add-Type -AssemblyName System.Speech; " +
                        "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        // Suche eine Stimme, die das Sprachkürzel (de oder en) im Namen oder Attribut hat
                        "$voice = $synth.GetInstalledVoices() | Where-Object { $_.VoiceInfo.Culture.TwoLetterISOLanguageName -eq '" + langTag + "' } | Select-Object -First 1; " +
                        "if ($voice) { $synth.SelectVoice($voice.VoiceInfo.Name); } " +
                        "$synth.Speak('" + sprechText + "')";
                
                new ProcessBuilder("powershell", "-Command", shellCommand).start();
                
            } else if (os.contains("mac")) {
                // MAC LOGIK
                // Anna ist die deutsche Standardstimme, Samantha die englische
                String voice = isEnglish ? "Samantha" : "Anna";
                new ProcessBuilder("say", "-v", voice, sprechText).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
}

    private void starteEinheitenRechner() {
        String text = basisBereinigung(inputField.getText());
        text = text.replaceAll("(\\d+\\.?\\d*)km", "($1*1000)m")
                   .replaceAll("(\\d+\\.?\\d*)cm", "($1*0.01)m")
                   .replaceAll("(\\d+\\.?\\d*)mm", "($1*0.001)m")
                   .replaceAll("(\\d+\\.?\\d*)kg", "($1*1000)g")
                   .replaceAll("(\\d+\\.?\\d*)t", "($1*1000000)g")
                   .replaceAll("(\\d+\\.?\\d*)min", "($1*60)s")
                   .replaceAll("(\\d+\\.?\\d*)h", "($1*3600)s");
        try {
            Polynomial res = new Parser(vorbereiten(text)).parse();
            resultField.setText(res.toString());
        } catch (Exception e) { resultField.setText("Error!"); }
    }

    private void starteBinom() {
        try {
            String input = inputField.getText().trim();
            if (input.endsWith("^2")) input = input.substring(0, input.length() - 2).trim();
            if (input.startsWith("(") && input.endsWith(")")) input = input.substring(1, input.length() - 1).trim();
            String[] parts = input.split("(?<=\\d|[a-zA-Z])(?=[+-])|(?<=[+-])(?=\\d|[a-zA-Z])");
            Polynomial polyA, polyB;
            int opIndex = -1;
            for (int i = 0; i < parts.length; i++) if (parts[i].equals("+") || parts[i].equals("-")) { opIndex = i; break; }
            if (opIndex != -1) {
                StringBuilder sbA = new StringBuilder(); for (int i = 0; i < opIndex; i++) sbA.append(parts[i]);
                polyA = new Parser(vorbereiten(sbA.toString())).parse();
                StringBuilder sbB = new StringBuilder(); for (int i = opIndex; i < parts.length; i++) sbB.append(parts[i]);
                polyB = new Parser(vorbereiten(sbB.toString())).parse();
            } else { polyA = new Parser(vorbereiten(input)).parse(); polyB = new Polynomial(0, ""); }
            Polynomial ergebnis = polyA.add(polyB).mul(polyA.add(polyB));
            resultField.setText(ergebnis.toString());
        } catch (Exception e) { resultField.setText("Error!"); }
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
        } catch (Exception e) { resultField.setText("Error!"); }
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

    private void initializeUI() {
        sideBar = new JPanel(); sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setBackground(Color.BLACK); sideBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sideBar.setPreferredSize(new Dimension(180, 550));

        JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        mainPanel.setBackground(Color.BLACK);

        label = new JLabel(""); label.setForeground(Color.WHITE);
        inputField = new JTextField(30); inputField.setBackground(Color.BLACK); 
        inputField.setForeground(Color.WHITE); inputField.setCaretColor(Color.WHITE);

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
        speakBtn = new JButton("🔊"); styleButton(speakBtn, new Color(41, 128, 185));

        sideBar.add(extendedBtn); sideBar.add(Box.createVerticalStrut(10));
        JButton[] extendedList = {tempumrechBtn, einheitBtn, prozentBtn, wurzelBtn, calcBinom, solveBtn, calcNormal};
        for (JButton b : extendedList) { b.setVisible(false); b.setMaximumSize(new Dimension(150, 30)); sideBar.add(b); sideBar.add(Box.createVerticalStrut(5)); }

        mainPanel.add(label); mainPanel.add(inputField); mainPanel.add(autoAusBtn);
        mainPanel.add(copyBtn); mainPanel.add(clearBtn); mainPanel.add(switchThemeBtn); mainPanel.add(langBtn); mainPanel.add(speakBtn);

        ergLabel = new JLabel(""); ergLabel.setForeground(Color.WHITE);
        resultField = new JTextField("", 40); resultField.setEditable(false); 
        resultField.setBorder(null); resultField.setBackground(null);
        resultField.setHorizontalAlignment(JTextField.CENTER);
        resultField.setFont(new Font("Monospaced", Font.BOLD, 22)); resultField.setForeground(Color.WHITE);

        mainPanel.add(ergLabel); mainPanel.add(resultField);
        add(sideBar, BorderLayout.WEST); add(mainPanel, BorderLayout.CENTER);
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
        wurzelBtn.addActionListener(e -> { inputField.setText(inputField.getText() + "√("); inputField.requestFocus(); });
        copyBtn.addActionListener(e -> { Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(resultField.getText()), null); JOptionPane.showMessageDialog(this, isEnglish ? "Copied!" : "Kopiert!"); });
        switchThemeBtn.addActionListener(e -> themeSwitch());
        autoAusBtn.addActionListener(e -> starteAuto());
        langBtn.addActionListener(e -> { isEnglish = !isEnglish; updateLanguage(); });
        extendedBtn.addActionListener(e -> {
            isExtended = !isExtended;
            for (JButton b : extendedList()) b.setVisible(isExtended);
            sideBar.setBorder(isExtended ? BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY), BorderFactory.createEmptyBorder(10, 10, 10, 10)) : BorderFactory.createEmptyBorder(10, 10, 10, 10));
            updateLanguage(); revalidate(); repaint();
        });
        speakBtn.addActionListener(e -> vorlesen(resultField.getText()));
    }
    
    private JButton[] extendedList() { return new JButton[]{tempumrechBtn, einheitBtn, prozentBtn, wurzelBtn, calcBinom, solveBtn, calcNormal}; }

   private void themeSwitch() {
    // 1. Prüfen, ob Dark Mode aktiv
    boolean isDark = sideBar.getBackground() == Color.BLACK;
    
    // 2. Farben festlegen
    Color bg = isDark ? Color.WHITE : Color.BLACK;
    Color fg = isDark ? Color.BLACK : Color.WHITE;
    Color borderCol = isDark ? Color.LIGHT_GRAY : Color.GRAY;

    // 3. Den Hintergrund der Haupt-Container ändern
    getContentPane().setBackground(bg);
    sideBar.setBackground(bg);
    
    for (java.awt.Component comp : getContentPane().getComponents()) {
        if (comp instanceof JPanel) {
            comp.setBackground(bg);
        }
    }

    // 4. Textfarben und Felder anpassen
    inputField.setBackground(bg);
    inputField.setForeground(fg);
    inputField.setCaretColor(fg);
    resultField.setForeground(fg);
    ergLabel.setForeground(fg);
    label.setForeground(fg);

    // 5. Sidebar-Border anpassen
    if (isExtended) {
        sideBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, borderCol), 
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    }
}

    static class Polynomial {
        Map<String, Double> terms = new TreeMap<>();
        Polynomial(double v, String var) { if (v != 0) terms.put(var, v); }
        Polynomial() {}
        void addTerm(String var, double val) { terms.put(var, terms.getOrDefault(var, 0.0) + val); }
        
        Polynomial add(Polynomial o) { Polynomial r = new Polynomial(); r.terms.putAll(this.terms); o.terms.forEach(r::addTerm); return r; }
        Polynomial sub(Polynomial o) { Polynomial r = new Polynomial(); r.terms.putAll(this.terms); o.terms.forEach((k, v) -> r.addTerm(k, -v)); return r; }
        
        Polynomial mul(Polynomial o) {
            Polynomial r = new Polynomial();
            for (var e1 : terms.entrySet()) for (var e2 : o.terms.entrySet()) {
                // Buchstaben kombinieren und sortieren (z.B. "m" + "m" = "mm")
                char[] c = (e1.getKey() + e2.getKey()).toCharArray(); 
                Arrays.sort(c);
                r.addTerm(new String(c), e1.getValue() * e2.getValue());
            } return r;
        }

        // Hilfsmethode: Macht aus "mm" -> "m²", aus "mmm" -> "m³" etc.
        private String formatExponenten(String var) {
            if (var.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            Map<Character, Integer> counts = new TreeMap<>();
            
            // Zähle wie oft jeder Buchstabe vorkommt
            for (char c : var.toCharArray()) {
                counts.put(c, counts.getOrDefault(c, 0) + 1);
            }

            for (var entry : counts.entrySet()) {
                sb.append(entry.getKey());
                int exp = entry.getValue();
                if (exp == 2) sb.append("²");
                else if (exp == 3) sb.append("³");
                else if (exp > 3) sb.append("^").append(exp); // Für sehr hohe Potenzen
            }
            return sb.toString();
        }

        @Override public String toString() {
            if (terms.isEmpty()) return "0";
            StringBuilder sb = new StringBuilder();
            for (var e : terms.entrySet()) {
                double v = e.getValue();
                String varRaw = e.getKey();
                String varPretty = formatExponenten(varRaw); // Hier wird m² erzeugt

                if (sb.length() > 0) sb.append(v > 0 ? " + " : " - "); 
                else if (v < 0) sb.append("-");
                
                double absV = Math.abs(v);
                // Zahl nur anzeigen, wenn sie nicht 1 ist oder keine Variable da ist
                if (absV != 1 || varPretty.isEmpty()) sb.append(formatZahl(absV));
                sb.append(varPretty);
            } return sb.toString();
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
                    Polynomial r = new Polynomial(); x.terms.forEach((k, v) -> r.addTerm(k, v / d)); x = r;
                } else return x;
            }
        }
        Polynomial power() {
            Polynomial b = fact();
            if (eat('^')) {
                double exp = power().terms.getOrDefault("", 1.0);
                if (b.terms.size() == 1 && b.terms.containsKey("")) return new Polynomial(Math.pow(b.terms.get(""), exp), "");
                Polynomial r = new Polynomial(1, ""); for (int i = 0; i < (int) exp; i++) r = r.mul(b); return r;
            } return b;
        }
        Polynomial fact() {
            if (eat('+')) return fact(); 
            if (eat('-')) return fact().mul(new Polynomial(-1, ""));
            
            Polynomial x;
            if (eat('(')) { 
                x = sum(); 
                eat(')'); 
            } 
            else if (eat('√')) { // <- Viel einfacher, da "sqrt" vorher in "√" umgewandelt wurde!
                if (eat('(')) {
                    x = sum();
                    eat(')');
                } else {
                    x = fact();
                }
                return new Polynomial(Math.sqrt(x.terms.getOrDefault("", 0.0)), "");
            } 
            else if (Character.isLetter(ch)) {
                StringBuilder sb = new StringBuilder();
                while (Character.isLetter(ch)) { sb.append((char) ch); next(); }
                x = new Polynomial(1, sb.toString());
            } else {
                StringBuilder sb = new StringBuilder();
                while (Character.isDigit(ch) || ch == '.') { sb.append((char) ch); next(); }
                if (sb.length() == 0) return new Polynomial(0, ""); // Verhindert einen Absturz bei Tippfehlern
                x = new Polynomial(Double.parseDouble(sb.toString()), "");
            }
            return x;
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new Main().setVisible(true)); }
}