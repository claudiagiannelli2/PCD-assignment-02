package pcd.ass02;

import javax.swing.*;
import io.vertx.core.Vertx;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class GenericGUI extends JFrame {
    private final JTextField addressInputField, wordInputField, depthInputField;
    protected final JButton searchButton;
    protected final JButton stopButton;
    private final JTextArea outputArea;
    private volatile boolean shouldStop = false;
    protected final Map<Integer, Integer> interimReport = new HashMap<>();

    public GenericGUI() {
        setTitle("Search Tool - Generic");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        inputPanel.add(new JLabel("Indirizzo:"));
        addressInputField = new JTextField();
        inputPanel.add(addressInputField);
        addressInputField.setText("https://scuola.eutampieri.eu");

        inputPanel.add(new JLabel("Parola:"));
        wordInputField = new JTextField();
        inputPanel.add(wordInputField);

        inputPanel.add(new JLabel("Profondita:"));
        depthInputField = new JTextField();
        inputPanel.add(depthInputField);

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
        shouldStop = true; // Imposta il flag di stop a true
    }

    abstract protected void startSearch(URL address, String word, int depth);

    private int getDepth() {
        return Integer.parseInt(depthInputField.getText());
    }

    protected boolean getStopFlag() {
        return this.shouldStop;
    }

    private void search() {
        String address = addressInputField.getText();
        String word = wordInputField.getText();
        int depth = getDepth();

        this.shouldStop = false;
        try {
            URL parsedURL = new URI(address).toURL();
            startSearch(parsedURL, word, depth);
        } catch (Exception e) {
            outputArea.append("Invalid URL\n");
            e.printStackTrace();
        }
    }

    public void displayTotalOccurrences(int totalOccurrences) {
        if (shouldStop) {
            outputArea.append("Stopped! Found " + totalOccurrences + " occurrences\n");
        }
        else {
            outputArea.append("Done! Found " + totalOccurrences + " occurrences\n");
        }
    }

    protected void updateStatus(Pair<Integer, Integer> x) {
        // controlla se la mappa interimReport contiene già una chiave corrispondente al livello di profondità (x.getX()). Se non la contiene, viene inserita una nuova chiave con valore 0.
        if (!interimReport.containsKey(x.getLeft())) {
            interimReport.put(x.getLeft(), 0);
        }
        // viene incrementato il valore della mappa corrispondente al livello di profondità con il numero di occorrenze trovate (x.getY()).
        interimReport.put(x.getLeft(), interimReport.get(x.getLeft()) + x.getRight());
        // Viene calcolato il numero totale di occorrenze trovate a tutti i livelli di profondità, sommando tutti i valori della mappa interimReport.
        int total = interimReport.values().stream().reduce(0, (acc, z) -> acc + z);
        outputArea.append("At level " + (this.getDepth() - x.getLeft()) + ": found " + interimReport.get(x.getLeft()) + " occurrences (total: " + total + ")\n");
    }

    protected void addToOutput(String message) {
        outputArea.append(message);
    }
}

