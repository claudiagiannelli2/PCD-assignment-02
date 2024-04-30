package pcd.ass02.eventLoop;

import io.vertx.core.Vertx;
import pcd.ass02.*;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Ass02punto1CLI {
    private volatile boolean stopFlag = false;
    private int totalOccurrences = 0;

    public Ass02punto1CLI() {}

   

    private void search(String indirizzo, String parola, int profondita) {
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
            System.out.println("At level " + (profondita - x.getLeft()) + ": found " + interimReport.get(x.getLeft()) + " occurrences (total: " + total + ")");
            return null;
        };

        try {
            // conversione in URL
            parsedURL = new URI(indirizzo).toURL();
            // Chiamata al tuo metodo per eseguire la ricerca
            Vertx vertx = Vertx.vertx();
            vertx.deployVerticle(new Ass02MyVerticle(parsedURL, parola, profondita, f, (x) -> {return !this.stopFlag;}, this));

        } catch (Exception e) {
            System.out.println("Invalid URL");
            e.printStackTrace();
        }
    }

    public void displayTotalOccurrences(int totalOccurrences) {
        this.totalOccurrences = totalOccurrences;
        if (stopFlag == true) {
            System.out.println("Stopped! Found " + totalOccurrences + " occurrences");
        }
        else {
            System.out.println("Done! Found " + totalOccurrences + " occurrences");
        }
    }

    public static void main(String[] args) {
        /*if (args.length != 3) {
            System.out.println("Usage: java Ass02punto1CLI <indirizzo> <parola> <profondita>");
            System.exit(1);
        }

        String indirizzo = args[0];
        String parola = args[1];
        int profondita = Integer.parseInt(args[2]);*/
        String indirizzo = "https://scuola.eutampieri.eu";
        String parola = "il";
        int profondita = 1;

        Ass02punto1CLI cli = new Ass02punto1CLI();
        cli.search(indirizzo, parola, profondita);
    }
}
