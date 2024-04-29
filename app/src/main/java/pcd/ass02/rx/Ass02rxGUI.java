package pcd.ass02.rx;
import javax.swing.*;

import com.google.common.base.Function;

import pcd.ass02.Pair;
import pcd.ass02.virtualThread.Ass02punto2GUI;

import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class Ass02rxGUI extends JFrame {
    private JTextField indirizzoField, parolaField, profonditaField;
    private JButton searchButton;
    private JButton stopButton;
    private JTextArea outputArea;
    private volatile boolean stopFlag = false;
    private Ass02rx rx;
    private int totalOccurrences; // Dichiarazione della variabile di istanza totalOccurrences

    public Ass02rxGUI() {
        setTitle("Search Tool - VT");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        inputPanel.add(new JLabel("Indirizzo:"));
        indirizzoField = new JTextField();
        indirizzoField.setText("https://scuola.eutampieri.eu");
        inputPanel.add(indirizzoField);

        inputPanel.add(new JLabel("Parola:"));
        parolaField = new JTextField();
        parolaField.setText("il");
        inputPanel.add(parolaField);

        inputPanel.add(new JLabel("Profondita:"));
        profonditaField = new JTextField();
        inputPanel.add(profonditaField);

        searchButton = new JButton("Cerca");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread.ofVirtual().start(() -> {
                    search();
                });
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
        // accetta una coppia di valori <Integer, Integer> (presumibilmente
        // rappresentanti il livello di profondità e il numero di occorrenze trovate a
        // quel livello) e restituisce Void
        // la x è la pair
        Function<Pair<Integer, Integer>, Void> f = (x) -> {
            // controlla se la mappa interimReport contiene già una chiave corrispondente al
            // livello di profondità (x.getX()). Se non la contiene, viene inserita una
            // nuova chiave con valore 0.
            if (!interimReport.containsKey(x.getLeft())) {
                interimReport.put(x.getLeft(), 0);
            }
            // viene incrementato il valore della mappa corrispondente al livello di
            // profondità con il numero di occorrenze trovate (x.getY()).
            interimReport.put(x.getLeft(), interimReport.get(x.getLeft()) + x.getRight());
            // Viene calcolato il numero totale di occorrenze trovate a tutti i livelli di
            // profondità, sommando tutti i valori della mappa interimReport.
            int total = interimReport.values().stream().reduce(0, (acc, z) -> acc + z);
            outputArea.append("At level " + (profondita - x.getLeft()) + ": found " + interimReport.get(x.getLeft())
                    + " occurrences (total: " + total + ")\n");
            return null;
        };

        try {
            // conversione in URL
            parsedURL = new URI(indirizzo).toURL();
            // Chiamata al tuo metodo per eseguire la ricerca
            totalOccurrences = 0;
            rx = new Ass02rx(parsedURL, parola, profondita, f, (x) -> {
                return !this.stopFlag;
            }); // Creare l'istanza del coordinatore
            rx.getWordOccurrences(parsedURL, profondita)
            .subscribe(
                            occurrences -> {
                                //outputArea.append("Done! Found " + occurrences + " occurrences\n");
                                totalOccurrences += occurrences; // Aggiorna il valore di totalOccurrences
                            },
                            error -> outputArea.append("Error: " + error.getMessage() + "\n"),
                            () -> {
                                if (this.stopFlag) {
                                    outputArea.append("Stopped! Found " + totalOccurrences + " occurrences\n");
                                } else {
                                    outputArea.append("Done! Found " + totalOccurrences + " occurrences\n");
                                }
                            }
                    );

        } catch (Exception e) {
            outputArea.append("Invalid URL\n");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Ass02rxGUI().setVisible(true);
            }

        });
    }
}
