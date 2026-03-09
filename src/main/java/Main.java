
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Main extends JFrame {
    private JTextField inputField;
    private JButton calcBinom, calcNormal, solveBtn, clearBtn, copyBtn, tempumrechBtn, switchThemeBtn, autoAusBtn, einheitBtn, prozentBtn, wurzelBtn;
    private JTextField resultField;
    private JLabel ergLabel, label;
    private String letzterBinomVerlauf = "";

    public Main() {

        setTitle("Super Taschenrechner");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

        // UI-Design
        label = new JLabel("Aufgabe:");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        add(label);
        label.setForeground(Color.WHITE);
        label.setBackground(Color.BLACK);

        inputField = new JTextField(40);
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        add(inputField);
        inputField.setBackground(Color.BLACK);
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setFocusTraversalKeysEnabled(false);

        // Buttons erstellen und stylen
        calcBinom = new JButton("Binomische Formel");
        styleButton(calcBinom, new Color(46, 204, 113));

        calcNormal = new JButton("Normales Rechnen");
        styleButton(calcNormal, new Color(52, 152, 219));

        solveBtn = new JButton("Gleichung lösen");
        styleButton(solveBtn, new Color(155, 89, 182));

        clearBtn = new JButton("Löschen");
        styleButton(clearBtn, new Color(231, 76, 60));

        copyBtn = new JButton("Kopieren");
        styleButton(copyBtn, new Color(149, 165, 166));

        tempumrechBtn = new JButton("Temperaturumrechnung");
        styleButton(tempumrechBtn, new Color(243, 156, 18));

        switchThemeBtn = new JButton("Theme wechseln");
        styleButton(switchThemeBtn, new Color(52, 73, 94));

        autoAusBtn = new JButton("Automatisch auswählen (kann Fehler machen!)");
        styleButton(autoAusBtn, new Color(100,100,100));

        einheitBtn = new JButton("Mit Einheiten rechnen");
        styleButton(einheitBtn, new Color(150, 75, 70));

        prozentBtn = new JButton("Mit Prozenten rechnen");
        styleButton(prozentBtn, new Color(50, 10, 180));

        wurzelBtn = new JButton("Starte mit Wurzeleingabe");
        styleButton(wurzelBtn, new Color(13, 90, 70));

        // Buttons hinzufügen
        add(calcNormal); 
        add(calcBinom); 
        add(solveBtn);
        add(tempumrechBtn); 
        add(einheitBtn); 
        add(prozentBtn);
        add(copyBtn); 
        add(clearBtn); 
        add(switchThemeBtn);
        add(autoAusBtn);
        add(wurzelBtn);

        // Ergebnis-Bereich
        ergLabel = new JLabel("Ergebnis:");
        ergLabel.setForeground(Color.WHITE);
        add(ergLabel);

        resultField = new JTextField("Warte auf Eingabe...", 55);
        resultField.setEditable(false);
        resultField.setBorder(null);
        resultField.setBackground(null);
        resultField.setHorizontalAlignment(JTextField.CENTER);
        resultField.setFont(new Font("Monospaced", Font.BOLD, 22));
        resultField.setForeground(Color.WHITE);
        add(resultField);

        // --- Action Listener ---
        calcNormal.addActionListener(e -> starteNormal());
        inputField.addActionListener(e -> starteAuto());
        calcBinom.addActionListener(e -> starteBinom());
        solveBtn.addActionListener(e -> starteGleichung());
        clearBtn.addActionListener(e -> { inputField.setText(""); resultField.setText(""); });
        copyBtn.addActionListener(e -> {
            String textZumKopieren = resultField.getText();
            if (!letzterBinomVerlauf.isEmpty() && letzterBinomVerlauf.contains("Ergebnis: " + textZumKopieren)) {
                textZumKopieren = letzterBinomVerlauf;
            }
            StringSelection sel = new StringSelection(textZumKopieren);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            JOptionPane.showMessageDialog(this, "Kopiert:\n" + textZumKopieren);
        });
        tempumrechBtn.addActionListener(e -> starteTemp());
        switchThemeBtn.addActionListener(e -> themeSwitch());
        autoAusBtn.addActionListener(e -> starteAuto());
        einheitBtn.addActionListener(e -> starteEinheitenRechner());
        prozentBtn.addActionListener(e -> starteProzent());
        wurzelBtn.addActionListener(e -> starteWurzel()
);
    }


private void starteAuto() {
        String text = inputField.getText().trim();

        if(text.endsWith("°F")) {
            starteTemp();
        }
        else if(text.endsWith("°C")) {
            starteTemp();
        }
        else if(text.contains("=")) {
            starteGleichung();
        }
        else if(text.matches(".*\\)\\s*\\^\\s*2$")) {
            starteBinom();
        }
        else if(text.matches(".*\\d+(m|cm|mm|km).*")) {
            starteEinheitenRechner();
        }
        else if(text.contains("%")) {
            starteProzent();
        }
        else {
            starteNormal();
        }
    }

private void starteWurzel() {
            inputField.setText(inputField.getText() + "√(");
            inputField.requestFocus();
}


private void starteProzent() {
    String text = inputField.getText().replace(",", ".").replaceAll("\\s+", "");
    
    // Pattern für "Zahl +/- Prozent%" (Kaufmännisch)
    Pattern kaufmaennisch = Pattern.compile("(\\d+\\.?\\d*)([+-])(\\d+\\.?\\d*)%");
    Matcher m1 = kaufmaennisch.matcher(text);
    
    // Pattern für "X% von Y" oder "X% * Y"
    Pattern vonLogik = Pattern.compile("(\\d+\\.?\\d*)%(?:von|of|\\*)(\\d+\\.?\\d*)");
    Matcher m2 = vonLogik.matcher(text);

    try {
        if (m1.find()) {
            double basis = Double.parseDouble(m1.group(1));
            String op = m1.group(2);
            double prozent = Double.parseDouble(m1.group(3));
            
            double ergebnis = op.equals("+") ? basis * (1 + prozent/100) : basis * (1 - prozent/100);
            resultField.setText(String.format(Locale.US, "%.2f", ergebnis));
            
        } else if (m2.find()) {
            double prozent = Double.parseDouble(m2.group(1));
            double wert = Double.parseDouble(m2.group(2));
            
            double ergebnis = (prozent / 100) * wert;
            resultField.setText(String.format(Locale.US, "%.2f", ergebnis));
            
        } else if (text.contains("%")) {
            // Einfache Umrechnung: "10%" -> 0.1
            String zahlRaw = text.replace("%", "");
            double zahl = Double.parseDouble(zahlRaw) / 100;
            resultField.setText(String.format(Locale.US, "%.2f", zahl));
        }
    } catch (Exception e) {
        resultField.setText("Prozent-Fehler!");
    }
}

    private void starteEinheitenRechner() {
    String text = inputField.getText().replace(",", ".").toLowerCase().replaceAll("\\s+", "");
    
    // Erweitertes Pattern: Erkennt nun m, cm, mm, km UND g, kg, t UND s, min, h
    Pattern pattern = Pattern.compile("([+-]?\\d+\\.?\\d*)(mm|cm|m|km|g|kg|t|min|h|s)");
    Matcher matcher = pattern.matcher(text);

    double m = 0, g = 0, s = 0; // Basiseinheiten: Meter, Gramm, Sekunden
    boolean gefunden = false;

    while (matcher.find()) {
        try {
            double wert = Double.parseDouble(matcher.group(1));
            String einheit = matcher.group(2);
            gefunden = true;

            switch (einheit) {
                // Längen (Basis: Meter)
                case "km": m += wert * 1000; break;
                case "m":  m += wert; break;
                case "cm": m += wert / 100.0; break;
                case "mm": m += wert / 1000.0; break;
                
                // Gewichte (Basis: Gramm)
                case "t":  g += wert * 1000000; break;
                case "kg": g += wert * 1000; break;
                case "g":  g += wert; break;
                
                // Zeit (Basis: Sekunden)
                case "h":   s += wert * 3600; break;
                case "min": s += wert * 60; break;
                case "s":   s += wert; break;
            }
        } catch (NumberFormatException e) { }
    }

    if (gefunden) {
        StringBuilder erg = new StringBuilder();
        if (m != 0) erg.append(formatEinheit(m, "m")).append(" ");
        if (g != 0) erg.append(formatEinheit(g, "g")).append(" ");
        if (s != 0) erg.append(formatEinheit(s, "s")).append(" ");
        
        resultField.setText(erg.toString().trim());
    } else {
        resultField.setText("Keine Einheiten (m, g, s etc.) gefunden!");
    }
    }

private String formatEinheit(double wert, String typ) {
    if (typ.equals("m")) {
        if (Math.abs(wert) >= 1000) return String.format(Locale.US, "%.2f km", wert / 1000);
        if (Math.abs(wert) < 1.0 && wert != 0) return String.format(Locale.US, "%.2f cm", wert * 100);
        return String.format(Locale.US, "%.2f m", wert);
    } 
    if (typ.equals("g")) {
        if (Math.abs(wert) >= 1000000) return String.format(Locale.US, "%.2f t", wert / 1000000);
        if (Math.abs(wert) >= 1000) return String.format(Locale.US, "%.2f kg", wert / 1000);
        return String.format(Locale.US, "%.2f g", wert);
    }
    if (typ.equals("s")) {
        if (Math.abs(wert) >= 3600) return String.format(Locale.US, "%.2f h", wert / 3600);
        if (Math.abs(wert) >= 60) return String.format(Locale.US, "%.2f min", wert / 60);
        return String.format(Locale.US, "%.2f s", wert);
    }
    return "";
}

    private void styleButton(JButton b, Color c) {
    b.setBackground(c);
    if (getContentPane().getBackground() == Color.BLACK) {
        b.setForeground(Color.WHITE);
    } else {
        b.setForeground(Color.BLACK);
    }
    b.setFocusPainted(false); 
    b.setFont(new Font("Arial", Font.BOLD, 12));
    b.setFocusable(false);
}

    private void updateButtonTextColors(Color color) {
        calcBinom.setForeground(color);
        calcNormal.setForeground(color);
        solveBtn.setForeground(color);
        clearBtn.setForeground(color);
        copyBtn.setForeground(color);
        tempumrechBtn.setForeground(color);
        switchThemeBtn.setForeground(color);
        autoAusBtn.setForeground(color);
        einheitBtn.setForeground(color);
        prozentBtn.setForeground(color);
    }

    private void themeSwitch() {
        if (getContentPane().getBackground() == Color.BLACK) {
            getContentPane().setBackground(Color.WHITE);
            inputField.setBackground(Color.WHITE); inputField.setForeground(Color.BLACK);
            resultField.setBackground(Color.WHITE); resultField.setForeground(Color.BLACK);
            inputField.setCaretColor(Color.BLACK);
            ergLabel.setForeground(Color.BLACK);
            label.setForeground(Color.BLACK);
            updateButtonTextColors(Color.BLACK);
        } else {
            getContentPane().setBackground(Color.BLACK);
            inputField.setBackground(Color.BLACK); inputField.setForeground(Color.WHITE);
            resultField.setBackground(Color.BLACK); resultField.setForeground(Color.WHITE);
            inputField.setCaretColor(Color.WHITE);
            ergLabel.setForeground(Color.WHITE);
            label.setForeground(Color.WHITE);
            updateButtonTextColors(Color.WHITE);
        }
    }

    private void starteTemp() {
        String text = inputField.getText().trim().toUpperCase();

        if(text.endsWith("°F")) {
            String numText = text.replace("°F", "").trim();
            try {
                double f = Double.parseDouble(numText);
                double c = (f - 32) * 5/9;
                resultField.setText(String.format("%.2f°C", c));
            } catch (NumberFormatException e) { resultField.setText("Ungültige Eingabe!"); }
        }
        else if(text.endsWith("°C")) {
            String numText = text.replace("°C", "").trim();
            try {
                double c = Double.parseDouble(numText);
                double f = (c * 9/5) + 32;
                resultField.setText(String.format("%.2f°F", f));
            } catch (NumberFormatException e) { resultField.setText("Ungültige Eingabe!"); }
        } else {
            resultField.setText("Format: 32°F oder 0°C");
        }
    }

    private void starteNormal() {
        try {
            Polynomial res = new Parser(vorbereiten(inputField.getText())).parse();
            resultField.setText(res.toString());
        } catch (Exception e) { resultField.setText("Syntax-Fehler!"); }
    }

private void starteBinom() {
    try {
        String input = inputField.getText().trim();
        
        // 1. Säubern: (a+b)^2 -> a+b
        if (input.endsWith("^2")) input = input.substring(0, input.length() - 2).trim();
        if (input.startsWith("(") && input.endsWith(")")) input = input.substring(1, input.length() - 1).trim();

        Polynomial polyA = new Polynomial();
        Polynomial polyB = new Polynomial();

        // 2. Splitting-Logik: Wir suchen das Trennzeichen (+ oder -), um a und b zu trennen
        // Das Regex sucht ein + oder - das NICHT am Anfang steht
        String[] parts = input.split("(?<=\\d|[a-zA-Z])(?=[+-])|(?<=[+-])(?=\\d|[a-zA-Z])");

        if (parts.length >= 2) {
            // Wir nehmen den Teil vor dem Operator und den Teil danach (inkl. Operator als Vorzeichen)
            // Beispiel: "25+5" -> polyA = 25, polyB = 5
            // Beispiel: "x-7" -> polyA = x, polyB = -7
            
            int opIndex = -1;
            for(int i=0; i < parts.length; i++) {
                if(parts[i].equals("+") || parts[i].equals("-")) {
                    opIndex = i;
                    break;
                }
            }

            if (opIndex != -1) {
                // Alles vor dem Operator ist a
                StringBuilder sbA = new StringBuilder();
                for(int i=0; i < opIndex; i++) sbA.append(parts[i]);
                polyA = new Parser(vorbereiten(sbA.toString())).parse();

                // Alles ab dem Operator (inklusive) ist b
                StringBuilder sbB = new StringBuilder();
                for(int i=opIndex; i < parts.length; i++) sbB.append(parts[i]);
                polyB = new Parser(vorbereiten(sbB.toString())).parse();
            } else {
                // Fallback, falls kein klarer Operator gefunden wurde
                Polynomial p = new Parser(vorbereiten(input)).parse();
                Object[] keys = p.terms.keySet().toArray();
                if(keys.length > 1) {
                    polyA = new Polynomial(p.terms.get(keys[1]), (String)keys[1]);
                    polyB = new Polynomial(p.terms.get(keys[0]), (String)keys[0]);
                } else {
                    polyA = p;
                    polyB = new Polynomial(0, "");
                }
            }
        } else {
            // Nur ein Term vorhanden (z.B. (30)^2)
            polyA = new Parser(vorbereiten(input)).parse();
            polyB = new Polynomial(0, "");
        }

        // 3. Teilschritte berechnen
        Polynomial a2 = polyA.mul(polyA);
        Polynomial b2 = polyB.mul(polyB);
        Polynomial zweiAB = polyA.mul(polyB).mul(new Polynomial(2, ""));
        
        // Endergebnis: (a+b)*(a+b)
        Polynomial summe = polyA.add(polyB);
        Polynomial ergebnis = summe.mul(summe);

        // 4. Speicher für Kopieren-Button (Plain Text)
        letzterBinomVerlauf = String.format("a=%s, b=%s | a²=%s, 2ab=%s, b²=%s | Ergebnis: %s", 
                                            polyA, polyB, a2, zweiAB, b2, ergebnis);

        // 5. Anzeige im resultField
        resultField.setText(ergebnis.toString());

        // 6. Anzeige im Popup (HTML für Zeilenumbrüche)
        String html = "<html><body style='font-family:Arial; padding:5px;'>" +
                      "<b style='color:#2ecc71;'>Binomische Zerlegung:</b><br><hr>" +
                      "a &nbsp;&nbsp;&nbsp;= " + polyA + "<br>" +
                      "b &nbsp;&nbsp;&nbsp;= " + polyB + "<br><br>" +
                      "a² &nbsp;&nbsp;= " + a2 + "<br>" +
                      "2ab = " + zweiAB + "<br>" +
                      "b² &nbsp;&nbsp;= " + b2 + "<br><hr>" +
                      "<b>Ergebnis: " + ergebnis + "</b>" +
                      "</body></html>";

        JOptionPane.showMessageDialog(this, html, "Rechenweg", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
        e.printStackTrace();
        resultField.setText("Fehler: Nutze (a+b)^2");
    }
}

    private void starteGleichung() {
        try {
            String raw = inputField.getText();
            if (!raw.contains("=")) { resultField.setText("Kein '=' gefunden!"); return; }

            String[] seiten = raw.split("=");
            Polynomial links = new Parser(vorbereiten(seiten[0])).parse();
            Polynomial rechts = new Parser(vorbereiten(seiten[1])).parse();

            String varName = "";
            for(String k : links.terms.keySet()) if(!k.isEmpty()) varName = k;
            if(varName.isEmpty()) for(String k : rechts.terms.keySet()) if(!k.isEmpty()) varName = k;

            if (varName.isEmpty()) { resultField.setText("Keine Variable gefunden!"); return; }

            double lVar = links.terms.getOrDefault(varName, 0.0);
            double rVar = rechts.terms.getOrDefault(varName, 0.0);
            double lConst = links.terms.getOrDefault("", 0.0);
            double rConst = rechts.terms.getOrDefault("", 0.0);

            double diffVar = lVar - rVar;
            double diffConst = rConst - lConst;

            if (diffVar == 0) {
                resultField.setText(diffConst == 0 ? "Unendlich viele Lösungen" : "Keine Lösung");
            } else {
                double x = diffConst / diffVar;
                String xStr = (x == (long)x) ? "" + (long)x : String.format(Locale.US, "%.2f", x);
                resultField.setText(varName + " = " + xStr);
            }
        } catch (Exception e) { resultField.setText("Fehler in der Gleichung!"); }
    }

private String vorbereiten(String s) {
    if (s == null || s.isEmpty()) return "0";

    // 1. Grundreinigung
    s = s.toLowerCase().replace(",", ".").replaceAll("\\s+", "");

    // 2. Google-Logik: Erkennt "100 + 10%" und macht daraus "100 * (1 + 10/100)"
    Pattern p = Pattern.compile("(\\d+\\.?\\d*)([+-])(\\d+\\.?\\d*)%");
    Matcher m = p.matcher(s);
    StringBuilder sb = new StringBuilder();
    int lastEnd = 0;
    while (m.find()) {
        sb.append(s, lastEnd, m.start());
        String basis = m.group(1);
        String op = m.group(2);
        String prozent = m.group(3);
        
        if (op.equals("+")) {
            sb.append(basis).append("*(1+").append(prozent).append("/100)");
        } else {
            sb.append(basis).append("*(1-").append(prozent).append("/100) ");
        }
        lastEnd = m.end();
    }
    sb.append(s.substring(lastEnd));
    s = sb.toString();

    // 3. Restliche Ersetzungen (Prozent ohne Vorzeichen & Wörter)
    s = s.replace("von", "*").replace("of", "*").replace("%", "/100");

    s = s.replace("sqrt*(", "sqrt(");

    // 4. Mathematische Verschönerung (2x -> 2*x etc.)
    return s.replaceAll("(\\d)([a-zA-Z])", "$1*$2")
            .replaceAll("(\\d)(\\()", "$1*$2")
            .replaceAll("([a-zA-Z])(\\()", "$1*$2")
            .replaceAll("(\\))(\\d)", "$1*$2")
            .replaceAll("(\\))([a-zA-Z])", "$1*$2")
            .replaceAll("(\\))(\\()", "$1*$2");
}

    // --- ALGEBRA LOGIK ---
    static class Polynomial {
        Map<String, Double> terms = new TreeMap<>();
        Polynomial(double val, String var) { if (val != 0) terms.put(var, val); }
        Polynomial() {}
        void addTerm(String var, double val) { terms.put(var, terms.getOrDefault(var, 0.0) + val); }
        Polynomial add(Polynomial o) { Polynomial r = new Polynomial(); r.terms.putAll(this.terms); o.terms.forEach(r::addTerm); return r; }
        Polynomial sub(Polynomial o) { Polynomial r = new Polynomial(); r.terms.putAll(this.terms); o.terms.forEach((k,v)->r.addTerm(k,-v)); return r; }
        Polynomial mul(Polynomial o) {
            Polynomial r = new Polynomial();
            for(var e1:terms.entrySet()) for(var e2:o.terms.entrySet()) {
                String v = sortVars(e1.getKey()+e2.getKey());
                r.addTerm(v, e1.getValue()*e2.getValue());
            }
            return r;
        }
        private String sortVars(String s) { char[] c = s.toCharArray(); Arrays.sort(c); return new String(c); }
        @Override
        public String toString() {
            if(terms.isEmpty()) return "0";
            StringBuilder sb = new StringBuilder();
            for(var e:terms.entrySet()){
                double v = e.getValue(); String var = e.getKey();
                if(v==0) continue;
                if(sb.length()>0) sb.append(v>0 ? " + " : " - "); else if(v<0) sb.append("-");
                double av = Math.abs(v);
                if(av!=1.0 || var.isEmpty()){
                    if(av==(long)av) sb.append((long)av); else sb.append(String.format(Locale.US, "%.2f", av));
                }
                sb.append(var);
            }
            return sb.toString();
        }
    }

    // --- PARSER MIT HOCHRECHNEN (^ OPERATOR) ---
    static class Parser {
        String s; int pos=-1,ch;
        Parser(String s){ this.s=s; next(); }
        void next(){ ch=(++pos<s.length())?s.charAt(pos):-1; }
        boolean eat(int c){ while(ch==' ')next(); if(ch==c){ next(); return true; } return false; }

        Polynomial parse(){ return sum(); }

        Polynomial sum(){
            Polynomial x = prod();
            for(;;){
                if(eat('+')) x=x.add(prod());
                else if(eat('-')) x=x.sub(prod());
                else return x;
            }
        }

        Polynomial prod(){
            Polynomial x = power();
            for(;;){
                if(eat('*')) x=x.mul(power());
                else if(eat('/')){
                    Polynomial d=power();
                    double val=d.terms.getOrDefault("",0.0);
                    Polynomial r=new Polynomial();
                    for(var e:x.terms.entrySet()) r.addTerm(e.getKey(), e.getValue()/val);
                    x=r;
                } else return x;
            }
        }

        Polynomial power(){
            Polynomial base=fact();
            if(eat('^')){
                Polynomial exponent=power();
                double exp=exponent.terms.getOrDefault("",0.0);
                int n=(int)exp;
                Polynomial result=new Polynomial(1,"");
                for(int i=0;i<n;i++) result=result.mul(base);
                return result;
            }
            return base;
        }

        Polynomial fact() {
    if (eat('+')) return fact(); // ignoriert führendes Plus
    if (eat('-')) return fact().mul(new Polynomial(-1, "")); // Vorzeichen-Wechsel

    Polynomial x;
    
    // 1. Klammern zuerst: ( ... )
    if (eat('(')) { 
        x = sum(); 
        eat(')'); 
    } 
    // 2. Funktionen oder Variablen: sqrt(...) oder x
    else if ((ch >= 'a' && ch <= 'z') || ch == '√') {
        StringBuilder func = new StringBuilder();
        
        // Sonderfall für das √ Zeichen
        if (ch == '√') {
            next();
            func.append("sqrt");
        } else {
            while (ch >= 'a' && ch <= 'z') {
                func.append((char) ch);
                next();
            }
        }

        // Wenn es 'sqrt' ist und eine Klammer folgt
        if (func.toString().equals("sqrt") && eat('(')) {
            x = sum();
            eat(')');
            double val = x.terms.getOrDefault("", 0.0);
            x = new Polynomial(Math.sqrt(val), "");
        } else {
            // Es ist eine normale Variable, z.B. 'x' oder 'y'
            x = new Polynomial(1, func.toString());
        }
    } 
    // 3. Reine Zahlen
    else {
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(ch) || ch == '.') { 
            sb.append((char) ch); 
            next(); 
        }
        double n = sb.length() > 0 ? Double.parseDouble(sb.toString()) : 1;
        x = new Polynomial(n, "");
    }
    return x;
}
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new Main().setVisible(true)); }
}