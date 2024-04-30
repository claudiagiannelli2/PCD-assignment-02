package pcd.ass02.eventLoop;

import io.vertx.core.Vertx;
import pcd.ass02.*;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Ass02Step1CLI {
    private volatile boolean stopFlag = false;

    public Ass02Step1CLI() {}

   

    private void search(String address, String word, int depth) {
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
            System.out.println("At level " + (depth - x.getLeft()) + ": found " + interimReport.get(x.getLeft()) + " occurrences (total: " + total + ")");
            return null;
        };

        try {
            // conversione in URL
            parsedURL = new URI(address).toURL();
            // Chiamata al tuo metodo per eseguire la ricerca
            Vertx vertx = Vertx.vertx();
            vertx.deployVerticle(new Ass02MyVerticle(parsedURL, word, depth, f, (x) -> {return !this.stopFlag;}, this));

        } catch (Exception e) {
            System.out.println("Invalid URL");
            e.printStackTrace();
        }
    }

    public void displayTotalOccurrences(int totalOccurrences) {
        if (stopFlag) {
            System.out.println("Stopped! Found " + totalOccurrences + " occurrences");
        }
        else {
            System.out.println("Done! Found " + totalOccurrences + " occurrences");
        }
    }

    public static void main(String[] args) {
        /*if (args.length != 3) {
            System.out.println("Usage: java Ass02punto1CLI <address> <word> <depth>");
            System.exit(1);
        }

        String address = args[0];
        String word = args[1];
        int depth = Integer.parseInt(args[2]);*/
        String address = "https://scuola.eutampieri.eu";
        String word = "il";
        int depth = 1;

        Ass02Step1CLI cli = new Ass02Step1CLI();
        cli.search(address, word, depth);
    }
}
