import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.file.*;
import java.util.Locale;
import java.net.URL;

public class Main extends JFrame {
    private JTextField inputField;
    private JButton calcBinom, calcNormal, solveBtn, clearBtn, copyBtn, tempumrechBtn, switchThemeBtn, autoAusBtn;
    private JTextField resultField;
    private JLabel ergLabel, label;

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

        // Buttons
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

        autoAusBtn = new JButton("Automatisch auswählen");
        styleButton(autoAusBtn, new Color(100,100,100));

        add(calcNormal); add(calcBinom); add(solveBtn); add(tempumrechBtn); add(clearBtn); add(copyBtn); add(autoAusBtn); add(switchThemeBtn);

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
        inputField.addActionListener(e -> starteNormal());
        calcBinom.addActionListener(e -> starteBinom());
        solveBtn.addActionListener(e -> starteGleichung());
        clearBtn.addActionListener(e -> { inputField.setText(""); resultField.setText(""); });
        copyBtn.addActionListener(e -> {
            StringSelection sel = new StringSelection(resultField.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            JOptionPane.showMessageDialog(this, "Kopiert!");
        });
        tempumrechBtn.addActionListener(e -> starteTemp());
        switchThemeBtn.addActionListener(e -> themeSwitch());
        autoAusBtn.addActionListener(e -> autoauswahl());
    }

    private void autoauswahl() {
        String text = inputField.getText().trim().toUpperCase();

        if(text.endsWith("°F")) {
            starteTemp();
        }
        else if(text.endsWith("°C")) {
            starteTemp();
        }
    }

    private void styleButton(JButton b, Color c) {
        b.setBackground(c);
        if (getContentPane().getBackground() == Color.BLACK) {
            b.setForeground(Color.WHITE);
        } else {
            b.setForeground(Color.BLACK);
        }
        b.setFocusPainted(false); b.setFont(new Font("Arial", Font.BOLD, 12));
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
            if (input.endsWith("^2")) {
                input = input.substring(0, input.length() - 2).trim();
            }
            if (input.startsWith("(") && input.endsWith(")")) {
                input = input.substring(1, input.length() - 1).trim();
            }
            Polynomial p = new Parser(vorbereiten(input)).parse();
            Polynomial res = p.mul(p);
            resultField.setText(res.toString());
        } catch (Exception e) { resultField.setText("Format: (a+b)^2"); }
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

        Polynomial fact(){
            if(eat('+')) return fact();
            if(eat('-')) return fact().mul(new Polynomial(-1,""));

            Polynomial x;
            if(eat('(')){ x=sum(); eat(')'); }
            else{
                StringBuilder sb=new StringBuilder();
                while(Character.isDigit(ch)||ch=='.'){ sb.append((char)ch); next(); }
                double n=sb.length()>0?Double.parseDouble(sb.toString()):1;
                StringBuilder v=new StringBuilder();
                while(Character.isLetter(ch)){ v.append((char)ch); next(); }
                x=new Polynomial(n, v.toString());
            }
            return x;
        }
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new Main().setVisible(true)); }
}