package pcd.ass02.eventLoop;
import javax.swing.*;

import io.vertx.core.Vertx;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import pcd.ass02.*;

public class Ass02punto1GUI extends JFrame {
    private JTextField indirizzoField, parolaField, profonditaField;
    private JButton searchButton;
    private JButton stopButton;
    private JTextArea outputArea;
    private volatile boolean stopFlag = false;
    private int totalOccurrences = 0;

    public Ass02punto1GUI() {
        setTitle("Search Tool - Vert.x");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        inputPanel.add(new JLabel("Indirizzo:"));
        indirizzoField = new JTextField();
        inputPanel.add(indirizzoField);
        indirizzoField.setText("https://scuola.eutampieri.eu");

        inputPanel.add(new JLabel("Parola:"));
        parolaField = new JTextField();
        inputPanel.add(parolaField);

        inputPanel.add(new JLabel("Profondita:"));
        profonditaField = new JTextField();
        inputPanel.add(profonditaField);

        searchButton = new JButton("Cerca");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });
        inputPanel.add(searchButton);
        stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopExecution();
            }
        });
        inputPanel.add(stopButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    private void stopExecution() {
        stopFlag = true; // Imposta il flag di stop a true
    }

    private void search() {
        String indirizzo = indirizzoField.getText();
        String parola = parolaField.getText();
        int profondita = Integer.parseInt(profonditaField.getText());

        URL parsedURL;
        this.stopFlag = false;
        final Map<Integer, Integer> interimReport = new HashMap<>();
        // accetta una coppia di valori <Integer, Integer> (presumibilmente rappresentanti il livello di profondità e il numero di occorrenze trovate a quel livello) e restituisce Void
        // la x è la pair
        Function<Pair<Integer, Integer>, Void> f = (x) -> {
            // controlla se la mappa interimReport contiene già una chiave corrispondente al livello di profondità (x.getX()). Se non la contiene, viene inserita una nuova chiave con valore 0.
            if (!interimReport.containsKey(x.getLeft())) {
                interimReport.put(x.getLeft(), 0);
            }
            // viene incrementato il valore della mappa corrispondente al livello di profondità con il numero di occorrenze trovate (x.getY()).
            interimReport.put(x.getLeft(), interimReport.get(x.getLeft()) + x.getRight());
            // Viene calcolato il numero totale di occorrenze trovate a tutti i livelli di profondità, sommando tutti i valori della mappa interimReport.
            int total = interimReport.values().stream().reduce(0, (acc, z) -> acc + z);
            outputArea.append("At level " + (profondita - x.getLeft()) + ": found " + interimReport.get(x.getLeft()) + " occurrences (total: " + total + ")\n");
            return null;
        };

        try {
            // conversione in URL
            parsedURL = new URI(indirizzo).toURL();
            // Chiamata al tuo metodo per eseguire la ricerca
            Vertx vertx = Vertx.vertx();
            vertx.deployVerticle(new Ass02MyVerticle(parsedURL, parola, profondita, f, (x) -> {return !this.stopFlag;}, this));

        } catch (Exception e) {
            outputArea.append("Invalid URL\n");
            e.printStackTrace();
        }
    }

    public void displayTotalOccurrences(int totalOccurrences) {
        this.totalOccurrences = totalOccurrences;
        if (stopFlag == true) {
            outputArea.append("Stopped! Found " + totalOccurrences + " occurrences\n");
        }
        else {
            outputArea.append("Done! Found " + totalOccurrences + " occurrences\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Ass02punto1GUI().setVisible(true);
            }
        });
    }
}
